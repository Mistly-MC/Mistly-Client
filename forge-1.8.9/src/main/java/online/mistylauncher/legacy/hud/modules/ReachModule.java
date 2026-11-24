package online.mistylauncher.legacy.hud.modules;

import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.features.Combat;
import online.mistylauncher.legacy.hud.Hud;
import online.mistylauncher.legacy.hud.TextModule;

/** The distance of your last hit on a player, coloured from green (close) to red (far). */
public final class ReachModule extends TextModule {
	public ReachModule() {
		super("reach", new Config.Module(true, 0, 2, 47));
	}

	@Override
	protected String text(final Minecraft minecraft, final boolean preview) {
		double reach = Combat.reach();
		return reach < 0 ? "- blocks" : String.format(java.util.Locale.ROOT, "%.2f blocks", reach);
	}

	@Override
	protected int colour(final Minecraft minecraft) {
		double reach = Combat.reach();
		return reach >= 0 && Hud.colours() ? reachColour(reach) : TEXT;
	}
}
