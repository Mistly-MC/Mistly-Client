package online.mistylauncher.legacy.hud.modules;

import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.TextModule;

/** Frames per second. */
public final class FpsModule extends TextModule {
	public FpsModule() {
		super("fps", new Config.Module(true, 0, 4, 4), 50);
	}

	@Override
	protected String value(final Minecraft minecraft, final boolean preview) {
		return Integer.toString(Minecraft.getDebugFPS());
	}

	@Override
	protected String unit() {
		return "FPS";
	}
}
