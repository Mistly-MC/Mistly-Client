package online.mistylauncher.client.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import online.mistylauncher.client.MistyClient;
import org.jspecify.annotations.Nullable;

/**
 * Kill sound: a "ting" when a player you hit dies. The server tells every client when a player
 * dies (the death event of the entity); it counts as your kill when you hit that player in the
 * last seconds. Servers that never let players die (instant respawn in some minigames) send no
 * death, so no ting there.
 */
public final class KillSound {
	/** A death this long after your last hit on that player still counts (burning, falling…). */
	private static final long KILL_WINDOW_MS = 5000;

	private static @Nullable Player target;
	private static long hitAt;

	private KillSound() {
	}

	/** The local player attacks {@code entity} (called before the attack is sent). */
	public static void attacked(final Player player, final Entity entity) {
		if (entity instanceof Player hit && entity != player) {
			target = hit;
			hitAt = Util.getMillis();
		}
	}

	/** An entity of the world got the death event from the server. */
	public static void died(final LivingEntity entity) {
		Player last = target;
		if (last == null || entity != last || Util.getMillis() - hitAt > KILL_WINDOW_MS) {
			return;
		}
		target = null;
		if (MistyClient.config().killSound) {
			ting();
		}
	}

	/** The ting: the experience orb sound, a little high. */
	public static void ting() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.5F, 1.0F));
	}
}
