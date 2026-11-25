package online.mistylauncher.client.features;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

/**
 * Any server's sidebar (its leaderboard), read line by line for the Leaderboard module. Every
 * server writes it its own way, so nothing is lost: "label: value" lines become aligned pairs
 * (also "Map:" over "Theatre", and with their bullets off: "● Wins: 70"), the other lines stay as
 * they are (a section title above bulleted lines in the accent colour), and only what is not
 * information goes: separator lines, the date, the server's address.
 */
public final class Sidebar {
	/** What a row is: the sidebar's title, a section title, a "label: value", or any other line. */
	public enum Kind { TITLE, HEADER, PAIR, TEXT }

	/** One row: for a pair its label and value, otherwise the text in {@code label}. */
	public record Row(Kind kind, String label, String value) {
	}

	private static final Pattern BULLET = Pattern.compile("^[●•▪▸►»·*\\-]+\\s*");
	private static final Pattern WORDS = Pattern.compile("[\\p{L}\\p{N}]");
	private static final Pattern DATE = Pattern.compile("^\\d{1,2}/\\d{1,2}/\\d{2,4}\\b");
	private static final Pattern ADDRESS = Pattern.compile("(?i)^(?:www\\.)?[a-z0-9-]+(?:\\.[a-z0-9-]+)+$");
	private static final int MAX_ROWS = 16;

	private Sidebar() {
	}

	/**
	 * The sidebar's lines as drawn, top to bottom (flattened like the chat: no font icons, no colour
	 * codes, single spaces), its title first. Empty without a sidebar.
	 */
	public static List<String> lines(final Minecraft minecraft) {
		List<String> lines = new ArrayList<>();
		if (minecraft.level == null) {
			return lines;
		}
		Scoreboard scoreboard = minecraft.level.getScoreboard();
		Objective sidebar = online.mistylauncher.client.compat.Versions.sidebar(scoreboard);
		if (sidebar == null) {
			return lines;
		}
		lines.add(CubeCraftGame.flat(sidebar.getDisplayName()));
		// the game's order: highest score first, then by name
		//? if >=1.20.3 {
		scoreboard.listPlayerScores(sidebar).stream()
			.filter(entry -> !entry.isHidden())
			.sorted(Comparator.comparing(net.minecraft.world.scores.PlayerScoreEntry::value).reversed().thenComparing(net.minecraft.world.scores.PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER))
			.limit(15)
			.forEach(entry -> lines.add(CubeCraftGame.flat(PlayerTeam.formatNameForTeam(scoreboard.getPlayersTeam(entry.owner()), entry.ownerName()))));
		//?} else {
		/*// before 1.20.3 the scores are plain ones, hidden when their owner starts with #
		scoreboard.getPlayerScores(sidebar).stream()
			.filter(score -> score.getOwner() != null && !score.getOwner().startsWith("#"))
			.sorted(Comparator.comparingInt(net.minecraft.world.scores.Score::getScore).reversed().thenComparing(net.minecraft.world.scores.Score::getOwner, String.CASE_INSENSITIVE_ORDER))
			.limit(15)
			.forEach(score -> lines.add(CubeCraftGame.flat(PlayerTeam.formatNameForTeam(scoreboard.getPlayersTeam(score.getOwner()), net.minecraft.network.chat.Component.literal(score.getOwner())))));
		*///?}
		return lines;
	}

	/** The sidebar of the server played on, as rows. */
	public static List<Row> rows(final Minecraft minecraft) {
		return rows(lines(minecraft));
	}

	/** {@code lines} (title first) as rows. */
	public static List<Row> rows(final List<String> lines) {
		List<Row> rows = new ArrayList<>();
		if (lines.isEmpty()) {
			return rows;
		}
		if (WORDS.matcher(lines.get(0)).find()) {
			rows.add(new Row(Kind.TITLE, lines.get(0), ""));
		}
		for (int i = 1; i < lines.size() && rows.size() < MAX_ROWS; i++) {
			String line = BULLET.matcher(lines.get(i)).replaceFirst("").strip();
			if (!information(line)) {
				continue;
			}
			if (line.endsWith(":")) {
				String next = i + 1 < lines.size() ? BULLET.matcher(lines.get(i + 1)).replaceFirst("").strip() : "";
				// the value on the next line ("Map:" then "Theatre"); a label whose value is an icon goes
				if (information(next) && !next.endsWith(":") && !next.contains(": ")) {
					rows.add(new Row(Kind.PAIR, line.substring(0, line.length() - 1).strip(), shortTime(next)));
					i++;
				}
				continue;
			}
			int colon = line.indexOf(": ");
			if (colon > 0) {
				String value = line.substring(colon + 2).strip();
				if (!value.isEmpty()) {
					rows.add(new Row(Kind.PAIR, line.substring(0, colon).strip(), shortTime(value)));
				}
				continue;
			}
			// a title of the lines under it ("Stats" over "● Wins: 70"), else a line of its own
			boolean header = i + 1 < lines.size() && BULLET.matcher(lines.get(i + 1)).lookingAt();
			rows.add(new Row(header ? Kind.HEADER : Kind.TEXT, line, ""));
		}
		return rows;
	}

	/** Not information: empty or separator lines, the date, the server's address. */
	private static boolean information(final String line) {
		return WORDS.matcher(line).find() && !DATE.matcher(line).find() && !ADDRESS.matcher(line).matches();
	}

	private static final Pattern MINUTES = Pattern.compile("(?iu)(\\d+)\\s*(?:minutes?|minutos?|minuti|minuten|мин\\p{L}*)");
	private static final Pattern SECONDS = Pattern.compile("(?iu)(\\d+)\\s*(?:secondes?|seconds?|segundos?|secondi|sekunden?|сек\\p{L}*)");

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
