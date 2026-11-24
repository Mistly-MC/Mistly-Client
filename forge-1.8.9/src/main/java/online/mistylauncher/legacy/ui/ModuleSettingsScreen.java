package online.mistylauncher.legacy.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.HudModule;

/**
 * What is behind a gear, as in the Fabric versions: the settings of a HUD module, or of an effect
 * (hitboxes, motion blur…), a switch or a slider each.
 */
public final class ModuleSettingsScreen extends MistyScreen {
	private static final int WIDTH = 240;
	private static final int ROW = 20;
	private static final int GAP = 4;
	private static final int PAD = 10;
	private static final int HEADER = 22;

	/** A switch of the screen: its name, its tooltip, its value. */
	public static final class Toggle {
		final String name;
		final String tip;
		final MistyToggle.Value value;

		public Toggle(final String name, final String tip, final MistyToggle.Value value) {
			this.name = name;
			this.tip = tip;
			this.value = value;
		}
	}

	private final String title;
	private final List<Toggle> toggles;
	private final List<HudModule.Slider> sliders;

	public ModuleSettingsScreen(final String title, final List<Toggle> toggles, final List<HudModule.Slider> sliders, final GuiScreen parent) {
		super(parent);
		this.title = title;
		this.toggles = toggles;
		this.sliders = sliders;
	}

	/** The gear of a HUD module: its settings, then its sliders. */
	public static ModuleSettingsScreen of(final HudModule module, final GuiScreen parent) {
		List<Toggle> toggles = new ArrayList<Toggle>();
		for (final HudModule.Setting setting : module.settings()) {
			toggles.add(new Toggle(setting.name(module), setting.tip(module), new MistyToggle.Value() {
				@Override
				public boolean get() {
					return module.setting(setting.key);
				}

				@Override
				public void set(final boolean value) {
					module.state().setOption(setting.key, value);
				}
			}));
		}
		return new ModuleSettingsScreen(I18n.format("misty.module.settings", module.name()), toggles, module.sliders(), parent);
	}

	/** The gear of an effect: its sliders only. */
	public static ModuleSettingsScreen sliders(final String title, final List<HudModule.Slider> sliders, final GuiScreen parent) {
		return new ModuleSettingsScreen(title, Collections.<Toggle>emptyList(), sliders, parent);
	}

	@Override
	protected float veil() {
		return 0.55F;
	}

	private int frameW() {
		return Math.min(this.width - 20, WIDTH);
	}

	private int frameH() {
		return HEADER + PAD + (this.toggles.size() + this.sliders.size()) * (ROW + GAP) + PAD + 18 + PAD;
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
		for (Toggle toggle : this.toggles) {
			this.add(new MistyToggle(x, y, inner, ROW, toggle.name, toggle.value).tooltip(toggle.tip));
			y += ROW + GAP;
		}
		for (HudModule.Slider slider : this.sliders) {
			this.add(slider.create(x, y, inner, ROW));
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
		Ui.text(this.title, fx + 26, fy + (HEADER - 8) / 2, Ui.TEXT);
	}

	@Override
	public void onGuiClosed() {
		Config.save();
	}
}
