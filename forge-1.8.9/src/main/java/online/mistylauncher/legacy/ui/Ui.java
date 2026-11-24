package online.mistylauncher.legacy.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Colours and drawing helpers shared by the Mistly screens: the launcher's "Night" theme, the
 * same as the Fabric versions' ({@code online.mistylauncher.client.ui.Ui}).
 */
public final class Ui {
	public static final int INK = 0xFF0A0F1D;
	public static final int GLASS = 0xB8111A30;
	public static final int GLASS_HOVER = 0xF01C2A50;
	public static final int SURFACE = 0xF50E1527;
	public static final int BORDER = 0x1FFFFFFF;
	public static final int LINE = 0x14FFFFFF;
	public static final int ACCENT = 0xFF9CC0FF;
	public static final int FILL = 0xFF6F9CFF;
	public static final int FILL_HOVER = 0xFF8AAEFF;
	public static final int TEXT = 0xFFEEF3FF;
	public static final int MUTED = 0xFFA3B0CB;
	public static final int DIM = 0xFF6F7C99;
	public static final int SLATE = 0xFF3C5486;
	public static final int TRACK = 0x26FFFFFF;

	public static final ResourceLocation LOGO = new ResourceLocation("misty", "textures/gui/logo.png");
	private static final int LOGO_SIZE = 128;

	private Ui() {
	}

	public static ResourceLocation icon(final String name) {
		return new ResourceLocation("misty", "textures/gui/icons/" + name + ".png");
	}

	public static FontRenderer font() {
		return Minecraft.getMinecraft().fontRendererObj;
	}

	/** Same colour with another opacity (0..1). */
	public static int alpha(final int color, final float alpha) {
		int a = Math.round((color >>> 24) * Math.max(0.0F, Math.min(1.0F, alpha)));
		return a << 24 | color & 0xFFFFFF;
	}

	/** A plain rectangle (nothing drawn when it is fully transparent). */
	public static void fill(final int x0, final int y0, final int x1, final int y1, final int color) {
		if ((color >>> 24) == 0 || x1 <= x0 || y1 <= y0) {
			return;
		}
		Gui.drawRect(x0, y0, x1, y1, color);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	/** Rectangle with its four corner pixels cut: the pixel-art rounding of the launcher. */
	public static void panel(final int x, final int y, final int width, final int height, final int color) {
		if (width < 3 || height < 3) {
			fill(x, y, x + width, y + height, color);
			return;
		}
		fill(x + 1, y, x + width - 1, y + 1, color);
		fill(x, y + 1, x + width, y + height - 1, color);
		fill(x + 1, y + height - 1, x + width - 1, y + height, color);
	}

	/** One-pixel border following the cut corners of {@link #panel}. */
	public static void border(final int x, final int y, final int width, final int height, final int color) {
		fill(x + 1, y, x + width - 1, y + 1, color);
		fill(x + 1, y + height - 1, x + width - 1, y + height, color);
		fill(x, y + 1, x + 1, y + height - 1, color);
		fill(x + width - 1, y + 1, x + width, y + height - 1, color);
	}

	/** Panel and its border. */
	public static void card(final int x, final int y, final int width, final int height, final int color, final int borderColor) {
		panel(x, y, width, height, color);
		border(x, y, width, height, borderColor);
	}

	/** A one-pixel outline around a rectangle (square corners). */
	public static void outline(final int x, final int y, final int width, final int height, final int color) {
		fill(x, y, x + width, y + 1, color);
		fill(x, y + height - 1, x + width, y + height, color);
		fill(x, y + 1, x + 1, y + height - 1, color);
		fill(x + width - 1, y + 1, x + width, y + height - 1, color);
	}

	/** Vertical gradient, {@code top} colour at the top, {@code bottom} at the bottom. */
	public static void gradient(final float x0, final float y0, final float x1, final float y1, final int top, final int bottom) {
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer buffer = tessellator.getWorldRenderer();
		buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(x1, y0, 0.0).color(top >> 16 & 255, top >> 8 & 255, top & 255, top >>> 24).endVertex();
		buffer.pos(x0, y0, 0.0).color(top >> 16 & 255, top >> 8 & 255, top & 255, top >>> 24).endVertex();
		buffer.pos(x0, y1, 0.0).color(bottom >> 16 & 255, bottom >> 8 & 255, bottom & 255, bottom >>> 24).endVertex();
		buffer.pos(x1, y1, 0.0).color(bottom >> 16 & 255, bottom >> 8 & 255, bottom & 255, bottom >>> 24).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	/**
	 * Part of a texture drawn at (x, y), {@code width} × {@code height} on the screen, from the
	 * region (u, v, regionW, regionH) of a {@code texW} × {@code texH} texture, tinted by {@code color}.
	 */
	public static void blit(final ResourceLocation texture, final float x, final float y, final float u, final float v, final float width, final float height,
		final int regionW, final int regionH, final int texW, final int texH, final int color) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >>> 24) / 255.0F);
		float fu = 1.0F / texW;
		float fv = 1.0F / texH;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer buffer = tessellator.getWorldRenderer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x, y + height, 0.0).tex(u * fu, (v + regionH) * fv).endVertex();
		buffer.pos(x + width, y + height, 0.0).tex((u + regionW) * fu, (v + regionH) * fv).endVertex();
		buffer.pos(x + width, y, 0.0).tex((u + regionW) * fu, v * fv).endVertex();
		buffer.pos(x, y, 0.0).tex(u * fu, v * fv).endVertex();
		tessellator.draw();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	/** A whole square texture (the 16 × 16 Mistly icons…) drawn {@code size} × {@code size}. */
	public static void blit(final ResourceLocation texture, final float x, final float y, final float size, final int texSize, final int color) {
		blit(texture, x, y, 0.0F, 0.0F, size, size, texSize, texSize, texSize, texSize, color);
	}

	/** The Mistly cloud. */
	public static void logo(final int x, final int y, final int size) {
		blit(LOGO, x, y, size, LOGO_SIZE, 0xFFFFFFFF);
	}

	public static void text(final String text, final float x, final float y, final int color) {
		font().drawString(text, x, y, color, false);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public static int width(final String text) {
		return font().getStringWidth(text);
	}

	public static void centered(final String text, final int centerX, final int y, final int color) {
		text(text, centerX - width(text) / 2, y, color);
	}

	/** Text drawn bigger or smaller than the font, its left edge at {@code x}. */
	public static void scaled(final String text, final float x, final float y, final float scale, final int color) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0.0F);
		GlStateManager.scale(scale, scale, 1.0F);
		text(text, 0, 0, color);
		GlStateManager.popMatrix();
	}

	/** Text drawn bigger or smaller than the font, centred on {@code centerX}. */
	public static void centeredScaled(final String text, final float centerX, final float y, final float scale, final int color) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(centerX, y, 0.0F);
		GlStateManager.scale(scale, scale, 1.0F);
		text(text, -width(text) / 2, 0, color);
		GlStateManager.popMatrix();
	}

	/** Cuts what is drawn to a rectangle (GUI pixels) until {@link #endScissor()}. */
	public static void scissor(final int x, final int y, final int width, final int height) {
		Minecraft minecraft = Minecraft.getMinecraft();
		int scale = new ScaledResolution(minecraft).getScaleFactor();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(x * scale, minecraft.displayHeight - (y + height) * scale, Math.max(0, width * scale), Math.max(0, height * scale));
	}

	public static void endScissor() {
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	/** A diamond (square turned 45°) of half-size {@code r}, with a border. */
	public static void diamond(final int cx, final int cy, final int r, final int border, final int inside) {
		for (int dy = -r; dy <= r; dy++) {
			int half = r - Math.abs(dy);
			fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, border);
			if (half > 0) {
				fill(cx - half + 1, cy + dy, cx + half, cy + dy + 1, inside);
			}
		}
	}

	/** A key cap of the previews: lit while it is held. */
	public static void keyCap(final int x, final int y, final String key, final boolean down) {
		int w = Math.max(12, width(key) + 6);
		card(x, y, w, 12, down ? 0xFF6F9CFF : 0xCC0A0F1D, down ? 0xFFEEF3FF : 0x66FFFFFF);
		text(key, x + (w - width(key)) / 2, y + 2, down ? INK : 0xFFFFFFFF);
	}

	/** A box of the previews: a line of text on the night. */
	public static void box(final int x, final int y, final String text) {
		fill(x, y, x + width(text) + 6, y + 12, 0x990A0F1D);
		text(text, x + 3, y + 2, 0xFFFFFFFF);
	}

	public static float clamp(final float value, final float min, final float max) {
		return Math.max(min, Math.min(max, value));
	}

	public static int clamp(final int value, final int min, final int max) {
		return Math.max(min, Math.min(max, value));
	}

	public static float lerp(final float t, final float a, final float b) {
		return a + (b - a) * t;
	}
}
