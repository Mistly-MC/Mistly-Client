package online.mistylauncher.client.mixin;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import online.mistylauncher.client.features.ShulkerPreview;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shulker preview: the game only knows how to draw its own tooltip pictures (bundles…), this adds ours. */
@Mixin(ClientTooltipComponent.class)
interface ClientTooltipComponentMixin {
	@Inject(
		method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void misty$shulkerPreview(final TooltipComponent component, final CallbackInfoReturnable<ClientTooltipComponent> cir) {
		if (component instanceof ShulkerPreview.Contents contents) {
			cir.setReturnValue(new ShulkerPreview.Picture(contents));
		}
	}
}
