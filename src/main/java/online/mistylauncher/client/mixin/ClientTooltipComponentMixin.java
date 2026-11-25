package online.mistylauncher.client.mixin;

//? if >=1.17 {
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
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import online.mistylauncher.client.compat.TooltipComponent;
import online.mistylauncher.client.features.ShulkerPreview;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Shulker preview on 1.16.5 (no tooltip pictures yet): the picture drawn in place of the item's
// tooltip, where the game puts a tooltip (right of the mouse, kept on the screen)
@Mixin(Screen.class)
abstract class ClientTooltipComponentMixin {
	@Inject(method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"), cancellable = true)
	private void misty$shulkerPreview(final PoseStack pose, final ItemStack stack, final int mouseX, final int mouseY, final CallbackInfo ci) {
		Optional<TooltipComponent> picture = ShulkerPreview.tooltip(stack);
		if (!picture.isPresent()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = (Screen)(Object)this;
		ShulkerPreview.Picture image = new ShulkerPreview.Picture((ShulkerPreview.Contents)picture.get());
		int width = image.getWidth(minecraft.font);
		int height = image.getHeight();
		int x = mouseX + 12;
		if (x + width > screen.width - 4) {
			x = Math.max(4, mouseX - 16 - width);
		}
		int y = Math.max(4, Math.min(mouseY - 12, screen.height - height - 4));
		pose.pushPose();
		pose.translate(0.0, 0.0, 400.0);
		image.renderImage(minecraft.font, x, y, pose, minecraft.getItemRenderer(), 400);
		pose.popPose();
		ci.cancel();
	}
}
*///?}
