package online.mistylauncher.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.PlayerSkin;
import online.mistylauncher.client.compat.Versions;
import online.mistylauncher.client.cosmetics.Cosmetics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Misty cape and elytra: the skin every renderer asks for goes through here. */
@Mixin(PlayerInfo.class)
abstract class PlayerInfoMixin {
	@Shadow
	@Final
	private GameProfile profile;

	//? if >=1.20.2 {
	@Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
	private void misty$dress(final CallbackInfoReturnable<PlayerSkin> cir) {
		cir.setReturnValue(Cosmetics.dress(Versions.id(this.profile), cir.getReturnValue()));
	}
	//?} else {
	/*// before 1.20.2 the cape and the elytra are asked for one by one
	@Inject(method = "getCapeLocation", at = @At("RETURN"), cancellable = true)
	private void misty$cape(final CallbackInfoReturnable<net.minecraft.resources.ResourceLocation> cir) {
		cir.setReturnValue(this.misty$dressed(cir.getReturnValue(), null).capeTexture());
	}

	@Inject(method = "getElytraLocation", at = @At("RETURN"), cancellable = true)
	private void misty$elytra(final CallbackInfoReturnable<net.minecraft.resources.ResourceLocation> cir) {
		cir.setReturnValue(this.misty$dressed(null, cir.getReturnValue()).elytraTexture());
	}

	private PlayerSkin misty$dressed(final net.minecraft.resources.ResourceLocation cape, final net.minecraft.resources.ResourceLocation elytra) {
		return Cosmetics.dress(Versions.id(this.profile), new PlayerSkin(net.minecraft.client.resources.DefaultPlayerSkin.getDefaultSkin(), null, cape, elytra, PlayerSkin.Model.WIDE, false));
	}
	*///?}
}
