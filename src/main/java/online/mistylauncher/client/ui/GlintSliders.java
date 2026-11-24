package online.mistylauncher.client.ui;

import java.util.List;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.config.MistyConfig;

/**
 * The enchantment colour's gear: a colour of the palette or "Custom", then the custom colour's
 * red, green and blue (moving one of them picks "Custom").
 */
public final class GlintSliders {
	private static final int CUSTOM = Palette.COLORS.length;

	private GlintSliders() {
	}

	public static List<EffectSettingsScreen.Slider> all() {
		return List.of(Choice::new, (x, y, w, h) -> new Channel(x, y, w, h, 16, "misty.option.red"),
			(x, y, w, h) -> new Channel(x, y, w, h, 8, "misty.option.green"), (x, y, w, h) -> new Channel(x, y, w, h, 0, "misty.option.blue"));
	}

	/** The palette's colours, then "Custom": "Colour: Purple". */
	private static final class Choice extends AbstractSliderButton {
		Choice(final int x, final int y, final int width, final int height) {
			super(x, y, width, height, Component.empty(), Math.clamp(MistyClient.config().glintColor, 0, CUSTOM) / (double) CUSTOM);
			this.updateMessage();
		}

		private int index() {
			return (int) Math.round(this.value * CUSTOM);
		}

		@Override
		protected void updateMessage() {
			int index = this.index();
			Component name = index == CUSTOM
				? Component.translatable("misty.color.custom").withColor(MistyClient.config().glintCustom & 0xFFFFFF)
				// black would vanish on the slider: its name stays readable in grey
				: Component.translatable("misty.color." + Palette.NAMES[index]).withColor(index == CUSTOM - 1 ? 0x808080 : Palette.COLORS[index] & 0xFFFFFF);
			this.setMessage(Component.translatable("misty.option.color", name));
		}

		@Override
		protected void applyValue() {
			MistyClient.config().glintColor = this.index();
		}
	}

	/** One channel of the custom colour, 0 to 255: "Red: 143". */
	private static final class Channel extends AbstractSliderButton {
		private final int shift;
		private final String label;

		Channel(final int x, final int y, final int width, final int height, final int shift, final String label) {
			super(x, y, width, height, Component.empty(), (MistyClient.config().glintCustom >> shift & 0xFF) / 255.0);
			this.shift = shift;
			this.label = label;
			this.updateMessage();
		}

		private int channel() {
			return (int) Math.round(this.value * 255.0);
		}

		@Override
		protected void updateMessage() {
			int colour = 0xFF << this.shift & 0xFFFFFF;
			this.setMessage(Component.translatable(this.label, Component.literal(Integer.toString(this.channel())).withColor(colour == 0x0000FF ? 0x6F9CFF : colour)));
		}

		@Override
		protected void applyValue() {
			MistyConfig config = MistyClient.config();
			config.glintCustom = config.glintCustom & ~(0xFF << this.shift) | this.channel() << this.shift;
			config.glintColor = CUSTOM;
		}
	}
}
