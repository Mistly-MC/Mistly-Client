package online.mistylauncher.client.mixin;

//? if >=1.21.9 {
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
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.config.MistyConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Dropped items lying flat (physics) or as flat pictures turned to you (2D), before 1.21.9
@Mixin(ItemEntityRenderer.class)
abstract class ItemEntityRendererMixin {
	@Shadow
	@Final
	private RandomSource random;
	@Shadow
	@Final
	private ItemRenderer itemRenderer;

	@Inject(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At("HEAD"), cancellable = true)
	private void misty$items(final ItemEntity entity, final float yaw, final float partialTicks, final PoseStack poseStack, final MultiBufferSource buffer,
		final int light, final CallbackInfo ci) {
		MistyConfig config = MistyClient.config();
		ItemStack stack = entity.getItem();
		if (!config.items2D && !config.itemPhysics || stack.isEmpty()) {
			return;
		}
		this.random.setSeed(ItemEntityRenderer.getSeedForItemStack(stack));
		BakedModel model = this.itemRenderer.getModel(stack, entity.level(), null, entity.getId());
		boolean block = model.isGui3d();
		poseStack.pushPose();
		if (config.items2D) {
			// standing on the ground, turned to the camera, flat (the ground transform lifts a flat item by its half)
			poseStack.translate(0.0F, block ? 0.0F : 0.13F, 0.0F);
			poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
			poseStack.scale(1.0F, 1.0F, 0.02F);
		} else {
			// lying on its back, its lowest point on the ground
			poseStack.translate(0.0F, block ? 0.125F : 0.02F, 0.0F);
			poseStack.mulPose(Axis.YP.rotationDegrees(entity.bobOffs * 57.29578F));
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		}
		ItemEntityRenderer.renderMultipleFromCount(this.itemRenderer, poseStack, buffer, light, stack, model, block, this.random);
		poseStack.popPose();
		ci.cancel();
	}
}
*///?}
