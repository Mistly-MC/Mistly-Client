package online.mistylauncher.client.mixin;

//? if >=1.17 {
import java.util.Optional;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import online.mistylauncher.client.features.ShulkerPreview;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shulker preview: the picture of a shulker's contents in its tooltip (Shift held). */
@Mixin(Item.class)
abstract class ItemMixin {
	@Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
	private void misty$shulkerPreview(final ItemStack stack, final CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
		Optional<TooltipComponent> picture = ShulkerPreview.tooltip(stack);
		if (picture.isPresent()) {
			cir.setReturnValue(picture);
		}
	}
}
//?} else {
/*import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

// 1.16.5: items have no tooltip pictures, the preview is drawn by ClientTooltipComponentMixin
@Mixin(Item.class)
abstract class ItemMixin {
}
*///?}
