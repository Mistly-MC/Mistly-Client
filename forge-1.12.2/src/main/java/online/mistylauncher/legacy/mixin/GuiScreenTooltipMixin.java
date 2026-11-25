package online.mistylauncher.legacy.mixin;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import online.mistylauncher.legacy.features.ShulkerPreview;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shulker preview: Shift held over a shulker box, the picture of its slots in place of its tooltip. */
@Mixin(GuiScreen.class)
abstract class GuiScreenTooltipMixin {
	@Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
	private void misty$shulker(final ItemStack stack, final int x, final int y, final CallbackInfo ci) {
		if (ShulkerPreview.tooltip((GuiScreen)(Object)this, stack, x, y)) {
			ci.cancel();
		}
	}
}
