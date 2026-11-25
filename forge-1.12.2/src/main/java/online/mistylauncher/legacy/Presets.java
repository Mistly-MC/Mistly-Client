package online.mistylauncher.legacy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.hud.Hud;
import online.mistylauncher.legacy.hud.HudModule;

/**
 * Presets, as in the Fabric versions: five slots that keep all the Mistly settings at once (the
 * switches, the sliders and where every HUD module sits), in {@code config/mistly-presets/}.
 * Loading one puts it in use right away. The first time, slot 1 holds a ready-made one, "PvP
 * Mistly"; saving over a named slot makes it the player's own again.
 */
public final class Presets {
	public static final int COUNT = 5;
	private static final String STARTER_NAME = "PvP Mistly";

	private Presets() {
	}

	private static File folder() {
		return new File(Minecraft.getMinecraft().gameDir, "config/mistly-presets");
	}

	private static File file(final int slot) {
		return new File(folder(), "preset-" + slot + ".json");
	}

	private static File nameFile(final int slot) {
		return new File(folder(), "preset-" + slot + ".name");
	}

	/** The slot's own name ("PvP Mistly"), null for a preset of the player's. */
	public static String name(final int slot) {
		try {
			File file = nameFile(slot);
			return file.isFile() ? new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim() : null;
		} catch (IOException e) {
			return null;
		}
	}

	/** When the slot was saved ("17/11 22:50"), null when it is empty. */
	public static String savedAt(final int slot) {
		File file = file(slot);
		return file.isFile() ? new SimpleDateFormat("dd/MM HH:mm").format(new Date(file.lastModified())) : null;
	}

	/** Keeps the settings in use in the slot (it becomes the player's own). */
	public static void save(final int slot) {
		Config.get().write(file(slot));
		nameFile(slot).delete();
	}

	/** Puts the slot's settings in use; false when it is empty or unreadable. */
	public static boolean load(final int slot) {
		Config loaded = Config.read(file(slot));
		if (loaded == null) {
			return false;
		}
		Config.use(loaded);
		return true;
	}

	/** At start: the first time, with no preset at all, slot 1 gets the ready-made "PvP Mistly". */
	public static void starter() {
		File marker = new File(folder(), "starter");
		if (marker.exists()) {
			return;
		}
		for (int slot = 1; slot <= COUNT; slot++) {
			if (file(slot).isFile()) {
				return;
			}
		}
		pvp().write(file(1));
		try {
			Files.write(nameFile(1).toPath(), STARTER_NAME.getBytes(StandardCharsets.UTF_8));
			// once only: emptying the slots later does not bring it back
			Files.write(marker.toPath(), new byte[0]);
		} catch (IOException e) {
			System.err.println("[Mistly Client] ready-made preset not named: " + e);
		}
	}

	/** "PvP Mistly": the HUD of a PvP player, low fire, double hit particles, the kill ting and Auto GG. */
	private static Config pvp() {
		Config config = new Config();
		config.particles = true;
		config.particleMultiplier = 200;
		config.lowFire = true;
		config.killSound = true;
		config.autoGG = true;
		for (HudModule module : Hud.modules()) {
			Config.Module state = module.defaultState();
			String id = module.id();
			if ("armor".equals(id) || "server".equals(id) || "cps".equals(id) || "effects".equals(id) || "kills".equals(id) || "leaderboard".equals(id)) {
				state.enabled = true;
			} else if ("music".equals(id) || "clock".equals(id)) {
				state.enabled = false;
			}
			// the leaderboard in the leaderboard's place, the music being off
			if ("leaderboard".equals(id)) {
				state.y = 0;
			}
			config.modules.put(id, state);
		}
		return config;
	}
}
