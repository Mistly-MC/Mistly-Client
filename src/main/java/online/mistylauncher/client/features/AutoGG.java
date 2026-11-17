package online.mistylauncher.client.features;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import online.mistylauncher.client.MistyClient;

/**
 * Auto GG on CubeCraft: "gg" in the chat once a game ends, in every mode but FFA, Parkour and
 * SkyBlock (they never end). Read in real CubeCraft logs, a game's end shows in four ways, not
 * all of them every time (in a party the leader often takes the group to the next game before the
 * end box), so any of them counts, once per game:
 * <ul>
 * <li>the end box: "Thank you for playing BedWars" (in English whatever the language);</li>
 * <li>the summary each player gets: a dashed line, then "● Vous avez atteint la 8e place.",
 * "● Durée du jeu: …" (BedWars has no place line, so any "●" line right after the dashes);</li>
 * <li>the player's own elimination: "You has been eliminated!";</li>
 * <li>the winners: "Light Blue won the game!" (and titles like "VICTORY" on other servers' wording).</li>
 * </ul>
 * What set it off goes to the log ("Auto GG: …").
 */
public final class AutoGG {
	private static final Pattern THANKS = Pattern.compile("(?i)^(?:thank you|thanks) for playing\\s+(.+?)$");
	/** The dashed line that opens the summary. */
	private static final Pattern DASHES = Pattern.compile("^-{10,}");
	/** Other system chat lines that end a game. */
	private static final Pattern CHAT = Pattern.compile(
		"(?i)(?:\\b(?:won|wins|win) the (?:game|match|round)\\b|\\b(?:is|are) the winners?\\b|^\\W*winners?\\W*[:!]|^\\W*(?:winning team|winner)\\b|\\bgame over\\b|\\bgame has ended\\b)");
	/** Titles that end a game (big text in the middle of the screen). */
	private static final Pattern TITLE = Pattern.compile("(?i)^\\W*(?:victory|you won|you win|winner|game over|defeat|draw|game ended)\\W*$");
	/** A player talking ("Name: …", "Name » …") before the words: not the server. */
	private static final Pattern SPEAKER = Pattern.compile("^[^:»>]{1,48}(?::|»|>>) ");

	private static final long DELAY_MS = 1500;
	/** One gg per game: the end messages come several at once. */
	private static final long COOLDOWN_MS = 15_000;

	private static long lastGg = -COOLDOWN_MS;
	private static long sendAt = -1;
	/** The gg of this game is said (an elimination and then "X won the game!" are one game). */
	private static boolean said;
	/** The message before this one (the summary's "●" lines follow a dashed line). */
	private static String previous = "";

	private AutoGG() {
	}

	/** A new game starts (its countdown): it will get its own gg. */
	public static void newGame() {
		said = false;
	}

	/** A system message from the server (not the action bar). */
	public static void chat(final Component message) {
		String text = CubeCraftGame.flat(message);
		String before = previous;
		previous = text;
		Matcher thanks = THANKS.matcher(text);
		if (thanks.find()) {
			if (!CubeCraftGame.ENDLESS.matcher(thanks.group(1)).find()) {
				trigger(text);
			}
			return;
		}
		if (text.startsWith("●") && DASHES.matcher(before).find()) {
			trigger(text);
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null && text.equalsIgnoreCase(minecraft.player.getName().getString() + " has been eliminated!")) {
			trigger(text);
			return;
		}
		Matcher matcher = CHAT.matcher(text);
		if (matcher.find() && !SPEAKER.matcher(text.substring(0, matcher.start())).find()) {
			trigger(text);
		}
	}

	/** A title from the server. */
	public static void title(final Component title) {
		String text = title.getString().strip();
		if (TITLE.matcher(text).matches()) {
			trigger(text);
		}
	}

	private static void trigger(final String what) {
		Minecraft minecraft = Minecraft.getInstance();
		long now = Util.getMillis();
		if (!MistyClient.config().autoGG || !CubeCraftGame.onCubeCraft(minecraft) || said || now - lastGg < COOLDOWN_MS || endless(minecraft)) {
			return;
		}
		MistyClient.LOG.info("Auto GG: {}", what);
		said = true;
		lastGg = now;
		sendAt = now + DELAY_MS;
	}

	/** Each client tick: sends the gg once its short delay is over. */
	public static void tick(final Minecraft minecraft) {
		if (sendAt < 0 || Util.getMillis() < sendAt) {
			return;
		}
		sendAt = -1;
		ClientPacketListener connection = minecraft.getConnection();
		if (connection != null && minecraft.player != null) {
			connection.sendChat("gg");
		}
	}

	/** FFA, Parkour or SkyBlock: by the mode's name, or by the sidebar. */
	private static boolean endless(final Minecraft minecraft) {
		String mode = CubeCraftGame.mode();
		return mode != null && CubeCraftGame.ENDLESS.matcher(mode).find() || CubeCraftGame.sidebarSays(minecraft, CubeCraftGame.ENDLESS);
	}
}
