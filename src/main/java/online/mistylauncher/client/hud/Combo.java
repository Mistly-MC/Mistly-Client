package online.mistylauncher.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * Combo of the Combo module: players hit in a row without being hit back.
 *
 * <p>Combat since 1.9 has an attack cooldown and invulnerability frames: clicking fast sends
 * attacks that do nothing. So an attack only counts when it lands, that is when the server shows
 * the target hurt (its hurt time starts again) shortly after the attack. Being hurt yourself ends
 * the combo, and so does a few seconds without landing a hit.
 */
public final class Combo {
	/** An attack that has not made the target hurt after this long missed (blocked, invulnerable…). */
	private static final long LAND_WINDOW_MS = 600;
	/** The combo ends after this long without a hit landing. */
	private static final long TIMEOUT_MS = 3000;

	/** The record line stays this long after the combo that set it ended. */
	private static final long RECORD_SHOWN_MS = 60_000;

	private static int combo;
	private static int record;
	private static long recordAt = -RECORD_SHOWN_MS;
	private static long lastLanded;
	private static @Nullable LivingEntity pending;
	private static long pendingAt;
	private static int pendingHurt;
	private static int ownHurt;

	private Combo() {
	}

	/** The local player attacks {@code target} (called before the attack is sent). */
	public static void attacked(final Player player, final Entity target) {
		if (target instanceof Player living && target != player) {
			pending = living;
			pendingAt = Util.getMillis();
			pendingHurt = ((LivingEntity)living).hurtTime;
		}
	}

	/** Game thread, every tick. */
	public static void tick(final Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player == null) {
			combo = 0;
			pending = null;
			ownHurt = 0;
			return;
		}
		long now = Util.getMillis();
		// hurt yourself (the hurt time starts again): the combo is over
		if (player.hurtTime > ownHurt) {
			end();
			pending = null;
		}
		ownHurt = player.hurtTime;

		LivingEntity target = pending;
		if (target != null) {
			if (target.hurtTime > pendingHurt) {
				combo++;
				lastLanded = now;
				pending = null;
			} else if (now - pendingAt > LAND_WINDOW_MS || target.isRemoved()) {
				pending = null;
			} else {
				pendingHurt = target.hurtTime;
			}
		}
		if (combo > 0 && now - lastLanded > TIMEOUT_MS) {
			end();
		}
	}

	/**
	 * A combo is over: it becomes the record when it beats the one shown, which stays one minute
	 * after the last combo that set it (none shown: any combo sets it).
	 */
	private static void end() {
		long now = Util.getMillis();
		if (combo > 0 && (record() == 0 || combo > record)) {
			record = combo;
			recordAt = now;
		}
		combo = 0;
	}

	/** The best finished combo of the last minute (0 when none: the record line goes). */
	public static int record() {
		return Util.getMillis() - recordAt < RECORD_SHOWN_MS ? record : 0;
	}

	public static int combo() {
		return combo;
	}
}
