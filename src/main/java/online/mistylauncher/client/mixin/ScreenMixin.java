package online.mistylauncher.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.menu.MistyBackground;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The menus outside the game (singleplayer, multiplayer, options and all the others) over the
 * launcher's landscape, sharp, instead of Minecraft's blurred panorama. Their buttons, sliders,
 * text fields and lists take the Misty look from the mod's textures (assets/minecraft/…).
 * With the Misty main menu turned off, the game's own background stays.
 */
@Mixin(Screen.class)
abstract class ScreenMixin {
	@Shadow
	protected @Nullable Minecraft minecraft;

	//? if >=1.20.5 {
	@Inject(method = "extractPanorama", at = @At("HEAD"), cancellable = true)
	private void misty$panorama(final GuiGraphicsExtractor graphics, final float a, final CallbackInfo ci) {
		if (MistyClient.config().customMenu) {
			MistyBackground.draw(graphics, graphics.guiWidth(), graphics.guiHeight());
			ci.cancel();
		}
	}

	@Inject(method = "extractBlurredBackground", at = @At("HEAD"), cancellable = true)
	// the GUI was given to it in 1.21.6, the time of the frame before
	//? if >=1.21.6 {
	private void misty$blur(final GuiGraphicsExtractor graphics, final CallbackInfo ci) {
	//?} else {
	/*private void misty$blur(final float a, final CallbackInfo ci) {
	*///?}
		// out of the game, the landscape stays sharp; in game, the world is blurred as usual
		if (MistyClient.config().customMenu && this.minecraft != null && this.minecraft.level == null) {
			ci.cancel();
		}
	}
	//?} else {
	/*// before 1.20.5 the menus out of a world draw dirt (the title screen has its own panorama): the landscape instead
	@Inject(method = "renderDirtBackground", at = @At("HEAD"), cancellable = true)
	//? if >=1.19.4 {
	private void misty$dirt(final GuiGraphicsExtractor graphics, final CallbackInfo ci) {
	//?} else {
	/^// the dirt got only its offset before 1.19.4: the landscape drawn on a pose stack of its own
	private void misty$dirt(final int offset, final CallbackInfo ci) {
		GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(new com.mojang.blaze3d.vertex.PoseStack());
	^///?}
		if (MistyClient.config().customMenu) {
			MistyBackground.draw(graphics, graphics.guiWidth(), graphics.guiHeight());
			ci.cancel();
		}
	}

	// before 1.20.2 a screen draws its background itself: the Mistly screens get theirs as they start drawing
	//? if <1.20.2 {
	/^@Inject(method = "render", at = @At("HEAD"))
	private void misty$background(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a, final CallbackInfo ci) {
		if ((Object)this instanceof online.mistylauncher.client.ui.MistyScreen screen) {
			screen.extractBackground(graphics, mouseX, mouseY, a);
		}
	}
	^///?}
	*///?}
}
