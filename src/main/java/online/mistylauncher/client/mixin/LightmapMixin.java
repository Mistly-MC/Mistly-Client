package online.mistylauncher.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import online.mistylauncher.client.MistyClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fullbright: the brightness the lightmap reads is pushed far above the game's maximum, so caves
 * and nights are lit. The option itself is not changed (nothing to restore).
 */
//? if >=26.1 {
@Mixin(net.minecraft.client.renderer.LightmapRenderStateExtractor.class)
//?} else {
/*@Mixin(net.minecraft.client.renderer.LightTexture.class)
*///?}
abstract class LightmapMixin {
	// The brightness (gamma) option: the first Double.floatValue() of extract() since 26.1, the
	// second of updateLightTexture() before (the first is the darkness effect)
	//? if >=26.1 {
	@ModifyExpressionValue(method = "extract", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 0))
	//?} else if >=1.19 {
	/*
	@ModifyExpressionValue(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
	*///?}
	//? if >=1.19 {
	private float misty$fullbright(final float gamma) {
		return MistyClient.config().fullbright ? 15.0F : gamma;
	}
	//?} else {
	/*// before 1.19 the option was a field of the options, a double
	@ModifyExpressionValue(method = "updateLightTexture", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;gamma:D"))
	private double misty$fullbright(final double gamma) {
		return MistyClient.config().fullbright ? 15.0 : gamma;
	}
	*///?}
}
