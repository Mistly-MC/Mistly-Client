package online.mistylauncher.client.hud.modules;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.hud.HudManager;
import online.mistylauncher.client.hud.TextModule;
import online.mistylauncher.client.ui.EffectSettingsScreen;
import online.mistylauncher.client.ui.Palette;
import online.mistylauncher.client.ui.Ui;

/**
 * Saturation, as AppleSkin shows it: the hidden food reserve the game spends before the hunger
 * bar, drawn over the drumsticks of the hunger bar (each drumstick holds 2 points: its outline in
 * the module's colour, as much of it as it holds). Behind the gear: the colour, and "As text" to
 * also show it in a box ("5.0 saturation") placed like any module.
 */
public final class SaturationModule extends TextModule {
	private static final String COLOR = "color";
	private static final String TEXT = "text";
	/** Yellow, the colour of the saturation in AppleSkin. */
	private static final int DEFAULT_COLOR = 3;
	private static final int ICON = 9;

	public SaturationModule() {
		super("saturation", new MistyConfig.Module(false, 6, 4, -40), 60);
	}

	@Override
	public List<Setting> settings() {
		return List.of(new Setting(TEXT, false));
	}

	@Override
	public List<EffectSettingsScreen.Slider> sliders() {
		return List.of((x, y, w, h) -> new Palette.Slider(x, y, w, h, "misty.option.color",
			() -> this.state().value(COLOR, DEFAULT_COLOR), value -> this.state().setValue(COLOR, value)));
	}

	private int color() {
		return Palette.color(this.state().value(COLOR, DEFAULT_COLOR));
	}

	/** The box only "as text"; the bar overlay does not need it. */
	@Override
	public boolean available(final Minecraft minecraft) {
		return minecraft.player != null && this.setting(TEXT);
	}

	@Override
	protected Component text(final Minecraft minecraft, final boolean preview) {
		float saturation = minecraft.player == null ? 5.0F : minecraft.player.getFoodData().getSaturationLevel();
		return value(String.format(Locale.ROOT, "%.1f", saturation), this.color(), "saturation");
	}

	/**
	 * After the game drew the hunger bar ({@code xRight}: its right end, {@code yLine}: its top):
	 * the saturation over its drumsticks, right to left like the food, 2 points each.
	 */
	public static void overlay(final GuiGraphicsExtractor graphics, final Player player, final int yLine, final int xRight) {
		if (!MistyClient.config().hud) {
			return;
		}
		SaturationModule module = null;
		for (var m : HudManager.modules()) {
			if (m instanceof SaturationModule saturation) {
				module = saturation;
			}
		}
		if (module == null || !module.state().enabled) {
			return;
		}
		float saturation = player.getFoodData().getSaturationLevel();
		int color = module.color();
		for (int i = 0; i < 10; i++) {
			float held = Math.min(2.0F, saturation - i * 2.0F);
			if (held <= 0.0F) {
				break;
			}
			int x = xRight - i * 8 - 9;
			// the part of the drumstick it fills, by quarters, from its right side
			int w = Math.max(2, Math.round(held / 2.0F * 4.0F) * ICON / 4);
			int left = x + ICON - w;
			graphics.fill(left, yLine, x + ICON, yLine + ICON, Ui.alpha(color, 0.25F));
			graphics.fill(left, yLine, x + ICON, yLine + 1, color);
			graphics.fill(left, yLine + ICON - 1, x + ICON, yLine + ICON, color);
			graphics.fill(x + ICON - 1, yLine, x + ICON, yLine + ICON, color);
			graphics.fill(left, yLine, left + 1, yLine + ICON, color);
		}
	}
}
