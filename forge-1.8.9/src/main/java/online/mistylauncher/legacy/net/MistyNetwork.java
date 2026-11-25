package online.mistylauncher.legacy.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.MistlyClient;
import online.mistylauncher.legacy.features.Outfit;

/**
 * The Mistly network ({@code mistly.cc/api/client}), as in the Fabric versions: tells the other
 * Mistly players you are one of them and what you wear, and asks the same about the players around.
 *
 * <p>The account is proven the way a Minecraft server does it: the game "joins" a random server id
 * at Mojang's session service and the Mistly site checks it there, so nobody can pose as someone
 * else and the Minecraft token never leaves the game (it only goes to Mojang, as for a server).
 * Offline accounts cannot be proven: they see no badges and are not shown to others.
 *
 * <p>Everything runs on one background thread; the game thread only hands over the list of
 * players around ({@link #tick}).
 */
public final class MistyNetwork {
	private static final String API = System.getProperty("misty.api", "https://mistly.cc/api/client");
	private static final int TIMEOUT_MS = 10_000;
	/** A sign of life at least this often (the site forgets a player after 3 minutes). */
	private static final long PRESENCE_EVERY_MS = 60_000;
	/** A player around is asked about again after this long. */
	private static final long LOOKUP_FRESH_MS = 60_000;
	private static final int LOOKUP_BATCH = 100;
	private static final long LOGIN_RETRY_MIN_MS = 60_000;
	private static final long LOGIN_RETRY_MAX_MS = 1_800_000;
	/**
	 * Proving the account costs a "join" at Mojang's session service, which also serves the game
	 * when it joins a server, and Mojang answers 429 when an account joins too often. So the token
	 * is kept between game sessions, the first proof waits a little after the game starts, and none
	 * is made just after a server join began.
	 */
	private static final long FIRST_LOGIN_DELAY_MS = 20_000;
	private static final long QUIET_AFTER_CONNECT_MS = 30_000;
	/** A token refused this soon after the proof means something is wrong: back off, do not loop. */
	private static final long FRESH_TOKEN_MS = 300_000;

	private static final SecureRandom RANDOM = new SecureRandom();

	private static ScheduledExecutorService worker;
	private static Minecraft minecraft;
	/** Players in the player list of the current server (written by the game thread). */
	private static volatile List<UUID> around = Collections.emptyList();
	private static int ticks;
	/** When the game last started joining a server (game thread). */
	private static volatile long connectedAt;
	private static volatile boolean connecting;
	private static boolean connected;

	// state of the background thread
	private static volatile String token;
	private static long loggedInAt;
	private static long nextLogin;
	private static long loginDelay = LOGIN_RETRY_MIN_MS;
	private static long lastPresence;
	private static String sentOutfit;
	/** The Mistly site counts this account among the first Mistly players (early player cape). */
	private static volatile boolean early;
	private static boolean restored;

	private MistyNetwork() {
	}

	public static void start(final Minecraft client) {
		minecraft = client;
		nextLogin = System.currentTimeMillis() + FIRST_LOGIN_DELAY_MS;
		worker = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(final Runnable runnable) {
				Thread thread = new Thread(runnable, "Mistly network");
				thread.setDaemon(true);
				return thread;
			}
		});
		worker.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				step();
			}
		}, 2, 3, TimeUnit.SECONDS);
		// when the game closes: the other players stop seeing you right away
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				stop();
			}
		}, "Mistly network bye"));
	}

	/** Game thread, every tick: once a second, notes who is in the player list. */
	public static void tick(final Minecraft client) {
		NetHandlerPlayClient connection = client.getNetHandler();
		boolean nowConnected = connection != null;
		// the game "joins" at Mojang while the connecting screen shows: our own join must not come
		// in between, or the server finds our server id instead of its own and refuses the player
		connecting = client.currentScreen instanceof GuiConnecting;
		if (connecting || nowConnected && !connected) {
			connectedAt = System.currentTimeMillis();
		}
		connected = nowConnected;
		if (++ticks < 20) {
			return;
		}
		ticks = 0;
		if (connection == null || client.thePlayer == null) {
			if (!around.isEmpty()) {
				around = Collections.emptyList();
				MistyPlayers.clear();
			}
			return;
		}
		UUID self = client.getSession().getProfile().getId();
		List<UUID> ids = new ArrayList<UUID>();
		for (NetworkPlayerInfo info : connection.getPlayerInfoMap()) {
			UUID id = info.getGameProfile().getId();
			// version 4 only: real accounts (offline and NPC ids are other versions)
			if (id != null && !id.equals(self) && id.version() == 4) {
				ids.add(id);
			}
		}
		around = ids;
	}

	private static void stop() {
		if (worker != null) {
			worker.shutdownNow();
		}
		if (token != null) {
			JsonObject body = new JsonObject();
			body.addProperty("bye", true);
			try {
				post("/presence", body, 2_000);
			} catch (IOException | RuntimeException e) {
				// closing anyway: the site forgets the player by itself after a few minutes
			}
		}
	}

	/** Whether this account may wear the early player cape (the Mistly site says so at sign-in). */
	public static boolean early() {
		return early;
	}

	private static void step() {
		try {
			if (!Config.get().network) {
				if (token != null) {
					JsonObject body = new JsonObject();
					body.addProperty("bye", true);
					post("/presence", body, TIMEOUT_MS);
					token = null;
				}
				MistyPlayers.clear();
				return;
			}
			long now = System.currentTimeMillis();
			if (token == null) {
				restore();
			}
			if (token == null) {
				if (now < nextLogin || now - connectedAt < QUIET_AFTER_CONNECT_MS) {
					return;
				}
				login();
				if (token == null) {
					return;
				}
			}
			String outfit = shownBack() + "|" + shownHat();
			if (now - lastPresence >= PRESENCE_EVERY_MS || !outfit.equals(sentOutfit)) {
				presence(outfit);
			}
			lookup();
		} catch (IOException | RuntimeException e) {
			// site unreachable: the next step tries again
		}
	}

	private static String shownBack() {
		return Config.get().cape ? Outfit.back() : Outfit.NONE;
	}

	private static String shownHat() {
		return Config.get().cape ? Outfit.hat() : Outfit.NONE;
	}

	private static void loginFailed() {
		nextLogin = System.currentTimeMillis() + loginDelay;
		loginDelay = Math.min(loginDelay * 2, LOGIN_RETRY_MAX_MS);
	}

	private static void login() throws IOException {
		if (connecting) {
			return;
		}
		GameProfile profile = minecraft.getSession().getProfile();
		if (profile.getId() == null) {
			loginFailed();
			return;
		}
		byte[] random = new byte[20];
		RANDOM.nextBytes(random);
		StringBuilder serverId = new StringBuilder();
		for (byte b : random) {
			serverId.append(String.format("%02x", b & 0xFF));
		}
		System.out.println("[Mistly Client] Mistly network: proving the account to Mojang (join)");
		try {
			online.mistylauncher.legacy.features.JoinRetry.original().joinServer(profile, minecraft.getSession().getToken(), serverId.toString());
		} catch (AuthenticationException e) {
			// offline account, or Mojang unreachable
			loginFailed();
			return;
		}
		JsonObject body = new JsonObject();
		body.addProperty("name", profile.getName());
		body.addProperty("serverId", serverId.toString());
		Response response = post("/login", body, TIMEOUT_MS);
		if (response.status != 200) {
			loginFailed();
			return;
		}
		JsonObject answer = new JsonParser().parse(response.body).getAsJsonObject();
		token = answer.get("token").getAsString();
		early = answer.has("early") && answer.get("early").getAsBoolean();
		loggedInAt = System.currentTimeMillis();
		save(profile.getId());
		lastPresence = 0;
		sentOutfit = null;
	}

	private static void presence(final String outfit) throws IOException {
		JsonObject body = new JsonObject();
		body.addProperty("cosmetic", shownBack());
		body.addProperty("hat", shownHat());
		Response response = post("/presence", body, TIMEOUT_MS);
		if (expired(response)) {
			return;
		}
		lastPresence = System.currentTimeMillis();
		// the token works: the next failed proof starts from the shortest wait again
		if (lastPresence - loggedInAt > FRESH_TOKEN_MS) {
			loginDelay = LOGIN_RETRY_MIN_MS;
		}
		sentOutfit = outfit;
	}

	private static void lookup() throws IOException {
		long now = System.currentTimeMillis();
		List<UUID> ask = new ArrayList<UUID>();
		for (UUID id : around) {
			MistyPlayers.Entry entry = MistyPlayers.get(id);
			if (entry == null || now - entry.fetchedAt >= LOOKUP_FRESH_MS) {
				ask.add(id);
				if (ask.size() == LOOKUP_BATCH) {
					break;
				}
			}
		}
		if (ask.isEmpty()) {
			return;
		}
		JsonArray uuids = new JsonArray();
		for (UUID id : ask) {
			uuids.add(new com.google.gson.JsonPrimitive(id.toString()));
		}
		JsonObject body = new JsonObject();
		body.add("uuids", uuids);
		Response response = post("/players", body, TIMEOUT_MS);
		if (expired(response) || response.status != 200) {
			return;
		}
		JsonObject players = new JsonParser().parse(response.body).getAsJsonObject().getAsJsonObject("players");
		java.util.Set<UUID> found = new java.util.HashSet<UUID>();
		for (Map.Entry<String, JsonElement> player : players.entrySet()) {
			JsonObject data = player.getValue().getAsJsonObject();
			String cosmetic = data.has("cosmetic") ? data.get("cosmetic").getAsString() : "";
			String hat = data.has("hat") ? data.get("hat").getAsString() : "";
			UUID id = UUID.fromString(player.getKey());
			found.add(id);
			MistyPlayers.put(id, true, cosmetic, hat);
		}
		for (UUID id : ask) {
			if (!found.contains(id)) {
				MistyPlayers.put(id, false, Outfit.NONE, Outfit.NONE);
			}
		}
	}

	/** The site no longer knows our token (it restarted, or we were away too long): sign in again. */
	private static boolean expired(final Response response) {
		if (response.status == 401) {
			token = null;
			forget();
			if (System.currentTimeMillis() - loggedInAt < FRESH_TOKEN_MS) {
				// refused right after a proof: wait longer each time instead of proving again and again
				loginFailed();
			} else {
				// an old token (kept from an earlier session): prove the account once more
				nextLogin = 0;
				loginDelay = LOGIN_RETRY_MIN_MS;
			}
			return true;
		}
		return false;
	}

	// ── the token, kept between game sessions (shared with the Fabric versions when the launcher started the game) ──

	private static File tokenFile() {
		String dir = System.getProperty("misty.launcher.dir");
		File folder = dir == null || dir.isEmpty() ? new File(minecraft.mcDataDir, "config") : new File(dir);
		return new File(folder, "misty-network.json");
	}

	/** Once per game: the token of an earlier session, if it is for this account. */
	private static void restore() {
		if (restored) {
			return;
		}
		restored = true;
		try {
			File file = tokenFile();
			if (!file.isFile()) {
				return;
			}
			JsonObject saved = new JsonParser().parse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
			UUID self = minecraft.getSession().getProfile().getId();
			if (saved.has("uuid") && saved.has("token") && self != null && self.toString().equals(saved.get("uuid").getAsString())) {
				token = saved.get("token").getAsString();
				early = saved.has("early") && saved.get("early").getAsBoolean();
				// a refusal of this token is not a reason to back off
				loggedInAt = 0;
				lastPresence = 0;
				sentOutfit = null;
			}
		} catch (IOException | RuntimeException e) {
			// unreadable: a new proof later
		}
	}

	private static void save(final UUID self) {
		try {
			JsonObject saved = new JsonObject();
			saved.addProperty("uuid", self.toString());
			saved.addProperty("token", token);
			saved.addProperty("early", early);
			File file = tokenFile();
			Files.createDirectories(file.getParentFile().toPath());
			Files.write(file.toPath(), saved.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException | RuntimeException e) {
			// not kept: a new proof at the next start
		}
	}

	private static void forget() {
		try {
			Files.deleteIfExists(tokenFile().toPath());
		} catch (IOException e) {
			// kept: refused at the next start, then forgotten
		}
	}

	private static final class Response {
		final int status;
		final String body;

		Response(final int status, final String body) {
			this.status = status;
			this.body = body;
		}
	}

	private static Response post(final String path, final JsonObject body, final int timeout) throws IOException {
		HttpURLConnection connection = (HttpURLConnection)new URL(API + path).openConnection();
		try {
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("User-Agent", "MistyClient/" + MistlyClient.version() + "+mc1.8.9");
			String bearer = token;
			if (bearer != null) {
				connection.setRequestProperty("Authorization", "Bearer " + bearer);
			}
			OutputStream out = connection.getOutputStream();
			try {
				out.write(body.toString().getBytes(StandardCharsets.UTF_8));
			} finally {
				out.close();
			}
			int status = connection.getResponseCode();
			InputStream in = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
			ByteArrayOutputStream read = new ByteArrayOutputStream();
			if (in != null) {
				try {
					byte[] buffer = new byte[4096];
					int n;
					while ((n = in.read(buffer)) > 0) {
						read.write(buffer, 0, n);
					}
				} finally {
					in.close();
				}
			}
			return new Response(status, new String(read.toByteArray(), StandardCharsets.UTF_8));
		} finally {
			connection.disconnect();
		}
	}
}
