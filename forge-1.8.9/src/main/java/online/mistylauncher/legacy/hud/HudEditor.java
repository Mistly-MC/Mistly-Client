package online.mistylauncher.legacy.hud;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.ui.SettingsScreen;
import org.lwjgl.input.Mouse;

/**
 * The HUD editor (Right Shift): drag a module to move it, click it to turn it on or off, scroll
 * over it to resize it, right-click it to put it back where it was. "Settings" opens the rest.
 */
public final class HudEditor extends GuiScreen {
	private static final int ON = 0xFF7FB2FF;
	private static final int OFF = 0x80FFFFFF;

	private HudModule dragged;
	private int offsetX;
	private int offsetY;
	private boolean moved;

	@Override
	public void initGui() {
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, this.width / 2 - 60, this.height / 2 - 10, 120, 20, I18n.format("misty.editor.settings")));
	}

	@Override
	protected void actionPerformed(final GuiButton button) {
		if (button.id == 0) {
			this.mc.displayGuiScreen(new SettingsScreen(this));
		}
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
		drawRect(0, 0, this.width, this.height, 0x60000000);
		for (HudModule module : Hud.modules()) {
			Hud.Bounds b = Hud.bounds(module, this.mc, this.width, this.height, true);
			boolean on = module.state().enabled;
			Hud.draw(module, this.mc, b, true);
			if (!on) {
				drawRect(b.x, b.y, b.x + b.width, b.y + b.height, 0xA0000000);
			}
			int colour = on ? ON : OFF;
			drawRect(b.x - 1, b.y - 1, b.x + b.width + 1, b.y, colour);
			drawRect(b.x - 1, b.y + b.height, b.x + b.width + 1, b.y + b.height + 1, colour);
			drawRect(b.x - 1, b.y, b.x, b.y + b.height, colour);
			drawRect(b.x + b.width, b.y, b.x + b.width + 1, b.y + b.height, colour);
			if (b.contains(mouseX, mouseY)) {
				this.drawCenteredString(this.fontRendererObj, module.name(), b.x + b.width / 2, Math.max(2, b.y - 11), 0xFFFFFF);
			}
		}
		this.drawCenteredString(this.fontRendererObj, I18n.format("misty.editor.hint"), this.width / 2, this.height / 2 + 16, 0xC0C8D8);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	/** The module under the pointer, the one drawn last first. */
	private HudModule under(final int x, final int y) {
		List<HudModule> modules = Hud.modules();
		for (int i = modules.size() - 1; i >= 0; i--) {
			if (Hud.bounds(modules.get(i), this.mc, this.width, this.height, true).contains(x, y)) {
				return modules.get(i);
			}
		}
		return null;
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int button) throws IOException {
		super.mouseClicked(mouseX, mouseY, button);
		HudModule module = this.under(mouseX, mouseY);
		if (module == null) {
			return;
		}
		if (button == 0) {
			Hud.Bounds b = Hud.bounds(module, this.mc, this.width, this.height, true);
			this.dragged = module;
			this.offsetX = mouseX - b.x;
			this.offsetY = mouseY - b.y;
			this.moved = false;
		} else if (button == 1) {
			boolean on = module.state().enabled;
			module.reset();
			module.state().enabled = on;
		}
	}

	@Override
	protected void mouseClickMove(final int mouseX, final int mouseY, final int button, final long held) {
		if (this.dragged == null || button != 0) {
			return;
		}
		this.moved = true;
		Hud.Bounds b = Hud.bounds(this.dragged, this.mc, this.width, this.height, true);
		Hud.place(this.dragged, b, mouseX - this.offsetX, mouseY - this.offsetY, this.width, this.height);
	}

	@Override
	protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
		super.mouseReleased(mouseX, mouseY, state);
		if (this.dragged != null && !this.moved) {
			this.dragged.state().enabled = !this.dragged.state().enabled;
		}
		this.dragged = null;
	}

	/** The wheel over a module resizes it. */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int wheel = Mouse.getEventDWheel();
		if (wheel == 0) {
			return;
		}
		int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		HudModule module = this.under(x, y);
		if (module != null) {
			Config.Module state = module.state();
			float step = wheel > 0 ? 0.1F : -0.1F;
			state.scale = Math.max(Hud.MIN_SCALE, Math.min(Hud.MAX_SCALE, Math.round((state.scale + step) * 10.0F) / 10.0F));
		}
	}

	@Override
	public void onGuiClosed() {
		Config.save();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
