package online.mistylauncher.legacy.features;

import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;

/** Full brightness: the gamma well above "Bright", given back as it was when turned off. */
public final class Fullbright {
	private static final float BRIGHT = 16.0F;
	private static boolean on;
	private static float gamma;

	private Fullbright() {
	}

	public static void tick(final Minecraft minecraft) {
		boolean wanted = Config.get().fullbright;
		if (wanted && !on) {
			gamma = minecraft.gameSettings.gammaSetting;
			on = true;
		}
		if (on && !wanted) {
			minecraft.gameSettings.gammaSetting = gamma;
			on = false;
		}
		if (on) {
			minecraft.gameSettings.gammaSetting = BRIGHT;
		}
	}
}
