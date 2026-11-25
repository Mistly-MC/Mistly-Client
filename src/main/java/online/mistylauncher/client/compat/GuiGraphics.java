package online.mistylauncher.client.compat;

//? if <1.20 {
/*import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

// The game's GuiGraphics (1.20) for the versions before it: what the mod draws with, over the
// pose stack the game gives and its GuiComponent helpers. The game's methods that get a pose
// stack make one of these (see stonecutter.gradle.kts).
public final class GuiGraphics {
	private static final Gradient GRADIENT = new Gradient();
	private final PoseStack pose;

	public GuiGraphics(final PoseStack pose) {
		this.pose = pose;
	}

	public PoseStack pose() {
		return this.pose;
	}

	public int guiWidth() {
		return Minecraft.getInstance().getWindow().getGuiScaledWidth();
	}

	public int guiHeight() {
		return Minecraft.getInstance().getWindow().getGuiScaledHeight();
	}

	public void fill(final int x1, final int y1, final int x2, final int y2, final int color) {
		GuiComponent.fill(this.pose, x1, y1, x2, y2, color);
	}

	public void fillGradient(final int x1, final int y1, final int x2, final int y2, final int top, final int bottom) {
		GRADIENT.gradient(this.pose, x1, y1, x2, y2, top, bottom);
	}

	public void renderOutline(final int x, final int y, final int width, final int height, final int color) {
		this.fill(x, y, x + width, y + 1, color);
		this.fill(x, y + height - 1, x + width, y + height, color);
		this.fill(x, y + 1, x + 1, y + height - 1, color);
		this.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
	}

	public int drawString(final Font font, final @Nullable String text, final int x, final int y, final int color, final boolean shadow) {
		if (text == null) {
			return 0;
		}
		return shadow ? font.drawShadow(this.pose, text, x, y, color) : font.draw(this.pose, text, x, y, color);
	}

	public int drawString(final Font font, final Component text, final int x, final int y, final int color, final boolean shadow) {
		return shadow ? font.drawShadow(this.pose, text, x, y, color) : font.draw(this.pose, text, x, y, color);
	}

	public int drawString(final Font font, final FormattedCharSequence text, final int x, final int y, final int color, final boolean shadow) {
		return shadow ? font.drawShadow(this.pose, text, x, y, color) : font.draw(this.pose, text, x, y, color);
	}

	// an area of the screen drawn into only (in GUI pixels, as the game's)
	public void enableScissor(final int x1, final int y1, final int x2, final int y2) {
		Window window = Minecraft.getInstance().getWindow();
		double scale = window.getGuiScale();
		RenderSystem.enableScissor((int)(x1 * scale), (int)(window.getHeight() - y2 * scale), Math.max(0, (int)((x2 - x1) * scale)),
			Math.max(0, (int)((y2 - y1) * scale)));
	}

	public void disableScissor() {
		RenderSystem.disableScissor();
	}

	public void blit(final ResourceLocation texture, final int x, final int y, final int width, final int height, final float u, final float v,
		final int regionW, final int regionH, final int texW, final int texH) {
		RenderSystem.setShaderTexture(0, texture);
		GuiComponent.blit(this.pose, x, y, width, height, u, v, regionW, regionH, texW, texH);
	}

	public void blit(final ResourceLocation texture, final int x, final int y, final float u, final float v, final int width, final int height,
		final int texW, final int texH) {
		RenderSystem.setShaderTexture(0, texture);
		GuiComponent.blit(this.pose, x, y, u, v, width, height, texW, texH);
	}

	public void renderItem(final ItemStack stack, final int x, final int y) {
		//? if >=1.19.4 {
		Minecraft.getInstance().getItemRenderer().renderGuiItem(this.pose, stack, x, y);
		//?} else {
		/^this.inPose(() -> Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, x, y));
		^///?}
	}

	public void renderItemDecorations(final Font font, final ItemStack stack, final int x, final int y) {
		//? if >=1.19.4 {
		Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(this.pose, font, stack, x, y);
		//?} else {
		/^this.inPose(() -> Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(font, stack, x, y));
		^///?}
	}

	public void renderItemDecorations(final Font font, final ItemStack stack, final int x, final int y, final @Nullable String text) {
		//? if >=1.19.4 {
		Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(this.pose, font, stack, x, y, text);
		//?} else {
		/^this.inPose(() -> Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(font, stack, x, y, text));
		^///?}
	}

	// before 1.19.4 the items are drawn through the model-view matrix: the pose put on it meanwhile
	//? if >=1.17 <1.19.4 {
	/^private void inPose(final Runnable draw) {
		PoseStack view = RenderSystem.getModelViewStack();
		view.pushPose();
		view.mulPoseMatrix(this.pose.last().pose());
		RenderSystem.applyModelViewMatrix();
		draw.run();
		view.popPose();
		RenderSystem.applyModelViewMatrix();
	}
	^///?}
	// a GL matrix before 1.17
	//? if <1.17 {
	/^private void inPose(final Runnable draw) {
		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(this.pose.last().pose());
		draw.run();
		RenderSystem.popMatrix();
	}
	^///?}

	public MultiBufferSource.BufferSource bufferSource() {
		return Minecraft.getInstance().renderBuffers().bufferSource();
	}

	public void flush() {
		RenderSystem.disableDepthTest();
		this.bufferSource().endBatch();
		RenderSystem.enableDepthTest();
	}

	// the game's gradient is only open to its GUI components (an instance's method before 1.19.4)
	private static final class Gradient extends GuiComponent {
		void gradient(final PoseStack pose, final int x1, final int y1, final int x2, final int y2, final int top, final int bottom) {
			fillGradient(pose, x1, y1, x2, y2, top, bottom);
		}
	}

	// a player's face from their skin texture: the face, then the hat layer over it
	public static void face(final GuiGraphics graphics, final ResourceLocation skin, final int x, final int y, final int size) {
		RenderSystem.setShaderTexture(0, skin);
		RenderSystem.enableBlend();
		GuiComponent.blit(graphics.pose, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
		GuiComponent.blit(graphics.pose, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
		RenderSystem.disableBlend();
	}
}
*///?}
