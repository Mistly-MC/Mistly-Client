package online.mistylauncher.legacy.features;

import java.util.Locale;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderTNTPrimed;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import online.mistylauncher.legacy.Config;

/**
 * The game's renderers of dropped items and lit TNT, with the Mistly settings on top, as in the
 * Fabric versions:
 * <ul>
 * <li>Item physics: dropped items lie flat on the ground, without bobbing or spinning.</li>
 * <li>2D items: dropped items drawn flat, always facing you.</li>
 * <li>TNT countdown: the seconds left over lit TNT ("2.35"), green, then yellow, orange, red.</li>
 * </ul>
 * Both settings off, the game's own drawing is used as it is.
 */
public final class Renderers {
	private Renderers() {
	}

	/** In pre-initialisation: the game makes its renderers from these factories. */
	public static void register() {
		RenderingRegistry.registerEntityRenderingHandler(EntityItem.class, new IRenderFactory<EntityItem>() {
			@Override
			public net.minecraft.client.renderer.entity.Render<? super EntityItem> createRenderFor(final RenderManager manager) {
				return new Items(manager, Minecraft.getMinecraft().getRenderItem());
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityTNTPrimed.class, new IRenderFactory<EntityTNTPrimed>() {
			@Override
			public net.minecraft.client.renderer.entity.Render<? super EntityTNTPrimed> createRenderFor(final RenderManager manager) {
				return new Tnt(manager);
			}
		});
	}

	/** Dropped items. */
	private static final class Items extends RenderEntityItem {
		private final RenderItem items;
		private final Random random = new Random();

		Items(final RenderManager manager, final RenderItem items) {
			super(manager, items);
			this.items = items;
		}

		/** How many copies the game draws for a stack of this size. */
		private static int copies(final ItemStack stack) {
			int size = stack.stackSize;
			return size > 48 ? 5 : size > 32 ? 4 : size > 16 ? 3 : size > 1 ? 2 : 1;
		}

		@Override
		public void doRender(final EntityItem entity, final double x, final double y, final double z, final float yaw, final float partialTicks) {
			Config config = Config.get();
			ItemStack stack = entity.getEntityItem();
			if (!config.itemPhysics && !config.items2D || stack == null || stack.getItem() == null) {
				super.doRender(entity, x, y, z, yaw, partialTicks);
				return;
			}
			this.random.setSeed(187L);
			this.bindEntityTexture(entity);
			this.renderManager.renderEngine.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
			GlStateManager.enableRescaleNormal();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.pushMatrix();
			IBakedModel model = this.items.getItemModelMesher().getItemModel(stack);
			boolean block = model.isGui3d();
			GlStateManager.translate((float)x, (float)y, (float)z);
			if (config.items2D) {
				// flat, facing the camera, standing on the ground
				GlStateManager.translate(0.0F, block ? 0.125F : 0.13F, 0.0F);
				GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
				GlStateManager.scale(1.0F, 1.0F, 0.02F);
				this.renderOne(stack, model);
			} else {
				// lying on the ground, turned at random (each item its own way), not moving
				GlStateManager.translate(0.0F, block ? 0.125F : 0.02F, 0.0F);
				GlStateManager.rotate(entity.hoverStart * 57.29578F, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
				int copies = copies(stack);
				for (int i = 0; i < copies; i++) {
					GlStateManager.pushMatrix();
					if (i > 0) {
						if (block) {
							GlStateManager.translate((this.random.nextFloat() * 2.0F - 1.0F) * 0.15F, (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F, 0.0F);
						} else {
							GlStateManager.translate(0.0F, 0.0F, -0.046875F * i);
						}
					}
					this.renderOne(stack, model);
					GlStateManager.popMatrix();
				}
			}
			GlStateManager.popMatrix();
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableBlend();
			this.bindEntityTexture(entity);
			this.renderManager.renderEngine.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
		}

		private void renderOne(final ItemStack stack, final IBakedModel model) {
			GlStateManager.pushMatrix();
			IBakedModel ground = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GROUND);
			this.items.renderItem(stack, ground);
			GlStateManager.popMatrix();
		}
	}

	/** Lit TNT, with its countdown. */
	private static final class Tnt extends RenderTNTPrimed {
		Tnt(final RenderManager manager) {
			super(manager);
		}

		@Override
		public void doRender(final EntityTNTPrimed entity, final double x, final double y, final double z, final float yaw, final float partialTicks) {
			super.doRender(entity, x, y, z, yaw, partialTicks);
			if (Config.get().tntCountdown) {
				float seconds = Math.max(0.0F, (entity.fuse - partialTicks + 1.0F) / 20.0F);
				this.renderLivingLabel(entity, color(seconds) + String.format(Locale.ROOT, "%.2f", seconds), x, y, z, 64);
			}
		}

		/** Green above 3 s, then yellow, orange, and red under 1 s. */
		private static String color(final float seconds) {
			return seconds > 3.0F ? "§a" : seconds > 2.0F ? "§e" : seconds > 1.0F ? "§6" : "§c";
		}
	}
}
