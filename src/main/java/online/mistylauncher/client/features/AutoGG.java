package online.mistylauncher.client.features;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import online.mistylauncher.client.MistyClient;

/**
 * Auto GG on CubeCraft: "gg" in the chat once a game ends, in every mode but FFA and Parkour
 * (they never end). CubeCraft ends every game, won or lost, with the same box in the chat:
 * "Thank you for playing Lucky Islands" (in English whatever the language), which names the mode.
 * Other wordings ("… won the game!", "VICTORY") are kept as a fallback. What set it off goes to
 * the log ("Auto GG: …").
 */
public final class AutoGG {
	/** The end box of CubeCraft: "Thank you for playing <mode>". */
	private static final Pattern THANKS = Pattern.compile("(?im)^\\s*(?:thank you|thanks) for playing\\s+(.+?)\\s*$|^\\s*merci d.avoir jou\u00e9 (?:\u00e0 |a )?(.+?)\\s*$");
	/** Other system chat lines that end a game. */
	private static final Pattern CHAT = Pattern.compile(
		"(?i)(?:\\b(?:won|wins|win) the (?:game|match|round)\\b|\\b(?:is|are) the winners?\\b|^\\W*winners?\\W*[:!]|^\\W*(?:winning team|winner)\\b|\\bgame over\\b|\\bgame has ended\\b)");
	/** Titles that end a game (big text in the middle of the screen). */
	private static final Pattern TITLE = Pattern.compile("(?i)^\\W*(?:victory|you won|you win|winner|game over|defeat|draw|game ended)\\W*$");
	/** Modes that never end: no gg there. Read on the sidebar. */
	private static final Pattern NO_GG = Pattern.compile("(?i)\\b(?:ffa|free ?for ?all|parkour|parcours|sky ?block)\\b");
	/** A player talking ("Name: …", "Name » …") before the words: not the server. */
	private static final Pattern SPEAKER = Pattern.compile("^[^:»>]{1,48}(?::|»|>>) ");

	private static final long DELAY_MS = 1500;
	/** One gg per game: the end messages often come several at once. */
	private static final long COOLDOWN_MS = 15_000;

	private static long lastGg = -COOLDOWN_MS;
	private static long sendAt = -1;

	private AutoGG() {
	}

	/** A system message from the server (not the action bar). */
	public static void chat(final Component message) {
		String text = message.getString().strip();
		Matcher thanks = THANKS.matcher(text);
		if (thanks.find()) {
			String mode = thanks.group(1) != null ? thanks.group(1) : thanks.group(2);
			if (!NO_GG.matcher(mode).find()) {
				trigger(text);
			}
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
		if (!MistyClient.config().autoGG || !onCubeCraft(minecraft) || now - lastGg < COOLDOWN_MS || neverEnds(minecraft)) {
			return;
		}
		MistyClient.LOG.info("Auto GG: {}", what);
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

	private static boolean onCubeCraft(final Minecraft minecraft) {
		ServerData server = minecraft.getCurrentServer();
		return server != null && server.ip.toLowerCase(Locale.ROOT).contains("cubecraft");
	}

	/** FFA or Parkour, read on the sidebar (its title and its lines, which are team prefixes). */
	private static boolean neverEnds(final Minecraft minecraft) {
		if (minecraft.level == null) {
			return false;
		}
		Scoreboard scoreboard = minecraft.level.getScoreboard();
		Objective sidebar = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
		if (sidebar == null) {
			return false;
		}
		if (NO_GG.matcher(sidebar.getDisplayName().getString()).find()) {
			return true;
		}
		for (PlayerTeam team : scoreboard.getPlayerTeams()) {
			if (NO_GG.matcher(team.getPlayerPrefix().getString() + team.getPlayerSuffix().getString()).find()) {
				return true;
			}
		}
		return false;
	}
}
