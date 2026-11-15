package online.mistylauncher.client.mixin;

import net.minecraft.world.item.component.ItemContainerContents;
import online.mistylauncher.client.features.ShulkerPreview;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shulker preview: while the picture shows the contents, the game's text lines about them are left out. */
@Mixin(ItemContainerContents.class)
abstract class ItemContainerContentsMixin {
	@Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
	private void misty$shulkerPreview(final CallbackInfo ci) {
		if (ShulkerPreview.active()) {
			ci.cancel();
		}
	}
}
