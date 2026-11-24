package online.mistylauncher.legacy.features;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/**
 * Reach and combo. Reach: the distance from your eyes to where you hit the player (the point the
 * crosshair is on), in blocks. Combo: the players you hit in a row; being hit, or 3 seconds
 * without hitting, ends it.
 */
public final class Combat {
	private static final long COMBO_TIMEOUT_MS = 3000;

	private static int combo;
	private static long lastHit;
	private static double reach = -1;
	/** The local player's hurt time last tick, to see a new hit taken. */
	private static int hurtBefore;

	private Combat() {
	}

	public static void attacked(final Minecraft minecraft, final Entity target) {
		long now = System.currentTimeMillis();
		combo = now - lastHit < COMBO_TIMEOUT_MS ? combo + 1 : 1;
		lastHit = now;
		Vec3 eyes = minecraft.thePlayer.getPositionEyes(1.0F);
		MovingObjectPosition aimed = minecraft.objectMouseOver;
		if (aimed != null && aimed.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && aimed.entityHit == target
			&& aimed.hitVec != null) {
			reach = eyes.distanceTo(aimed.hitVec);
		} else {
			reach = distanceToBox(eyes, target.getEntityBoundingBox());
		}
	}

	/** The closest point of the box, from {@code eyes}. */
	static double distanceToBox(final Vec3 eyes, final AxisAlignedBB box) {
		double dx = Math.max(0, Math.max(box.minX - eyes.xCoord, eyes.xCoord - box.maxX));
		double dy = Math.max(0, Math.max(box.minY - eyes.yCoord, eyes.yCoord - box.maxY));
		double dz = Math.max(0, Math.max(box.minZ - eyes.zCoord, eyes.zCoord - box.maxZ));
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/** Each tick: a hit taken (the hurt time starts again) ends the combo. */
	public static void tick(final Minecraft minecraft) {
		if (minecraft.thePlayer == null) {
			combo = 0;
			hurtBefore = 0;
			return;
		}
		int hurt = minecraft.thePlayer.hurtTime;
		if (hurt > hurtBefore) {
			combo = 0;
		}
		hurtBefore = hurt;
	}

	/** The current combo, 0 once it timed out. */
	public static int combo() {
		return System.currentTimeMillis() - lastHit < COMBO_TIMEOUT_MS ? combo : 0;
	}

	/** The last reach, negative before the first hit. */
	public static double reach() {
		return reach;
	}
}
