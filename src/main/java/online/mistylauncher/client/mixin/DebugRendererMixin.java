package online.mistylauncher.client.mixin;

//? if >=1.21.11 {
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import online.mistylauncher.client.features.DamageIndicators;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Damage indicators: drawn in the world with the game's own texts, while it collects them each frame. */
@Mixin(DebugRenderer.class)
abstract class DebugRendererMixin {
	@Inject(method = "emitGizmos(Lnet/minecraft/client/renderer/culling/Frustum;DDDF)V", at = @At("TAIL"))
	private void misty$damage(final Frustum frustum, final double x, final double y, final double z, final float partialTicks, final CallbackInfo ci) {
		DamageIndicators.emit();
	}
}
//?} else {
/*// no gizmos before 1.21.11: nothing to hook (the option is hidden)
@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.renderer.debug.DebugRenderer.class)
abstract class DebugRendererMixin {
}
*///?}
