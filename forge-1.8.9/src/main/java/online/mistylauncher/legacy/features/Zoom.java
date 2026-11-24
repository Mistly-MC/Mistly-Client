package online.mistylauncher.legacy.features;

import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.Keys;

/** Zoom while the zoom key (C) is held: the field of view divided by the level of the settings (×4 at first), with the smooth camera. */
public final class Zoom {
	public static final float MIN = 1.5F;
	public static final float MAX = 10.0F;

	private static boolean zooming;
	private static float fov;
	private static boolean smooth;

	private Zoom() {
	}

	/** How much it magnifies, from the settings. */
	public static float level() {
		return Math.max(MIN, Math.min(MAX, Config.get().zoomLevel));
	}

	public static void tick(final Minecraft minecraft) {
		boolean wanted = Config.get().zoom && minecraft.currentScreen == null && minecraft.thePlayer != null
			&& Keys.ZOOM.isKeyDown();
		if (wanted && !zooming) {
			fov = minecraft.gameSettings.fovSetting;
			smooth = minecraft.gameSettings.smoothCamera;
			zooming = true;
		}
		if (zooming && !wanted) {
			minecraft.gameSettings.fovSetting = fov;
			minecraft.gameSettings.smoothCamera = smooth;
			zooming = false;
		}
		if (zooming) {
			minecraft.gameSettings.fovSetting = fov / level();
			minecraft.gameSettings.smoothCamera = Config.get().zoomSmooth || smooth;
		}
	}
}
