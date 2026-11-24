package online.mistylauncher.legacy.hud.modules;

import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.TextModule;

/** Frames per second. */
public final class FpsModule extends TextModule {
	public FpsModule() {
		super("fps", new Config.Module(true, 0, 2, 2));
	}

	@Override
	protected String text(final Minecraft minecraft, final boolean preview) {
		return Minecraft.getDebugFPS() + " FPS";
	}
}
