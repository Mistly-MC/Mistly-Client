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
	/** How much the zoom magnifies (×1.5 to ×10). */
	public float zoomLevel = 4.0F;
	/** The zoom glides in and out. */
	public boolean zoomSmooth = true;
	/** The wheel changes the zoom while it is held. */
	public boolean zoomScroll = true;
	/** Each zoom starts again from the level of the settings (the wheel's change is not kept). */
	public boolean zoomReset = true;
	/** Full brightness, in caves and at night. */
	public boolean fullbright = false;
	/** The Mistly cosmetics shown (yours and the other Mistly players'). */
	public boolean cape = true;
	/** The Mistly main menu in place of the game's. */
	public boolean customMenu = true;
	/** The HUD modules in game. */
	public boolean hud = true;
	/** The launcher equipped the early player cape once: it can be worn from the wardrobe. */
	public boolean earlyCape;
	/** The Mistly network: the other Mistly players see you, and you see them (their cosmetics, their badge). */
	public boolean network = true;
	/** The Mistly cloud before the Mistly players' names above their head. */
	public boolean nametagBadge = true;
	/** Your own name above your head in third person (F5). */
	public boolean ownNametag = false;
	/** Motion blur, and its strength (1 to 9). */
	public boolean motionBlur = false;
	public int motionBlurStrength = 5;
	/** More hit particles (critical hits and sharpness), and how many (100 % to 1000 %). */
	public boolean particles = false;
	public int particleMultiplier = 200;
	/** The server's leaderboard (sidebar) hidden. */
	public boolean hideScoreboard = false;
	/** Low fire: the flames over the screen when burning cover less of it. */
	public boolean lowFire = false;
	/** Short swords: swords drawn smaller in hand. */
	public boolean shortSword = false;
	/** Entity hitboxes always shown, and their colour (index of the palette). */
	public boolean hitboxes = false;
	public int hitboxColor = 0;
	/** Enchantment glint in another colour: a palette colour, or the custom one after the palette. */
	public boolean glint = false;
	public int glintColor = 7;
	public int glintCustom = 0x8F4DFF;
	/** "gg" in the chat at the end of each CubeCraft game. */
	public boolean autoGG = false;
	/** The seconds left over lit TNT. */
	public boolean tntCountdown = true;
	/** A "ting" when something you hit dies. */
	public boolean killSound = false;
	/** Behind its gear: the ting for the players you kill, and for the mobs. */
	public boolean killSoundPlayers = true;
	public boolean killSoundMobs = false;
	/** Chunk borders always shown, and their colours (border, grid). */
	public boolean chunkViewer = false;
	public int chunkBorderColor = 5;
	public int chunkGridColor = 3;
	/** Dropped items lying flat on the ground, or drawn in 2D facing you (one or the other). */
	public boolean itemPhysics = false;
	public boolean items2D = false;
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
		/** The module's other settings (behind its gear), by key. */
		public Map<String, Boolean> options = new LinkedHashMap<String, Boolean>();
		/** The module's numbers behind its gear (a colour…), by key. */
		public Map<String, Integer> values = new LinkedHashMap<String, Integer>();

		public Module() {
		}

		public Module(final boolean enabled, final int anchor, final int x, final int y) {
			this.enabled = enabled;
			this.anchor = anchor;
			this.x = x;
			this.y = y;
		}

		/** One of the module's settings, {@code byDefault} when never changed. */
		public boolean option(final String key, final boolean byDefault) {
			if ("text".equals(key)) {
				return this.text;
			}
			if ("right".equals(key)) {
				return this.right;
			}
			Boolean value = this.options == null ? null : this.options.get(key);
			return value == null ? byDefault : value;
		}

		public void setOption(final String key, final boolean value) {
			if ("text".equals(key)) {
				this.text = value;
			} else if ("right".equals(key)) {
				this.right = value;
			} else {
				if (this.options == null) {
					this.options = new LinkedHashMap<String, Boolean>();
				}
				this.options.put(key, value);
			}
		}

		public int value(final String key, final int byDefault) {
			Integer value = this.values == null ? null : this.values.get(key);
			return value == null ? byDefault : value;
		}

		public void setValue(final String key, final int value) {
			if (this.values == null) {
				this.values = new LinkedHashMap<String, Integer>();
			}
			this.values.put(key, value);
		}

		public Module copy() {
			Module copy = new Module(this.enabled, this.anchor, this.x, this.y);
			copy.scale = this.scale;
			copy.right = this.right;
			copy.text = this.text;
			copy.options = new LinkedHashMap<String, Boolean>(this.options == null ? new LinkedHashMap<String, Boolean>() : this.options);
			copy.values = new LinkedHashMap<String, Integer>(this.values == null ? new LinkedHashMap<String, Integer>() : this.values);
			return copy;
		}
	}

	public static Config get() {
		return current;
	}

	/** Puts other settings in use (a preset loaded). */
	public static void use(final Config config) {
		current = config;
		save();
	}

	private static File file() {
		return new File(Minecraft.getMinecraft().mcDataDir, "config/misty-client.json");
	}

	public static void load() {
		Config read = read(file());
		if (read != null) {
			current = read;
		}
	}

	/** Settings read from a file, null when it is missing or damaged. */
	public static Config read(final File file) {
		if (!file.isFile()) {
			return null;
		}
		try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
			Config read = GSON.fromJson(reader, Config.class);
			if (read != null && read.modules == null) {
				read.modules = new LinkedHashMap<String, Module>();
			}
			return read;
		} catch (IOException | RuntimeException e) {
			// a damaged file: the defaults, written again at the next save
			System.err.println("[Mistly Client] settings unreadable, defaults used: " + e);
			return null;
		}
	}

	public static void save() {
		current.write(file());
	}

	/** Writes these settings in {@code file} (the settings in use, or a preset). */
	public void write(final File file) {
		try {
			Files.createDirectories(file.getParentFile().toPath());
			File temporary = new File(file.getParentFile(), file.getName() + ".tmp");
			try (Writer writer = Files.newBufferedWriter(temporary.toPath(), StandardCharsets.UTF_8)) {
				GSON.toJson(this, writer);
			}
			Files.move(temporary.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.err.println("[Mistly Client] settings not saved: " + e);
		}
	}
}
