package online.mistylauncher.legacy.ui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.HudModule;
import online.mistylauncher.legacy.hud.Hud;

/** Mistly Client's settings: zoom, fullbright, the cape, the colours and the potions counter. */
public final class SettingsScreen extends GuiScreen {
	private static final int ZOOM = 1;
	private static final int FULLBRIGHT = 2;
	private static final int CAPE = 3;
	private static final int COLOURS = 4;
	private static final int POTIONS_RIGHT = 5;
	private static final int POTIONS_TEXT = 6;
	private static final int DONE = 0;

	private final GuiScreen parent;

	public SettingsScreen(final GuiScreen parent) {
		this.parent = parent;
	}

	private static String onOff(final String key, final boolean on) {
		return I18n.format(key) + ": " + I18n.format(on ? "misty.on" : "misty.off");
	}

	private static Config.Module potions() {
		for (HudModule module : Hud.modules()) {
			if ("potions".equals(module.id())) {
				return module.state();
			}
		}
		return new Config.Module();
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		Config c = Config.get();
		int left = this.width / 2 - 155;
		int right = this.width / 2 + 5;
		int y = this.height / 6 + 24;
		this.buttonList.add(new GuiButton(ZOOM, left, y, 150, 20, onOff("misty.option.zoom", c.zoom)));
		this.buttonList.add(new GuiButton(FULLBRIGHT, right, y, 150, 20, onOff("misty.option.fullbright", c.fullbright)));
		this.buttonList.add(new GuiButton(CAPE, left, y + 24, 150, 20, onOff("misty.option.cape", c.cape)));
		this.buttonList.add(new GuiButton(COLOURS, right, y + 24, 150, 20, onOff("misty.option.colours", c.colours)));
		Config.Module potions = potions();
		this.buttonList.add(new GuiButton(POTIONS_RIGHT, left, y + 48, 150, 20,
			I18n.format("misty.option.potionsSide") + ": " + I18n.format(potions.right ? "misty.right" : "misty.left")));
		this.buttonList.add(new GuiButton(POTIONS_TEXT, right, y + 48, 150, 20, onOff("misty.option.potionsText", potions.text)));
		this.buttonList.add(new GuiButton(DONE, this.width / 2 - 100, y + 96, 200, 20, I18n.format("gui.done")));
	}

	@Override
	protected void actionPerformed(final GuiButton button) {
		Config c = Config.get();
		switch (button.id) {
			case ZOOM:
				c.zoom = !c.zoom;
				break;
			case FULLBRIGHT:
				c.fullbright = !c.fullbright;
				break;
			case CAPE:
				c.cape = !c.cape;
				break;
			case COLOURS:
				c.colours = !c.colours;
				break;
			case POTIONS_RIGHT:
				potions().right = !potions().right;
				break;
			case POTIONS_TEXT:
				potions().text = !potions().text;
				break;
			default:
				Config.save();
				this.mc.displayGuiScreen(this.parent);
				return;
		}
		Config.save();
		this.initGui();
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, I18n.format("misty.settings.title"), this.width / 2, this.height / 6, 0xFFFFFF);
		this.drawCenteredString(this.fontRendererObj, I18n.format("misty.settings.keys"), this.width / 2, this.height / 6 + 24 + 76, 0xA0A8B8);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void onGuiClosed() {
		Config.save();
	}
}
