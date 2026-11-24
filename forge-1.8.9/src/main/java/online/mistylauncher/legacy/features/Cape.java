package online.mistylauncher.legacy.features;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import online.mistylauncher.legacy.Config;

/**
 * The Misty cape: the one equipped in the launcher ({@code client.json} in Misty's folder, read
 * again when it changes), drawn on your player as the game draws a cape.
 */
public final class Cape {
	private static final ResourceLocation MISTY = new ResourceLocation("misty", "textures/cape/misty.png");
	private static final ResourceLocation EARLY = new ResourceLocation("misty", "textures/cape/early.png");
	private static final long REFRESH_MS = 2000;

	private static ResourceLocation equipped;
	private static long readAt;
	private static long modified = -1;

	private Cape() {
	}

	/** One cape layer on each player renderer (default and slim arms). */
	public static void addLayers() {
		for (RenderPlayer renderer : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()) {
			renderer.addLayer(new Layer(renderer));
		}
	}

	/** Misty's data folder: the one the launcher gives the game, else %LOCALAPPDATA%\Misty. */
	private static File launcherDir() {
		String given = System.getProperty("misty.launcher.dir");
		if (given != null && !given.isEmpty()) {
			return new File(given);
		}
		String local = System.getenv("LOCALAPPDATA");
		return local == null ? null : new File(local, "Misty");
	}

	/** The cape equipped in the launcher, null for none (elytra and hats have no 1.8.9 version). */
	static ResourceLocation equipped() {
		long now = System.currentTimeMillis();
		if (now - readAt < REFRESH_MS) {
			return equipped;
		}
		readAt = now;
		File dir = launcherDir();
		File file = dir == null ? null : new File(dir, "client.json");
		if (file == null || !file.isFile()) {
			equipped = null;
			modified = -1;
			return null;
		}
		if (file.lastModified() != modified) {
			modified = file.lastModified();
			try {
				equipped = fromJson(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
			} catch (IOException e) {
				equipped = null;
			}
		}
		return equipped;
	}

	/** {"cosmetic": "misty_cape"} → the Misty cape. */
	static ResourceLocation fromJson(final String json) {
		try {
			com.google.gson.JsonObject object = new com.google.gson.JsonParser().parse(json).getAsJsonObject();
			String cosmetic = object.has("cosmetic") && object.get("cosmetic").isJsonPrimitive() ? object.get("cosmetic").getAsString() : "";
			if ("misty_cape".equals(cosmetic)) {
				return MISTY;
			}
			if ("early_cape".equals(cosmetic)) {
				return EARLY;
			}
		} catch (RuntimeException e) {
			// not JSON: no cape
		}
		return null;
	}

	/** Drawn after the player's model, as the game's own cape layer (LayerCape) draws a cape. */
	private static final class Layer implements LayerRenderer<AbstractClientPlayer> {
		private final RenderPlayer renderer;

		Layer(final RenderPlayer renderer) {
			this.renderer = renderer;
		}

		@Override
		public void doRenderLayer(final AbstractClientPlayer player, final float limbSwing, final float limbSwingAmount,
			final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
			Minecraft minecraft = Minecraft.getMinecraft();
			if (player != minecraft.thePlayer || player.isInvisible() || !Config.get().cape) {
				return;
			}
			ResourceLocation texture = equipped();
			if (texture == null) {
				return;
			}
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			minecraft.getTextureManager().bindTexture(texture);
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
			float lift = MathHelper.clamp_float((float) dy * 10.0F, -6.0F, 32.0F);
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
}
