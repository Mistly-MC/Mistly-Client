package online.mistylauncher.legacy.net;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.features.Outfit;

/**
 * Who plays with Mistly among the players around, and what they wear. Filled by
 * {@link MistyNetwork} from its own thread, read by the game thread.
 */
public final class MistyPlayers {
	/** An answer of the Mistly network about one player ({@code misty} false: not a Mistly player). */
	static final class Entry {
		final boolean misty;
		final String back;
		final String hat;
		final long fetchedAt;

		Entry(final boolean misty, final String back, final String hat, final long fetchedAt) {
			this.misty = misty;
			this.back = back;
			this.hat = hat;
			this.fetchedAt = fetchedAt;
		}
	}

	private static final Map<UUID, Entry> KNOWN = new ConcurrentHashMap<UUID, Entry>();

	private MistyPlayers() {
	}

	private static boolean isSelf(final UUID id) {
		Minecraft minecraft = Minecraft.getMinecraft();
		// the session's account, or the player of this world (an offline name has its own id)
		return id.equals(minecraft.getSession().getProfile().getId()) || minecraft.player != null && id.equals(minecraft.player.getUniqueID());
	}

	/** True for yourself and for the players the Mistly network reports as online with Mistly. */
	public static boolean isMisty(final UUID id) {
		if (id == null) {
			return false;
		}
		if (isSelf(id)) {
			return true;
		}
		Entry entry = KNOWN.get(id);
		return entry != null && entry.misty;
	}

	/** What a player wears on the back (yourself: what the launcher or the wardrobe chose). */
	public static String back(final UUID id) {
		if (isSelf(id)) {
			return Outfit.back();
		}
		Entry entry = KNOWN.get(id);
		return entry != null && entry.misty ? entry.back : Outfit.NONE;
	}

	/** What a player wears on the head. */
	public static String hat(final UUID id) {
		if (isSelf(id)) {
			return Outfit.hat();
		}
		Entry entry = KNOWN.get(id);
		return entry != null && entry.misty ? entry.hat : Outfit.NONE;
	}

	static Entry get(final UUID id) {
		return KNOWN.get(id);
	}

	static void put(final UUID id, final boolean misty, final String back, final String hat) {
		KNOWN.put(id, new Entry(misty, back, hat, System.currentTimeMillis()));
	}

	static void clear() {
		KNOWN.clear();
	}
}
