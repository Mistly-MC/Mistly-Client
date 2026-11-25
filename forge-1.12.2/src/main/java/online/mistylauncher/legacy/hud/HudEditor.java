package online.mistylauncher.legacy.hud;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.ui.MistyButton;
import online.mistylauncher.legacy.ui.MistyScreen;
import online.mistylauncher.legacy.ui.SettingsScreen;
import online.mistylauncher.legacy.ui.Ui;

/**
 * HUD editor (Right Shift in game), as in the Fabric versions: every module is shown where it
 * sits. Drag to move, click to turn on or off, drag the corner or scroll to resize, right-click
 * to put back.
 */
public final class HudEditor extends MistyScreen {
	private static final int PANEL_WIDTH = 188;
	private static final int PANEL_HEIGHT = 70;
	/** A module sticks to the screen edges (with this margin) and to the centre lines. */
	private static final int MARGIN = 4;
	private static final int SNAP = 5;
	/** A press that moved less than this is a click, not a drag. */
	private static final int CLICK_SLOP = 2;
	/** The resize handle at the bottom-right corner of a module, and how far around it a press catches it. */
	private static final int HANDLE = 4;
	private static final int HANDLE_REACH = 3;

	private HudModule dragged;
	private int grabX;
	private int grabY;
	private int pressX;
	private int pressY;
	private boolean moved;
	private boolean centeredX;
	private boolean centeredY;
	/** Resizing by the handle: the module, its top-left corner, and its size at scale 1. */
	private HudModule resized;
	private int resizeX;
	private int resizeY;
	private float baseW;
	private float baseH;

	public HudEditor() {
		this(null);
	}

	public HudEditor(final GuiScreen parent) {
		super(parent);
	}

	@Override
	protected float veil() {
		return 0.35F;
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		int x = (this.width - PANEL_WIDTH) / 2;
		int y = (this.height - PANEL_HEIGHT) / 2 + PANEL_HEIGHT - 26;
		int buttonWidth = (PANEL_WIDTH - 16 - 8) / 3;
		final HudEditor self = this;
		this.add(new MistyButton(x + 8, y, buttonWidth, 18, I18n.format("misty.editor.settings"), new Runnable() {
			@Override
			public void run() {
				Config.save();
				self.mc.displayGuiScreen(new SettingsScreen(self));
			}
		}));
		this.add(new MistyButton(x + 12 + buttonWidth, y, buttonWidth, 18, I18n.format("misty.editor.reset"), new Runnable() {
			@Override
			public void run() {
				Hud.resetAll();
				Config.save();
			}
		}));
		this.add(new MistyButton(x + 16 + 2 * buttonWidth, y, buttonWidth, 18, I18n.format("gui.done"), new Runnable() {
			@Override
			public void run() {
				self.mc.displayGuiScreen(self.parent);
			}
		}).primary());
	}

	private Hud.Bounds bounds(final HudModule module) {
		return Hud.bounds(module, this.mc, this.width, this.height, true);
	}

	/** Topmost module under the pointer (the last drawn is on top). */
	private HudModule moduleAt(final int x, final int y) {
		List<HudModule> modules = Hud.modules();
		for (int i = modules.size() - 1; i >= 0; i--) {
			if (this.bounds(modules.get(i)).contains(x, y)) {
				return modules.get(i);
			}
		}
		return null;
	}

	/** Whether (x, y) is on the resize handle of {@code bounds} (its bottom-right corner). */
	private static boolean onHandle(final Hud.Bounds bounds, final int x, final int y) {
		int cx = bounds.x + bounds.width;
		int cy = bounds.y + bounds.height;
		return x >= cx - HANDLE - HANDLE_REACH + 1 && x <= cx + HANDLE_REACH && y >= cy - HANDLE - HANDLE_REACH + 1 && y <= cy + HANDLE_REACH;
	}

	/** Topmost module whose resize handle is under the pointer. */
	private HudModule handleAt(final int x, final int y) {
		List<HudModule> modules = Hud.modules();
		for (int i = modules.size() - 1; i >= 0; i--) {
			if (onHandle(this.bounds(modules.get(i)), x, y)) {
				return modules.get(i);
			}
		}
		return null;
	}

	@Override
	protected void drawUnder(final int mouseX, final int mouseY) {
		if (this.dragged != null && this.moved) {
			if (this.centeredX) {
				Ui.fill(this.width / 2, 0, this.width / 2 + 1, this.height, Ui.alpha(Ui.ACCENT, 0.5F));
			}
			if (this.centeredY) {
				Ui.fill(0, this.height / 2, this.width, this.height / 2 + 1, Ui.alpha(Ui.ACCENT, 0.5F));
			}
		}
		// the panel first: a module dragged over it stays visible
		int x = (this.width - PANEL_WIDTH) / 2;
		int y = (this.height - PANEL_HEIGHT) / 2;
		Ui.card(x, y, PANEL_WIDTH, PANEL_HEIGHT, Ui.SURFACE, Ui.BORDER);
		Ui.logo(x + 10, y + 7, 22);
		Ui.text("MISTLY", x + 38, y + 9, Ui.TEXT);
		Ui.text(I18n.format("misty.editor.title"), x + 38, y + 20, Ui.ACCENT);
	}

	@Override
	protected void drawOver(final int mouseX, final int mouseY) {
		int y = (this.height - PANEL_HEIGHT) / 2;
		Ui.centered(I18n.format("misty.editor.hint1"), this.width / 2, y + PANEL_HEIGHT + 8, Ui.TEXT);
		Ui.centered(I18n.format("misty.editor.hint2"), this.width / 2, y + PANEL_HEIGHT + 20, Ui.MUTED);

		HudModule onHandle = this.resized != null ? this.resized : this.dragged == null ? this.handleAt(mouseX, mouseY) : null;
		HudModule hovered = this.resized != null ? this.resized : this.dragged != null ? this.dragged : onHandle != null ? onHandle : this.moduleAt(mouseX, mouseY);
		for (HudModule module : Hud.modules()) {
			Hud.Bounds b = this.bounds(module);
			boolean enabled = module.state().enabled;
			Hud.draw(module, this.mc, b, true);
			if (!enabled) {
				Ui.fill(b.x, b.y, b.x + b.width, b.y + b.height, Ui.alpha(Ui.INK, 0.72F));
			}
			int outline = module == hovered ? 0xFFFFFFFF : enabled ? Ui.ACCENT : Ui.DIM;
			Ui.outline(b.x - 1, b.y - 1, b.width + 2, b.height + 2, outline);
			// the resize handle, on the module under the pointer: a square over its bottom-right corner
			if (module == hovered && this.dragged == null) {
				int cx = b.x + b.width + 1;
				int cy = b.y + b.height + 1;
				Ui.fill(cx - HANDLE - 1, cy - HANDLE - 1, cx + 1, cy + 1, Ui.INK);
				Ui.fill(cx - HANDLE, cy - HANDLE, cx, cy, module == onHandle ? Ui.ACCENT : 0xFFFFFFFF);
			}
		}
		if (hovered != null) {
			this.label(hovered);
		}
	}

	/** Name and state of the module under the pointer, above it (below when it touches the top). */
	private void label(final HudModule module) {
		Hud.Bounds b = this.bounds(module);
		String text = module.name() + " · " + I18n.format(module.state().enabled ? "misty.editor.on" : "misty.editor.off");
		if (module == this.resized) {
			// while it is resized: its size, in % of its normal size
			text = module.name() + " · " + Math.round(module.state().scale * 100.0F) + " %";
		}
		int width = Ui.width(text) + 8;
		int x = Ui.clamp(b.x + (b.width - width) / 2, 2, Math.max(2, this.width - width - 2));
		int y = b.y - 15 >= 2 ? b.y - 15 : b.y + b.height + 3;
		Ui.panel(x, y, width, 12, Ui.SURFACE);
		Ui.text(text, x + 4, y + 2, Ui.TEXT);
	}

	private boolean onButton(final int x, final int y) {
		for (GuiButton button : this.buttonList) {
			if (button instanceof MistyButton && ((MistyButton)button).hovered(x, y)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void mouseClicked(final int x, final int y, final int button) throws IOException {
		if (this.onButton(x, y)) {
			super.mouseClicked(x, y, button);
			return;
		}
		// the resize handle first: it sits over the corner, partly outside the module
		HudModule handled = this.handleAt(x, y);
		if (handled != null && button == 0) {
			Hud.Bounds b = this.bounds(handled);
			float scale = Math.max(Hud.MIN_SCALE, Math.min(Hud.MAX_SCALE, handled.state().scale));
			this.resized = handled;
			this.resizeX = b.x;
			this.resizeY = b.y;
			this.baseW = Math.max(1.0F, b.width / scale);
			this.baseH = Math.max(1.0F, b.height / scale);
			return;
		}
		HudModule module = this.moduleAt(x, y);
		if (module == null) {
			return;
		}
		if (button == 1) {
			module.reset();
			return;
		}
		if (button == 0) {
			Hud.Bounds b = this.bounds(module);
			this.dragged = module;
			this.grabX = x - b.x;
			this.grabY = y - b.y;
			this.pressX = x;
			this.pressY = y;
			this.moved = false;
		}
	}

	@Override
	protected void mouseClickMove(final int x, final int y, final int button, final long held) {
		if (this.resized != null) {
			this.resizeTo(x, y);
		} else if (this.dragged != null) {
			this.dragTo(x, y);
		}
	}

	/** Resizes the grabbed module live, its top-left corner fixed, its bottom-right corner following the pointer. */
	private void resizeTo(final int pointerX, final int pointerY) {
		float byWidth = (pointerX - this.resizeX) / this.baseW;
		float byHeight = (pointerY - this.resizeY) / this.baseH;
		// by steps of 5 %, 100 % sticks a little
		float scale = Math.round(Math.max(byWidth, byHeight) * 20.0F) / 20.0F;
		if (Math.abs(scale - 1.0F) <= 0.05F) {
			scale = 1.0F;
		}
		Config.Module state = this.resized.state();
		state.scale = Math.max(Hud.MIN_SCALE, Math.min(Hud.MAX_SCALE, scale));
		Hud.place(this.resized, this.bounds(this.resized), this.resizeX, this.resizeY, this.width, this.height);
	}

	/** Moves the grabbed module so the point it was grabbed by is under the pointer. */
	private void dragTo(final int pointerX, final int pointerY) {
		if (!this.moved && Math.abs(pointerX - this.pressX) < CLICK_SLOP && Math.abs(pointerY - this.pressY) < CLICK_SLOP) {
			return;
		}
		this.moved = true;
		Hud.Bounds b = this.bounds(this.dragged);
		int centerX = (this.width - b.width) / 2;
		int centerY = (this.height - b.height) / 2;
		int x = snap(pointerX - this.grabX, MARGIN, centerX, this.width - b.width - MARGIN);
		int y = snap(pointerY - this.grabY, MARGIN, centerY, this.height - b.height - MARGIN);
		this.centeredX = x == centerX;
		this.centeredY = y == centerY;
		x = Ui.clamp(x, 0, Math.max(0, this.width - b.width));
		y = Ui.clamp(y, 0, Math.max(0, this.height - b.height));
		Hud.place(this.dragged, b, x, y, this.width, this.height);
	}

	private static int snap(final int value, final int... targets) {
		for (int target : targets) {
			if (Math.abs(value - target) <= SNAP) {
				return target;
			}
		}
		return value;
	}

	/** The grabbed module let go: a click without moving turns it on or off. */
	@Override
	protected void mouseReleased(final int x, final int y, final int state) {
		super.mouseReleased(x, y, state);
		if (this.resized != null) {
			this.resized = null;
			Config.save();
			return;
		}
		if (this.dragged != null) {
			if (!this.moved) {
				this.dragged.state().enabled = !this.dragged.state().enabled;
			}
			this.dragged = null;
			Config.save();
		}
	}

	/** The wheel over a module resizes it around its top-left corner, by steps of 10 %. */
	@Override
	protected boolean scrolled(final int x, final int y, final int direction) {
		HudModule module = this.moduleAt(x, y);
		if (module == null) {
			return false;
		}
		Hud.Bounds before = this.bounds(module);
		Config.Module state = module.state();
		float scale = Math.round((state.scale + (direction > 0 ? 0.1F : -0.1F)) * 10.0F) / 10.0F;
		state.scale = Math.max(Hud.MIN_SCALE, Math.min(Hud.MAX_SCALE, scale));
		Hud.place(module, this.bounds(module), before.x, before.y, this.width, this.height);
		return true;
	}

	@Override
	public void onGuiClosed() {
		Config.save();
	}
}
