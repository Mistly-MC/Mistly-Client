package online.mistylauncher.legacy.hud.modules;

import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.features.Clicks;
import online.mistylauncher.legacy.hud.TextModule;

/** Clicks per second, left | right. */
public final class CpsModule extends TextModule {
	public CpsModule() {
		super("cps", new Config.Module(true, 0, 2, 17));
	}

	@Override
	protected String text(final Minecraft minecraft, final boolean preview) {
		return Clicks.left() + " | " + Clicks.right() + " CPS";
	}
}
