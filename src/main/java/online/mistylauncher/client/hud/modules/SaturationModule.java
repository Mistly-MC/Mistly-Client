package online.mistylauncher.client.hud.modules;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.hud.TextModule;
import online.mistylauncher.client.ui.EffectSettingsScreen;
import online.mistylauncher.client.ui.Palette;

/**
 * Saturation: the hidden food reserve the game spends before the hunger bar ("5.0 saturation"),
 * in the colour picked behind the gear.
 */
public final class SaturationModule extends TextModule {
	private static final String COLOR = "color";
	/** Yellow, the colour of the game's saturation in most mods. */
	private static final int DEFAULT_COLOR = 3;

	public SaturationModule() {
		super("saturation", new MistyConfig.Module(false, 6, 4, -40), 60);
	}

	@Override
	public List<EffectSettingsScreen.Slider> sliders() {
		return List.of((x, y, w, h) -> new Palette.Slider(x, y, w, h, "misty.option.color",
			() -> this.state().value(COLOR, DEFAULT_COLOR), value -> this.state().setValue(COLOR, value)));
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return minecraft.player != null;
	}

	@Override
	protected Component text(final Minecraft minecraft, final boolean preview) {
		float saturation = minecraft.player == null ? 5.0F : minecraft.player.getFoodData().getSaturationLevel();
		return value(String.format(Locale.ROOT, "%.1f", saturation), Palette.color(this.state().value(COLOR, DEFAULT_COLOR)), "saturation");
	}
}
