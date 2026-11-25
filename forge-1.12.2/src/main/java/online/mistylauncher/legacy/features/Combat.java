package online.mistylauncher.legacy.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import online.mistylauncher.legacy.Config;

/**
 * What happens when you hit, as in the Fabric versions:
 * <ul>
 * <li>Reach: the distance from your eyes to where you hit the player, in blocks.</li>
 * <li>Combo: players hit in a row without being hit back. A hit counts when it lands (the target
 * shows hurt shortly after); being hurt, or 3 seconds without landing a hit, ends it. The best
 * finished combo of the last minute is the record.</li>
 * <li>Kill sound: a "ting" when what you hit dies (its death, its health at 0, or a chat line
 * "Target … You" on minigame servers).</li>
 * <li>Hit particles: the critical and sharpness bursts spawned several times.</li>
 * </ul>
 */
public final class Combat {
	/** An attack that has not made the target hurt after this long missed. */
	private static final long LAND_WINDOW_MS = 600;
	private static final long COMBO_TIMEOUT_MS = 3000;
	private static final long RECORD_SHOWN_MS = 60_000;
	/** A death this long after your last hit still counts (burning, falling…). */
	private static final long KILL_WINDOW_MS = 5000;
	public static final int PARTICLES_MIN = 100;
	public static final int PARTICLES_MAX = 1000;
	public static final int PARTICLES_STEP = 50;

	private static double reach = -1;
	private static int combo;
	private static int record;
	private static long recordAt = -RECORD_SHOWN_MS;
	private static long lastLanded;
	private static EntityLivingBase pending;
	private static long pendingAt;
	private static int pendingHurt;
	private static int ownHurt;
	private static EntityLivingBase target;
	private static long hitAt;

	private Combat() {
	}

	/** The local player attacks {@code entity} (before the attack is sent). */
	public static void attacked(final Minecraft minecraft, final Entity entity) {
		EntityPlayerSP player = minecraft.player;
		if (entity instanceof EntityPlayer) {
			Vec3d eyes = player.getPositionEyes(1.0F);
			RayTraceResult aimed = minecraft.objectMouseOver;
			if (aimed != null && aimed.typeOfHit == RayTraceResult.Type.ENTITY && aimed.entityHit == entity && aimed.hitVec != null) {
				reach = eyes.distanceTo(aimed.hitVec);
			} else {
				reach = distanceToBox(eyes, entity.getEntityBoundingBox());
			}
			pending = (EntityLivingBase)entity;
			pendingAt = System.currentTimeMillis();
			pendingHurt = pending.hurtTime;
		}
		if (entity instanceof EntityLivingBase && entity != player && entity.isEntityAlive()) {
			target = (EntityLivingBase)entity;
			hitAt = System.currentTimeMillis();
		}
		particles(minecraft, player, entity);
	}

	/** The closest point of the box, from {@code eyes}. */
	static double distanceToBox(final Vec3d eyes, final AxisAlignedBB box) {
		double dx = Math.max(0, Math.max(box.minX - eyes.x, eyes.x - box.maxX));
		double dy = Math.max(0, Math.max(box.minY - eyes.y, eyes.y - box.maxY));
		double dz = Math.max(0, Math.max(box.minZ - eyes.z, eyes.z - box.maxZ));
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/** Each tick: hits landed, the combo's end, deaths of what you hit. */
	public static void tick(final Minecraft minecraft) {
		EntityPlayerSP player = minecraft.player;
		if (player == null) {
			combo = 0;
			pending = null;
			target = null;
			ownHurt = 0;
			return;
		}
		long now = System.currentTimeMillis();
		// hurt yourself (the hurt time starts again): the combo is over
		if (player.hurtTime > ownHurt) {
			end();
			pending = null;
		}
		ownHurt = player.hurtTime;
		EntityLivingBase hit = pending;
		if (hit != null) {
			if (hit.hurtTime > pendingHurt) {
				combo++;
				lastLanded = now;
				pending = null;
			} else if (now - pendingAt > LAND_WINDOW_MS || hit.isDead) {
				pending = null;
			} else {
				pendingHurt = hit.hurtTime;
			}
		}
		if (combo > 0 && now - lastLanded > COMBO_TIMEOUT_MS) {
			end();
		}
		EntityLivingBase last = killTarget();
		if (last != null && (last.getHealth() <= 0.0F || last.isDead && last.deathTime > 0)) {
			kill();
		}
	}

	/** A combo is over: it becomes the record when it beats the one shown (a minute after the last). */
	private static void end() {
		if (combo > 0 && (record() == 0 || combo > record)) {
			record = combo;
			recordAt = System.currentTimeMillis();
		}
		combo = 0;
	}

	public static int combo() {
		return combo;
	}

	/** The best finished combo of the last minute (0 when none). */
	public static int record() {
		return System.currentTimeMillis() - recordAt < RECORD_SHOWN_MS ? record : 0;
	}

	/** The last reach, negative before the first hit. */
	public static double reach() {
		return reach;
	}

	// ── kill sound ──

	private static EntityLivingBase killTarget() {
		if (target != null && System.currentTimeMillis() - hitAt > KILL_WINDOW_MS) {
			target = null;
		}
		return target;
	}

	/** A chat line: "Target … You" (victim first, as death messages are) means you killed it. */
	public static void chat(final String text) {
		EntityLivingBase last = killTarget();
		EntityPlayerSP self = Minecraft.getMinecraft().player;
		if (!(last instanceof EntityPlayer) || self == null || self.getHealth() <= 0.0F) {
			return;
		}
		int victim = text.indexOf(last.getName());
		int killer = text.indexOf(self.getName());
		if (victim >= 0 && killer > victim) {
			kill();
		}
	}

	/** What you hit died: the ting, for players and for mobs as chosen behind the switch's gear. */
	private static void kill() {
		EntityLivingBase dead = target;
		target = null;
		Config config = Config.get();
		if (config.killSound && (dead instanceof EntityPlayer ? config.killSoundPlayers : config.killSoundMobs)) {
			ting();
		}
	}

	/** The ting: the experience orb sound, a little high. */
	public static void ting() {
		Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(net.minecraft.init.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5F));
	}

	// ── hit particles ──

	public static int particlePercent() {
		return Math.max(PARTICLES_MIN, Math.min(PARTICLES_MAX, Config.get().particleMultiplier));
	}

	/**
	 * The bursts the game makes on this hit (a critical hit, a sharpness hit), made again: from ×1
	 * (100 %) to ×10; a share like 250 % gives 2 or 3 bursts, 3 half the time.
	 */
	private static void particles(final Minecraft minecraft, final EntityPlayerSP player, final Entity entity) {
		Config config = Config.get();
		if (!config.particles || !(entity instanceof EntityLivingBase)) {
			return;
		}
		// as the game decides them (EntityPlayer.attackTargetEntityWithCurrentItem)
		boolean critical = player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater()
			&& !player.isPotionActive(net.minecraft.init.MobEffects.BLINDNESS) && player.getRidingEntity() == null;
		boolean sharp = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), ((EntityLivingBase)entity).getCreatureAttribute()) > 0.0F;
		if (!critical && !sharp) {
			return;
		}
		float times = particlePercent() / 100.0F;
		int extra = (int)times - 1 + (Math.random() < times - (int)times ? 1 : 0);
		for (int i = 0; i < extra; i++) {
			if (critical) {
				minecraft.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT);
			}
			if (sharp) {
				minecraft.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT_MAGIC);
			}
		}
	}
}
