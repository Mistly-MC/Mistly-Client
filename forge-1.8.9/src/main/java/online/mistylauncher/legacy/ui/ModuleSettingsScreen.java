package online.mistylauncher.legacy.ui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.HudModule;

/** The settings of one HUD module, behind its gear: a switch each. */
public final class ModuleSettingsScreen extends MistyScreen {
	private static final int WIDTH = 240;
	private static final int ROW = 20;
	private static final int GAP = 4;
	private static final int PAD = 10;
	private static final int HEADER = 22;

	private final HudModule module;

	public ModuleSettingsScreen(final HudModule module, final GuiScreen parent) {
		super(parent);
		this.module = module;
	}

	@Override
	protected float veil() {
		return 0.55F;
	}

	private int frameW() {
		return Math.min(this.width - 20, WIDTH);
	}

	private int frameH() {
		return HEADER + PAD + this.module.settings().size() * (ROW + GAP) + PAD + 18 + PAD;
	}

	private int frameX() {
		return (this.width - this.frameW()) / 2;
	}

	private int frameY() {
		return (this.height - this.frameH()) / 2;
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		int x = this.frameX() + PAD;
		int inner = this.frameW() - 2 * PAD;
		int y = this.frameY() + HEADER + PAD;
		for (final HudModule.Setting setting : this.module.settings()) {
			MistyToggle toggle = new MistyToggle(x, y, inner, ROW, setting.name(this.module), new MistyToggle.Value() {
				@Override
				public boolean get() {
					return ModuleSettingsScreen.this.module.setting(setting.key);
				}

				@Override
				public void set(final boolean value) {
					ModuleSettingsScreen.this.module.state().setOption(setting.key, value);
				}
			});
			toggle.tooltip(setting.tip(this.module));
			this.add(toggle);
			y += ROW + GAP;
		}
		this.add(new MistyButton(x, y + PAD - GAP, inner, 18, I18n.format("gui.done"), new Runnable() {
			@Override
			public void run() {
				ModuleSettingsScreen.this.mc.displayGuiScreen(ModuleSettingsScreen.this.parent);
			}
		}).primary());
	}

	@Override
	protected void drawUnder(final int mouseX, final int mouseY) {
		int fx = this.frameX();
		int fy = this.frameY();
		int fw = this.frameW();
		Ui.card(fx, fy, fw, this.frameH(), 0xF00B1120, 0x33FFFFFF);
		Ui.fill(fx + 1, fy + 1, fx + fw - 1, fy + HEADER, 0xFF111A30);
		Ui.fill(fx + 1, fy + HEADER, fx + fw - 1, fy + HEADER + 1, Ui.LINE);
		Ui.logo(fx + 8, fy + 4, 14);
		Ui.text(I18n.format("misty.module.settings", this.module.name()), fx + 26, fy + (HEADER - 8) / 2, Ui.TEXT);
	}

	@Override
	public void onGuiClosed() {
		Config.save();
	}
}
