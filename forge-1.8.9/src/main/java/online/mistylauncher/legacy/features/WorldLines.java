package online.mistylauncher.legacy.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.ui.Palette;
import org.lwjgl.opengl.GL11;

/**
 * Lines drawn in the world once it is drawn, as in the Fabric versions:
 * <ul>
 * <li>Hitboxes: each entity's box, as F3 + B shows it (with the eyes' line and where it looks),
 * always on, in the colour picked behind the switch's gear.</li>
 * <li>Chunk viewer: the chunk borders (the game of 1.8.9 has no F3 + G): the corners of the
 * chunks around in the border colour, the walls of yours gridded every 2 blocks in the grid
 * colour.</li>
 * </ul>
 */
public final class WorldLines {
	private WorldLines() {
	}

	/** After the world is drawn (RenderWorldLastEvent). */
	public static void draw(final float partialTicks) {
		Minecraft minecraft = Minecraft.getMinecraft();
		Entity camera = minecraft.getRenderViewEntity();
		if (minecraft.theWorld == null || camera == null) {
			return;
		}
		Config config = Config.get();
		if (!config.hitboxes && !config.chunkViewer) {
			return;
		}
		double cx = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
		double cy = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
		double cz = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;
		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.depthMask(false);
		if (config.hitboxes) {
			hitboxes(minecraft, camera, cx, cy, cz, partialTicks);
		}
		if (config.chunkViewer) {
			chunks(camera, cx, cy, cz);
		}
		GL11.glLineWidth(1.0F);
		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}

	private static void hitboxes(final Minecraft minecraft, final Entity camera, final double cx, final double cy, final double cz, final float partialTicks) {
		int color = Palette.color(Config.get().hitboxColor);
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		GL11.glLineWidth(2.0F);
		for (Entity entity : minecraft.theWorld.loadedEntityList) {
			// as the game: not yourself in first person, not the invisible
			if (entity == camera && minecraft.gameSettings.thirdPersonView == 0 || entity.isInvisible()) {
				continue;
			}
			double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - cx;
			double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - cy;
			double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - cz;
			AxisAlignedBB box = entity.getEntityBoundingBox();
			float border = entity.getCollisionBorderSize();
			AxisAlignedBB shown = new AxisAlignedBB(box.minX - entity.posX + x - border, box.minY - entity.posY + y - border, box.minZ - entity.posZ + z - border,
				box.maxX - entity.posX + x + border, box.maxY - entity.posY + y + border, box.maxZ - entity.posZ + z + border);
			RenderGlobal.drawOutlinedBoundingBox(shown, r, g, b, 255);
			if (entity instanceof EntityLivingBase) {
				// the eyes' height, in red, as the game draws it
				RenderGlobal.drawOutlinedBoundingBox(new AxisAlignedBB(x - entity.width / 2.0, y + entity.getEyeHeight() - 0.01, z - entity.width / 2.0,
					x + entity.width / 2.0, y + entity.getEyeHeight() + 0.01, z + entity.width / 2.0), 255, 0, 0, 255);
			}
			// where it looks, in blue
			Vec3 look = entity.getLook(partialTicks);
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer buffer = tessellator.getWorldRenderer();
			buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
			buffer.pos(x, y + entity.getEyeHeight(), z).color(0, 0, 255, 255).endVertex();
			buffer.pos(x + look.xCoord * 2.0, y + entity.getEyeHeight() + look.yCoord * 2.0, z + look.zCoord * 2.0).color(0, 0, 255, 255).endVertex();
			tessellator.draw();
		}
	}

	private static void chunks(final Entity camera, final double cx, final double cy, final double cz) {
		int border = Palette.color(Config.get().chunkBorderColor);
		int grid = Palette.color(Config.get().chunkGridColor);
		double x0 = (Math.floor(camera.posX / 16.0) * 16.0) - cx;
		double z0 = (Math.floor(camera.posZ / 16.0) * 16.0) - cz;
		double bottom = -cy;
		double top = 256.0 - cy;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer buffer = tessellator.getWorldRenderer();
		// the corners of the chunks around (border colour)
		GL11.glLineWidth(1.0F);
		buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
		for (int i = -16; i <= 32; i += 16) {
			for (int j = -16; j <= 32; j += 16) {
				line(buffer, x0 + i, bottom, z0 + j, x0 + i, top, z0 + j, border, 0.8F);
			}
		}
		tessellator.draw();
		// the walls of your chunk, gridded every 2 blocks (grid colour)
		buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
		for (int i = 2; i < 16; i += 2) {
			line(buffer, x0 + i, bottom, z0, x0 + i, top, z0, grid, 0.6F);
			line(buffer, x0 + i, bottom, z0 + 16, x0 + i, top, z0 + 16, grid, 0.6F);
			line(buffer, x0, bottom, z0 + i, x0, top, z0 + i, grid, 0.6F);
			line(buffer, x0 + 16, bottom, z0 + i, x0 + 16, top, z0 + i, grid, 0.6F);
		}
		int fromY = Math.max(0, (int)Math.floor(cy) - 32) & ~1;
		int toY = Math.min(256, (int)Math.floor(cy) + 32);
		for (int y = fromY; y <= toY; y += 2) {
			double ly = y - cy;
			int color = y % 16 == 0 ? border : grid;
			line(buffer, x0, ly, z0, x0 + 16, ly, z0, color, 0.6F);
			line(buffer, x0, ly, z0 + 16, x0 + 16, ly, z0 + 16, color, 0.6F);
			line(buffer, x0, ly, z0, x0, ly, z0 + 16, color, 0.6F);
			line(buffer, x0 + 16, ly, z0, x0 + 16, ly, z0 + 16, color, 0.6F);
		}
		tessellator.draw();
		// your chunk's corners, thicker
		GL11.glLineWidth(2.0F);
		buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
		for (int i = 0; i <= 16; i += 16) {
			for (int j = 0; j <= 16; j += 16) {
				line(buffer, x0 + i, bottom, z0 + j, x0 + i, top, z0 + j, border, 1.0F);
			}
		}
		tessellator.draw();
	}

	private static void line(final WorldRenderer buffer, final double x1, final double y1, final double z1, final double x2, final double y2, final double z2,
		final int color, final float alpha) {
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		int a = Math.round(alpha * 255);
		buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
		buffer.pos(x2, y2, z2).color(r, g, b, a).endVertex();
	}
}
