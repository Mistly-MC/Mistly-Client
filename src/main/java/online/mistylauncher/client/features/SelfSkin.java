package online.mistylauncher.client.features;

import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import online.mistylauncher.client.MistyClient;
import org.jspecify.annotations.Nullable;

/**
 * The player's own skin for the Mistly menus (title screen, wardrobe, settings).
 *
 * <p>The game asks Mojang for the player's profile (with the skin) once, at start. When Mojang
 * answers 429 (too many requests, it happens), the game keeps a profile without a skin for the
 * whole session: the menus show a default skin. So the last profile with a skin is kept in
 * {@code config/mistly-skin.json} and used right away, and the profile is asked again in the
 * background, less and less often, until Mojang answers.
 */
public final class SelfSkin {
	private static final String TEXTURES = "textures";
	/** Seconds between the tries: 10 s, 30 s, 1 min, 2 min, then every 5 min. */
	private static final long[] RETRIES = { 10, 30, 60, 120, 300 };

	private static volatile @Nullable GameProfile profile;
	private static boolean started;
	private static boolean saved;

	private SelfSkin() {
	}

	/** The player's profile with the skin: the game's, else the kept one, else the game's (no skin). */
	public static GameProfile profile() {
		GameProfile game = Minecraft.getInstance().getGameProfile();
		if (hasTextures(game)) {
			if (!saved) {
				saved = true;
				save(game);
			}
			return game;
		}
		if (!started) {
			started = true;
			profile = load(game.id());
			Thread retry = new Thread(() -> retry(game.id()), "Mistly skin");
			retry.setDaemon(true);
			retry.start();
		}
		GameProfile kept = profile;
		return kept != null ? kept : game;
	}

	private static boolean hasTextures(final GameProfile profile) {
		return !profile.properties().get(TEXTURES).isEmpty();
	}

	/** Asks Mojang for the profile again until it answers with the skin. */
	private static void retry(final UUID id) {
		for (int attempt = 0; ; attempt++) {
			try {
				Thread.sleep(RETRIES[Math.min(attempt, RETRIES.length - 1)] * 1000L);
				var result = Minecraft.getInstance().services().sessionService().fetchProfile(id, true);
				if (result != null && hasTextures(result.profile())) {
					profile = result.profile();
					save(result.profile());
					MistyClient.LOG.info("Skin loaded after Mojang's refusal at start");
					return;
				}
			} catch (InterruptedException e) {
				return;
			} catch (RuntimeException e) {
				// 429 again, or no connection: the next try comes later
			}
		}
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("mistly-skin.json");
	}

	private static void save(final GameProfile profile) {
		Property textures = profile.properties().get(TEXTURES).iterator().next();
		JsonObject json = new JsonObject();
		json.addProperty("id", profile.id().toString());
		json.addProperty("name", profile.name());
		json.addProperty("value", textures.value());
		if (textures.signature() != null) {
			json.addProperty("signature", textures.signature());
		}
		try {
			Files.createDirectories(file().getParent());
			Files.writeString(file(), json.toString(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			MistyClient.LOG.warn("Skin not kept", e);
		}
	}

	/** The kept profile of this player, null when there is none (or it is another account's). */
	private static @Nullable GameProfile load(final UUID id) {
		try {
			if (!Files.isRegularFile(file())) {
				return null;
			}
			JsonObject json = JsonParser.parseString(Files.readString(file(), StandardCharsets.UTF_8)).getAsJsonObject();
			if (!id.toString().equals(json.get("id").getAsString())) {
				return null;
			}
			String signature = json.has("signature") ? json.get("signature").getAsString() : null;
			Property textures = new Property(TEXTURES, json.get("value").getAsString(), signature);
			return new GameProfile(id, json.get("name").getAsString(), new PropertyMap(ImmutableMultimap.of(TEXTURES, textures)));
		} catch (IOException | RuntimeException e) {
			MistyClient.LOG.warn("Kept skin unreadable", e);
			return null;
		}
	}
}
