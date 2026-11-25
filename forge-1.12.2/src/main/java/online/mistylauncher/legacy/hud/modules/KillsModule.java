package online.mistylauncher.legacy.hud.modules;

import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.features.CubeCraft;
import online.mistylauncher.legacy.hud.TextModule;

/**
 * Kills of the current CubeCraft game, in Lucky Islands, EggWars, BedWars, SkyWars, Pillars of
 * Fortune and FFA. Shown only while the player is in one of them.
 */
public final class KillsModule extends TextModule {
	public KillsModule() {
		super("kills", new Config.Module(true, 3, 4, 100), 50);
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return CubeCraft.countingKills();
	}

	@Override
	protected String value(final Minecraft minecraft, final boolean preview) {
		return Integer.toString(preview && !this.available(minecraft) ? 4 : CubeCraft.kills());
	}

	@Override
	protected String unit() {
		return CubeCraft.kills() == 1 ? "kill" : "kills";
	}
}
