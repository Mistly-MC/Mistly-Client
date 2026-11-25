package online.mistylauncher.client.compat;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines;
//?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}

/**
 * The interface's drawing that changed in 1.21.6: its own 2D matrix (before: the 3D pose stack)
 * and pictures drawn with a pipeline and a tint (before: the shader colour).
 */
public final class Gui2D {
	private Gui2D() {
	}

	public static void push(final GuiGraphicsExtractor graphics) {
		//? if >=1.21.6 {
		graphics.pose().pushMatrix();
		//?} else {
		/*graphics.pose().pushPose();
		*///?}
	}

	public static void pop(final GuiGraphicsExtractor graphics) {
		//? if >=1.21.6 {
		graphics.pose().popMatrix();
		//?} else {
		/*graphics.pose().popPose();
		*///?}
	}

	public static void translate(final GuiGraphicsExtractor graphics, final float x, final float y) {
		//? if >=1.21.6 {
		graphics.pose().translate(x, y);
		//?} else {
		/*graphics.pose().translate(x, y, 0.0F);
		*///?}
	}

	public static void scale(final GuiGraphicsExtractor graphics, final float x, final float y) {
		//? if >=1.21.6 {
		graphics.pose().scale(x, y);
		//?} else {
		/*graphics.pose().scale(x, y, 1.0F);
		*///?}
	}

	/** A picture: (u, v) and the region's size in it, stretched to width × height. */
	public static void blit(final GuiGraphicsExtractor graphics, final Identifier texture, final int x, final int y, final float u, final float v,
		final int width, final int height, final int regionW, final int regionH, final int texW, final int texH, final int color) {
		//? if >=1.21.6 {
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, regionW, regionH, texW, texH, color);
		//?} else {
		/*RenderSystem.enableBlend();
		RenderSystem.setShaderColor((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, (color >>> 24) / 255.0F);
		graphics.blit(texture, x, y, width, height, u, v, regionW, regionH, texW, texH);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		*///?}
	}

	public static void blit(final GuiGraphicsExtractor graphics, final Identifier texture, final int x, final int y, final float u, final float v,
		final int width, final int height, final int regionW, final int regionH, final int texW, final int texH) {
		blit(graphics, texture, x, y, u, v, width, height, regionW, regionH, texW, texH, 0xFFFFFFFF);
	}

	/** The region the size of the picture drawn. */
	public static void blit(final GuiGraphicsExtractor graphics, final Identifier texture, final int x, final int y, final float u, final float v,
		final int width, final int height, final int texW, final int texH) {
		blit(graphics, texture, x, y, u, v, width, height, width, height, texW, texH, 0xFFFFFFFF);
	}

	/** The region the size of the picture drawn, tinted. */
	public static void blit(final GuiGraphicsExtractor graphics, final Identifier texture, final int x, final int y, final float u, final float v,
		final int width, final int height, final int texW, final int texH, final int color) {
		blit(graphics, texture, x, y, u, v, width, height, width, height, texW, texH, color);
	}

	/** A sprite of the game's GUI atlas, tinted (ARGB: its alpha fades it). */
	public static void sprite(final GuiGraphicsExtractor graphics, final Identifier sprite, final int x, final int y, final int width, final int height,
		final int color) {
		//? if >=1.21.6 {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, color);
		//?} else {
		/*RenderSystem.enableBlend();
		RenderSystem.setShaderColor((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, (color >>> 24) / 255.0F);
		blitSprite(graphics, sprite, x, y, width, height);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		*///?}
	}

	/** A sprite of the game's GUI atlas. */
	public static void sprite(final GuiGraphicsExtractor graphics, final Identifier sprite, final int x, final int y, final int width, final int height) {
		//? if >=1.21.6 {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
		//?} else {
		/*RenderSystem.enableBlend();
		blitSprite(graphics, sprite, x, y, width, height);
		*///?}
	}

	//? if <1.21.6 {
	/*private static void blitSprite(final GuiGraphicsExtractor graphics, final Identifier sprite, final int x, final int y, final int width, final int height) {
		//? if >=1.20.2 {
		graphics.blitSprite(sprite, x, y, width, height);
		//?} else {
		/^// no GUI atlas before 1.20.2: the textures the sprites came from
		String path = sprite.getPath();
		String namespace = sprite.getNamespace();
		if (path.startsWith("hud/food_")) {
			int u = path.equals("hud/food_empty") ? 16 : path.equals("hud/food_half") ? 61 : 52;
			graphics.blit(new Identifier("textures/gui/icons.png"), x, y, width, height, u, 27, 9, 9, 256, 256);
			return;
		}
		String texture = path.startsWith("textures/") ? path
			: path.startsWith("mob_effect/") ? "textures/" + path + ".png"
			: path.startsWith("container/slot/") ? "textures/item/empty_armor_slot_" + path.substring("container/slot/".length()) + ".png"
			: "textures/gui/sprites/" + path + ".png";
		graphics.blit(new Identifier(namespace, texture), x, y, 0.0F, 0.0F, width, height, width, height);
		^///?}
	}
	*///?}
}
