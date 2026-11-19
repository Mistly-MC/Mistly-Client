package online.mistylauncher.client.ui;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** The colours a setting can pick (hitboxes, saturation, chunk viewer), with a slider to pick one. */
public final class Palette {
	/** ARGB colours, with their name key ({@code misty.color.<name>}). */
	public static final int[] COLORS = { 0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF, 0xFF6F9CFF, 0xFFAA55FF, 0xFFFF77DD, 0xFF000000 };
	public static final String[] NAMES = { "white", "red", "orange", "yellow", "green", "cyan", "blue", "purple", "pink", "black" };

	private Palette() {
	}

	/** {@code index} kept inside the palette. */
	public static int index(final int index) {
		return Mth.clamp(index, 0, COLORS.length - 1);
	}

	public static int color(final int index) {
		return COLORS[index(index)];
	}

	/** A slider over the palette, its colour's name drawn in that colour: "{label}: Red". */
	public static final class Slider extends AbstractSliderButton {
		private final String label;
		private final IntConsumer change;

		/** {@code label}: a translation key with one %s for the colour's name. */
		public Slider(final int x, final int y, final int width, final int height, final String label, final IntSupplier value, final IntConsumer change) {
			super(x, y, width, height, Component.empty(), Palette.index(value.getAsInt()) / (double)(COLORS.length - 1));
			this.label = label;
			this.change = change;
			this.updateMessage();
		}

		private int index() {
			return (int)Math.round(this.value * (COLORS.length - 1));
		}

		@Override
		protected void updateMessage() {
			int index = this.index();
			// black would vanish on the slider: its name stays readable in grey
			int color = index == COLORS.length - 1 ? 0x808080 : COLORS[index] & 0xFFFFFF;
			this.setMessage(Component.translatable(this.label, Component.translatable("misty.color." + NAMES[index]).withColor(color)));
		}

		@Override
		protected void applyValue() {
			this.change.accept(this.index());
		}
	}
}
