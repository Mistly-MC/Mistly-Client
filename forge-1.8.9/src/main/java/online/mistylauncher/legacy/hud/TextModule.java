package online.mistylauncher.legacy.hud;

import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;

/** A module of one line of text in a box ("144 FPS"). */
public abstract class TextModule extends HudModule {
	private static final int PADDING = 3;
	private static final int HEIGHT = 13;

	protected TextModule(final String id, final Config.Module defaults) {
		super(id, defaults);
	}

	/** The line shown; in the editor, a sample when there is nothing to show. */
	protected abstract String text(Minecraft minecraft, boolean preview);

	/** Its colour (RGB, opaque). */
	protected int colour(final Minecraft minecraft) {
		return TEXT;
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		return Math.max(40, minecraft.fontRendererObj.getStringWidth(this.text(minecraft, preview)) + 2 * PADDING);
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return HEIGHT;
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		int width = this.width(minecraft, preview);
		background(width, HEIGHT);
		String text = this.text(minecraft, preview);
		int x = (width - minecraft.fontRendererObj.getStringWidth(text)) / 2;
		minecraft.fontRendererObj.drawStringWithShadow(text, x, 3, 0xFF000000 | this.colour(minecraft));
	}

	/** Green up to 2 blocks, yellow around 2.6, red from 3 (reach). */
	protected static int reachColour(final double blocks) {
		if (blocks <= 2.0) {
			return 0x55FF55;
		}
		if (blocks <= 2.6) {
			return mix(0x55FF55, 0xFFFF55, (blocks - 2.0) / 0.6);
		}
		return mix(0xFFFF55, 0xFF5555, (blocks - 2.6) / 0.4);
	}

	/** Green under 80 ms, yellow under 150, orange under 250, red above (ping). */
	protected static int pingColour(final int ms) {
		return ms < 80 ? 0x55FF55 : ms < 150 ? 0xFFFF55 : ms < 250 ? 0xFFAA00 : 0xFF5555;
	}

	private static int mix(final int a, final int b, final double t) {
		double u = Math.max(0, Math.min(1, t));
		int colour = 0;
		for (int shift = 0; shift <= 16; shift += 8) {
			int x = (a >> shift) & 0xFF;
			int y = (b >> shift) & 0xFF;
			colour |= ((int) Math.round(x + (y - x) * u)) << shift;
		}
		return colour;
	}
}
