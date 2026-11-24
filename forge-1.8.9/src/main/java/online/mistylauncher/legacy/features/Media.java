package online.mistylauncher.legacy.features;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

/**
 * Pictures and songs from outside the game, as in the Fabric versions:
 * <ul>
 * <li>Music: the song playing on the computer (Spotify, Deezer, a browser…). The launcher reads it
 * from Windows' media controls and writes {@code music.json} and {@code music-cover.png} in its
 * folder; this reads them again when they change.</li>
 * <li>Server flag: the country of the server played on (country.is, from its IP), its flag from
 * flagcdn.com, once per server joined. Nothing for local and private addresses.</li>
 * </ul>
 */
public final class Media {
	/** The song, as last read. Times in milliseconds; {@code at}: when the position was measured. */
	public static final class Song {
		public final String title;
		public final String artist;
		public final String app;
		public final boolean playing;
		public final long position;
		public final long duration;
		public final long at;
		public final long cover;

		public Song(final String title, final String artist, final String app, final boolean playing, final long position, final long duration,
			final long at, final long cover) {
			this.title = title;
			this.artist = artist;
			this.app = app;
			this.playing = playing;
			this.position = position;
			this.duration = duration;
			this.at = at;
			this.cover = cover;
		}

		/** Where the song is now: the measured position, moved on by the time since (while playing). */
		public long positionNow() {
			long now = this.playing ? this.position + Math.max(0, System.currentTimeMillis() - this.at) : this.position;
			return this.duration > 0 ? Math.min(now, this.duration) : now;
		}
	}

	private static Song song;
	private static long lastModified = -1;
	private static long loadedCover = -1;
	private static ResourceLocation cover;
	private static int coverWidth;
	private static int coverHeight;
	private static int ticks;

	private static NetHandlerPlayClient connection;
	private static volatile BufferedImage flagImage;
	private static ResourceLocation flag;
	private static int flagWidth;
	private static int flagHeight;

	private Media() {
	}

	public static Song song() {
		return song;
	}

	/** The cover of the current song, null while there is none. */
	public static ResourceLocation cover() {
		return cover != null && song != null && song.cover == loadedCover ? cover : null;
	}

	public static int coverWidth() {
		return coverWidth;
	}

	public static int coverHeight() {
		return coverHeight;
	}

	/** The flag of the current server, null while unknown. */
	public static ResourceLocation flag() {
		return flag;
	}

	public static int flagWidth() {
		return flagWidth;
	}

	public static int flagHeight() {
		return flagHeight;
	}

	public static void tick(final Minecraft minecraft) {
		music(minecraft);
		server(minecraft);
	}

	// ── music ──

	private static void music(final Minecraft minecraft) {
		if (++ticks < 10) {
			return;
		}
		ticks = 0;
		String dir = System.getProperty("misty.launcher.dir");
		if (dir == null || dir.isEmpty()) {
			return;
		}
		try {
			File file = new File(dir, "music.json");
			if (!file.isFile()) {
				song = null;
				lastModified = -1;
				return;
			}
			if (file.lastModified() == lastModified) {
				return;
			}
			lastModified = file.lastModified();
			JsonObject json = new JsonParser().parse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
			song = new Song(text(json, "title"), text(json, "artist"), text(json, "app"), json.has("playing") && json.get("playing").getAsBoolean(),
				number(json, "position"), number(json, "duration"), number(json, "at"), number(json, "cover"));
			if (song.cover != loadedCover) {
				loadCover(minecraft, new File(dir, "music-cover.png"), song.cover);
			}
		} catch (IOException | RuntimeException e) {
			// half-written file: read again at the next check
			lastModified = -1;
		}
	}

	private static void loadCover(final Minecraft minecraft, final File file, final long id) {
		loadedCover = id;
		if (cover != null) {
			minecraft.getTextureManager().deleteTexture(cover);
			cover = null;
		}
		if (id == 0 || !file.isFile()) {
			return;
		}
		try {
			BufferedImage image = ImageIO.read(file);
			if (image != null) {
				coverWidth = image.getWidth();
				coverHeight = image.getHeight();
				cover = minecraft.getTextureManager().getDynamicTextureLocation("mistly_music_cover", new DynamicTexture(image));
			}
		} catch (IOException | RuntimeException e) {
			// no cover: the module shows a note instead
		}
	}

	private static String text(final JsonObject json, final String key) {
		return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : "";
	}

	private static long number(final JsonObject json, final String key) {
		return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsLong() : 0;
	}

	// ── server flag ──

	private static void server(final Minecraft minecraft) {
		BufferedImage loaded = flagImage;
		if (loaded != null) {
			flagImage = null;
			flagWidth = loaded.getWidth();
			flagHeight = loaded.getHeight();
			flag = minecraft.getTextureManager().getDynamicTextureLocation("mistly_server_flag", new DynamicTexture(loaded));
		}
		NetHandlerPlayClient current = minecraft.getNetHandler();
		if (current == connection) {
			return;
		}
		connection = current;
		if (flag != null) {
			minecraft.getTextureManager().deleteTexture(flag);
			flag = null;
		}
		if (current == null || minecraft.isIntegratedServerRunning()) {
			return;
		}
		SocketAddress remote = current.getNetworkManager().getRemoteAddress();
		if (!(remote instanceof InetSocketAddress) || ((InetSocketAddress)remote).getAddress() == null) {
			return;
		}
		final InetAddress ip = ((InetSocketAddress)remote).getAddress();
		if (ip.isLoopbackAddress() || ip.isSiteLocalAddress() || ip.isLinkLocalAddress() || ip.isAnyLocalAddress()) {
			return;
		}
		final NetHandlerPlayClient asked = current;
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String body = new String(get("https://api.country.is/" + ip.getHostAddress()), StandardCharsets.UTF_8);
					String code = new JsonParser().parse(body).getAsJsonObject().get("country").getAsString().toLowerCase(Locale.ROOT);
					if (!code.matches("[a-z]{2}")) {
						return;
					}
					BufferedImage image = ImageIO.read(new ByteArrayInputStream(get("https://flagcdn.com/w40/" + code + ".png")));
					// still on that server
					if (image != null && connection == asked) {
						flagImage = image;
					}
				} catch (IOException | RuntimeException e) {
					// no flag: the address alone
				}
			}
		}, "Mistly server flag");
		thread.setDaemon(true);
		thread.start();
	}

	private static byte[] get(final String url) throws IOException {
		HttpURLConnection http = (HttpURLConnection)new URL(url).openConnection();
		try {
			http.setConnectTimeout(8000);
			http.setReadTimeout(10000);
			http.setRequestProperty("User-Agent", "Mistly-Client");
			if (http.getResponseCode() != 200) {
				throw new IOException("HTTP " + http.getResponseCode());
			}
			InputStream in = http.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				byte[] buffer = new byte[4096];
				int n;
				while ((n = in.read(buffer)) > 0) {
					out.write(buffer, 0, n);
				}
			} finally {
				in.close();
			}
			return out.toByteArray();
		} finally {
			http.disconnect();
		}
	}
}
