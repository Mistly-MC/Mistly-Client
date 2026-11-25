package online.mistylauncher.legacy.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.features.Cape;
import online.mistylauncher.legacy.features.Outfit;
import org.lwjgl.input.Mouse;

/**
 * You in 3D, as in the Fabric versions: your skin, the cape and the hat you wear. It turns slowly
 * on its own; drag to turn it yourself. No entity is needed (the title screen has no world): the
 * player model is drawn by hand, as the game's player renderer draws it.
 */
public class PlayerPreview extends MistyButton {
	/** Degrees per second of the slow turn, while nobody drags. */
	private static final float AUTO_TURN = 18.0F;
	private static final float PLAYER_HEIGHT = 1.8F;
	/** The player is 1.8 blocks tall: a little room above the head and under the feet. */
	private static final float HEIGHT_IN_BLOCKS = 2.05F;
	/** Room added above the head for a hat (it floats up to half a block over the head). */
	private static final float HAT_ROOM = 0.5F;

	private static ModelPlayer wide;
	private static ModelPlayer slim;

	private float rotationX;
	private float rotationY = -25.0F;
	private float headYaw;
	private float headPitch;
	private long lastFrame = System.currentTimeMillis();
	private long draggedAt;
	private boolean dragging;
	private int lastX;
	private int lastY;

	public PlayerPreview(final int x, final int y, final int width, final int height) {
		super(x, y, width, height, "", null);
	}

	@Override
	public void press() {
	}

	@Override
	public boolean mousePressed(final Minecraft minecraft, final int mouseX, final int mouseY) {
		if (this.hovered(mouseX, mouseY)) {
			this.dragging = true;
			this.lastX = mouseX;
			this.lastY = mouseY;
			return true;
		}
		return false;
	}

	@Override
	public void mouseReleased(final int mouseX, final int mouseY) {
		this.dragging = false;
	}

	@Override
	public void playPressSound(final net.minecraft.client.audio.SoundHandler sounds) {
	}

	private static ModelPlayer model(final boolean slimArms) {
		if (slimArms) {
			if (slim == null) {
				slim = new ModelPlayer(0.0F, true);
			}
			return slim;
		}
		if (wide == null) {
			wide = new ModelPlayer(0.0F, false);
		}
		return wide;
	}

	@Override
	protected void drawContents(final int mouseX, final int mouseY) {
		long now = System.currentTimeMillis();
		float seconds = Math.min(0.1F, (now - this.lastFrame) / 1000.0F);
		this.lastFrame = now;
		if (this.dragging && Mouse.isButtonDown(0)) {
			// both ways as in the Fabric versions
			this.rotationX = Ui.clamp(this.rotationX - (mouseY - this.lastY) * 1.5F, -35.0F, 35.0F);
			this.rotationY -= (mouseX - this.lastX) * 2.5F;
			this.lastX = mouseX;
			this.lastY = mouseY;
			this.draggedAt = now;
		} else {
			this.dragging = false;
		}
		if (now - this.draggedAt > 2500L) {
			this.rotationY = MathHelper.wrapDegrees(this.rotationY + AUTO_TURN * seconds);
		}
		boolean cosmetics = Config.get().cape;
		ResourceLocation cape = cosmetics ? Outfit.capeTexture() : null;
		boolean hat = cosmetics && Outfit.CLOUDLY.equals(Outfit.hat());
		float ageInTicks = (now % 1_000_000L) / 50.0F;

		// the head looks at the mouse (as in the inventory) as long as the body faces you
		float headX = this.x + this.width / 2.0F;
		float headY = this.y + this.height * 0.18F;
		float lookX = (float)Math.atan((headX - mouseX) / 40.0F);
		float lookY = (float)Math.atan((headY - mouseY) / 40.0F);
		float facing = MathHelper.wrapDegrees(this.rotationY);
		float weight = Ui.clamp(1.0F - Math.abs(facing) / 100.0F, 0.0F, 1.0F);
		float targetYaw = Ui.clamp(lookX * 60.0F - facing, -70.0F, 70.0F) * weight;
		float targetPitch = Ui.clamp(-lookY * 20.0F, -30.0F, 30.0F) * weight;
		this.headYaw = Ui.lerp(0.25F, this.headYaw, targetYaw);
		this.headPitch = Ui.lerp(0.25F, this.headPitch, targetPitch);

		// like the inventory: the model centred in its area, seen from a little above or below
		float room = hat ? HAT_ROOM : 0.0F;
		float scale = this.height / (HEIGHT_IN_BLOCKS + room);
		ModelPlayer model = model(SelfSkin.slim());
		this.pose(model, ageInTicks);

		Ui.scissor(this.x, this.y, this.width, this.height);
		boolean depth = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
		GlStateManager.enableDepth();
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.x + this.width / 2.0F, this.y + this.height / 2.0F, 50.0F);
		GlStateManager.scale(-scale, scale, scale);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(-this.rotationX, 1.0F, 0.0F, 0.0F);
		// the extra room goes above the head: the feet stay where they were
		GlStateManager.translate(0.0F, -(PLAYER_HEIGHT / 2.0F + 0.0625F + room / 2.0F), 0.0F);
		GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		// as the player renderer: the body turned, flipped, a little smaller
		GlStateManager.rotate(-this.rotationY, 0.0F, 1.0F, 0.0F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.scale(-0.9375F, -0.9375F, 0.9375F);
		GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		Minecraft minecraft = Minecraft.getMinecraft();
		minecraft.getTextureManager().bindTexture(SelfSkin.texture());
		float s = 0.0625F;
		model.bipedHead.render(s);
		model.bipedBody.render(s);
		model.bipedRightArm.render(s);
		model.bipedLeftArm.render(s);
		model.bipedRightLeg.render(s);
		model.bipedLeftLeg.render(s);
		model.bipedHeadwear.render(s);
		model.bipedBodyWear.render(s);
		model.bipedRightArmwear.render(s);
		model.bipedLeftArmwear.render(s);
		model.bipedRightLegwear.render(s);
		model.bipedLeftLegwear.render(s);
		if (cape != null) {
			// as the game's cape layer, for a player standing still
			minecraft.getTextureManager().bindTexture(cape);
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, 0.0F, 0.125F);
			GlStateManager.rotate(9.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
			model.renderCape(s);
			GlStateManager.popMatrix();
		}
		if (hat) {
			GlStateManager.pushMatrix();
			model.bipedHead.postRender(s);
			Cape.drawCloud(ageInTicks);
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		if (!depth) {
			GlStateManager.disableDepth();
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Ui.endScissor();
	}

	/** Standing still, the arms swaying a little, the head where it looks; each layer of the skin follows its part. */
	private void pose(final ModelPlayer model, final float ageInTicks) {
		model.bipedHead.rotateAngleY = this.headYaw * (float)Math.PI / 180.0F;
		model.bipedHead.rotateAngleX = this.headPitch * (float)Math.PI / 180.0F;
		model.bipedBody.rotateAngleX = 0.0F;
		model.bipedBody.rotateAngleY = 0.0F;
		model.bipedRightArm.rotateAngleX = MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		model.bipedLeftArm.rotateAngleX = -MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		model.bipedRightArm.rotateAngleY = 0.0F;
		model.bipedLeftArm.rotateAngleY = 0.0F;
		model.bipedRightArm.rotateAngleZ = MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		model.bipedLeftArm.rotateAngleZ = -(MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
		model.bipedRightLeg.rotateAngleX = 0.0F;
		model.bipedLeftLeg.rotateAngleX = 0.0F;
		model.bipedRightLeg.rotateAngleY = 0.0F;
		model.bipedLeftLeg.rotateAngleY = 0.0F;
		ModelBase.copyModelAngles(model.bipedHead, model.bipedHeadwear);
		ModelBase.copyModelAngles(model.bipedBody, model.bipedBodyWear);
		ModelBase.copyModelAngles(model.bipedRightArm, model.bipedRightArmwear);
		ModelBase.copyModelAngles(model.bipedLeftArm, model.bipedLeftArmwear);
		ModelBase.copyModelAngles(model.bipedRightLeg, model.bipedRightLegwear);
		ModelBase.copyModelAngles(model.bipedLeftLeg, model.bipedLeftLegwear);
	}
}
