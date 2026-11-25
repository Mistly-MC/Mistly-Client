package online.mistylauncher.legacy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiSlider;
import online.mistylauncher.legacy.Events;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge's sliders: the Mistly handle too. */
@Mixin(GuiSlider.class)
abstract class GuiSliderMixin {
	@Inject(method = "mouseDragged", at = @At("TAIL"))
	private void misty$handle(final Minecraft minecraft, final int mouseX, final int mouseY, final CallbackInfo ci) {
		GuiSlider slider = (GuiSlider)(Object)this;
		Events.sliderHandle(slider, slider.sliderValue, slider.isMouseOver());
	}
}
