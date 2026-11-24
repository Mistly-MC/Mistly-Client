package online.mistylauncher.legacy.hud.modules;

import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.features.Combat;
import online.mistylauncher.legacy.hud.TextModule;

/** The players you hit in a row; being hit or 3 seconds without hitting ends it. */
public final class ComboModule extends TextModule {
	public ComboModule() {
		super("combo", new Config.Module(true, 0, 2, 62));
	}

	@Override
	protected String text(final Minecraft minecraft, final boolean preview) {
		return Combat.combo() + " combo";
	}
}
