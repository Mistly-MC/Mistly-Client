package online.mistylauncher.client.hud.modules;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.hud.TextModule;
import online.mistylauncher.client.ui.Ui;

/**
 * Memory the game uses out of what it may take ("2.3 / 5.0 GB"), read once a second. Behind the
 * gear: coloured by use, green, then yellow over 60 %, red over 85 %.
 */
public final class RamModule extends TextModule {
	private static final String COLOR = "color";
	private static final double GB = 1024.0 * 1024.0 * 1024.0;

	private long readAt;
	private long used;
	private long max;

	public RamModule() {
		super("ram", new MistyConfig.Module(false, 0, 4, 68), 70);
	}

	@Override
	public List<Setting> settings() {
		return List.of(new Setting(COLOR, true));
	}

	@Override
	protected Component text(final Minecraft minecraft, final boolean preview) {
		long now = Util.getMillis();
		if (now - this.readAt > 1000) {
			this.readAt = now;
			Runtime runtime = Runtime.getRuntime();
			this.used = runtime.totalMemory() - runtime.freeMemory();
			this.max = runtime.maxMemory();
		}
		double share = this.max <= 0 ? 0 : (double)this.used / this.max;
		int color = !this.setting(COLOR) ? Ui.TEXT : share > 0.85 ? 0xFF5A6E : share > 0.6 ? 0xE8D44D : 0x55E07A;
		return value(String.format(Locale.ROOT, "%.1f / %.1f", this.used / GB, this.max / GB), color, "GB RAM");
	}
}
