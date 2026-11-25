package online.mistylauncher.legacy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionSlider;
import online.mistylauncher.legacy.Events;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The game's option sliders get the Mistly handle (the game draws its own from the button's edges). */
@Mixin(GuiOptionSlider.class)
abstract class GuiOptionSliderMixin {
	@Shadow
	private float sliderValue;

	@Inject(method = "mouseDragged", at = @At("TAIL"))
	private void misty$handle(final Minecraft minecraft, final int mouseX, final int mouseY, final CallbackInfo ci) {
		GuiButton slider = (GuiButton)(Object)this;
		Events.sliderHandle(slider, this.sliderValue, slider.isMouseOver());
	}
}
