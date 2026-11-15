package online.mistylauncher.client.hud.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.features.CubeCraftGame;
import online.mistylauncher.client.hud.TextModule;

/**
 * Kills of the current CubeCraft game (Lucky Islands for now, see CubeCraftGame). Shown only
 * while the player is in that mode: it stays on, and comes back with the next game.
 */
public final class KillsModule extends TextModule {
	public KillsModule() {
		super("kills", new MistyConfig.Module(true, 3, 4, 100), 50);
	}

	/** Switched on in the CubeCraft options of the Server category, not in the HUD one. */
	@Override
	public boolean inHudCategory() {
		return false;
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return CubeCraftGame.playing(CubeCraftGame.LUCKY_ISLANDS);
	}

	@Override
	protected Component text(final Minecraft minecraft, final boolean preview) {
		int kills = CubeCraftGame.kills();
		return value(Integer.toString(preview && !this.available(minecraft) ? 4 : kills), kills == 1 ? "kill" : "kills");
	}
}
