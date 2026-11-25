package online.mistylauncher.legacy.mixin;

import java.util.Random;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import online.mistylauncher.legacy.Config;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dropped items, as in the Fabric versions: item physics (lying flat on the ground, without
 * bobbing or spinning) or 2D items (flat, always facing you). Both settings off, the game draws
 * them as it does.
 */
@Mixin(RenderEntityItem.class)
abstract class RenderEntityItemMixin extends Render<EntityItem> {
	@Shadow
	@Final
	private RenderItem itemRenderer;

	@Unique
	private final Random misty$random = new Random();

	protected RenderEntityItemMixin(final RenderManager manager) {
		super(manager);
	}

	/** How many copies the game draws for a stack of this size. */
	@Unique
	private static int misty$copies(final ItemStack stack) {
		int size = stack.getCount();
		return size > 48 ? 5 : size > 32 ? 4 : size > 16 ? 3 : size > 1 ? 2 : 1;
	}

	@Inject(method = "doRender(Lnet/minecraft/entity/item/EntityItem;DDDFF)V", at = @At("HEAD"), cancellable = true)
	private void misty$render(final EntityItem entity, final double x, final double y, final double z, final float yaw, final float partialTicks,
		final CallbackInfo ci) {
		Config config = Config.get();
		ItemStack stack = entity.getItem();
		if (!config.itemPhysics && !config.items2D || stack.isEmpty()) {
			return;
		}
		ci.cancel();
		this.misty$random.setSeed(187L);
		this.bindEntityTexture(entity);
		this.renderManager.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.pushMatrix();
		IBakedModel model = this.itemRenderer.getItemModelMesher().getItemModel(stack);
		boolean block = model.isGui3d();
		GlStateManager.translate((float)x, (float)y, (float)z);
		if (config.items2D) {
			// flat, facing the camera, standing on the ground
			GlStateManager.translate(0.0F, block ? 0.125F : 0.13F, 0.0F);
			GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(1.0F, 1.0F, 0.02F);
			this.misty$renderOne(stack, model);
		} else {
			// lying on the ground, turned at random (each item its own way), not moving
			GlStateManager.translate(0.0F, block ? 0.125F : 0.02F, 0.0F);
			GlStateManager.rotate(entity.hoverStart * 57.29578F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			int copies = misty$copies(stack);
			for (int i = 0; i < copies; i++) {
				GlStateManager.pushMatrix();
				if (i > 0) {
					if (block) {
						GlStateManager.translate((this.misty$random.nextFloat() * 2.0F - 1.0F) * 0.15F, (this.misty$random.nextFloat() * 2.0F - 1.0F) * 0.15F, 0.0F);
					} else {
						GlStateManager.translate(0.0F, 0.0F, -0.046875F * i);
					}
				}
				this.misty$renderOne(stack, model);
				GlStateManager.popMatrix();
			}
		}
		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		this.bindEntityTexture(entity);
		this.renderManager.renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
	}

	@Unique
	private void misty$renderOne(final ItemStack stack, final IBakedModel model) {
		GlStateManager.pushMatrix();
		IBakedModel ground = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GROUND, false);
		this.itemRenderer.renderItem(stack, ground);
		GlStateManager.popMatrix();
	}
}
