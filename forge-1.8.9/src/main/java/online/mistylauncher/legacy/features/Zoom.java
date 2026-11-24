package online.mistylauncher.legacy.features;

import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.Keys;

/** Zoom while the zoom key (C) is held: a quarter of the field of view, with the smooth camera. */
public final class Zoom {
	private static boolean zooming;
	private static float fov;
	private static boolean smooth;

	private Zoom() {
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
			minecraft.gameSettings.fovSetting = fov / 4.0F;
			minecraft.gameSettings.smoothCamera = true;
		}
	}
}
