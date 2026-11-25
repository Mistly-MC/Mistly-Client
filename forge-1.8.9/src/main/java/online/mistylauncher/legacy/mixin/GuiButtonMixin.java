package online.mistylauncher.legacy.mixin;

import net.minecraft.client.gui.GuiButton;
import online.mistylauncher.legacy.Events;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** The game's buttons: their text in the Mistly colours (white under the mouse), not yellow. */
@Mixin(GuiButton.class)
abstract class GuiButtonMixin {
	@Shadow
	protected boolean hovered;

	@ModifyArg(method = "drawButton", index = 4, at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/GuiButton;drawCenteredString(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;III)V"))
	private int misty$text(final int color) {
		return Events.buttonText((GuiButton)(Object)this, this.hovered, color);
	}
}
