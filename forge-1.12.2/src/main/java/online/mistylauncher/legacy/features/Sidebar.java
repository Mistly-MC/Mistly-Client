package online.mistylauncher.legacy.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

/**
 * Any server's sidebar (its leaderboard), read line by line for the Leaderboard module, as in the
 * Fabric versions: "label: value" lines become aligned pairs (also "Map:" over "Theatre"), the
 * other lines stay as they are, and only what is not information goes (separators, the date, the
 * server's address).
 */
public final class Sidebar {
	public enum Kind { TITLE, HEADER, PAIR, TEXT }

	public static final class Row {
		public final Kind kind;
		public final String label;
		public final String value;

		public Row(final Kind kind, final String label, final String value) {
			this.kind = kind;
			this.label = label;
			this.value = value;
		}
	}

	private static final Pattern BULLET = Pattern.compile("^[●•▪▸►»·*\\-]+\\s*");
	private static final Pattern WORDS = Pattern.compile("[\\p{L}\\p{N}]");
	private static final Pattern DATE = Pattern.compile("^\\d{1,2}/\\d{1,2}/\\d{2,4}\\b");
	private static final Pattern ADDRESS = Pattern.compile("(?i)^(?:www\\.)?[a-z0-9-]+(?:\\.[a-z0-9-]+)+$");
	private static final Pattern MINUTES = Pattern.compile("(?iu)(\\d+)\\s*(?:minutes?|minutos?|minuti|minuten|мин\\p{L}*)");
	private static final Pattern SECONDS = Pattern.compile("(?iu)(\\d+)\\s*(?:secondes?|seconds?|segundos?|secondi|sekunden?|сек\\p{L}*)");
	private static final int MAX_ROWS = 16;

	private Sidebar() {
	}

	/** The sidebar taken away while the HUD is drawn (hidden, or shown by the Leaderboard module). */
	private static ScoreObjective hidden;

	/** The sidebar's objective, null without one (also while it is taken away). */
	public static ScoreObjective objective(final Minecraft minecraft) {
		if (hidden != null) {
			return hidden;
		}
		return minecraft.world == null ? null : minecraft.world.getScoreboard().getObjectiveInDisplaySlot(1);
	}

	/**
	 * Before the game draws its HUD: its sidebar taken away when it is hidden or replaced (the
	 * game has no way to leave it out), given back right after ({@link #giveBack}).
	 */
	public static void takeAway(final Minecraft minecraft, final boolean away) {
		if (!away || minecraft.world == null) {
			return;
		}
		Scoreboard scoreboard = minecraft.world.getScoreboard();
		ScoreObjective sidebar = scoreboard.getObjectiveInDisplaySlot(1);
		if (sidebar != null) {
			hidden = sidebar;
			scoreboard.setObjectiveInDisplaySlot(1, null);
		}
	}

	public static void giveBack(final Minecraft minecraft) {
		if (hidden != null && minecraft.world != null) {
			Scoreboard scoreboard = minecraft.world.getScoreboard();
			// unless the server changed it meanwhile
			if (scoreboard.getObjectiveInDisplaySlot(1) == null && scoreboard.getObjective(hidden.getName()) == hidden) {
				scoreboard.setObjectiveInDisplaySlot(1, hidden);
			}
		}
		hidden = null;
	}

	/** The sidebar's lines as drawn, top to bottom (flattened), its title first. Empty without one. */
	public static List<String> lines(final Minecraft minecraft, final ScoreObjective sidebar) {
		List<String> lines = new ArrayList<String>();
		if (sidebar == null) {
			return lines;
		}
		Scoreboard scoreboard = sidebar.getScoreboard();
		lines.add(CubeCraft.flat(sidebar.getDisplayName()));
		// as the game: the 15 highest scores, the highest on top, hidden entries ("#…") left out
		Collection<Score> sorted = scoreboard.getSortedScores(sidebar);
		List<Score> shown = new ArrayList<Score>();
		for (Score score : sorted) {
			if (score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) {
				shown.add(score);
			}
		}
		if (shown.size() > 15) {
			shown = shown.subList(shown.size() - 15, shown.size());
		}
		for (int i = shown.size() - 1; i >= 0; i--) {
			Score score = shown.get(i);
			lines.add(CubeCraft.flat(ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(score.getPlayerName()), score.getPlayerName())));
		}
		return lines;
	}

	/** The sidebar of the server played on, as rows. */
	public static List<Row> rows(final Minecraft minecraft, final ScoreObjective sidebar) {
		List<String> lines = lines(minecraft, sidebar);
		List<Row> rows = new ArrayList<Row>();
		if (lines.isEmpty()) {
			return rows;
		}
		if (WORDS.matcher(lines.get(0)).find()) {
			rows.add(new Row(Kind.TITLE, lines.get(0), ""));
		}
		for (int i = 1; i < lines.size() && rows.size() < MAX_ROWS; i++) {
			String line = BULLET.matcher(lines.get(i)).replaceFirst("").trim();
			if (!information(line)) {
				continue;
			}
			if (line.endsWith(":")) {
				String next = i + 1 < lines.size() ? BULLET.matcher(lines.get(i + 1)).replaceFirst("").trim() : "";
				if (information(next) && !next.endsWith(":") && !next.contains(": ")) {
					rows.add(new Row(Kind.PAIR, line.substring(0, line.length() - 1).trim(), shortTime(next)));
					i++;
				}
				continue;
			}
			int colon = line.indexOf(": ");
			if (colon > 0) {
				String value = line.substring(colon + 2).trim();
				if (!value.isEmpty()) {
					rows.add(new Row(Kind.PAIR, line.substring(0, colon).trim(), shortTime(value)));
				}
				continue;
			}
			boolean header = i + 1 < lines.size() && BULLET.matcher(lines.get(i + 1)).lookingAt();
			rows.add(new Row(header ? Kind.HEADER : Kind.TEXT, line, ""));
		}
		return rows;
	}

	private static boolean information(final String line) {
		return WORDS.matcher(line).find() && !DATE.matcher(line).find() && !ADDRESS.matcher(line).matches();
	}

	/** "4 minutes, 59 secondes" → "4:59", "45 seconds" → "0:45"; anything else as it is. */
	private static String shortTime(final String value) {
		Matcher minutes = MINUTES.matcher(value);
		Matcher seconds = SECONDS.matcher(value);
		boolean m = minutes.find();
		boolean s = seconds.find();
		if (!m && !s) {
			return value;
		}
		int sec = s ? Integer.parseInt(seconds.group(1)) : 0;
		return (m ? Integer.parseInt(minutes.group(1)) : 0) + ":" + (sec < 10 ? "0" : "") + sec;
	}
}
