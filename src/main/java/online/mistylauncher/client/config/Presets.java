package online.mistylauncher.client.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;
import net.fabricmc.loader.api.FabricLoader;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.hud.HudManager;
import online.mistylauncher.client.hud.HudModule;
import org.jspecify.annotations.Nullable;

/**
 * Presets: five slots that keep all the Mistly settings at once (the switches, the sliders and
 * where every HUD module sits), in {@code config/mistly-presets/preset-<n>.json}. Loading one
 * puts it in use right away.
 *
 * <p>The first time, with no preset yet, slot 1 holds a ready-made one, "PvP Mistly". A slot may
 * have a name ({@code preset-<n>.name}); saving over it makes it the player's own again.
 */
public final class Presets {
	public static final int COUNT = 5;
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM HH:mm");
	private static final String STARTER_NAME = "PvP Mistly";

	private Presets() {
	}

	private static Path folder() {
		return FabricLoader.getInstance().getConfigDir().resolve("mistly-presets");
	}

	private static Path file(final int slot) {
		return folder().resolve("preset-" + slot + ".json");
	}

	private static Path nameFile(final int slot) {
		return folder().resolve("preset-" + slot + ".name");
	}

	/** The slot's own name ("PvP Mistly"), null for a preset of the player's (shown as "Preset 2"). */
	public static @Nullable String name(final int slot) {
		try {
			Path file = nameFile(slot);
			return Files.isRegularFile(file) ? Files.readString(file, StandardCharsets.UTF_8).strip() : null;
		} catch (IOException e) {
			return null;
		}
	}

	/** When the slot was saved ("17/11 22:50"), null when it is empty. */
	public static @Nullable String savedAt(final int slot) {
		Path file = file(slot);
		try {
			if (!Files.isRegularFile(file)) {
				return null;
			}
			Instant at = Files.getLastModifiedTime(file).toInstant();
			return DATE.format(LocalDateTime.ofInstant(at, ZoneId.systemDefault()));
		} catch (IOException e) {
			return null;
		}
	}

	/** Keeps the settings in use in the slot (replacing what it held, which becomes the player's). */
	public static void save(final int slot) {
		MistyClient.config().write(file(slot));
		try {
			Files.deleteIfExists(nameFile(slot));
		} catch (IOException e) {
			MistyClient.LOG.warn("Preset name not removed", e);
		}
	}

	/** Puts the slot's settings in use; false when it is empty or unreadable. */
	public static boolean load(final int slot) {
		MistyConfig loaded = MistyConfig.read(file(slot));
		if (loaded == null) {
			return false;
		}
		MistyClient.useConfig(loaded);
		return true;
	}

	/** At start: the first time, with no preset at all, slot 1 gets the ready-made "PvP Mistly". */
	public static void starter() {
		Path marker = folder().resolve("starter");
		if (Files.exists(marker) || IntStream.rangeClosed(1, COUNT).anyMatch(slot -> Files.isRegularFile(file(slot)))) {
			return;
		}
		pvp().write(file(1));
		try {
			Files.writeString(nameFile(1), STARTER_NAME, StandardCharsets.UTF_8);
			// once only: emptying the slots later does not bring it back
			Files.writeString(marker, "", StandardCharsets.UTF_8);
		} catch (IOException e) {
			MistyClient.LOG.warn("Ready-made preset not named", e);
		}
	}

	/**
	 * "PvP Mistly": the HUD of a PvP player (FPS, ping, CPS, reach, combo, keystrokes, armour,
	 * effects, the server, CubeCraft's kills and leaderboard) without the music, the PvP visuals
	 * (low fire, low shield, double hit particles), the kill ting and Auto GG.
	 */
	private static MistyConfig pvp() {
		MistyConfig config = new MistyConfig();
		config.particles = true;
		config.particleMultiplier = 200;
		config.lowFire = true;
		config.lowShield = true;
		config.killSound = true;
		config.autoGG = true;
		for (HudModule module : HudManager.modules()) {
			MistyConfig.Module state = module.defaultState();
			switch (module.id()) {
				case "armor", "server", "cps", "effects", "kills", "leaderboard" -> state.enabled = true;
				case "music", "clock" -> state.enabled = false;
				default -> {
				}
			}
			// the leaderboard info in the leaderboard's place, the music being off
			if (module.id().equals("leaderboard")) {
				state.y = 0;
			}
			config.modules.put(module.id(), state);
		}
		return config;
	}
}
