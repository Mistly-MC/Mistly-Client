package online.mistylauncher.legacy.mixin;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import online.mistylauncher.legacy.Events;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Forge's buttons (mod list, config screens): the same text colours as the game's. */
@Mixin(GuiButtonExt.class)
abstract class GuiButtonExtMixin {
	@ModifyArg(method = "drawButton", index = 4, at = @At(value = "INVOKE",
		target = "Lnet/minecraftforge/fml/client/config/GuiButtonExt;drawCenteredString(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;III)V"))
	private int misty$text(final int color) {
		GuiButton button = (GuiButton)(Object)this;
		return Events.buttonText(button, button.isMouseOver(), color);
	}
}
