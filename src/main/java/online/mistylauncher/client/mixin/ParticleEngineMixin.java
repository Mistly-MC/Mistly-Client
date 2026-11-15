package online.mistylauncher.client.mixin;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import online.mistylauncher.client.features.HitParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Particle multiplier: each burst of hit particles on an entity (crit, sharpness). */
@Mixin(ParticleEngine.class)
abstract class ParticleEngineMixin {
	@Inject(method = "createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;)V", at = @At("HEAD"))
	private void misty$multiply(final Entity entity, final ParticleOptions particle, final CallbackInfo ci) {
		HitParticles.burst((ParticleEngine)(Object)this, entity, particle);
	}
}
