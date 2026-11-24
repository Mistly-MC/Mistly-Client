package online.mistylauncher.legacy.hud.modules;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.TextModule;
import online.mistylauncher.legacy.ui.Ui;

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
		super("ram", new Config.Module(false, 0, 4, 68), 70);
	}

	@Override
	public List<Setting> settings() {
		return Collections.singletonList(new Setting(COLOR, true));
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return true;
	}

	private void read() {
		long now = System.currentTimeMillis();
		if (now - this.readAt > 1000) {
			this.readAt = now;
			Runtime runtime = Runtime.getRuntime();
			this.used = runtime.totalMemory() - runtime.freeMemory();
			this.max = runtime.maxMemory();
		}
	}

	@Override
	protected String value(final Minecraft minecraft, final boolean preview) {
		this.read();
		return String.format(Locale.ROOT, "%.1f / %.1f", this.used / GB, this.max / GB);
	}

	@Override
	protected String unit() {
		return "GB RAM";
	}

	@Override
	protected int valueColor(final Minecraft minecraft, final boolean preview) {
		this.read();
		double share = this.max <= 0 ? 0 : (double)this.used / this.max;
		return !this.setting(COLOR) ? Ui.TEXT : 0xFF000000 | (share > 0.85 ? 0xFF5A6E : share > 0.6 ? 0xE8D44D : 0x55E07A);
	}
}
