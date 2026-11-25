package online.mistylauncher.client.mixin;

import net.minecraft.client.gui.components.AbstractSliderButton;
import org.spongepowered.asm.mixin.Mixin;

/**
 * The game's sliders before 1.19.4, cut from the rows of the buttons (no slider texture of their
 * own): drawn over with the Mistly track and handle, as the later versions draw them from
 * textures/gui/slider.png or the GUI sprites. Nothing to do from 1.19.4.
 */
@Mixin(AbstractSliderButton.class)
abstract class OldSliderMixin {
	//? if <1.19.4 {
	/*private static final net.minecraft.resources.ResourceLocation TRACK = new net.minecraft.resources.ResourceLocation("textures/gui/sprites/widget/slider.png");
	private static final net.minecraft.resources.ResourceLocation TRACK_HOT = new net.minecraft.resources.ResourceLocation("textures/gui/sprites/widget/slider_highlighted.png");
	private static final net.minecraft.resources.ResourceLocation HANDLE = new net.minecraft.resources.ResourceLocation("textures/gui/sprites/widget/slider_handle.png");
	private static final net.minecraft.resources.ResourceLocation HANDLE_HOT = new net.minecraft.resources.ResourceLocation("textures/gui/sprites/widget/slider_handle_highlighted.png");

	@org.spongepowered.asm.mixin.Shadow
	protected double value;

	@org.spongepowered.asm.mixin.injection.Inject(method = "renderBg", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
	private void misty$look(final com.mojang.blaze3d.vertex.PoseStack pose, final net.minecraft.client.Minecraft minecraft, final int mouseX, final int mouseY,
		final org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
		net.minecraft.client.gui.components.AbstractWidget self = (net.minecraft.client.gui.components.AbstractWidget)(Object)this;
		boolean hot = self.isHoveredOrFocused();
		int x = self.x;
		int y = self.y;
		int width = self.getWidth();
		com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		// the track: its two halves from the 200 wide texture, as the game draws a button
		com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, hot ? TRACK_HOT : TRACK);
		int half = width / 2;
		net.minecraft.client.gui.GuiComponent.blit(pose, x, y, 0.0F, 0.0F, half, 20, 200, 20);
		net.minecraft.client.gui.GuiComponent.blit(pose, x + half, y, 200 - (width - half), 0.0F, width - half, 20, 200, 20);
		// the handle, 8 wide, where the value is
		com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, hot ? HANDLE_HOT : HANDLE);
		net.minecraft.client.gui.GuiComponent.blit(pose, x + (int)(this.value * (width - 8)), y, 0.0F, 0.0F, 8, 20, 8, 20);
	}
	*///?}
}
