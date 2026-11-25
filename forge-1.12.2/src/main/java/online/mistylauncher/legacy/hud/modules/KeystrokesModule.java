package online.mistylauncher.legacy.hud.modules;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.HudModule;
import online.mistylauncher.legacy.ui.Ui;

/** The movement keys, the two mouse buttons and the jump key, lit while they are held. */
public final class KeystrokesModule extends HudModule {
	private static final int KEY = 18;
	private static final int GAP = 2;
	private static final int WIDTH = 3 * KEY + 2 * GAP;
	private static final int MOUSE_HEIGHT = 16;
	private static final int JUMP_HEIGHT = 10;

	public KeystrokesModule() {
		super("keystrokes", new Config.Module(true, 3, 4, 0));
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		return WIDTH;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return 2 * (KEY + GAP) + MOUSE_HEIGHT + GAP + JUMP_HEIGHT;
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		GameSettings s = minecraft.gameSettings;
		key(KEY + GAP, 0, KEY, KEY, label(s.keyBindForward), s.keyBindForward.isKeyDown());
		int y = KEY + GAP;
		key(0, y, KEY, KEY, label(s.keyBindLeft), s.keyBindLeft.isKeyDown());
		key(KEY + GAP, y, KEY, KEY, label(s.keyBindBack), s.keyBindBack.isKeyDown());
		key(2 * (KEY + GAP), y, KEY, KEY, label(s.keyBindRight), s.keyBindRight.isKeyDown());
		y += KEY + GAP;
		int half = (WIDTH - GAP) / 2;
		key(0, y, half, MOUSE_HEIGHT, "LMB", s.keyBindAttack.isKeyDown());
		key(half + GAP, y, half, MOUSE_HEIGHT, "RMB", s.keyBindUseItem.isKeyDown());
		y += MOUSE_HEIGHT + GAP;
		boolean jump = s.keyBindJump.isKeyDown();
		Ui.panel(0, y, WIDTH, JUMP_HEIGHT, jump ? Ui.FILL : BACKGROUND);
		Ui.fill(WIDTH / 2 - 10, y + JUMP_HEIGHT / 2 - 1, WIDTH / 2 + 10, y + JUMP_HEIGHT / 2 + 1, jump ? Ui.INK : Ui.TEXT);
	}

	private static void key(final int x, final int y, final int width, final int height, final String label, final boolean down) {
		Ui.panel(x, y, width, height, down ? Ui.FILL : BACKGROUND);
		Ui.text(label, x + (width - Ui.width(label) + 1) / 2, y + (height - 7) / 2, down ? Ui.INK : Ui.TEXT);
	}

	/** Name of the key bound to a movement: "W", or the start of a longer name ("UP"). */
	private static String label(final KeyBinding key) {
		String name = GameSettings.getKeyDisplayString(key.getKeyCode()).toUpperCase(Locale.ROOT);
		int space = name.indexOf(' ');
		if (space > 0) {
			name = name.substring(0, space);
		}
		return name.length() > 3 ? name.substring(0, 3) : name;
	}
}
