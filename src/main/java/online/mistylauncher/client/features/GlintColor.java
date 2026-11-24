package online.mistylauncher.client.features;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.compat.Versions;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.ui.Palette;

/**
 * Enchantment colour: the glint of enchanted items and armor in the colour picked behind the gear,
 * one of the palette's or a custom one (red, green, blue).
 * <p>
 * The game's glint textures are loaded as usual (from the resource packs, with their blur) and
 * recoloured: each pixel keeps its brightness, in the colour picked. They load again by themselves
 * when the resource packs change. Turned off, the game's textures come back.
 */
public final class GlintColor {
	/** The glints recoloured: on items, and on armor (on entities before 1.21.2). */
	private static final Identifier[] GLINTS = {
		Identifier.withDefaultNamespace("textures/misc/enchanted_glint_item.png"),
		//? if >=1.21.2 {
		Identifier.withDefaultNamespace("textures/misc/enchanted_glint_armor.png")
		//?} else {
		/*Identifier.withDefaultNamespace("textures/misc/enchanted_glint_entity.png")
		*///?}
	};
	/** The game's own glint, untouched. */
	private static final int VANILLA = -1;

	/** The colour (RGB) the glint textures have now, or VANILLA. */
	private static int applied = VANILLA;

	private GlintColor() {
	}

	/** The colour picked (RGB): the palette's, or the custom one after it. */
	public static int color() {
		MistyConfig config = MistyClient.config();
		if (config.glintColor >= Palette.COLORS.length) {
			return config.glintCustom & 0xFFFFFF;
		}
		return Palette.color(config.glintColor) & 0xFFFFFF;
	}

	/** Each tick: the glint textures follow the setting (on, off, another colour). */
	public static void tick(final Minecraft minecraft) {
		int wanted = MistyClient.config().glint ? color() : VANILLA;
		if (wanted == applied) {
			return;
		}
		TextureManager textures = minecraft.getTextureManager();
		for (Identifier glint : GLINTS) {
			if (wanted == VANILLA) {
				// the game loads its own again when it next draws a glint
				textures.release(glint);
			} else {
				//? if >=1.21.5 {
				textures.registerAndLoad(glint, new Tinted(glint, wanted));
				//?} else {
				/*textures.register(glint, new Tinted(glint, wanted));
				*///?}
			}
		}
		applied = wanted;
	}

	/**
	 * Each pixel in {@code rgb}, as bright as it was: the glint is added to what is under it, so
	 * its brightness is its strength, and its colour is the picked one.
	 */
	static void recolor(final NativeImage image, final int rgb) {
		int r = rgb >> 16 & 0xFF;
		int g = rgb >> 8 & 0xFF;
		int b = rgb & 0xFF;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = Versions.getPixel(image, x, y);
				int bright = Math.max(argb >> 16 & 0xFF, Math.max(argb >> 8 & 0xFF, argb & 0xFF));
				Versions.setPixel(image, x, y, argb & 0xFF000000 | r * bright / 255 << 16 | g * bright / 255 << 8 | b * bright / 255);
			}
		}
	}

	/** A glint texture as the game loads it (with its blur), then recoloured. */
	private static final class Tinted extends SimpleTexture {
		private final int rgb;

		Tinted(final Identifier location, final int rgb) {
			super(location);
			this.rgb = rgb;
		}

		//? if >=1.21.5 {
		@Override
		public net.minecraft.client.renderer.texture.TextureContents loadContents(final ResourceManager resources) throws IOException {
			net.minecraft.client.renderer.texture.TextureContents contents = super.loadContents(resources);
			recolor(contents.image(), this.rgb);
			return contents;
		}
		//?} else {
		/*@Override
		protected SimpleTexture.TextureImage getTextureImage(final ResourceManager resources) {
			SimpleTexture.TextureImage image = super.getTextureImage(resources);
			try {
				recolor(image.getImage(), this.rgb);
			} catch (IOException e) {
				// the game's own error texture, left as it is
			}
			return image;
		}
		*///?}
	}
}
