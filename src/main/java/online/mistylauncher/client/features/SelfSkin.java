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
import online.mistylauncher.client.compat.Versions;
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
			profile = load(Versions.id(game));
			Thread retry = new Thread(() -> retry(Versions.id(game)), "Mistly skin");
			retry.setDaemon(true);
			retry.start();
		}
		GameProfile kept = profile;
		return kept != null ? kept : game;
	}

	/** The profile's properties (a record since 1.21.9). */
	private static PropertyMap properties(final GameProfile profile) {
		//? if >=1.21.9 {
		return profile.properties();
		//?} else {
		/*return profile.getProperties();
		*///?}
	}

	private static boolean hasTextures(final GameProfile profile) {
		return !properties(profile).get(TEXTURES).isEmpty();
	}

	/** Asks Mojang for the profile again until it answers with the skin. */
	private static void retry(final UUID id) {
		for (int attempt = 0; ; attempt++) {
			try {
				Thread.sleep(RETRIES[Math.min(attempt, RETRIES.length - 1)] * 1000L);
				//? if >=1.21.9 {
				var result = Minecraft.getInstance().services().sessionService().fetchProfile(id, true);
				//?} else if >=1.20.2 {
				/*var result = Minecraft.getInstance().getMinecraftSessionService().fetchProfile(id, true);
				*///?}
				//? if >=1.20.2 {
				GameProfile found = result != null ? result.profile() : null;
				//?} else {
				/*// authlib 4 fills a profile in instead (an empty one when Mojang refuses)
				GameProfile found = Minecraft.getInstance().getMinecraftSessionService().fillProfileProperties(new GameProfile(id, null), true);
				*///?}
				if (found != null && hasTextures(found)) {
					profile = found;
					save(found);
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
		Property textures = properties(profile).get(TEXTURES).iterator().next();
		JsonObject json = new JsonObject();
		json.addProperty("id", Versions.id(profile).toString());
		json.addProperty("name", Versions.name(profile));
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
			//? if >=1.21.9 {
			return new GameProfile(id, json.get("name").getAsString(), new PropertyMap(ImmutableMultimap.of(TEXTURES, textures)));
			//?} else {
			/*GameProfile kept = new GameProfile(id, json.get("name").getAsString());
			kept.getProperties().put(TEXTURES, textures);
			return kept;
			*///?}
		} catch (IOException | RuntimeException e) {
			MistyClient.LOG.warn("Kept skin unreadable", e);
			return null;
		}
	}
}
