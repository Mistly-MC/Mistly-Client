package online.mistylauncher.client.features;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.config.MistyConfig;

/**
 * Particle multiplier: the hit particles (critical hits and sharpness) are spawned several times,
 * from ×1 (100 %, the game's) to ×10 (1000 %). A share like 250 % gives 2 or 3 bursts, 3 half the
 * time.
 */
public final class HitParticles {
	public static final int MIN = 100;
	public static final int MAX = 1000;
	public static final int STEP = 50;

	private static final RandomSource RANDOM = RandomSource.create();
	/** Inside our own extra bursts: they are not multiplied again. */
	private static boolean spawning;

	private HitParticles() {
	}

	public static int percent(final MistyConfig config) {
		return Mth.clamp(config.particleMultiplier, MIN, MAX);
	}

	/** The game makes one burst of {@code particle} on {@code entity}: adds the extra ones. */
	public static void burst(final ParticleEngine engine, final Entity entity, final ParticleOptions particle) {
		MistyConfig config = MistyClient.config();
		if (spawning || !config.particles || particle != ParticleTypes.CRIT && particle != ParticleTypes.ENCHANTED_HIT) {
			return;
		}
		float times = percent(config) / 100.0F;
		int extra = (int)times - 1 + (RANDOM.nextFloat() < times - (int)times ? 1 : 0);
		spawning = true;
		try {
			for (int i = 0; i < extra; i++) {
				engine.createTrackingEmitter(entity, particle);
			}
		} finally {
			spawning = false;
		}
	}
}
