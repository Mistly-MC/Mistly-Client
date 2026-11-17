package online.mistylauncher.client.features;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jspecify.annotations.Nullable;

/**
 * The CubeCraft game being played, read from the server's chat, and the player's kills in it
 * (for the Kills module and Auto GG).
 *
 * <ul>
 * <li>A game's countdown names its mode: "BedWars commence dans 5 secondes.", "Pillars of Fortune
 * is starting in 4 seconds." (the mode's name is never translated, the words after it are; not
 * "You are now invincible for 5 seconds."). It starts a new game: the kills go back to 0.</li>
 * <li>The end box names it too: "Thank you for playing BedWars". The count stays shown until the
 * next game or until the player leaves the mode.</li>
 * <li>FFA has no countdown (players join a running arena): it is read on the sidebar.</li>
 * <li>Back in the main lobby ("… has joined the lobby!", the welcome message) or on another
 * server: no mode any more.</li>
 * <li>A kill: a death message that names another player first and the player after it
 * ("Victim was slain by You.", "Victim died in the void while escaping You.", "… by You's pet
 * Slime."), as CubeCraft counts all of them. The victim must be a player of the tab list, so
 * "Red team's bed has been broken by You." is no kill.</li>
 * </ul>
 */
public final class CubeCraftGame {
	/** The modes whose kills the Kills module counts (by the start of their name: "EggWars Solo"…). */
	private static final Pattern KILL_MODES = Pattern.compile("(?i)^(?:team )?(?:lucky islands|eggwars|bedwars|bed wars|skywars|sky wars|pillars of fortune|ffa|free for all)\\b");
	/** The modes that never end (no gg there), by their name or the sidebar's words. */
	public static final Pattern ENDLESS = Pattern.compile("(?i)\\b(?:ffa|free ?for ?all|parkour|parcours|sky ?block)\\b");
	private static final Pattern FFA = Pattern.compile("(?i)\\b(?:ffa|free ?for ?all)\\b");

	/** A countdown line: "<mode> commence dans 5 secondes." in any language (it ends with the unit). */
	private static final Pattern COUNTDOWN = Pattern.compile(
		"(?iu)^(.+?) (?:commence dans|is starting in|starts in|starting in|begins in|beginnt in|startet in|empieza en|comienza en|inizia tra|inizia fra|начн\\p{L}* через) \\d+ \\p{L}+\\.$");
	private static final Pattern THANKS = Pattern.compile("(?i)^(?:thank you|thanks) for playing\\s+(.+?)$");
	private static final Pattern LOBBY = Pattern.compile("(?i)has joined the lobby!?$|^bienvenue sur cubecraft|^welcome to cubecraft");
	/** A death message: the victim's name first, then the rest of the sentence. */
	private static final Pattern DEATH = Pattern.compile("^(\\w{3,16}) (\\p{L}.*)$");
	/** Not deaths although they name two players ("X showcased their weapon to Y."). */
	private static final Pattern NOT_DEATH = Pattern.compile("(?i)\\bshowcased\\b");

	private static @Nullable String mode;
	private static int kills;
	private static @Nullable ClientPacketListener connection;
	private static int ticks;

	private CubeCraftGame() {
	}

	/** The mode played (or waited for) right now, null when none. */
	public static @Nullable String mode() {
		return onCubeCraft(Minecraft.getInstance()) ? mode : null;
	}

	/** In a mode whose kills are counted (Lucky Islands, EggWars, BedWars, SkyWars, Pillars of Fortune, FFA). */
	public static boolean countingKills() {
		String current = mode();
		return current != null && KILL_MODES.matcher(current).find();
	}

	/** The player's kills in the current game. */
	public static int kills() {
		return kills;
	}

	/**
	 * A server message as CubeCraft writes it, flattened: without the icons of its font (team and
	 * rank glyphs around the names in BedWars: private-use characters), without the old colour
	 * codes ("§2", how the sidebar's lines are told apart), on one line, with single spaces.
	 */
	public static String flat(final Component message) {
		return message.getString().replaceAll("§.", "").replaceAll("[\\p{Co}\\p{Cf}]", "").replaceAll("\\s+", " ").strip();
	}

	/** A system message from the server. */
	public static void chat(final Component message) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!onCubeCraft(minecraft)) {
			return;
		}
		String text = flat(message);
		Matcher thanks = THANKS.matcher(text);
		if (thanks.find()) {
			mode = thanks.group(1);
			return;
		}
		if (LOBBY.matcher(text).find()) {
			mode = null;
			kills = 0;
			return;
		}
		Matcher countdown = COUNTDOWN.matcher(text);
		if (countdown.matches()) {
			mode = countdown.group(1);
			kills = 0;
			AutoGG.newGame();
			return;
		}
		if (mode != null && minecraft.player != null && text.indexOf(':') < 0 && !NOT_DEATH.matcher(text).find()) {
			Matcher death = DEATH.matcher(text);
			String self = minecraft.player.getName().getString();
			if (death.matches() && !death.group(1).equals(self) && isPlayer(minecraft, death.group(1))
				&& Pattern.compile("(?<![\\w])" + Pattern.quote(self) + "(?![\\w])").matcher(death.group(2)).find()) {
				kills++;
			}
		}
	}

	/** Each client tick: another server (or none) forgets the game; FFA is read on the sidebar. */
	public static void tick(final Minecraft minecraft) {
		ClientPacketListener current = minecraft.getConnection();
		if (current != connection) {
			connection = current;
			mode = null;
			kills = 0;
		}
		if (++ticks % 20 != 0 || !onCubeCraft(minecraft)) {
			return;
		}
		boolean ffa = sidebarSays(minecraft, FFA);
		if (ffa && !"FFA".equals(mode)) {
			mode = "FFA";
			kills = 0;
		} else if (!ffa && "FFA".equals(mode)) {
			mode = null;
			kills = 0;
		}
	}

	/** The sidebar (its title or its lines, which are team prefixes and suffixes) has those words. */
	public static boolean sidebarSays(final Minecraft minecraft, final Pattern words) {
		if (minecraft.level == null) {
			return false;
		}
		Scoreboard scoreboard = minecraft.level.getScoreboard();
		Objective sidebar = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
		if (sidebar == null) {
			return false;
		}
		if (words.matcher(sidebar.getDisplayName().getString()).find()) {
			return true;
		}
		for (PlayerTeam team : scoreboard.getPlayerTeams()) {
			if (words.matcher(team.getPlayerPrefix().getString() + team.getPlayerSuffix().getString()).find()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The sidebar's lines as drawn, top to bottom (flattened like the chat: no font icons, single
	 * spaces), its title first. Empty without a sidebar.
	 */
	public static List<String> sidebarLines(final Minecraft minecraft) {
		List<String> lines = new ArrayList<>();
		if (minecraft.level == null) {
			return lines;
		}
		Scoreboard scoreboard = minecraft.level.getScoreboard();
		Objective sidebar = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
		if (sidebar == null) {
			return lines;
		}
		lines.add(flat(sidebar.getDisplayName()));
		// the game's order: highest score first, then by name
		scoreboard.listPlayerScores(sidebar).stream()
			.filter(entry -> !entry.isHidden())
			.sorted(Comparator.comparing(PlayerScoreEntry::value).reversed().thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER))
			.limit(15)
			.forEach(entry -> lines.add(flat(PlayerTeam.formatNameForTeam(scoreboard.getPlayersTeam(entry.owner()), entry.ownerName()))));
		return lines;
	}

	/** One "label: value" of the sidebar. */
	public record Info(String label, String value) {
	}

	/** Not information: the date and server line at the bottom ("17/11/26 (de04o)", "play.cubecraft.net"). */
	private static final Pattern NOT_INFO = Pattern.compile("(?i)^\\d{1,2}/\\d{1,2}/\\d{2,4}\\b|cubecraft\\.net");

	/**
	 * The sidebar's information, top to bottom, in both of CubeCraft's layouts: "Kills: 0" on one
	 * line, or "Map:" with "Theatre" on the line under it. Empty values (a rank shown as an icon)
	 * are left out; times are made short ("4 minutes, 59 secondes" → "4:59").
	 */
	public static List<Info> sidebarInfo(final List<String> lines) {
		List<Info> infos = new ArrayList<>();
		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.isBlank() || NOT_INFO.matcher(line).find()) {
				continue;
			}
			if (line.endsWith(":")) {
				String next = i + 1 < lines.size() ? lines.get(i + 1) : "";
				// not when the next line is a whole "label: value" itself ("Rank:" whose rank is an icon)
				if (!next.isBlank() && !next.endsWith(":") && !next.contains(": ") && !NOT_INFO.matcher(next).find()) {
					infos.add(new Info(line.substring(0, line.length() - 1).strip(), shortTime(next)));
					i++;
				}
				continue;
			}
			int colon = line.indexOf(": ");
			if (colon > 0) {
				String value = line.substring(colon + 2).strip();
				if (!value.isEmpty()) {
					infos.add(new Info(line.substring(0, colon).strip(), shortTime(value)));
				}
			}
		}
		return infos;
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


	/** A player of the tab list (not a team's name or a mob). */
	private static boolean isPlayer(final Minecraft minecraft, final String name) {
		ClientPacketListener listener = minecraft.getConnection();
		return listener != null && listener.getPlayerInfo(name) != null;
	}

	public static boolean onCubeCraft(final Minecraft minecraft) {
		ServerData server = minecraft.getCurrentServer();
		return server != null && server.ip.toLowerCase(Locale.ROOT).contains("cubecraft");
	}
}
