package online.mistylauncher.legacy.mixin;

import net.minecraftforge.client.GuiIngameForge;
import online.mistylauncher.legacy.Events;
import online.mistylauncher.legacy.net.Badge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The HUD (Forge draws the game's): the Mistly HUD over it, the badges in the player list, the saturation. */
@Mixin(GuiIngameForge.class)
abstract class GuiIngameForgeMixin {
	@Inject(method = "renderGameOverlay", at = @At("HEAD"))
	private void misty$before(final float partialTicks, final CallbackInfo ci) {
		Events.hudBefore();
	}

	@Inject(method = "renderGameOverlay", at = @At("TAIL"))
	private void misty$after(final float partialTicks, final CallbackInfo ci) {
		Events.hudAfter();
	}

	// Forge's own methods: their names are not the game's obfuscated ones
	@Inject(method = "renderPlayerList", at = @At("HEAD"), remap = false)
	private void misty$listBefore(final int width, final int height, final CallbackInfo ci) {
		Badge.beforePlayerList();
	}

	@Inject(method = "renderPlayerList", at = @At("TAIL"), remap = false)
	private void misty$listAfter(final int width, final int height, final CallbackInfo ci) {
		Badge.afterPlayerList();
	}

	@Inject(method = "renderFood", at = @At("TAIL"), remap = false)
	private void misty$food(final int width, final int height, final CallbackInfo ci) {
		Events.food(width, height);
	}
}
