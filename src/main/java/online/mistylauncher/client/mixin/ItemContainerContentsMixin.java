package online.mistylauncher.client.mixin;

import online.mistylauncher.client.features.ShulkerPreview;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shulker preview: while the picture shows the contents, the game's text lines about them are left out. */
//? if >=1.21.5 {
@Mixin(net.minecraft.world.item.component.ItemContainerContents.class)
abstract class ItemContainerContentsMixin {
	@Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
	private void misty$shulkerPreview(final CallbackInfo ci) {
		if (ShulkerPreview.active()) {
			ci.cancel();
		}
	}
}
//?} else {
/*// before 1.21.5 the shulker box block writes those lines
@Mixin(net.minecraft.world.level.block.ShulkerBoxBlock.class)
abstract class ItemContainerContentsMixin {
	@Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
	private void misty$shulkerPreview(final CallbackInfo ci) {
		if (ShulkerPreview.active()) {
			ci.cancel();
		}
	}
}
*///?}
