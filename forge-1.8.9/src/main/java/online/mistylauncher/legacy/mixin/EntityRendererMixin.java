package online.mistylauncher.legacy.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import online.mistylauncher.legacy.features.WorldLines;
import online.mistylauncher.legacy.features.Zoom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The view: the zoom's field of view, and hitboxes and chunk borders once the world is drawn. */
@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin {
	@Inject(method = "getFOVModifier", at = @At("RETURN"), cancellable = true)
	private void misty$zoom(final float partialTicks, final boolean useFovSetting, final CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(Zoom.fov(cir.getReturnValueF()));
	}

	// the world drawn, before the hand
	@Inject(method = "renderWorldPass", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
		args = "ldc=hand"))
	private void misty$lines(final int pass, final float partialTicks, final long finishTimeNano, final CallbackInfo ci) {
		WorldLines.draw(partialTicks);
	}
}
