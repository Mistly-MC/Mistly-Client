package online.mistylauncher.legacy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;

/** The settings, kept in {@code config/misty-client.json} of the game folder. */
public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static Config current = new Config();

	/** Zoom while the zoom key (C) is held. */
	public boolean zoom = true;
	/** Full brightness, in caves and at night. */
	public boolean fullbright = false;
	/** The cape equipped in the launcher, on your player. */
	public boolean cape = true;
	/** Ping and reach change colour with their value. */
	public boolean colours = true;
	/** Each HUD module: on or off, where, how big, and its own options. */
	public Map<String, Module> modules = new LinkedHashMap<String, Module>();

	public static final class Module {
		public boolean enabled;
		/** 0..8: column (left, centre, right) + 3 × row (top, middle, bottom). */
		public int anchor;
		public int x;
		public int y;
		public float scale = 1.0F;
		/** Counters (potions): the next kinds go to the right of the first one, else to its left. */
		public boolean right;
		/** Counters (potions): "Potions : 5" instead of the slots. */
		public boolean text;

		public Module() {
		}

		public Module(final boolean enabled, final int anchor, final int x, final int y) {
			this.enabled = enabled;
			this.anchor = anchor;
			this.x = x;
			this.y = y;
		}

		public Module copy() {
			Module copy = new Module(this.enabled, this.anchor, this.x, this.y);
			copy.scale = this.scale;
			copy.right = this.right;
			copy.text = this.text;
			return copy;
		}
	}

	public static Config get() {
		return current;
	}

	private static File file() {
		return new File(Minecraft.getMinecraft().mcDataDir, "config/misty-client.json");
	}

	public static void load() {
		File file = file();
		if (!file.isFile()) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
			Config read = GSON.fromJson(reader, Config.class);
			if (read != null) {
				if (read.modules == null) {
					read.modules = new LinkedHashMap<String, Module>();
				}
				current = read;
			}
		} catch (IOException | RuntimeException e) {
			// a damaged file: the defaults, written again at the next save
			System.err.println("[Mistly Client] settings unreadable, defaults used: " + e);
		}
	}

	public static void save() {
		File file = file();
		try {
			Files.createDirectories(file.getParentFile().toPath());
			File temporary = new File(file.getParentFile(), file.getName() + ".tmp");
			try (Writer writer = Files.newBufferedWriter(temporary.toPath(), StandardCharsets.UTF_8)) {
				GSON.toJson(current, writer);
			}
			Files.move(temporary.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.err.println("[Mistly Client] settings not saved: " + e);
		}
	}
}
