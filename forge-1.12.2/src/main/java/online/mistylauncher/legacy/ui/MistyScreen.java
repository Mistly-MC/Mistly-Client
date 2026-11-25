package online.mistylauncher.legacy.ui;

import java.io.IOException;
import java.util.Arrays;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import online.mistylauncher.legacy.menu.MistyBackground;
import org.lwjgl.input.Mouse;

/**
 * Base of the Mistly screens: the game (or, in the menus, the night landscape) stays visible
 * behind a night veil. Its buttons are {@link MistyButton}s: a click runs their action.
 */
public abstract class MistyScreen extends GuiScreen {
	protected final GuiScreen parent;

	protected MistyScreen(final GuiScreen parent) {
		this.parent = parent;
	}

	public final GuiScreen parent() {
		return this.parent;
	}

	/** Opacity of the veil over what is behind (0..1). */
	protected abstract float veil();

	protected <T extends GuiButton> T add(final T button) {
		this.buttonList.add(button);
		return button;
	}

	/** Builds the screen again (a category chosen, a setting that changes the layout). */
	protected void rebuild() {
		this.buttonList.clear();
		this.initGui();
	}

	protected void drawBackground() {
		if (this.mc.world == null) {
			MistyBackground.draw(this.width, this.height);
		}
		Ui.fill(0, 0, this.width, this.height, Ui.alpha(Ui.INK, this.veil()));
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
		this.drawBackground();
		this.drawUnder(mouseX, mouseY);
		for (GuiButton button : this.buttonList) {
			button.drawButton(this.mc, mouseX, mouseY, partialTicks);
		}
		this.drawOver(mouseX, mouseY);
		for (GuiButton button : this.buttonList) {
			if (button instanceof MistyButton && ((MistyButton)button).tooltip != null && ((MistyButton)button).hovered(mouseX, mouseY)) {
				this.drawHoveringText(Arrays.asList(this.fontRenderer.listFormattedStringToWidth(((MistyButton)button).tooltip, 200).toArray(new String[0])), mouseX, mouseY);
				net.minecraft.client.renderer.GlStateManager.disableLighting();
				break;
			}
		}
	}

	/** Under the buttons: frames and panels. */
	protected void drawUnder(final int mouseX, final int mouseY) {
	}

	/** Over the buttons: texts and previews. */
	protected void drawOver(final int mouseX, final int mouseY) {
	}

	@Override
	protected void actionPerformed(final GuiButton button) {
		if (button instanceof MistyButton) {
			((MistyButton)button).press();
		}
	}

	/** The mouse wheel: {@code direction} 1 up, -1 down. False: not used. */
	protected boolean scrolled(final int mouseX, final int mouseY, final int direction) {
		return false;
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int wheel = Mouse.getEventDWheel();
		if (wheel != 0) {
			int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
			int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
			this.scrolled(x, y, wheel > 0 ? 1 : -1);
		}
	}

	@Override
	protected void keyTyped(final char typed, final int key) throws IOException {
		if (key == 1) {
			this.mc.displayGuiScreen(this.parent);
			return;
		}
		super.keyTyped(typed, key);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
