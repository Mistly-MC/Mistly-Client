package online.mistylauncher.client.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import net.fabricmc.loader.api.FabricLoader;
import online.mistylauncher.client.MistyClient;
import org.jspecify.annotations.Nullable;

/**
 * Presets: five slots that keep all the Mistly settings at once (the switches, the sliders and
 * where every HUD module sits), in {@code config/mistly-presets/preset-<n>.json}. Loading one
 * puts it in use right away.
 */
public final class Presets {
	public static final int COUNT = 5;
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM HH:mm");

	private Presets() {
	}

	private static Path file(final int slot) {
		return FabricLoader.getInstance().getConfigDir().resolve("mistly-presets").resolve("preset-" + slot + ".json");
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

	/** Keeps the settings in use in the slot (replacing what it held). */
	public static void save(final int slot) {
		MistyClient.config().write(file(slot));
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
}
