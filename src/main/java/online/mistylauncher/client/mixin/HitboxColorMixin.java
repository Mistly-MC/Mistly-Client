package online.mistylauncher.client.mixin;

import online.mistylauncher.client.features.Hitboxes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >=1.21.9 {
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Hitbox color: the color of an entity's box (white in the game), chosen behind the hitboxes' gear. */
@Mixin(EntityHitboxDebugRenderer.class)
abstract class HitboxColorMixin {
	@ModifyVariable(method = "showHitboxes", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
	private int misty$color(final int color, final Entity entity, final float partialTicks, final boolean serverEntity) {
		// the server's copy (a development view) keeps its green
		return serverEntity ? color : Hitboxes.color();
	}
}
//?} else if >=1.21 {
/*import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// Hitbox color before 1.21.9: the red, green and blue the box is drawn with (white in the game)
@Mixin(EntityRenderDispatcher.class)
abstract class HitboxColorMixin {
	private static final String BOX = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderHitbox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;FFFF)V";

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = BOX), index = 4)
	private float misty$red(final float red) {
		return (Hitboxes.color() >> 16 & 0xFF) / 255.0F;
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = BOX), index = 5)
	private float misty$green(final float green) {
		return (Hitboxes.color() >> 8 & 0xFF) / 255.0F;
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = BOX), index = 6)
	private float misty$blue(final float blue) {
		return (Hitboxes.color() & 0xFF) / 255.0F;
	}
}
*///?} else {
/*import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// Hitbox color before 1.21: the box's colour is set inside renderHitbox, where it draws the line box
@Mixin(EntityRenderDispatcher.class)
abstract class HitboxColorMixin {
	private static final String BOX = "Lnet/minecraft/client/renderer/LevelRenderer;renderLineBox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/AABB;FFFF)V";

	@ModifyArg(method = "renderHitbox", at = @At(value = "INVOKE", target = BOX, ordinal = 0), index = 3)
	private static float misty$red(final float red) {
		return (Hitboxes.color() >> 16 & 0xFF) / 255.0F;
	}

	@ModifyArg(method = "renderHitbox", at = @At(value = "INVOKE", target = BOX, ordinal = 0), index = 4)
	private static float misty$green(final float green) {
		return (Hitboxes.color() >> 8 & 0xFF) / 255.0F;
	}

	@ModifyArg(method = "renderHitbox", at = @At(value = "INVOKE", target = BOX, ordinal = 0), index = 5)
	private static float misty$blue(final float blue) {
		return (Hitboxes.color() & 0xFF) / 255.0F;
	}
}
*///?}
