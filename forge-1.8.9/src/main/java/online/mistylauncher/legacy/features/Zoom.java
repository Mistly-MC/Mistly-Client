package online.mistylauncher.legacy.features;

import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.Keys;

/**
 * Zoom, as in the Fabric versions (like OptiFine's): hold the zoom key (C by default) to magnify,
 * the mouse wheel changes how much, the mouse turns slower while zoomed. It glides in and out
 * unless turned off.
 */
public final class Zoom {
	public static final float MIN = 1.5F;
	public static final float MAX = 10.0F;

	/** Current magnification (1 = no zoom), eased towards the wanted one. */
	private static float current = 1.0F;
	private static long lastFrame = System.currentTimeMillis();
	/** Level of this zoom (the wheel changes it); back to the setting at each new zoom when zoomReset. */
	private static float level = Float.NaN;
	private static boolean wasHeld;
	/** The level changed with the wheel: saved when the key is released (unless zoomReset). */
	private static boolean changed;
	/** The mouse sensitivity before the zoom slowed it, given back after. */
	private static float sensitivity = -1.0F;

	private Zoom() {
	}

	/** The level of the settings, kept in its bounds. */
	public static float level() {
		return Math.max(MIN, Math.min(MAX, Config.get().zoomLevel));
	}

	/** Zooming now: the key held in game (not in a menu). */
	public static boolean held() {
		Minecraft minecraft = Minecraft.getMinecraft();
		return Config.get().zoom && minecraft.currentScreen == null && minecraft.thePlayer != null && Keys.ZOOM.isKeyDown();
	}

	/** The field of view of this frame, divided by the magnification. */
	public static float fov(final float fov) {
		Config config = Config.get();
		long now = System.currentTimeMillis();
		float seconds = Math.max(0.0F, Math.min(0.1F, (now - lastFrame) / 1000.0F));
		lastFrame = now;
		boolean holding = held();
		if (holding && (!wasHeld || Float.isNaN(level))) {
			// a new zoom starts from the level of the settings
			level = config.zoomLevel;
		}
		wasHeld = holding;
		float wanted = holding ? Math.max(MIN, Math.min(MAX, level)) : 1.0F;
		if (config.zoomSmooth) {
			// eased: about a fifth of a second to get there
			current += (wanted - current) * (1.0F - (float)Math.exp(-seconds * 14.0F));
			if (Math.abs(current - wanted) < 0.002F) {
				current = wanted;
			}
		} else {
			current = wanted;
		}
		if (Float.isNaN(current) || current < 1.0F) {
			current = 1.0F;
		}
		return fov / current;
	}

	/** The mouse wheel while zooming: more or less zoom. True if the wheel was used for it. */
	public static boolean scroll(final int amount) {
		Config config = Config.get();
		if (!held() || !config.zoomScroll || amount == 0) {
			return false;
		}
		float step = amount > 0 ? 1.15F : 1.0F / 1.15F;
		level = Math.max(MIN, Math.min(MAX, (Float.isNaN(level) ? config.zoomLevel : level) * step));
		if (!config.zoomReset) {
			// kept for the next zooms
			config.zoomLevel = level;
			changed = true;
		}
		return true;
	}

	/** Each tick: the mouse slower while zoomed (aiming stays precise); the wheel's change saved. */
	public static void tick(final Minecraft minecraft) {
		boolean zoomed = current > 1.01F;
		if (zoomed && sensitivity < 0.0F) {
			sensitivity = minecraft.gameSettings.mouseSensitivity;
		}
		if (sensitivity >= 0.0F) {
			if (zoomed) {
				// the game's sensitivity is a 0..1 setting: scaled like the Fabric versions' factor
				minecraft.gameSettings.mouseSensitivity = sensitivity / (float)Math.sqrt(current);
			} else {
				minecraft.gameSettings.mouseSensitivity = sensitivity;
				sensitivity = -1.0F;
			}
		}
		if (changed && !held()) {
			changed = false;
			Config.save();
		}
	}
}
