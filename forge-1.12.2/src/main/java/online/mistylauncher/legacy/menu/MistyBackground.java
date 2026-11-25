package online.mistylauncher.legacy.menu;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import online.mistylauncher.legacy.ui.Ui;

/**
 * Background of the Mistly main menu: the night landscape of the launcher and of the website
 * (stars, moon, four ridges with their fir trees, mist), scrolling slowly to the right, the far
 * layers slower than the near ones. The same as the Fabric versions' (same textures).
 */
public final class MistyBackground {
	/** Size of the scene, in pixels of the website (the menu scales it). */
	private static final float SCENE_W = 1600.0F;
	private static final float SCENE_H = 440.0F;
	/** One texture pixel of the ridges is 8 scene pixels (pixel art). */
	private static final int CELL = 8;

	private static final ResourceLocation[] RIDGES = {
		scene("ridge0"), scene("ridge1"), scene("ridge2"), scene("ridge3"),
	};
	/** Ridge textures: 200 × 55 cells, mirrored → 400 × 55. */
	private static final int RIDGE_W = 400;
	private static final int RIDGE_H = 55;
	/** Speed of each ridge, in scene pixels per second (far → near). */
	private static final float[] RIDGE_SPEED = {4.0F, 7.0F, 11.0F, 17.0F};

	private static final ResourceLocation STARS = scene("stars");
	private static final ResourceLocation TWINKLE = scene("twinkle");
	/** Star textures: 800 × 220 (2 scene pixels each), mirrored → 1600 × 220. */
	private static final int STARS_W = 1600;
	private static final int STARS_H = 220;
	private static final float STARS_SPEED = 1.5F;

	private static final ResourceLocation MOON = scene("moon");
	private static final int MOON_TEX = 11;
	private static final float MOON_X = 852.0F;
	private static final float MOON_Y = 60.0F;
	private static final float MOON_SIZE = 88.0F;

	private static final int SKY_TOP = 0xFF0A0F1D;
	private static final int SKY_BOTTOM = 0xFF17264D;
	private static final int MIST = 0xD6E4FF;
	private static final int GROUND = 0x070B16;

	private MistyBackground() {
	}

	private static ResourceLocation scene(final String name) {
		return new ResourceLocation("misty", "textures/gui/scene/" + name + ".png");
	}

	public static void draw(final int width, final int height) {
		float seconds = (System.currentTimeMillis() % 3_600_000L) / 1000.0F;
		// like the website ("slice"): the scene covers the width and at least 74 % of the height
		float scale = Math.max(width / SCENE_W, height * 0.74F / SCENE_H);
		float top = height - SCENE_H * scale;

		Ui.gradient(0, 0, width, height, SKY_TOP, SKY_BOTTOM);

		// stars, very far away: they barely move; the twinkling ones fade in and out
		float starsWidth = STARS_W * 2.0F * scale;
		layer(STARS, STARS_W, STARS_H, 2.0F * scale, top, seconds * STARS_SPEED * scale, starsWidth, width, 0xFFFFFFFF);
		float twinkle = 0.25F + 0.75F * (0.5F + 0.5F * (float)Math.sin(seconds / 8.0F * Math.PI * 2.0));
		layer(TWINKLE, STARS_W, STARS_H, 2.0F * scale, top, seconds * STARS_SPEED * scale, starsWidth, width, alpha(0xFFFFFF, twinkle));

		// moon: floats up and down by 10 scene pixels every 9 seconds, with a soft glow
		float bob = 5.0F * (1.0F - (float)Math.cos(seconds / 9.0F * Math.PI * 2.0));
		float moonX = (width - SCENE_W * scale) / 2.0F + MOON_X * scale;
		float moonY = top + (MOON_Y - bob) * scale;
		float moonSize = MOON_SIZE * scale;
		glow(moonX + moonSize / 2.0F, moonY + moonSize / 2.0F, moonSize * 1.25F);
		Ui.blit(MOON, moonX, moonY, 0.0F, 0.0F, moonSize, moonSize, MOON_TEX, MOON_TEX, MOON_TEX, MOON_TEX, 0xFFFFFFFF);

		// ridges, far to near, each followed by its band of mist
		float ridgeWidth = RIDGE_W * CELL * scale;
		for (int i = 0; i < RIDGES.length; i++) {
			layer(RIDGES[i], RIDGE_W, RIDGE_H, CELL * scale, top, seconds * RIDGE_SPEED[i] * scale, ridgeWidth, width, 0xFFFFFFFF);
			if (i < 3) {
				int bandY = Math.round(top + (30 + i * 7) * CELL * scale);
				int bandH = Math.max(8, Math.round(64 * scale));
				int middle = alpha(MIST, 0.16F);
				Ui.gradient(0, bandY, width, bandY + bandH / 2, alpha(MIST, 0.0F), middle);
				Ui.gradient(0, bandY + bandH / 2, width, bandY + bandH, middle, alpha(MIST, 0.0F));
			}
		}

		// fade towards the ground
		int fade = Math.round(96 * scale);
		Ui.gradient(0, height - fade, width, height, alpha(GROUND, 0.0F), alpha(GROUND, 1.0F));
	}

	/** The landscape inside a box (the settings' previews), magnified {@code zoom} times around its centre. */
	public static void drawIn(final int x, final int y, final int width, final int height, final float zoom) {
		Ui.scissor(x, y, width, height);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + width / 2.0F, y + height / 2.0F, 0.0F);
		GlStateManager.scale(zoom, zoom, 1.0F);
		GlStateManager.translate(-width / 2.0F, -height / 2.0F, 0.0F);
		draw(width, height);
		GlStateManager.popMatrix();
		Ui.endScissor();
	}

	/** One looping layer: drawn twice side by side, moved right by {@code offset} (wrapped on a tile). */
	private static void layer(final ResourceLocation texture, final int texW, final int texH, final float pixel, final float top,
		final float offset, final float tileWidth, final int screenWidth, final int color) {
		float shift = offset % tileWidth;
		for (float x = shift - tileWidth; x < screenWidth; x += tileWidth) {
			Ui.blit(texture, x, top, 0.0F, 0.0F, texW * pixel, texH * pixel, texW, texH, texW, texH, color);
		}
	}

	/** Soft round glow: a few stacked translucent rings, cheap and pixel-art friendly. */
	private static void glow(final float centerX, final float centerY, final float radius) {
		for (int ring = 6; ring >= 1; ring--) {
			float r = radius * ring / 6.0F;
			int color = alpha(0xBCD4FF, 0.035F);
			int steps = Math.max(6, Math.round(r / 2.0F));
			for (int s = -steps; s <= steps; s++) {
				float dy = r * s / steps;
				float half = (float)Math.sqrt(Math.max(0.0F, r * r - dy * dy));
				int y = Math.round(centerY + dy);
				Ui.fill(Math.round(centerX - half), y, Math.round(centerX + half), y + Math.max(1, Math.round(r / steps)), color);
			}
		}
	}

	private static int alpha(final int rgb, final float alpha) {
		return Math.round(255 * Math.max(0.0F, Math.min(1.0F, alpha))) << 24 | rgb & 0xFFFFFF;
	}
}
