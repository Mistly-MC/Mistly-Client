package online.mistylauncher.legacy.hud.modules;

import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.features.Sidebar;
import online.mistylauncher.legacy.hud.HudModule;
import online.mistylauncher.legacy.ui.Ui;

/**
 * The leaderboard (the servers' sidebar) in the Mistly style, on every server: its title, its
 * "label: value" lines aligned, its other lines as they are. It stands in the leaderboard's place:
 * the server's own is not drawn while this one shows.
 */
public final class LeaderboardModule extends HudModule {
	private static final int ROW = 10;
	private static final int PADDING = 5;
	private static final List<Sidebar.Row> SAMPLE = Arrays.asList(
		new Sidebar.Row(Sidebar.Kind.TITLE, "Leaderboard", ""),
		new Sidebar.Row(Sidebar.Kind.PAIR, "Map", "Theatre"),
		new Sidebar.Row(Sidebar.Kind.PAIR, "Players", "11/12"));

	public LeaderboardModule() {
		super("leaderboard", new Config.Module(true, 5, -4, 60));
	}

	private static List<Sidebar.Row> rows(final Minecraft minecraft, final boolean preview) {
		List<Sidebar.Row> rows = Sidebar.rows(minecraft, Sidebar.objective(minecraft));
		return rows.isEmpty() && preview ? SAMPLE : rows;
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return !Sidebar.rows(minecraft, Sidebar.objective(minecraft)).isEmpty();
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		int width = 60;
		for (Sidebar.Row row : rows(minecraft, preview)) {
			int w = row.kind == Sidebar.Kind.PAIR ? Ui.width(row.label) + 8 + Ui.width(row.value) : Ui.width(row.label);
			width = Math.max(width, PADDING + w + PADDING);
		}
		return width;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return Math.max(1, rows(minecraft, preview).size()) * ROW + 2 * PADDING - 2;
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		int width = this.width(minecraft, preview);
		background(width, this.height(minecraft, preview));
		int y = PADDING;
		for (Sidebar.Row row : rows(minecraft, preview)) {
			switch (row.kind) {
				case TITLE:
					Ui.centered(row.label, width / 2, y, Ui.TEXT);
					break;
				case HEADER:
					Ui.text(row.label, PADDING, y, Ui.ACCENT);
					break;
				case PAIR:
					Ui.text(row.label, PADDING, y, Ui.ACCENT);
					Ui.text(row.value, width - PADDING - Ui.width(row.value), y, Ui.TEXT);
					break;
				default:
					Ui.text(row.label, PADDING, y, Ui.TEXT);
					break;
			}
			y += ROW;
		}
	}
}
