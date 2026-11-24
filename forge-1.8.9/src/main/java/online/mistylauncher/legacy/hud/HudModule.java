package online.mistylauncher.legacy.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import online.mistylauncher.legacy.Config;

/** One box of the HUD: what it shows, its size, and where the player put it (Config.Module). */
public abstract class HudModule {
	/** Behind the modules' text. */
	public static final int BACKGROUND = 0x90101420;
	public static final int TEXT = 0xFFFFFFFF;

	private final String id;
	private final Config.Module defaults;

	protected HudModule(final String id, final Config.Module defaults) {
		this.id = id;
		this.defaults = defaults;
	}

	public final String id() {
		return this.id;
	}

	public final String name() {
		return I18n.format("misty.module." + this.id);
	}

	/** The saved state, or the defaults put there. */
	public final Config.Module state() {
		Config.Module state = Config.get().modules.get(this.id);
		if (state == null) {
			state = this.defaults.copy();
			Config.get().modules.put(this.id, state);
		}
		return state;
	}

	public final void reset() {
		Config.get().modules.put(this.id, this.defaults.copy());
	}

	/** False when there is nothing to show right now (no potions, ping in single player…). */
	public boolean available(final Minecraft minecraft) {
		return minecraft.thePlayer != null;
	}

	public abstract int width(Minecraft minecraft, boolean preview);

	public abstract int height(Minecraft minecraft, boolean preview);

	/**
	 * Draws the module with its top-left corner at (0, 0). {@code preview}: in the editor, where
	 * sample values stand in for what is missing.
	 */
	public abstract void draw(Minecraft minecraft, boolean preview);

	/**
	 * For a module that widens with what it shows (the potions counter): the width of its first
	 * item, which stays where the player put it while the others are added on one side
	 * ({@link #growsRight()}). 0: the module is placed by its whole width.
	 */
	public int fixedWidth(final Minecraft minecraft, final boolean preview) {
		return 0;
	}

	/** With a {@link #fixedWidth}: the items after the first are added on the right, else on the left. */
	public boolean growsRight() {
		return false;
	}

	protected static void background(final int width, final int height) {
		Gui.drawRect(0, 0, width, height, BACKGROUND);
	}
}
