package online.mistylauncher.legacy.hud;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.modules.*;

/** The HUD modules, where they go on the screen, and drawing them. */
public final class Hud {
	public static final float MIN_SCALE = 0.5F;
	public static final float MAX_SCALE = 2.0F;

	private static final List<HudModule> MODULES = Collections.unmodifiableList(Arrays.asList(
		new FpsModule(), new PingModule(), new CpsModule(), new ReachModule(), new ComboModule(), new KillsModule(), new LeaderboardModule(),
		new CoordsModule(), new KeystrokesModule(), new ArmorModule(), new EffectsModule(), new ClockModule(), new ServerModule(), new MusicModule(),
		new SaturationModule(), new RamModule(), new PotionsModule(), new MinimapModule()));

	private Hud() {
	}

	public static List<HudModule> modules() {
		return MODULES;
	}

	/** A module's place on the screen, in GUI pixels. */
	public static final class Bounds {
		public final int x;
		public final int y;
		public final int width;
		public final int height;

		Bounds(final int x, final int y, final int width, final int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public boolean contains(final int px, final int py) {
			return px >= this.x && px < this.x + this.width && py >= this.y && py < this.y + this.height;
		}
	}

	/** Start of a box of {@code size} along an axis of {@code screen} pixels: 0 start, 1 centre, 2 end. */
	private static int base(final int side, final int screen, final int size) {
		return side == 0 ? 0 : side == 1 ? (screen - size) / 2 : screen - size;
	}

	private static float scale(final Config.Module state) {
		return Math.max(MIN_SCALE, Math.min(MAX_SCALE, state.scale));
	}

	/** The width a module is placed by: its first item's for one that widens, else all of it. */
	private static int fixedWidth(final HudModule module, final Minecraft minecraft, final boolean preview, final float scale, final int width) {
		int fixed = Math.round(module.fixedWidth(minecraft, preview) * scale);
		return fixed > 0 && fixed < width ? fixed : width;
	}

	public static Bounds bounds(final HudModule module, final Minecraft minecraft, final int screenWidth, final int screenHeight, final boolean preview) {
		Config.Module state = module.state();
		float scale = scale(state);
		int width = Math.round(module.width(minecraft, preview) * scale);
		int height = Math.round(module.height(minecraft, preview) * scale);
		int anchor = Math.max(0, Math.min(8, state.anchor));
		// a module that widens keeps its first item in place: the rest goes to one side only
		int fixed = fixedWidth(module, minecraft, preview, scale, width);
		int first = base(anchor % 3, screenWidth, fixed) + state.x;
		int x = module.growsRight() ? first : first + fixed - width;
		int y = base(anchor / 3, screenHeight, height) + state.y;
		// always fully on screen, whatever the window size became
		x = Math.max(0, Math.min(x, Math.max(0, screenWidth - width)));
		y = Math.max(0, Math.min(y, Math.max(0, screenHeight - height)));
		return new Bounds(x, y, width, height);
	}

	/**
	 * Moves a module so its top-left corner is at (x, y). It is tied to the nearest third of the
	 * screen: a module dropped in a corner stays in that corner when the window is resized.
	 */
	public static void place(final HudModule module, final Bounds bounds, final int x, final int y, final int screenWidth, final int screenHeight) {
		Config.Module state = module.state();
		int fixed = fixedWidth(module, Minecraft.getMinecraft(), true, scale(state), bounds.width);
		int first = module.growsRight() ? x : x + bounds.width - fixed;
		int column = third(first + fixed / 2, screenWidth);
		int row = third(y + bounds.height / 2, screenHeight);
		state.anchor = column + 3 * row;
		state.x = first - base(column, screenWidth, fixed);
		state.y = y - base(row, screenHeight, bounds.height);
	}

	private static int third(final int position, final int screen) {
		return position < screen / 3 ? 0 : position > screen * 2 / 3 ? 2 : 1;
	}

	public static void draw(final HudModule module, final Minecraft minecraft, final Bounds bounds, final boolean preview) {
		float scale = scale(module.state());
		GlStateManager.pushMatrix();
		GlStateManager.translate(bounds.x, bounds.y, 0.0F);
		GlStateManager.scale(scale, scale, 1.0F);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		module.draw(minecraft, preview);
		GlStateManager.popMatrix();
	}

	/** Every module switched on that has something to show. */
	public static void drawAll(final int screenWidth, final int screenHeight, final boolean preview) {
		if (!Config.get().hud) {
			return;
		}
		Minecraft minecraft = Minecraft.getMinecraft();
		for (HudModule module : MODULES) {
			if (module.state().enabled && module.available(minecraft)) {
				draw(module, minecraft, bounds(module, minecraft, screenWidth, screenHeight, preview), preview);
			}
		}
	}

	/** The Leaderboard module shows the server's sidebar in its place: the game's is not drawn. */
	public static boolean replacesScoreboard() {
		Minecraft minecraft = Minecraft.getMinecraft();
		for (HudModule module : MODULES) {
			if (module instanceof LeaderboardModule) {
				return Config.get().hud && module.state().enabled && module.available(minecraft);
			}
		}
		return false;
	}

	/** Every module back where it first was. */
	public static void resetAll() {
		for (HudModule module : MODULES) {
			module.reset();
		}
	}
}
