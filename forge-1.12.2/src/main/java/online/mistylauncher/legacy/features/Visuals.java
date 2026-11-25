package online.mistylauncher.legacy.features;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.ui.Palette;

/**
 * Visual settings, as in the Fabric versions: motion blur, the enchantment glint's colour, low fire
 * and short swords. Done without changing what the resource packs give (the glint is recoloured as
 * it loads, the swords are scaled in hand), so they work with every pack.
 */
public final class Visuals {
	public static final int BLUR_MIN = 1;
	public static final int BLUR_MAX = 9;
	/** How far the flames go down (the screen is about 2 high). */
	private static final float FIRE_DROP = 0.3F;
	/** Size of a sword in hand. */
	private static final float SWORD_SCALE = 0.75F;

	private static final ResourceLocation GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	/** The glint is drawn tinted purple by the game: its texture is recoloured through that tint. */
	private static final float[] GLINT_TINT = {0.5F, 0.25F, 0.8F};
	private static final int VANILLA = -1;
	private static int glintApplied = VANILLA;

	private static boolean blurFailed;
	private static String blurLoaded;

	private Visuals() {
	}

	/** Each tick: the motion blur and the glint follow the settings. */
	public static void tick(final Minecraft minecraft) {
		blur(minecraft);
		glint(minecraft);
	}

	// ── motion blur ──

	public static int blurStrength() {
		return Math.max(BLUR_MIN, Math.min(BLUR_MAX, Config.get().motionBlurStrength));
	}

	/** Turned on again in the settings: tried again, even if it failed before. */
	public static void retryBlur() {
		blurFailed = false;
	}

	/**
	 * The game's post effect (as its "super secret settings"): each frame mixed with the ones
	 * before. Drawn before the HUD, which stays sharp.
	 */
	private static void blur(final Minecraft minecraft) {
		EntityRenderer renderer = minecraft.entityRenderer;
		Config config = Config.get();
		boolean wanted = config.motionBlur && !blurFailed && minecraft.world != null;
		String name = "misty:shaders/post/motion_blur_" + blurStrength() + ".json";
		ShaderGroup group = renderer.getShaderGroup();
		boolean ours = group != null && blurLoaded != null && group.getShaderGroupName().equals(blurLoaded);
		if (!wanted) {
			if (ours) {
				renderer.stopUseShader();
			}
			blurLoaded = null;
			return;
		}
		if (ours && blurLoaded.equals(name) && renderer.isShaderActive()) {
			return;
		}
		// another post effect (spectating a creeper…) is left alone
		if (group != null && !ours) {
			return;
		}
		try {
			renderer.loadShader(new ResourceLocation(name));
			blurLoaded = renderer.getShaderGroup() != null ? renderer.getShaderGroup().getShaderGroupName() : null;
			if (blurLoaded == null) {
				blurFailed = true;
			}
		} catch (RuntimeException e) {
			blurFailed = true;
			System.err.println("[Mistly Client] motion blur turned off, the effect does not work here: " + e);
		}
	}

	// ── glint colour ──

	/** The colour picked (RGB): the palette's, or the custom one after it. */
	public static int glintColor() {
		Config config = Config.get();
		if (config.glintColor >= Palette.COLORS.length) {
			return config.glintCustom & 0xFFFFFF;
		}
		return Palette.color(config.glintColor) & 0xFFFFFF;
	}

	private static void glint(final Minecraft minecraft) {
		int wanted = Config.get().glint ? glintColor() : VANILLA;
		if (wanted == glintApplied) {
			return;
		}
		minecraft.getTextureManager().deleteTexture(GLINT);
		if (wanted != VANILLA) {
			minecraft.getTextureManager().loadTexture(GLINT, new Tinted(wanted));
		}
		glintApplied = wanted;
	}

	/** The glint texture as the packs give it, each pixel as bright as it was, in the colour picked. */
	private static final class Tinted extends SimpleTexture {
		private final int rgb;

		Tinted(final int rgb) {
			super(GLINT);
			this.rgb = rgb;
		}

		@Override
		public void loadTexture(final IResourceManager resources) throws IOException {
			this.deleteGlTexture();
			InputStream in = resources.getResource(GLINT).getInputStream();
			BufferedImage image;
			try {
				image = TextureUtil.readBufferedImage(in);
			} finally {
				in.close();
			}
			// the game tints the glint purple: the colour divided by that tint gives the wanted one
			float[] wanted = {(this.rgb >> 16 & 255) / 255.0F, (this.rgb >> 8 & 255) / 255.0F, (this.rgb & 255) / 255.0F};
			float scale = 1.0F;
			for (int c = 0; c < 3; c++) {
				if (wanted[c] / GLINT_TINT[c] > scale) {
					scale = wanted[c] / GLINT_TINT[c];
				}
			}
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int argb = image.getRGB(x, y);
					int bright = Math.max(argb >> 16 & 0xFF, Math.max(argb >> 8 & 0xFF, argb & 0xFF));
					int pixel = argb & 0xFF000000;
					for (int c = 0; c < 3; c++) {
						int value = Math.round(bright * wanted[c] / GLINT_TINT[c] / scale);
						pixel |= Math.min(255, value) << (16 - 8 * c);
					}
					image.setRGB(x, y, pixel);
				}
			}
			TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), image, true, false);
		}
	}

	// ── low fire ──

	/** The flames over the screen while burning, lower (RenderBlockOverlayEvent, FIRE). False: the game's. */
	public static boolean fire(final Minecraft minecraft) {
		if (!Config.get().lowFire) {
			return false;
		}
		// as the game draws them (ItemRenderer.renderFireInFirstPerson), moved down
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
		GlStateManager.depthFunc(519);
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		for (int i = 0; i < 2; i++) {
			GlStateManager.pushMatrix();
			TextureAtlasSprite sprite = minecraft.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1");
			minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			float u0 = sprite.getMinU();
			float u1 = sprite.getMaxU();
			float v0 = sprite.getMinV();
			float v1 = sprite.getMaxV();
			GlStateManager.translate(-(i * 2 - 1) * 0.24F, -0.3F - FIRE_DROP, 0.0F);
			GlStateManager.rotate((i * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
			buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(-0.5, -0.5, -0.5).tex(u1, v1).endVertex();
			buffer.pos(0.5, -0.5, -0.5).tex(u0, v1).endVertex();
			buffer.pos(0.5, 0.5, -0.5).tex(u0, v0).endVertex();
			buffer.pos(-0.5, 0.5, -0.5).tex(u1, v0).endVertex();
			tessellator.draw();
			GlStateManager.popMatrix();
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		GlStateManager.depthFunc(515);
		return true;
	}

	// ── short swords ──

	private static final String[] SWORDS = {"wooden_sword", "stone_sword", "iron_sword", "golden_sword", "diamond_sword"};

	/** Once the item models are made: the swords' get their hand size from the setting. */
	public static void models(final IRegistry<ModelResourceLocation, IBakedModel> registry) {
		for (String sword : SWORDS) {
			ModelResourceLocation location = new ModelResourceLocation("minecraft:" + sword, "inventory");
			IBakedModel model = registry.getObject(location);
			if (model != null) {
				registry.putObject(location, new Sword(model));
			}
		}
	}

	/**
	 * A sword's model: everything as the model says, how it is held too; only in first person with
	 * short swords, scaled down around its middle (every item model says how it is held in 1.12.2).
	 */
	private static final class Sword implements IBakedModel {
		private final IBakedModel model;

		Sword(final IBakedModel model) {
			this.model = model;
		}

		@Override
		public Pair<? extends IBakedModel, Matrix4f> handlePerspective(final ItemCameraTransforms.TransformType type) {
			Pair<? extends IBakedModel, Matrix4f> held = this.model.handlePerspective(type);
			boolean firstPerson = type == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
				|| type == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
			if (!Config.get().shortSword || !firstPerson) {
				return held;
			}
			Matrix4f scale = new Matrix4f();
			scale.setIdentity();
			scale.setScale(SWORD_SCALE);
			Matrix4f matrix = held.getRight() != null ? new Matrix4f(held.getRight()) : new Matrix4f(scale);
			if (held.getRight() != null) {
				// the model is drawn centred after this matrix: the scale shrinks it around its middle
				matrix.mul(scale);
			}
			return Pair.of(held.getLeft(), matrix);
		}

		@Override
		public List<BakedQuad> getQuads(final IBlockState state, final EnumFacing side, final long rand) {
			return this.model.getQuads(state, side, rand);
		}

		@Override
		public boolean isAmbientOcclusion() {
			return this.model.isAmbientOcclusion();
		}

		@Override
		public boolean isGui3d() {
			return this.model.isGui3d();
		}

		@Override
		public boolean isBuiltInRenderer() {
			return this.model.isBuiltInRenderer();
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return this.model.getParticleTexture();
		}

		@SuppressWarnings("deprecation")
		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return this.model.getItemCameraTransforms();
		}

		@Override
		public ItemOverrideList getOverrides() {
			return this.model.getOverrides();
		}
	}
}
