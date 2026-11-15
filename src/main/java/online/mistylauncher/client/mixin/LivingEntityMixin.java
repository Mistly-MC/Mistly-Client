package online.mistylauncher.client.mixin;

import net.minecraft.world.entity.LivingEntity;
import online.mistylauncher.client.features.KillSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Kill sound: the death event the server sends when an entity dies. */
@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
	@Inject(method = "handleEntityEvent", at = @At("HEAD"))
	private void misty$death(final byte id, final CallbackInfo ci) {
		// 3: EntityEvent.DEATH (only the client gets entity events)
		if (id == 3) {
			KillSound.died((LivingEntity)(Object)this);
		}
	}
}
