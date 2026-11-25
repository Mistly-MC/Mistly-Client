package online.mistylauncher.client.mixin;

//? if >=1.21.2 {
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.renderer.GameRenderer;
import online.mistylauncher.client.features.MotionBlur;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Motion blur: once the world is drawn (and the game's own post effects applied), before the HUD. */
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
	@Shadow
	@Final
	private CrossFrameResourcePool resourcePool;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;endFrame()V"))
	private void misty$motionBlur(final CallbackInfo ci) {
		MotionBlur.apply(this.resourcePool);
	}
}
//?} else if >=1.21 {
/*import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import online.mistylauncher.client.features.MotionBlur;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Motion blur before 1.21.2: once the world is drawn and the game's own post effect applied,
// before the HUD (the game binds its main target again for the HUD right after)
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;bindWrite(Z)V", ordinal = 0))
	private void misty$motionBlur(final DeltaTracker deltaTracker, final boolean advance, final CallbackInfo ci) {
		MotionBlur.apply(deltaTracker.getGameTimeDeltaTicks());
	}
}
*///?} else {
/*import net.minecraft.client.renderer.GameRenderer;
import online.mistylauncher.client.features.MotionBlur;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Motion blur before 1.21 (the renderer gets the frame's partial tick): as above
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;bindWrite(Z)V", ordinal = 0))
	private void misty$motionBlur(final float partialTick, final long nanoTime, final boolean renderLevel, final CallbackInfo ci) {
		MotionBlur.apply(partialTick);
	}
}
*///?}
