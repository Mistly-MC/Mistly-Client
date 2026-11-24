package online.mistylauncher.legacy.hud.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.features.Clicks;
import online.mistylauncher.legacy.hud.HudModule;

/** The movement keys and the mouse buttons, lit while held, with the CPS on the mouse buttons. */
public final class KeystrokesModule extends HudModule {
	private static final int KEY = 20;
	private static final int GAP = 1;
	private static final int SIZE = 3 * KEY + 2 * GAP;
	private static final int HELD = 0xD0FFFFFF;
	private static final int HELD_TEXT = 0xFF101420;

	public KeystrokesModule() {
		super("keystrokes", new Config.Module(true, 2, -2, 2));
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		return SIZE;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return 3 * KEY + 2 * GAP;
	}

	private static void key(final Minecraft minecraft, final int x, final int y, final int w, final String label, final boolean held) {
		Gui.drawRect(x, y, x + w, y + KEY, held ? HELD : BACKGROUND);
		int tx = x + (w - minecraft.fontRendererObj.getStringWidth(label)) / 2;
		minecraft.fontRendererObj.drawString(label, tx, y + 6, held ? HELD_TEXT : TEXT);
	}

	private static String label(final KeyBinding binding) {
		return GameSettings.getKeyDisplayString(binding.getKeyCode());
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		GameSettings s = minecraft.gameSettings;
		key(minecraft, KEY + GAP, 0, KEY, label(s.keyBindForward), s.keyBindForward.isKeyDown());
		key(minecraft, 0, KEY + GAP, KEY, label(s.keyBindLeft), s.keyBindLeft.isKeyDown());
		key(minecraft, KEY + GAP, KEY + GAP, KEY, label(s.keyBindBack), s.keyBindBack.isKeyDown());
		key(minecraft, 2 * (KEY + GAP), KEY + GAP, KEY, label(s.keyBindRight), s.keyBindRight.isKeyDown());
		int half = (SIZE - GAP) / 2;
		key(minecraft, 0, 2 * (KEY + GAP), half, Clicks.left() + " CPS", s.keyBindAttack.isKeyDown());
		key(minecraft, half + GAP, 2 * (KEY + GAP), SIZE - half - GAP, Clicks.right() + " CPS", s.keyBindUseItem.isKeyDown());
	}
}
