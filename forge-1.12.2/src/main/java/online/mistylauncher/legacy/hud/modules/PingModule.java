package online.mistylauncher.legacy.hud.modules;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.TextModule;
import online.mistylauncher.legacy.ui.Ui;

/** Your ping on the server, coloured by latency (not shown in single player). */
public final class PingModule extends TextModule {
	private static final String COLOR = "color";

	public PingModule() {
		super("ping", new Config.Module(true, 0, 4, 20), 50);
	}

	@Override
	public List<Setting> settings() {
		return Collections.singletonList(new Setting(COLOR, true));
	}

	/** Milliseconds, negative when unknown. */
	private static int ping(final Minecraft minecraft) {
		if (minecraft.player == null || minecraft.getConnection() == null || minecraft.isSingleplayer()) {
			return -1;
		}
		NetworkPlayerInfo info = minecraft.getConnection().getPlayerInfo(minecraft.player.getUniqueID());
		return info == null ? -1 : info.getResponseTime();
	}

	private static int shown(final Minecraft minecraft, final boolean preview) {
		int ms = ping(minecraft);
		// the editor shows a typical value while not connected
		return ms <= 0 && preview ? 42 : Math.max(0, ms);
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return ping(minecraft) >= 0;
	}

	@Override
	protected String value(final Minecraft minecraft, final boolean preview) {
		return Integer.toString(shown(minecraft, preview));
	}

	@Override
	protected String unit() {
		return "ms";
	}

	/** Green when the connection is good, then yellow, orange and red as the ping grows. */
	@Override
	protected int valueColor(final Minecraft minecraft, final boolean preview) {
		int ms = shown(minecraft, preview);
		if (!this.setting(COLOR) || ms <= 0) {
			return Ui.TEXT;
		}
		return 0xFF000000 | (ms < 80 ? 0x55E07A : ms < 150 ? 0xE8D44D : ms < 250 ? 0xF2994A : 0xEB5757);
	}
}
