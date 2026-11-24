package online.mistylauncher.legacy.hud;

import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.ui.Ui;

/** A module that is one short line: a value in white (or its colour) and its unit in the accent colour. */
public abstract class TextModule extends HudModule {
	protected static final int HEIGHT = 14;
	private static final int PADDING = 5;

	private final int minWidth;

	protected TextModule(final String id, final Config.Module defaults, final int minWidth) {
		super(id, defaults);
		this.minWidth = minWidth;
	}

	/** The value shown; in the editor, a sample when there is nothing to show. */
	protected abstract String value(Minecraft minecraft, boolean preview);

	/** Its unit, after the value ("" for none). */
	protected String unit() {
		return "";
	}

	/** The value's colour (ARGB). */
	protected int valueColor(final Minecraft minecraft, final boolean preview) {
		return Ui.TEXT;
	}

	private String line(final Minecraft minecraft, final boolean preview) {
		String unit = this.unit();
		return unit.isEmpty() ? this.value(minecraft, preview) : this.value(minecraft, preview) + " " + unit;
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		return Math.max(this.minWidth, Ui.width(this.line(minecraft, preview)) + 2 * PADDING);
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return HEIGHT;
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		int width = this.width(minecraft, preview);
		background(width, HEIGHT);
		drawLine(this.value(minecraft, preview), this.valueColor(minecraft, preview), this.unit(), (width - Ui.width(this.line(minecraft, preview))) / 2, 3);
	}

	/** "value unit": the value in its colour, the unit in the accent colour. */
	protected static void drawLine(final String value, final int color, final String unit, final int x, final int y) {
		Ui.text(value, x, y, color);
		if (!unit.isEmpty()) {
			Ui.text(" " + unit, x + Ui.width(value), y, Ui.ACCENT);
		}
	}
}
