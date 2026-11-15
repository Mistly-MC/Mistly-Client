package online.mistylauncher.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import online.mistylauncher.client.features.PvpVisuals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Low fire: the flames over the screen when burning, drawn lower. */
@Mixin(ScreenEffectRenderer.class)
abstract class ScreenEffectRendererMixin {
	//? if >=26.2 {
	@Inject(method = "submitFire", at = @At("HEAD"))
	private static void misty$lowFire(final PoseStack pose, final net.minecraft.client.renderer.SubmitNodeCollector collector, final TextureAtlasSprite sprite, final CallbackInfo ci) {
	//?} else {
	/*@Inject(method = "renderFire", at = @At("HEAD"))
	private static void misty$lowFire(final PoseStack pose, final net.minecraft.client.renderer.MultiBufferSource buffers, final TextureAtlasSprite sprite, final CallbackInfo ci) {
	*///?}
		pose.pushPose();
		PvpVisuals.fire(pose);
	}

	//? if >=26.2 {
	@Inject(method = "submitFire", at = @At("RETURN"))
	private static void misty$lowFireEnd(final PoseStack pose, final net.minecraft.client.renderer.SubmitNodeCollector collector, final TextureAtlasSprite sprite, final CallbackInfo ci) {
	//?} else {
	/*@Inject(method = "renderFire", at = @At("RETURN"))
	private static void misty$lowFireEnd(final PoseStack pose, final net.minecraft.client.renderer.MultiBufferSource buffers, final TextureAtlasSprite sprite, final CallbackInfo ci) {
	*///?}
		pose.popPose();
	}
}
