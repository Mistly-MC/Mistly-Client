package online.mistylauncher.legacy.ui;

import net.minecraft.client.Minecraft;

/** A slider of the Mistly screens: a track, a knob, and its value in the label. */
public class MistySlider extends MistyButton {
	/** The slider's value (0..1) and its label. */
	public interface Model {
		double get();

		void set(double value);

		String label();
	}

	private final Model model;
	private boolean dragging;

	public MistySlider(final int x, final int y, final int width, final int height, final Model model) {
		super(x, y, width, height, "", null);
		this.model = model;
	}

	@Override
	public void press() {
	}

	private void moveTo(final int mouseX) {
		double value = (mouseX - (this.x + 4)) / (double)(this.width - 8);
		this.model.set(Math.max(0.0, Math.min(1.0, value)));
	}

	@Override
	public boolean mousePressed(final Minecraft minecraft, final int mouseX, final int mouseY) {
		if (super.mousePressed(minecraft, mouseX, mouseY)) {
			this.dragging = true;
			this.moveTo(mouseX);
			return true;
		}
		return false;
	}

	@Override
	protected void mouseDragged(final Minecraft minecraft, final int mouseX, final int mouseY) {
		if (this.visible && this.dragging) {
			this.moveTo(mouseX);
		}
	}

	@Override
	public void mouseReleased(final int mouseX, final int mouseY) {
		this.dragging = false;
	}

	@Override
	protected void drawContents(final int mouseX, final int mouseY) {
		// follows the mouse only while its button is held
		if (this.dragging && !org.lwjgl.input.Mouse.isButtonDown(0)) {
			this.dragging = false;
		}
		this.mouseDragged(Minecraft.getMinecraft(), mouseX, mouseY);
		int x = this.x;
		int y = this.y;
		boolean hot = this.hovered || this.dragging;
		Ui.card(x, y, this.width, this.height, hot ? Ui.GLASS_HOVER : Ui.GLASS, hot ? Ui.alpha(Ui.ACCENT, 0.6F) : Ui.BORDER);
		double value = this.model.get();
		int knob = x + 4 + (int)Math.round(value * (this.width - 8));
		Ui.fill(x + 4, y + this.height - 4, knob, y + this.height - 3, Ui.FILL);
		Ui.fill(knob, y + this.height - 4, x + this.width - 4, y + this.height - 3, Ui.TRACK);
		Ui.panel(knob - 2, y + this.height - 6, 5, 5, 0xFFFFFFFF);
		Ui.centered(this.model.label(), x + this.width / 2, y + (this.height - 10) / 2, Ui.TEXT);
	}
}
