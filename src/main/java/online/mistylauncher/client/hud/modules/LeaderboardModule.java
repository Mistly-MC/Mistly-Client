package online.mistylauncher.client.hud.modules;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.features.Sidebar;
import online.mistylauncher.client.hud.HudModule;
import online.mistylauncher.client.ui.Ui;

/**
 * The leaderboard (the servers' sidebar) in the Mistly style, on every server: its title, its
 * "label: value" lines aligned, its other lines as they are (see Sidebar). It stands in the
 * leaderboard's place: the server's own is not drawn while this one shows.
 */
public final class LeaderboardModule extends HudModule {
	private static final int ROW = 10;
	private static final int PADDING = 5;
	private static final List<Sidebar.Row> SAMPLE = List.of(
		new Sidebar.Row(Sidebar.Kind.TITLE, "Leaderboard", ""),
		new Sidebar.Row(Sidebar.Kind.PAIR, "Map", "Theatre"),
		new Sidebar.Row(Sidebar.Kind.PAIR, "Players", "11/12"));

	public LeaderboardModule() {
		super("leaderboard", new MistyConfig.Module(true, 5, -4, 60));
	}

	private static List<Sidebar.Row> rows(final Minecraft minecraft, final boolean preview) {
		List<Sidebar.Row> rows = Sidebar.rows(minecraft);
		return rows.isEmpty() && preview ? SAMPLE : rows;
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return !Sidebar.rows(minecraft).isEmpty();
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		Font font = minecraft.font;
		int width = 60;
		for (Sidebar.Row row : rows(minecraft, preview)) {
			int w = row.kind() == Sidebar.Kind.PAIR ? font.width(row.label()) + 8 + font.width(row.value()) : font.width(row.label());
			width = Math.max(width, PADDING + w + PADDING);
		}
		return width;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		int rows = Math.max(1, rows(minecraft, preview).size());
		return rows * ROW + 2 * PADDING - 2;
	}

	@Override
	public void draw(final GuiGraphicsExtractor graphics, final Minecraft minecraft, final boolean preview) {
		Font font = minecraft.font;
		int width = this.width(minecraft, preview);
		background(graphics, width, this.height(minecraft, preview));
		int y = PADDING;
		for (Sidebar.Row row : rows(minecraft, preview)) {
			switch (row.kind()) {
				case TITLE -> Ui.centered(graphics, font, net.minecraft.network.chat.Component.literal(row.label()), width / 2, y, Ui.TEXT);
				case HEADER -> graphics.text(font, row.label(), PADDING, y, Ui.ACCENT, false);
				case PAIR -> {
					graphics.text(font, row.label(), PADDING, y, Ui.ACCENT, false);
					graphics.text(font, row.value(), width - PADDING - font.width(row.value()), y, Ui.TEXT, false);
				}
				case TEXT -> graphics.text(font, row.label(), PADDING, y, Ui.TEXT, false);
			}
			y += ROW;
		}
	}
}
