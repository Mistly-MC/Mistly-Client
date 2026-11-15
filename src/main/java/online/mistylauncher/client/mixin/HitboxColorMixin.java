package online.mistylauncher.client.mixin;

import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.world.entity.Entity;
import online.mistylauncher.client.features.Hitboxes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
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
