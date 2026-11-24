package online.mistylauncher.legacy.hud.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.Hud;
import online.mistylauncher.legacy.hud.TextModule;

/** Your ping on the server, coloured by latency (not shown in single player). */
public final class PingModule extends TextModule {
	public PingModule() {
		super("ping", new Config.Module(true, 0, 2, 32));
	}

	/** Milliseconds, negative when unknown. */
	private static int ping(final Minecraft minecraft) {
		if (minecraft.thePlayer == null || minecraft.getNetHandler() == null || minecraft.isSingleplayer()) {
			return -1;
		}
		NetworkPlayerInfo info = minecraft.getNetHandler().getPlayerInfo(minecraft.thePlayer.getUniqueID());
		return info == null ? -1 : info.getResponseTime();
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return ping(minecraft) >= 0;
	}

	@Override
	protected String text(final Minecraft minecraft, final boolean preview) {
		int ms = ping(minecraft);
		return (ms < 0 ? 42 : ms) + " ms";
	}

	@Override
	protected int colour(final Minecraft minecraft) {
		int ms = ping(minecraft);
		return ms >= 0 && Hud.colours() ? pingColour(ms) : TEXT;
	}
}
