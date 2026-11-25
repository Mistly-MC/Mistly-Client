package online.mistylauncher.legacy.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

/** Button of the Mistly screens: frosted glass, accent bar on hover (as in the Fabric versions). */
public class MistyButton extends GuiButton {
	private final Runnable onPress;
	private boolean primary;
	private boolean alignLeft;
	private ResourceLocation texture;
	private boolean cloud;
	private Selected selected;
	/** Shown under the mouse by the screen (null: none). */
	public String tooltip;

	/** Whether a button is drawn as chosen (tabs). */
	public interface Selected {
		boolean get();
	}

	public MistyButton(final int x, final int y, final int width, final int height, final String message, final Runnable onPress) {
		super(0, x, y, width, height, message);
		this.onPress = onPress;
	}

	/** Filled with the accent colour: the main action of a screen. */
	public MistyButton primary() {
		this.primary = true;
		return this;
	}

	/** Label on the left, after an accent bar. */
	public MistyButton alignLeft() {
		this.alignLeft = true;
		return this;
	}

	/** A 16 × 16 Mistly icon instead of the label (the label stays as its tooltip). */
	public MistyButton texture(final ResourceLocation icon) {
		this.texture = icon;
		this.tooltip = this.displayString;
		return this;
	}

	/** The Mistly cloud instead of the label. */
	public MistyButton cloud() {
		this.cloud = true;
		this.tooltip = this.displayString;
		return this;
	}

	public MistyButton selectedWhen(final Selected chosen) {
		this.selected = chosen;
		return this;
	}

	public MistyButton tooltip(final String text) {
		this.tooltip = text;
		return this;
	}

	public void press() {
		this.onPress.run();
	}

	public boolean hovered(final int mouseX, final int mouseY) {
		return this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
	}

	@Override
	public void drawButton(final Minecraft minecraft, final int mouseX, final int mouseY, final float partialTicks) {
		if (!this.visible) {
			return;
		}
		this.hovered = this.hovered(mouseX, mouseY);
		this.drawContents(mouseX, mouseY);
	}

	/** What the button looks like; a subclass may draw nothing (an area that only reacts). */
	protected void drawContents(final int mouseX, final int mouseY) {
		int x = this.x;
		int y = this.y;
		boolean hot = this.enabled && this.hovered;
		boolean chosen = this.selected != null && this.selected.get();
		int background;
		int textColor;
		if (this.primary || chosen) {
			background = hot ? Ui.FILL_HOVER : Ui.FILL;
			textColor = Ui.INK;
		} else {
			background = hot ? Ui.GLASS_HOVER : Ui.GLASS;
			textColor = this.enabled ? (hot ? 0xFFFFFFFF : Ui.TEXT) : Ui.DIM;
		}
		Ui.card(x, y, this.width, this.height, background, hot ? Ui.alpha(Ui.ACCENT, 0.6F) : Ui.BORDER);
		if (this.cloud) {
			int size = Math.min(this.width, this.height) - 6;
			Ui.logo(x + (this.width - size) / 2, y + (this.height - size) / 2, size);
			return;
		}
		if (this.texture != null) {
			int size = Math.min(this.width, this.height) - 4;
			Ui.blit(this.texture, x + (this.width - size) / 2, y + (this.height - size) / 2, size, 16, 0xFFFFFFFF);
			return;
		}
		int textY = y + (this.height - 8) / 2;
		if (this.alignLeft) {
			if (hot) {
				Ui.fill(x + 1, y + 3, x + 3, y + this.height - 3, Ui.ACCENT);
			}
			Ui.text(this.displayString, x + (hot ? 12 : 9), textY, textColor);
		} else {
			Ui.centered(this.displayString, x + this.width / 2, textY, textColor);
		}
	}
}
