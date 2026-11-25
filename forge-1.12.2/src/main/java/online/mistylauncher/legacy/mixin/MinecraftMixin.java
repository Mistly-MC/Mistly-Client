package online.mistylauncher.legacy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.InventoryPlayer;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.Events;
import online.mistylauncher.legacy.menu.MistyTitle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The game's tick, its screens and the mouse in game. */
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
	@Shadow
	public WorldClient world;

	@Shadow
	public abstract void displayGuiScreen(GuiScreen screen);

	@Inject(method = "runTick", at = @At("TAIL"))
	private void misty$tick(final CallbackInfo ci) {
		Events.tick();
	}

	/** The Mistly main menu in place of the game's (unless turned off in the settings). */
	@Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
	private void misty$menu(final GuiScreen screen, final CallbackInfo ci) {
		boolean title = screen instanceof GuiMainMenu || screen == null && this.world == null;
		if (title && Config.get().customMenu) {
			this.displayGuiScreen(new MistyTitle());
			ci.cancel();
		}
	}

	/** Each mouse button in game, for the CPS (the first key state the tick sets is the mouse's). */
	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;setKeyBindState(IZ)V", ordinal = 0))
	private void misty$click(final int key, final boolean pressed) {
		KeyBinding.setKeyBindState(key, pressed);
		Events.mouseButton(key + 100, pressed);
	}

	/** The wheel while zooming changes the zoom, not the hotbar slot. */
	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;changeCurrentItem(I)V"))
	private void misty$wheel(final InventoryPlayer inventory, final int dwheel) {
		if (!Events.wheel(dwheel)) {
			inventory.changeCurrentItem(dwheel);
		}
	}
}
