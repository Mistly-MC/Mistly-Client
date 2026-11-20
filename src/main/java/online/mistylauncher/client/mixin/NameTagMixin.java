package online.mistylauncher.client.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.net.MistyPlayers;
import online.mistylauncher.client.social.Badge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >=1.21.2 {
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.injection.ModifyArg;
*///?}

/** The Misty cloud in front of the Misty players' name above their head. */
@Mixin(EntityRenderer.class)
abstract class NameTagMixin {
	//? if >=1.21.2 {
	@Inject(method = "getNameTag", at = @At("RETURN"), cancellable = true)
	private void misty$badge(final Entity entity, final CallbackInfoReturnable<Component> cir) {
		cir.setReturnValue(badge(entity, cir.getReturnValue()));
	}
	//?} else {
	/*// before 1.21.2 the name is given to renderNameTag while the entity is drawn
	@ModifyArg(method = "render", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;renderNameTag(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V"),
		index = 1)
	private Component misty$badge(final Entity entity, final Component name, final PoseStack pose, final MultiBufferSource buffers, final int light, final float partialTicks) {
		return badge(entity, name);
	}
	*///?}

	private static Component badge(final Entity entity, final Component name) {
		if (name != null && entity instanceof Player player && MistyClient.config().nametagBadge && MistyPlayers.isMisty(player.getUUID())) {
			return Badge.prefix(name);
		}
		return name;
	}
}
