package online.mistylauncher.client.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import online.mistylauncher.client.compat.Versions;
import online.mistylauncher.client.features.Java4Shell;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Java4Shell: the server list pings every server as soon as it opens, so while this Java is exposed
 * (see {@link Java4Shell}) a warning comes first, whatever opens it (the Misty menu, the classic
 * menu, a mod). Screens are opened by {@code Gui.setScreen} since 26.2, by {@code Minecraft.setScreen}
 * before.
 */
//? if >=26.2 {
@Mixin(net.minecraft.client.gui.Gui.class)
//?} else {
/*@Mixin(net.minecraft.client.Minecraft.class)
*///?}
abstract class OpenScreenMixin {
	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	private void misty$java4shell(final @Nullable Screen screen, final CallbackInfo ci) {
		if (screen instanceof JoinMultiplayerScreen) {
			Screen first = Java4Shell.guard(Versions.screen(), screen);
			if (first != screen) {
				ci.cancel();
				Versions.setScreen(first);
			}
		}
	}
}
