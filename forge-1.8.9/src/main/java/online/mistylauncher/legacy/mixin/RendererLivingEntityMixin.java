package online.mistylauncher.legacy.mixin;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import online.mistylauncher.legacy.Events;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The names above the players: the Mistly ones drawn with their cloud instead. */
@Mixin(RendererLivingEntity.class)
abstract class RendererLivingEntityMixin {
	@Inject(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At("HEAD"), cancellable = true)
	private void misty$name(final EntityLivingBase entity, final double x, final double y, final double z, final CallbackInfo ci) {
		if (Events.nameTag(entity, ((Render<?>)(Object)this).getRenderManager(), x, y, z)) {
			ci.cancel();
		}
	}
}
