package online.mistylauncher.legacy.mixin;

import net.minecraft.client.gui.GuiScreen;
import online.mistylauncher.legacy.Events;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The menus out of a world: the launcher's landscape instead of the dirt. */
@Mixin(GuiScreen.class)
abstract class GuiScreenMixin {
	@Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
	private void misty$background(final int tint, final CallbackInfo ci) {
		if (Events.background((GuiScreen)(Object)this)) {
			ci.cancel();
		}
	}
}
