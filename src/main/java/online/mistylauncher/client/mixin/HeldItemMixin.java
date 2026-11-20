package online.mistylauncher.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import online.mistylauncher.client.features.PvpVisuals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Low shield and short swords: the item in hand in first person, moved just before it is drawn
 * (the game pops the hand's pose right after, so nothing else moves).
 */
//? if >=26.3 {
@Mixin(net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer.class)
abstract class HeldItemMixin {
	@Inject(method = "submitArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
	private void misty$heldItem(
		final net.minecraft.client.renderer.state.level.PlayerRenderState playerState, final net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState state,
		final float partialTicks, final float xRot, final InteractionHand hand, final float attack, final ItemStack itemStack, final float inverseArmHeight,
		final PoseStack poseStack, final net.minecraft.client.renderer.SubmitNodeCollector collector, final int lightCoords, final CallbackInfo ci
	) {
		PvpVisuals.heldItem(itemStack, poseStack);
	}
}
//?} else if >=1.21.9 {
/*@Mixin(net.minecraft.client.renderer.ItemInHandRenderer.class)
abstract class HeldItemMixin {
	// "submit" since 26.2
	@Inject(method = {"renderArmWithItem", "submitArmWithItem"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"))
	private void misty$heldItem(
		final net.minecraft.client.player.AbstractClientPlayer player, final float partialTicks, final float xRot, final InteractionHand hand, final float attack,
		final ItemStack itemStack, final float inverseArmHeight, final PoseStack poseStack, final net.minecraft.client.renderer.SubmitNodeCollector collector, final int lightCoords, final CallbackInfo ci
	) {
		PvpVisuals.heldItem(itemStack, poseStack);
	}
}
*///?} else {
/*@Mixin(net.minecraft.client.renderer.ItemInHandRenderer.class)
abstract class HeldItemMixin {
	@Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
	private void misty$heldItem(
		final net.minecraft.client.player.AbstractClientPlayer player, final float partialTicks, final float xRot, final InteractionHand hand, final float attack,
		final ItemStack itemStack, final float inverseArmHeight, final PoseStack poseStack, final net.minecraft.client.renderer.MultiBufferSource buffer, final int lightCoords, final CallbackInfo ci
	) {
		PvpVisuals.heldItem(itemStack, poseStack);
	}
}
*///?}
