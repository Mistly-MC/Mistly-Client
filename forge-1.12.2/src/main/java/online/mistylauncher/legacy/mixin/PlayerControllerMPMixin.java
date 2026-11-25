package online.mistylauncher.legacy.mixin;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import online.mistylauncher.legacy.Events;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The local player hits something: reach, combo, kill sound, hit particles. */
@Mixin(PlayerControllerMP.class)
abstract class PlayerControllerMPMixin {
	@Inject(method = "attackEntity", at = @At("HEAD"))
	private void misty$attack(final EntityPlayer player, final Entity target, final CallbackInfo ci) {
		Events.attack(player, target);
	}
}
