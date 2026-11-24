package online.mistylauncher.legacy.hud;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.ui.Ui;

/** One box of the HUD: what it shows, its size, and where the player put it (Config.Module). */
public abstract class HudModule {
	/** Background of every module: the launcher's night sky, see-through (as in the Fabric versions). */
	public static final int BACKGROUND = 0xB00A0F1D;

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

	/** A copy of the module's first state (where it sits, on or off), for a ready-made preset. */
	public final Config.Module defaultState() {
		return this.defaults.copy();
	}

	public final void reset() {
		Config.get().modules.put(this.id, this.defaults.copy());
	}

	/** One setting of the module, shown behind its gear in the settings. */
	public static final class Setting {
		public final String key;
		public final boolean byDefault;

		public Setting(final String key, final boolean byDefault) {
			this.key = key;
			this.byDefault = byDefault;
		}

		public String name(final HudModule module) {
			return I18n.format("misty.module." + module.id() + "." + this.key);
		}

		public String tip(final HudModule module) {
			return I18n.format("misty.module." + module.id() + "." + this.key + ".tip");
		}
	}

	/** The module's own settings (none: no gear). */
	public List<Setting> settings() {
		return Collections.emptyList();
	}

	/** A slider behind the module's gear, under its switches (a colour…). */
	public interface Slider {
		online.mistylauncher.legacy.ui.MistySlider create(int x, int y, int width, int height);
	}

	/** The module's sliders behind its gear; none by default. */
	public List<Slider> sliders() {
		return Collections.emptyList();
	}

	/** The module has a gear: settings or sliders. */
	public final boolean hasGear() {
		return !this.settings().isEmpty() || !this.sliders().isEmpty();
	}

	/** False for a module switched on from elsewhere than the HUD category (CubeCraft's kills). */
	public boolean inHudCategory() {
		return true;
	}

	/** Current value of one of the module's settings. */
	public final boolean setting(final String key) {
		for (Setting setting : this.settings()) {
			if (setting.key.equals(key)) {
				return this.state().option(key, setting.byDefault);
			}
		}
		return false;
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
		Ui.panel(0, 0, width, height, BACKGROUND);
	}
}
