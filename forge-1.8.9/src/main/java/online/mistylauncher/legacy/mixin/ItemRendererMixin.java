package online.mistylauncher.legacy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import online.mistylauncher.legacy.features.Visuals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Low fire: the flames over the screen drawn lower. */
@Mixin(ItemRenderer.class)
abstract class ItemRendererMixin {
	@Inject(method = "renderFireInFirstPerson", at = @At("HEAD"), cancellable = true)
	private void misty$fire(final float partialTicks, final CallbackInfo ci) {
		if (Visuals.fire(Minecraft.getMinecraft())) {
			ci.cancel();
		}
	}
}
