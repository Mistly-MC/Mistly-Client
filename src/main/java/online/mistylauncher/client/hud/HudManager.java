package online.mistylauncher.client.hud;

import online.mistylauncher.client.compat.Gui2D;
import online.mistylauncher.client.compat.Versions;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.hud.modules.ArmorModule;
import online.mistylauncher.client.hud.modules.ClockModule;
import online.mistylauncher.client.hud.modules.ComboModule;
import online.mistylauncher.client.hud.modules.CoordinatesModule;
import online.mistylauncher.client.hud.modules.CpsModule;
import online.mistylauncher.client.hud.modules.EffectsModule;
import online.mistylauncher.client.hud.modules.FpsModule;
import online.mistylauncher.client.hud.modules.KeystrokesModule;
import online.mistylauncher.client.hud.modules.KillsModule;
import online.mistylauncher.client.hud.modules.MusicModule;
import online.mistylauncher.client.hud.modules.LeaderboardModule;
import online.mistylauncher.client.hud.modules.PingModule;
import online.mistylauncher.client.hud.modules.ReachModule;
import online.mistylauncher.client.hud.modules.ServerModule;
import online.mistylauncher.client.hud.modules.SaturationModule;
import online.mistylauncher.client.hud.modules.RamModule;
import online.mistylauncher.client.hud.modules.TotemModule;
import online.mistylauncher.client.hud.modules.PotionModule;
import online.mistylauncher.client.hud.modules.MinimapModule;

/** The HUD modules: where each one sits on the screen, and their drawing in game. */
public final class HudManager {
	public static final float MIN_SCALE = 0.5F;
	public static final float MAX_SCALE = 2.0F;

	private static final List<HudModule> MODULES = List.of(
		new FpsModule(), new PingModule(), new CpsModule(), new ReachModule(), new ComboModule(), new KillsModule(), new LeaderboardModule(), new CoordinatesModule(), new KeystrokesModule(), new ArmorModule(), new EffectsModule(), new ClockModule(), new ServerModule(), new MusicModule(),
		new SaturationModule(), new RamModule(), new TotemModule(), new PotionModule(), new MinimapModule()
	);

	/** Rectangle a module covers on the screen, in GUI pixels. */
	public record Bounds(int x, int y, int width, int height) {
		public boolean contains(final double pointX, final double pointY) {
			return pointX >= this.x && pointX < this.x + this.width && pointY >= this.y && pointY < this.y + this.height;
		}
	}

	private HudManager() {
	}

	public static void init(final MistyConfig config) {
		MODULES.forEach(module -> module.bind(config));
	}

	public static List<HudModule> modules() {
		return MODULES;
	}

	public static void resetAll(final MistyConfig config) {
		MODULES.forEach(module -> module.reset(config));
	}

	/** Start of a module of {@code size} along an axis of {@code screen} pixels: 0 start, 1 centre, 2 end. */
	private static int base(final int side, final int screen, final int size) {
		return side == 0 ? 0 : side == 1 ? (screen - size) / 2 : screen - size;
	}

	public static Bounds bounds(final HudModule module, final Minecraft minecraft, final int screenWidth, final int screenHeight, final boolean preview) {
		MistyConfig.Module state = module.state();
		float scale = Math.clamp(state.scale, MIN_SCALE, MAX_SCALE);
		int width = Math.round(module.width(minecraft, preview) * scale);
		int height = Math.round(module.height(minecraft, preview) * scale);
		int anchor = Math.clamp(state.anchor, 0, 8);
		int x = base(anchor % 3, screenWidth, width) + state.x;
		int y = base(anchor / 3, screenHeight, height) + state.y;
		// always fully on screen, whatever the window size became
		return new Bounds(Math.clamp(x, 0, Math.max(0, screenWidth - width)), Math.clamp(y, 0, Math.max(0, screenHeight - height)), width, height);
	}

	/**
	 * Moves a module so its top-left corner is at (x, y). It is tied to the nearest third of the
	 * screen: a module dropped in a corner stays in that corner when the window is resized.
	 */
	public static void place(final HudModule module, final Bounds bounds, final int x, final int y, final int screenWidth, final int screenHeight) {
		MistyConfig.Module state = module.state();
		int column = third(x + bounds.width() / 2, screenWidth);
		int row = third(y + bounds.height() / 2, screenHeight);
		state.anchor = column + 3 * row;
		state.x = x - base(column, screenWidth, bounds.width());
		state.y = y - base(row, screenHeight, bounds.height());
	}

	private static int third(final int position, final int screen) {
		return position < screen / 3 ? 0 : position > screen * 2 / 3 ? 2 : 1;
	}

	public static void draw(final GuiGraphicsExtractor graphics, final Minecraft minecraft, final HudModule module, final Bounds bounds, final boolean preview) {
		float scale = Math.clamp(module.state().scale, MIN_SCALE, MAX_SCALE);
		Gui2D.push(graphics);
		Gui2D.translate(graphics, bounds.x(), bounds.y());
		Gui2D.scale(graphics, scale, scale);
		module.draw(graphics, minecraft, preview);
		Gui2D.pop(graphics);
	}

	/** The Effects module is shown in its place: the game's effect icons at the top right are not drawn. */
	public static boolean hidesGameEffects() {
		for (HudModule module : MODULES) {
			if (module instanceof EffectsModule effects) {
				return MistyClient.config().hud && effects.state().enabled && effects.setting(EffectsModule.HIDE_VANILLA);
			}
		}
		return false;
	}

	/** The CubeCraft info module shows the leaderboard's information in its place: the leaderboard is not drawn. */
	public static boolean replacesScoreboard() {
		Minecraft minecraft = Minecraft.getInstance();
		for (HudModule module : MODULES) {
			if (module instanceof LeaderboardModule info) {
				return MistyClient.config().hud && info.state().enabled && info.available(minecraft);
			}
		}
		return false;
	}

	/** In game, under the chat and the player list. */
	public static void render(final GuiGraphicsExtractor graphics, final Minecraft minecraft) {
		// hidden behind the F3 screen only: showDebugScreen() is also true while a debug entry is
		// always on (the hitboxes of the HUD category), which is not the F3 screen
		if (!MistyClient.config().hud || Versions.screen() instanceof HudEditorScreen || Versions.debugScreen()) {
			return;
		}
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		for (HudModule module : MODULES) {
			if (module.state().enabled && module.available(minecraft)) {
				draw(graphics, minecraft, module, bounds(module, minecraft, width, height, false), false);
			}
		}
	}
}
