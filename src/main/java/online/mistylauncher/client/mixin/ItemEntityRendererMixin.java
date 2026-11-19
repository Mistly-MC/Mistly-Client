package online.mistylauncher.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
//? if >=26.1 {
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?} else {
/*import net.minecraft.client.renderer.state.CameraRenderState;
*///?}
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.config.MistyConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dropped items, two ways instead of the game's floating and spinning:
 * <ul>
 * <li>physics: lying flat on the ground, still, each turned its own way;</li>
 * <li>2D: a flat picture always turned to you, as in old versions.</li>
 * </ul>
 */
@Mixin(ItemEntityRenderer.class)
abstract class ItemEntityRendererMixin {
	@Shadow
	@Final
	private RandomSource random;

	@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;"
		//? if >=26.1 {
		+ "Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		//?} else {
		/*+ "Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
		*///?}
		at = @At("HEAD"), cancellable = true)
	private void misty$items(final ItemEntityRenderState state, final PoseStack poseStack, final SubmitNodeCollector collector, final CameraRenderState camera,
		final CallbackInfo ci) {
		MistyConfig config = MistyClient.config();
		if (!config.items2D && !config.itemPhysics || state.item.isEmpty()) {
			return;
		}
		AABB box = state.item.getModelBoundingBox();
		poseStack.pushPose();
		if (config.items2D) {
			// standing on the ground, turned to the camera, flat
			poseStack.translate(0.0F, -(float)box.minY + 0.0625F, 0.0F);
			rotate(poseStack, camera.orientation);
			poseStack.scale(1.0F, 1.0F, 0.02F);
		} else {
			// lying on its back: its depth becomes its height, its lowest point on the ground
			poseStack.translate(0.0F, (float)box.maxZ + 0.01F, 0.0F);
			rotate(poseStack, Axis.YP.rotationDegrees(state.bobOffset * 57.29578F));
			rotate(poseStack, Axis.XP.rotationDegrees(90.0F));
		}
		ItemEntityRenderer.submitMultipleFromCount(poseStack, collector, state.lightCoords, state, this.random, box);
		poseStack.popPose();
		ci.cancel();
	}

	/** A rotation of the pose: "rotate" since 26.3, "mulPose" before. */
	private static void rotate(final PoseStack poseStack, final org.joml.Quaternionfc rotation) {
		//? if >=26.3 {
		poseStack.rotate(rotation);
		//?} else {
		/*poseStack.mulPose(rotation);
		*///?}
	}
}
