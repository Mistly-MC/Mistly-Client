package online.mistylauncher.legacy.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.net.MistyPlayers;

/**
 * The Mistly cosmetics on the players: yours (equipped in the launcher or the wardrobe) and the
 * other Mistly players' (the Mistly network says what they wear). The cape is drawn as the game
 * draws a cape, the Cloudly hat floats over the head.
 */
public final class Cape {
	private static final ResourceLocation CLOUDLY = new ResourceLocation("misty", "textures/entity/cloudly.png");
	private static ModelRenderer cloud;

	private Cape() {
	}

	/** The cloud alone (as in the Fabric versions), y -8 being the top of the head. */
	private static ModelRenderer cloud() {
		if (cloud == null) {
			ModelBase model = new ModelBase() {
			};
			model.textureWidth = 64;
			model.textureHeight = 64;
			cloud = new ModelRenderer(model);
			cloud.setTextureOffset(0, 0).addBox(-5.0F, -12.5F, -4.0F, 10, 3, 8);
			cloud.setTextureOffset(0, 12).addBox(-3.0F, -15.0F, -3.0F, 6, 3, 6);
			cloud.setTextureOffset(26, 12).addBox(0.0F, -14.0F, 0.5F, 4, 2, 4);
			cloud.setTextureOffset(0, 22).addBox(-7.0F, -12.0F, -3.0F, 3, 2, 6);
			cloud.setTextureOffset(20, 22).addBox(4.0F, -12.0F, -2.5F, 3, 2, 5);
		}
		return cloud;
	}

	/**
	 * The Cloudly hat on a head: called once the head's transform is applied (its pivot and its
	 * turn), it floats up and down a little, glowing.
	 */
	public static void drawCloud(final float ageInTicks) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(CLOUDLY);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, MathHelper.sin(ageInTicks * 0.08F) * 0.03F, 0.0F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		float lightX = OpenGlHelper.lastBrightnessX;
		float lightY = OpenGlHelper.lastBrightnessY;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
		cloud().render(0.0625F);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	/** Drawn after the player's model, as the game's own cape layer (LayerCape) draws a cape. */
	public static final class CapeLayer implements LayerRenderer<AbstractClientPlayer> {
		private final RenderPlayer renderer;

		public CapeLayer(final RenderPlayer renderer) {
			this.renderer = renderer;
		}

		@Override
		public void doRenderLayer(final AbstractClientPlayer player, final float limbSwing, final float limbSwingAmount,
			final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
			if (player.isInvisible() || !Config.get().cape) {
				return;
			}
			ResourceLocation texture = Outfit.capeTexture(MistyPlayers.back(player.getUniqueID()));
			if (texture == null) {
				return;
			}
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, 0.0F, 0.125F);
			// how the cape swings: the player's motion, as LayerCape computes it
			double dx = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * partialTicks
				- (player.prevPosX + (player.posX - player.prevPosX) * partialTicks);
			double dy = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * partialTicks
				- (player.prevPosY + (player.posY - player.prevPosY) * partialTicks);
			double dz = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * partialTicks
				- (player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);
			float yaw = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
			double sin = MathHelper.sin(yaw * (float) Math.PI / 180.0F);
			double cos = -MathHelper.cos(yaw * (float) Math.PI / 180.0F);
			float lift = MathHelper.clamp((float) dy * 10.0F, -6.0F, 32.0F);
			float back = Math.max(0.0F, (float) (dx * sin + dz * cos) * 100.0F);
			float side = (float) (dx * cos - dz * sin) * 100.0F;
			float bob = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
			float walked = player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks;
			lift += MathHelper.sin(walked * 6.0F) * 32.0F * bob;
			if (player.isSneaking()) {
				lift += 25.0F;
			}
			GlStateManager.rotate(6.0F + back / 2.0F + lift, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(side / 2.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(-side / 2.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
			this.renderer.getMainModel().renderCape(0.0625F);
			GlStateManager.popMatrix();
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}

	/** The Cloudly hat, on the head as it turns. */
	public static final class HatLayer implements LayerRenderer<AbstractClientPlayer> {
		private final RenderPlayer renderer;

		public HatLayer(final RenderPlayer renderer) {
			this.renderer = renderer;
		}

		@Override
		public void doRenderLayer(final AbstractClientPlayer player, final float limbSwing, final float limbSwingAmount,
			final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
			if (player.isInvisible() || !Config.get().cape || !Outfit.CLOUDLY.equals(MistyPlayers.hat(player.getUniqueID()))) {
				return;
			}
			GlStateManager.pushMatrix();
			if (player.isSneaking()) {
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
			}
			this.renderer.getMainModel().bipedHead.postRender(0.0625F);
			drawCloud(ageInTicks);
			GlStateManager.popMatrix();
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}
}
