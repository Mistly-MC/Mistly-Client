package online.mistylauncher.legacy.hud.modules;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.features.Combat;
import online.mistylauncher.legacy.hud.TextModule;
import online.mistylauncher.legacy.ui.Ui;

/** The distance of your last hit on a player, coloured from green (close) to red (far). */
public final class ReachModule extends TextModule {
	private static final String COLOR = "color";
	private static final float NEAR = 2.0F;
	private static final float MIDDLE = 2.6F;
	private static final float FAR = 3.0F;
	private static final int GREEN = 0x55E07A;
	private static final int YELLOW = 0xE8D44D;
	private static final int RED = 0xEB5757;

	public ReachModule() {
		super("reach", new Config.Module(true, 3, 4, 64), 58);
	}

	@Override
	public List<Setting> settings() {
		return Collections.singletonList(new Setting(COLOR, true));
	}

	private static double reach(final boolean preview) {
		double reach = Combat.reach();
		return reach < 0.0 && preview ? 2.85 : reach;
	}

	@Override
	protected String value(final Minecraft minecraft, final boolean preview) {
		double reach = reach(preview);
		return reach < 0.0 ? "-" : String.format(Locale.ROOT, "%.2f", reach);
	}

	@Override
	protected String unit() {
		return "m";
	}

	/** The farther the hit, the redder: green, yellow, then red, blended in between. */
	@Override
	protected int valueColor(final Minecraft minecraft, final boolean preview) {
		double reach = reach(preview);
		if (reach < 0.0 || !this.setting(COLOR)) {
			return Ui.TEXT;
		}
		int color;
		if (reach <= NEAR) {
			color = GREEN;
		} else if (reach <= MIDDLE) {
			color = blend(GREEN, YELLOW, (float)((reach - NEAR) / (MIDDLE - NEAR)));
		} else {
			color = blend(YELLOW, RED, Ui.clamp((float)((reach - MIDDLE) / (FAR - MIDDLE)), 0.0F, 1.0F));
		}
		return 0xFF000000 | color;
	}

	private static int blend(final int from, final int to, final float t) {
		int r = Math.round(Ui.lerp(t, from >> 16 & 0xFF, to >> 16 & 0xFF));
		int g = Math.round(Ui.lerp(t, from >> 8 & 0xFF, to >> 8 & 0xFF));
		int b = Math.round(Ui.lerp(t, from & 0xFF, to & 0xFF));
		return r << 16 | g << 8 | b;
	}
}
