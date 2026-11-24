package online.mistylauncher.legacy.ui;

import net.minecraft.client.resources.I18n;

/** The colours a setting can pick (hitboxes, chunk viewer, saturation, glint), with a slider to pick one. */
public final class Palette {
	/** ARGB colours, with their name key ({@code misty.color.<name>}). */
	public static final int[] COLORS = {0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF, 0xFF6F9CFF, 0xFFAA55FF, 0xFFFF77DD, 0xFF000000};
	public static final String[] NAMES = {"white", "red", "orange", "yellow", "green", "cyan", "blue", "purple", "pink", "black"};
	/** The colour of each name in a label (the font of 1.8.9 has these codes only; black stays readable in grey). */
	private static final String[] CODES = {"§f", "§c", "§6", "§e", "§a", "§b", "§9", "§5", "§d", "§7"};

	/** A colour setting: read and changed. */
	public interface Value {
		int get();

		void set(int value);
	}

	private Palette() {
	}

	public static int index(final int index) {
		return Math.max(0, Math.min(COLORS.length - 1, index));
	}

	public static int color(final int index) {
		return COLORS[index(index)];
	}

	/** "Colour: Red", the colour's name in its colour. */
	public static String label(final String key, final int index) {
		int i = index(index);
		return I18n.format(key, CODES[i] + I18n.format("misty.color." + NAMES[i]) + "§r");
	}

	/** A slider over the palette; {@code key}: a translation with one %s for the colour's name. */
	public static MistySlider slider(final int x, final int y, final int width, final int height, final String key, final Value value) {
		return new MistySlider(x, y, width, height, new MistySlider.Model() {
			@Override
			public double get() {
				return index(value.get()) / (double)(COLORS.length - 1);
			}

			@Override
			public void set(final double position) {
				value.set((int)Math.round(position * (COLORS.length - 1)));
			}

			@Override
			public String label() {
				return Palette.label(key, value.get());
			}
		});
	}
}
