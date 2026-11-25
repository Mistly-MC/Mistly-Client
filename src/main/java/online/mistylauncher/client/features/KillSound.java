package online.mistylauncher.client.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.config.MistyConfig;
import org.jspecify.annotations.Nullable;

/**
 * Kill sound: a "ting" when something you hit dies (a player or a mob). Three ways to notice the
 * death, since servers differ:
 * <ul>
 * <li>the death event the server sends for the entity (vanilla);</li>
 * <li>its health reaching 0 (servers that skip the event);</li>
 * <li>a chat line naming it before you, like "Target was killed by You" (minigame servers that
 * never let players die: they teleport them and announce the kill in the chat).</li>
 * </ul>
 * It counts as your kill when you hit it in the last seconds.
 */
public final class KillSound {
	/** A death this long after your last hit still counts (burning, falling…). */
	private static final long KILL_WINDOW_MS = 5000;

	private static @Nullable LivingEntity target;
	private static long hitAt;

	private KillSound() {
	}

	/** The local player attacks {@code entity} (called before the attack is sent). */
	public static void attacked(final Player player, final Entity entity) {
		if (entity instanceof LivingEntity hit && entity != player && hit.isAlive()) {
			target = hit;
			hitAt = Util.getMillis();
		}
	}

	/** An entity of the world got the death event from the server. */
	public static void died(final LivingEntity entity) {
		if (entity == current()) {
			kill();
		}
	}

	/** Each client tick: the health of what you hit, synced by the server, reached 0. */
	public static void tick(final Minecraft minecraft) {
		LivingEntity last = current();
		if (last != null && last.getHealth() <= 0.0F) {
			kill();
		}
	}

	/** A chat line: "Target … You" (victim first, as death messages are) means you killed it. */
	public static void chat(final Component message) {
		LivingEntity last = current();
		Player self = Minecraft.getInstance().player;
		if (!(last instanceof Player) || self == null || self.isDeadOrDying()) {
			return;
		}
		String text = message.getString();
		int victim = text.indexOf(last.getName().getString());
		int killer = text.indexOf(self.getName().getString());
		if (victim >= 0 && killer > victim) {
			kill();
		}
	}

	/** What you hit, while a death still counts as yours; null otherwise. */
	private static @Nullable LivingEntity current() {
		if (target != null && Util.getMillis() - hitAt > KILL_WINDOW_MS) {
			target = null;
		}
		return target;
	}

	/** What you hit died: the ting, for players and for mobs as chosen behind the switch's gear. */
	private static void kill() {
		LivingEntity dead = target;
		target = null;
		MistyConfig config = MistyClient.config();
		if (config.killSound && (dead instanceof Player ? config.killSoundPlayers : config.killSoundMobs)) {
			ting();
		}
	}

	/** The ting: the experience orb sound, a little high. */
	public static void ting() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.5F, 1.0F));
	}
}
