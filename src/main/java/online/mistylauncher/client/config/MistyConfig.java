package online.mistylauncher.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import online.mistylauncher.client.MistyClient;
import org.jspecify.annotations.Nullable;

/** Settings of the mod, kept in {@code config/misty-client.json} of the profile. */
public final class MistyConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/** Misty main menu instead of the Minecraft one. */
	public boolean customMenu = true;
	/** HUD modules shown in game. */
	public boolean hud = true;
	/**
	 * Misty network: other Misty players see your badge and cosmetics, and you see theirs.
	 * Off: nothing is sent and nothing is fetched.
	 */
	public boolean network = true;
	/** Misty capes and elytra are drawn (yours and the other players'). */
	public boolean cosmetics = true;
	/** Zoom while the zoom key (C) is held. */
	public boolean zoom = true;
	/** How much the zoom magnifies (1.5 to 10), changed with the mouse wheel while zooming. */
	public float zoomLevel = 4.0F;
	/** The zoom glides in and out instead of jumping. */
	public boolean zoomSmooth = true;
	/** The mouse wheel changes the zoom while the key is held. */
	public boolean zoomScroll = true;
	/** The wheel's change only lasts while the key is held: the next zoom starts from zoomLevel again. */
	public boolean zoomReset = true;
	/** The Misty cloud in front of the Misty players' name above their head. */
	public boolean nametagBadge = true;
	/** Your own name above your head in third person (F5). */
	public boolean ownNametag = false;
	/** Fullbright: everything lit, even caves and nights. */
	public boolean fullbright = false;
	/** Motion blur: what moves on the screen leaves a short trail (see MotionBlur). */
	public boolean motionBlur = false;
	/** How long the trail is: 1 (short) to 9 (long). */
	public int motionBlurStrength = 5;
	/** Particle multiplier: the hit particles (crit, sharpness) are spawned several times. */
	public boolean particles = false;
	/** How many times, in percent: 100 (the game's) to 1000 (×10). */
	public int particleMultiplier = 200;
	/** Shulker preview: Shift over a shulker box shows what it holds as a picture. */
	public boolean shulkerPreview = true;
	/** The sidebar of the servers (the leaderboard on the right of the screen) is not drawn. */
	public boolean hideScoreboard = false;
	/** Low fire: the flames over the screen when burning are drawn lower. */
	public boolean lowFire = false;
	/** Low shield: the shield in hand is held lower in first person. */
	public boolean lowShield = false;
	/** Short swords: swords in hand are drawn smaller in first person. */
	public boolean shortSword = false;
	/** Color of the hitboxes: an index in Palette.COLORS (0: white, the game's). */
	public int hitboxColor = 0;
	/** Auto GG: "gg" in the chat at the end of each CubeCraft game (not FFA nor Parkour). */
	public boolean autoGG = false;
	/** TNT countdown: the seconds before a lit TNT explodes, over it. */
	public boolean tntCountdown = true;
	/** Kill sound: a ting when a player you hit dies. */
	public boolean killSound = false;
	/** Damage indicators: the damage you deal, in the air over what you hit. */
	public boolean damageIndicators = false;
	/** ... on players. */
	public boolean damagePlayers = true;
	/** ... on mobs. */
	public boolean damageMobs = true;
	/** Chunk viewer colours: the chunk's border and the grid inside (indexes in Palette.COLORS). */
	public int chunkBorderColor = 5;
	public int chunkGridColor = 3;
	/** Dropped items lie flat on the ground, still (items "with physics"). */
	public boolean itemPhysics = false;
	/** Dropped items are flat pictures turned to you (2D items); turns the physics off. */
	public boolean items2D = false;
	/** State of each HUD module, by module id. */
	public Map<String, Module> modules = new LinkedHashMap<>();

	/** Where a HUD module sits: a corner, side or centre of the screen plus an offset in GUI pixels. */
	public static final class Module {
		public boolean enabled;
		/** 0..8: column (left, centre, right) + 3 × row (top, middle, bottom). */
		public int anchor;
		public int x;
		public int y;
		public float scale = 1.0F;
		/** The module's own settings (its gear in the settings screen), by key; missing: its default. */
		public Map<String, Boolean> options = new LinkedHashMap<>();

		public Module() {
		}

		public boolean option(final String key, final boolean byDefault) {
			Boolean value = this.options == null ? null : this.options.get(key);
			return value == null ? byDefault : value;
		}

		public void setOption(final String key, final boolean value) {
			if (this.options == null) {
				this.options = new LinkedHashMap<>();
			}
			this.options.put(key, value);
		}

		public Module(final boolean enabled, final int anchor, final int x, final int y) {
			this.enabled = enabled;
			this.anchor = anchor;
			this.x = x;
			this.y = y;
		}

		/** The module's number settings (a colour's index…), by key; missing: its default. */
		public Map<String, Integer> values = new LinkedHashMap<>();

		public int value(final String key, final int byDefault) {
			Integer value = this.values == null ? null : this.values.get(key);
			return value == null ? byDefault : value;
		}

		public void setValue(final String key, final int value) {
			if (this.values == null) {
				this.values = new LinkedHashMap<>();
			}
			this.values.put(key, value);
		}

		public Module copy() {
			Module copy = new Module(this.enabled, this.anchor, this.x, this.y);
			copy.scale = this.scale;
			copy.options = this.options == null ? new LinkedHashMap<>() : new LinkedHashMap<>(this.options);
			copy.values = this.values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(this.values);
			return copy;
		}
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("misty-client.json");
	}

	public static MistyConfig load() {
		MistyConfig config = read(file());
		return config != null ? config : new MistyConfig();
	}

	/** The settings kept in {@code file} (the settings file or a preset), null when missing or unreadable. */
	public static @Nullable MistyConfig read(final Path file) {
		if (Files.isRegularFile(file)) {
			try {
				MistyConfig config = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), MistyConfig.class);
				if (config != null) {
					if (config.modules == null) {
						config.modules = new LinkedHashMap<>();
					}
					config.modules.values().removeIf(module -> module == null);
					return config;
				}
			} catch (IOException | RuntimeException e) {
				MistyClient.LOG.warn("Unreadable settings in {}", file.getFileName(), e);
			}
		}
		return null;
	}

	public void save() {
		this.write(file());
	}

	/** Writes these settings in {@code file} (the settings file or a preset). */
	public void write(final Path file) {
		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(this), StandardCharsets.UTF_8);
		} catch (IOException e) {
			MistyClient.LOG.warn("Settings not saved in {}", file.getFileName(), e);
		}
	}
}
