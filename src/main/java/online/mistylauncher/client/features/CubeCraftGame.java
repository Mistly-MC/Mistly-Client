package online.mistylauncher.client.features;

import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * The CubeCraft game being played, read from the server's chat, and the player's kills in it
 * (for the Kills module, Lucky Islands for now).
 *
 * <ul>
 * <li>A game's countdown names its mode: "Lucky Islands commence dans 5 secondes." (the mode's
 * name is never translated). It starts a new game: the kills go back to 0.</li>
 * <li>The end box names it too: "Thank you for playing Lucky Islands". The count stays shown
 * until the next game or until the player leaves the mode.</li>
 * <li>Back in the main lobby ("… has joined the lobby!", the welcome message) or on another
 * server: no mode any more.</li>
 * <li>A kill: a death message that names another player first and the player after it
 * ("Victim was slain by You.", "Victim died in the void while escaping You.", "… by You's pet
 * Slime."), as CubeCraft gives points for all of them.</li>
 * </ul>
 */
public final class CubeCraftGame {
	public static final String LUCKY_ISLANDS = "Lucky Islands";

	/** A countdown line: "<mode> commence dans 5 secondes." in any language (it ends with the unit). */
	private static final Pattern COUNTDOWN = Pattern.compile("(?iu)^.+\\b\\d+\\s+(?:seconde?s?|second[io]|segundos?|sekunden?|секунд\\p{L}*)\\.$");
	private static final Pattern THANKS = Pattern.compile("(?i)^\\s*(?:thank you|thanks) for playing\\s+(.+?)\\s*$");
	private static final Pattern LOBBY = Pattern.compile("(?i)has joined the lobby!?$|^bienvenue sur cubecraft|^welcome to cubecraft");
	/** A death message: the victim's name first, then the rest of the sentence. */
	private static final Pattern DEATH = Pattern.compile("^(\\w{3,16}) (\\p{L}.*)$");

	private static @Nullable String mode;
	private static int kills;
	private static @Nullable ClientPacketListener connection;

	private CubeCraftGame() {
	}

	/** The mode played (or waited for) right now, null when none. */
	public static @Nullable String mode() {
		return onCubeCraft(Minecraft.getInstance()) ? mode : null;
	}

	public static boolean playing(final String name) {
		return name.equals(mode());
	}

	/** The player's kills in the current game. */
	public static int kills() {
		return kills;
	}

	/** A system message from the server. */
	public static void chat(final Component message) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!onCubeCraft(minecraft)) {
			return;
		}
		String text = message.getString().strip();
		var thanks = THANKS.matcher(text);
		if (thanks.find()) {
			mode = name(thanks.group(1));
			return;
		}
		if (LOBBY.matcher(text).find()) {
			mode = null;
			kills = 0;
			return;
		}
		if (COUNTDOWN.matcher(text).matches()) {
			mode = text.startsWith(LUCKY_ISLANDS + " ") ? LUCKY_ISLANDS : "other";
			kills = 0;
			return;
		}
		if (mode != null && minecraft.player != null && text.indexOf(':') < 0) {
			var death = DEATH.matcher(text);
			String self = minecraft.player.getName().getString();
			if (death.matches() && !death.group(1).equals(self)
				&& Pattern.compile("(?<![\\w])" + Pattern.quote(self) + "(?![\\w])").matcher(death.group(2)).find()) {
				kills++;
			}
		}
	}

	/** Each client tick: another server (or none) forgets the game. */
	public static void tick(final Minecraft minecraft) {
		ClientPacketListener current = minecraft.getConnection();
		if (current != connection) {
			connection = current;
			mode = null;
			kills = 0;
		}
	}

	/** The end box's mode, as the countdown would name it. */
	private static String name(final String played) {
		return played.toLowerCase(Locale.ROOT).startsWith(LUCKY_ISLANDS.toLowerCase(Locale.ROOT)) ? LUCKY_ISLANDS : "other";
	}

	private static boolean onCubeCraft(final Minecraft minecraft) {
		ServerData server = minecraft.getCurrentServer();
		return server != null && server.ip.toLowerCase(Locale.ROOT).contains("cubecraft");
	}
}
