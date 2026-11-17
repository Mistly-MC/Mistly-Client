package online.mistylauncher.client.hud.modules;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.features.CubeCraftGame;
import online.mistylauncher.client.hud.HudModule;
import online.mistylauncher.client.ui.Ui;
import org.jspecify.annotations.Nullable;

/**
 * Pillars of Fortune on CubeCraft: the time left and the players left, read on the server's
 * sidebar ("Time remaining:" then "4 minutes, 59 secondes", "Joueurs restants:" then "8"), so
 * they stay in view even with the leaderboard hidden. Shown only in that mode.
 */
public final class PillarsModule extends HudModule {
	private static final Pattern MODE = Pattern.compile("(?i)pillars of fortune");
	/** The sidebar's labels, in the languages of CubeCraft. */
	private static final Pattern TIME = Pattern.compile("(?i)time (?:remaining|left)|temps restant|tiempo restante|tempo rimanente|verbleibende zeit|оставшееся время");
	private static final Pattern PLAYERS = Pattern.compile("(?i)players (?:remaining|left)|joueurs restants|jugadores restantes|giocatori rimanenti|verbleibende spieler|игрок\\p{L}* остал");
	private static final Pattern NUMBER = Pattern.compile("\\d+");
	private static final int ROW = 10;
	private static final int PADDING = 5;

	public PillarsModule() {
		super("pillars", new MistyConfig.Module(true, 3, 4, 118));
	}

	/** Switched on in the CubeCraft options of the Server category, not in the HUD one. */
	@Override
	public boolean inHudCategory() {
		return false;
	}

	private static boolean inPillars(final Minecraft minecraft, final List<String> sidebar) {
		String mode = CubeCraftGame.mode();
		return mode != null && MODE.matcher(mode).find() || !sidebar.isEmpty() && MODE.matcher(sidebar.get(0)).find();
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return CubeCraftGame.onCubeCraft(minecraft) && inPillars(minecraft, CubeCraftGame.sidebarLines(minecraft));
	}

	/** "4 minutes, 59 secondes" → "4:59"; "45 secondes" → "0:45"; anything else as it is. */
	private static String time(final @Nullable String text) {
		if (text == null) {
			return "-";
		}
		Matcher numbers = NUMBER.matcher(text);
		int[] read = new int[2];
		int count = 0;
		while (count < 2 && numbers.find()) {
			read[count++] = Integer.parseInt(numbers.group());
		}
		if (count == 0) {
			return text;
		}
		boolean minutes = text.toLowerCase().contains("min");
		int m = minutes ? read[0] : 0;
		int s = minutes ? (count > 1 ? read[1] : 0) : read[0];
		return m + ":" + (s < 10 ? "0" : "") + s;
	}

	/** The two values, from the sidebar (an example in the editor). */
	private static String[] values(final Minecraft minecraft, final boolean preview) {
		List<String> sidebar = CubeCraftGame.sidebarLines(minecraft);
		if (!inPillars(minecraft, sidebar)) {
			return preview ? new String[] { "4:59", "8" } : new String[] { "-", "-" };
		}
		String players = CubeCraftGame.sidebarValue(sidebar, PLAYERS);
		return new String[] { time(CubeCraftGame.sidebarValue(sidebar, TIME)), players == null ? "-" : players };
	}

	private static Component[] labels() {
		return new Component[] { Component.translatable("misty.module.pillars.time"), Component.translatable("misty.module.pillars.players") };
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		Font font = minecraft.font;
		Component[] labels = labels();
		String[] values = values(minecraft, preview);
		int label = Math.max(font.width(labels[0]), font.width(labels[1]));
		int value = Math.max(font.width(values[0]), font.width(values[1]));
		return Math.max(60, PADDING + label + 6 + value + PADDING);
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return 2 * ROW + 2 * PADDING - 2;
	}

	@Override
	public void draw(final GuiGraphicsExtractor graphics, final Minecraft minecraft, final boolean preview) {
		Font font = minecraft.font;
		int width = this.width(minecraft, preview);
		background(graphics, width, this.height(minecraft, preview));
		Component[] labels = labels();
		String[] values = values(minecraft, preview);
		for (int i = 0; i < 2; i++) {
			int y = PADDING + i * ROW;
			graphics.text(font, labels[i], PADDING, y, Ui.ACCENT, false);
			graphics.text(font, values[i], width - PADDING - font.width(values[i]), y, Ui.TEXT, false);
		}
	}
}
