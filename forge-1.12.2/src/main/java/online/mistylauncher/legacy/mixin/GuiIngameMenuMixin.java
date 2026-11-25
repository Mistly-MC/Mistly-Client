package online.mistylauncher.legacy.mixin;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import online.mistylauncher.legacy.Events;
import online.mistylauncher.legacy.ui.MistyButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The pause menu: a way into the Mistly settings in its top-right corner. */
@Mixin(GuiIngameMenu.class)
abstract class GuiIngameMenuMixin extends GuiScreen {
	@Inject(method = "initGui", at = @At("TAIL"))
	private void misty$settings(final CallbackInfo ci) {
		this.buttonList.add(Events.pauseButton((GuiIngameMenu)(Object)this));
	}

	/** The Mistly button runs its own action (the menu would take it for one of its own). */
	@Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
	private void misty$press(final GuiButton button, final CallbackInfo ci) {
		if (button instanceof MistyButton) {
			((MistyButton)button).press();
			ci.cancel();
		}
	}
}
