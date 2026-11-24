package online.mistylauncher.legacy.features;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import online.mistylauncher.legacy.Config;

/**
 * CubeCraft, as in the Fabric versions: the game being played and your kills in it (for the Kills
 * module), and Auto GG ("gg" in the chat once a game ends, in every mode but FFA, Parkour and
 * SkyBlock, which never end).
 *
 * <ul>
 * <li>A game's countdown names its mode: "BedWars commence dans 5 secondes." (it starts a new
 * game: the kills go back to 0). The end box names it too: "Thank you for playing BedWars".</li>
 * <li>FFA has no countdown: it is read on the sidebar.</li>
 * <li>A kill: a death message naming another player of the tab list first and you after it.</li>
 * <li>A game's end: the end box, the summary after a dashed line, your own elimination, the
 * winners, or a title like "VICTORY".</li>
 * </ul>
 */
public final class CubeCraft {
	private static final Pattern KILL_MODES = Pattern.compile("(?i)^(?:team )?(?:lucky islands|eggwars|bedwars|bed wars|skywars|sky wars|pillars of fortune|ffa|free for all)\\b");
	/** The modes that never end (no gg there), by their name or the sidebar's words. */
	private static final Pattern ENDLESS = Pattern.compile("(?i)\\b(?:ffa|free ?for ?all|parkour|parcours|sky ?block)\\b");
	private static final Pattern FFA = Pattern.compile("(?i)\\b(?:ffa|free ?for ?all)\\b");
	private static final Pattern COUNTDOWN = Pattern.compile(
		"(?iu)^(.+?) (?:commence dans|is starting in|starts in|starting in|begins in|beginnt in|startet in|empieza en|comienza en|inizia tra|inizia fra|начн\\p{L}* через) \\d+ \\p{L}+\\.$");
	private static final Pattern THANKS = Pattern.compile("(?i)^(?:thank you|thanks) for playing\\s+(.+?)$");
	private static final Pattern LOBBY = Pattern.compile("(?i)has joined the lobby!?$|^bienvenue sur cubecraft|^welcome to cubecraft");
	private static final Pattern DEATH = Pattern.compile("^(\\w{3,16}) (\\p{L}.*)$");
	private static final Pattern NOT_DEATH = Pattern.compile("(?i)\\bshowcased\\b");
	private static final Pattern DASHES = Pattern.compile("^-{10,}");
	private static final Pattern CHAT_END = Pattern.compile(
		"(?i)(?:\\b(?:won|wins|win) the (?:game|match|round)\\b|\\b(?:is|are) the winners?\\b|^\\W*winners?\\W*[:!]|^\\W*(?:winning team|winner)\\b|\\bgame over\\b|\\bgame has ended\\b)");
	private static final Pattern TITLE_END = Pattern.compile("(?i)^\\W*(?:victory|you won|you win|winner|game over|defeat|draw|game ended)\\W*$");
	/** A player talking ("Name: …", "Name » …") before the words: not the server. */
	private static final Pattern SPEAKER = Pattern.compile("^[^:»>]{1,48}(?::|»|>>) ");
	private static final long GG_DELAY_MS = 1500;
	private static final long GG_COOLDOWN_MS = 15_000;

	private static String mode;
	private static int kills;
	private static NetHandlerPlayClient connection;
	private static int ticks;
	private static long lastGg = -GG_COOLDOWN_MS;
	private static long sendAt = -1;
	private static boolean said;
	private static String previous = "";
	private static String lastTitle = "";

	private CubeCraft() {
	}

	public static boolean onCubeCraft(final Minecraft minecraft) {
		ServerData server = minecraft.getCurrentServerData();
		return server != null && server.serverIP != null && server.serverIP.toLowerCase(Locale.ROOT).contains("cubecraft");
	}

	public static String mode() {
		return onCubeCraft(Minecraft.getMinecraft()) ? mode : null;
	}

	/** In a mode whose kills are counted. */
	public static boolean countingKills() {
		String current = mode();
		return current != null && KILL_MODES.matcher(current).find();
	}

	public static int kills() {
		return kills;
	}

	/** A server message flattened: no colour codes, no font icons, single spaces. */
	public static String flat(final String text) {
		return text.replaceAll("§.", "").replaceAll("[\\p{Co}\\p{Cf}]", "").replaceAll("\\s+", " ").trim();
	}

	/** A chat line from the server (not the line over the hotbar). */
	public static void chat(final String raw) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (!onCubeCraft(minecraft)) {
			return;
		}
		String text = flat(raw);
		String before = previous;
		previous = text;
		Matcher thanks = THANKS.matcher(text);
		if (thanks.find()) {
			mode = thanks.group(1);
			if (!ENDLESS.matcher(thanks.group(1)).find()) {
				gg(text);
			}
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
			said = false;
			return;
		}
		if (text.startsWith("●") && DASHES.matcher(before).find()) {
			gg(text);
			return;
		}
		if (minecraft.thePlayer != null && text.equalsIgnoreCase(minecraft.thePlayer.getName() + " has been eliminated!")) {
			gg(text);
			return;
		}
		Matcher end = CHAT_END.matcher(text);
		if (end.find() && !SPEAKER.matcher(text.substring(0, end.start())).find()) {
			gg(text);
			return;
		}
		if (mode != null && minecraft.thePlayer != null && text.indexOf(':') < 0 && !NOT_DEATH.matcher(text).find()) {
			Matcher death = DEATH.matcher(text);
			String self = minecraft.thePlayer.getName();
			if (death.matches() && !death.group(1).equals(self) && isPlayer(minecraft, death.group(1))
				&& Pattern.compile("(?<![\\w])" + Pattern.quote(self) + "(?![\\w])").matcher(death.group(2)).find()) {
				kills++;
			}
		}
	}

	/** Each tick: another server forgets the game; FFA read on the sidebar; titles; the gg sent. */
	public static void tick(final Minecraft minecraft) {
		NetHandlerPlayClient current = minecraft.getNetHandler();
		if (current != connection) {
			connection = current;
			mode = null;
			kills = 0;
		}
		// titles (big text in the middle of the screen): the game keeps the one shown
		String title = "";
		try {
			String shown = ReflectionHelper.getPrivateValue(net.minecraft.client.gui.GuiIngame.class, minecraft.ingameGUI, "displayedTitle", "field_175201_x");
			title = shown == null ? "" : flat(shown);
		} catch (RuntimeException e) {
			// not readable: no titles
		}
		if (!title.equals(lastTitle)) {
			lastTitle = title;
			if (TITLE_END.matcher(title).matches()) {
				gg(title);
			}
		}
		if (sendAt >= 0 && System.currentTimeMillis() >= sendAt) {
			sendAt = -1;
			if (minecraft.thePlayer != null && minecraft.getNetHandler() != null) {
				minecraft.thePlayer.sendChatMessage("gg");
			}
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

	private static void gg(final String what) {
		Minecraft minecraft = Minecraft.getMinecraft();
		long now = System.currentTimeMillis();
		if (!Config.get().autoGG || !onCubeCraft(minecraft) || said || now - lastGg < GG_COOLDOWN_MS || endless(minecraft)) {
			return;
		}
		System.out.println("[Mistly Client] Auto GG: " + what);
		said = true;
		lastGg = now;
		sendAt = now + GG_DELAY_MS;
	}

	private static boolean endless(final Minecraft minecraft) {
		String current = mode();
		return current != null && ENDLESS.matcher(current).find() || sidebarSays(minecraft, ENDLESS);
	}

	/** The sidebar (its title or its lines, team prefixes and suffixes) has those words. */
	private static boolean sidebarSays(final Minecraft minecraft, final Pattern words) {
		if (minecraft.theWorld == null) {
			return false;
		}
		Scoreboard scoreboard = minecraft.theWorld.getScoreboard();
		ScoreObjective sidebar = scoreboard.getObjectiveInDisplaySlot(1);
		if (sidebar == null) {
			return false;
		}
		if (words.matcher(flat(sidebar.getDisplayName())).find()) {
			return true;
		}
		for (ScorePlayerTeam team : scoreboard.getTeams()) {
			if (words.matcher(flat(team.getColorPrefix() + team.getColorSuffix())).find()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isPlayer(final Minecraft minecraft, final String name) {
		NetHandlerPlayClient listener = minecraft.getNetHandler();
		return listener != null && listener.getPlayerInfo(name) != null;
	}
}
