package online.mistylauncher.client.hud.modules;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.features.CubeCraftGame;
import online.mistylauncher.client.hud.HudModule;
import online.mistylauncher.client.ui.Ui;

/**
 * The information of CubeCraft's sidebar in the Mistly style, whatever the place: the lobby
 * ("Lobby: EU #5", "Players: 1 097"), a game's waiting room ("Map: Theatre", "Players: 11/12"),
 * FFA (kills, deaths, killstreaks), Parkour (map, level), Pillars of Fortune (time and players
 * left). Read from the sidebar, so it keeps working with the leaderboard hidden.
 */
public final class GameInfoModule extends HudModule {
	private static final int ROW = 10;
	private static final int PADDING = 5;
	private static final int MAX_ROWS = 10;
	private static final List<CubeCraftGame.Info> SAMPLE = List.of(new CubeCraftGame.Info("Map", "Theatre"), new CubeCraftGame.Info("Players", "11/12"));

	public GameInfoModule() {
		super("cubecraft", new MistyConfig.Module(true, 5, -4, 0));
	}

	/** Switched on in the CubeCraft options of the Server category, not in the HUD one. */
	@Override
	public boolean inHudCategory() {
		return false;
	}

	private static List<CubeCraftGame.Info> infos(final Minecraft minecraft, final boolean preview) {
		List<CubeCraftGame.Info> infos = CubeCraftGame.onCubeCraft(minecraft) ? CubeCraftGame.sidebarInfo(CubeCraftGame.sidebarLines(minecraft)) : List.of();
		if (infos.isEmpty() && preview) {
			return SAMPLE;
		}
		return infos.size() > MAX_ROWS ? infos.subList(0, MAX_ROWS) : infos;
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return !infos(minecraft, false).isEmpty();
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		Font font = minecraft.font;
		int label = 0;
		int value = 0;
		for (CubeCraftGame.Info info : infos(minecraft, preview)) {
			label = Math.max(label, font.width(info.label()));
			value = Math.max(value, font.width(info.value()));
		}
		return Math.max(60, PADDING + label + 8 + value + PADDING);
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return Math.max(1, infos(minecraft, preview).size()) * ROW + 2 * PADDING - 2;
	}

	@Override
	public void draw(final GuiGraphicsExtractor graphics, final Minecraft minecraft, final boolean preview) {
		Font font = minecraft.font;
		int width = this.width(minecraft, preview);
		background(graphics, width, this.height(minecraft, preview));
		int y = PADDING;
		for (CubeCraftGame.Info info : infos(minecraft, preview)) {
			graphics.text(font, info.label(), PADDING, y, Ui.ACCENT, false);
			graphics.text(font, info.value(), width - PADDING - font.width(info.value()), y, Ui.TEXT, false);
			y += ROW;
		}
	}
}
