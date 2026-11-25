package online.mistylauncher.client.ui;

import online.mistylauncher.client.compat.Versions;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? if >=1.21.2 {
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
//?}
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.PlayerSkin;
import online.mistylauncher.client.cosmetics.Cosmetic;
import online.mistylauncher.client.cosmetics.Cosmetics;
//? if >=1.21.2 {
import online.mistylauncher.client.cosmetics.HatHolder;
//?}
import online.mistylauncher.client.cosmetics.Outfit;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The player in 3D, drawn by the game's own player renderer (like in the inventory): their
 * skin and what they wear on the back (the Misty cape, or their own Mojang cape), with the
 * right depth from every side. It turns slowly on its own; drag to turn it yourself.
 *
 * <p>No entity is needed: a player render state is filled by hand, so it works on the title
 * screen too. The game draws only one such 3D picture per area of a screen, so the body and the
 * cape must be one render, not two.
 */
public class PlayerPreview extends AbstractWidget {
	/** Degrees per second of the slow turn, while nobody drags. */
	private static final float AUTO_TURN = 18.0F;
	private static final float PLAYER_HEIGHT = 1.8F;
	/** The player is 1.8 blocks tall: a little room above the head and under the feet. */
	private static final float HEIGHT_IN_BLOCKS = 2.05F;
	/** Room added above the head for a hat (it floats up to half a block over the head). */
	private static final float HAT_ROOM = 0.5F;

	private final Supplier<Outfit> outfit;
	//? if >=1.21.2 {
	private final AvatarRenderState state = new AvatarRenderState();
	//?}
	private float rotationX = 0.0F;
	private float rotationY = -25.0F;
	private float headYaw;
	private float headPitch;
	private long lastFrame = Util.getMillis();
	/** Last time the player turned it: the slow turn starts again a moment after. */
	private long draggedAt;

	public PlayerPreview(final int x, final int y, final int width, final int height, final Supplier<Outfit> outfit) {
		super(x, y, width, height, Component.empty());
		this.outfit = outfit;
		//? if >=1.21.2 {
		AvatarRenderState s = this.state;
		s.entityType = Versions.playerType();
		//? if >=1.21.9 {
		s.lightCoords = LightCoordsUtil.FULL_BRIGHT;
		//?}
		s.boundingBoxWidth = 0.6F;
		s.boundingBoxHeight = PLAYER_HEIGHT;
		s.eyeHeight = 1.62F;
		s.scale = 1.0F;
		s.ageScale = 1.0F;
		s.pose = Pose.STANDING;
		// every layer of the skin (hat, jacket, sleeves, trousers)
		s.showHat = true;
		s.showJacket = true;
		s.showLeftSleeve = true;
		s.showRightSleeve = true;
		s.showLeftPants = true;
		s.showRightPants = true;
		// the cape hangs a little away from the back, as when standing in the game
		s.capeLean = 6.0F;
		//?}
	}

	/** The local player's skin (kept and asked again when Mojang refused it at start, see SelfSkin). */
	public static PlayerSkin skin() {
		Minecraft minecraft = Minecraft.getInstance();
		//? if >=1.21.9 {
		return minecraft.getSkinManager().createLookup(online.mistylauncher.client.features.SelfSkin.profile(), false).get();
		//?} else if >=1.20.2 {
		/*return minecraft.getSkinManager().getInsecureSkin(online.mistylauncher.client.features.SelfSkin.profile());
		*///?} else {
		/*// before 1.20.2 the skin manager gives the textures one by one
		com.mojang.authlib.GameProfile profile = online.mistylauncher.client.features.SelfSkin.profile();
		var textures = minecraft.getSkinManager().getInsecureSkinInformation(profile);
		var body = textures.get(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN);
		var cape = textures.get(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.CAPE);
		String model = body != null ? body.getMetadata("model") : net.minecraft.client.resources.DefaultPlayerSkin.getSkinModelName(Versions.id(profile));
		return new PlayerSkin(minecraft.getSkinManager().getInsecureSkinLocation(profile), body != null ? body.getUrl() : null,
			cape != null ? minecraft.getSkinManager().registerTexture(cape, com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.CAPE) : null, null,
			"slim".equals(model) ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE, false);
		*///?}
	}

	@Override
	protected void extractWidgetRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		long now = Util.getMillis();
		float seconds = Math.min(0.1F, (now - this.lastFrame) / 1000.0F);
		this.lastFrame = now;
		if (now - this.draggedAt > 2500L) {
			this.rotationY = Mth.wrapDegrees(this.rotationY + AUTO_TURN * seconds);
		}
		// what is worn: a Misty cape (its texture also holds the elytra), or the own Mojang cape;
		// the Misty elytra alone mean no cape on the back; and the hat
		Outfit worn = this.outfit.get();
		PlayerSkin skin = worn.back() == Cosmetic.ELYTRA ? skin() : Cosmetics.wear(worn.back(), skin());
		boolean cape = worn.back() != Cosmetic.ELYTRA && Cosmetics.hasCape(skin);
		float ageInTicks = (now % 1_000_000L) / 50.0F;
		// the whole body turns; the head looks at the mouse (as in the inventory), as long as the
		// body faces you: seen from behind, the head goes back to straight ahead
		float headX = this.getX() + this.getWidth() / 2.0F;
		float headY = this.getY() + this.getHeight() * 0.18F;
		float lookX = (float)Math.atan((headX - mouseX) / 40.0F);
		float lookY = (float)Math.atan((headY - mouseY) / 40.0F);
		float facing = Mth.wrapDegrees(this.rotationY);
		float weight = Mth.clamp(1.0F - Math.abs(facing) / 100.0F, 0.0F, 1.0F);
		float targetYaw = Mth.clamp(lookX * 60.0F - facing, -70.0F, 70.0F) * weight;
		float targetPitch = Mth.clamp(-lookY * 20.0F, -30.0F, 30.0F) * weight;
		// eased, so the head turns smoothly instead of jumping
		this.headYaw = Mth.lerp(0.25F, this.headYaw, targetYaw);
		this.headPitch = Mth.lerp(0.25F, this.headPitch, targetPitch);

		// like the inventory: the model centred in its area, seen from a little above or below
		float room = worn.hat() == Cosmetic.NONE ? 0.0F : HAT_ROOM;
		float scale = this.getHeight() / (HEIGHT_IN_BLOCKS + room);
		// the extra room goes above the head: the feet stay where they were
		Vector3f translation = new Vector3f(0.0F, PLAYER_HEIGHT / 2.0F + 0.0625F + room / 2.0F, 0.0F);
		Quaternionf camera = new Quaternionf().rotateX(this.rotationX * Mth.DEG_TO_RAD);
		Quaternionf pose = new Quaternionf().rotateZ((float)Math.PI).mul(camera);
		//? if >=1.21.2 {
		AvatarRenderState s = this.state;
		s.skin = skin;
		s.showCape = cape;
		((HatHolder)s).misty$setHat(worn.hat());
		s.ageInTicks = ageInTicks;
		s.bodyRot = 180.0F + this.rotationY;
		s.yRot = this.headYaw;
		s.xRot = this.headPitch;
		graphics.entity(s, scale, translation, pose, camera, this.getX(), this.getY(), this.getRight(), this.getBottom());
		//?} else {
		/*// no player render state before 1.21.2: the player model is drawn by hand, as the game's
		// player renderer would (body turned, flipped, a little smaller), with its cape and hat
		graphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
		com.mojang.blaze3d.vertex.PoseStack stack = graphics.pose();
		stack.pushPose();
		stack.translate(this.getX() + this.getWidth() / 2.0F, this.getY() + this.getHeight() / 2.0F, 50.0F);
		stack.scale(scale, scale, -scale);
		stack.translate(translation.x, translation.y, translation.z);
		stack.mulPose(pose);
		com.mojang.blaze3d.platform.Lighting.setupForEntityInInventory();
		stack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-this.rotationY));
		stack.scale(-0.9375F, -0.9375F, 0.9375F);
		stack.translate(0.0F, -1.501F, 0.0F);
		net.minecraft.client.model.PlayerModel<net.minecraft.world.entity.LivingEntity> model = model(skin.model() == PlayerSkin.Model.SLIM);
		pose(model, ageInTicks);
		net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers = graphics.bufferSource();
		int light = LightCoordsUtil.FULL_BRIGHT;
		int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;
		Versions.renderModel(model, stack, buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(skin.texture())), light, overlay);
		if (cape) {
			// as the game's cape layer, for a player standing still
			stack.pushPose();
			stack.translate(0.0F, 0.0F, 0.125F);
			stack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(9.0F));
			stack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
			model.renderCloak(stack, buffers.getBuffer(net.minecraft.client.renderer.RenderType.entitySolid(skin.capeTexture())), light, overlay);
			stack.popPose();
		}
		if (worn.hat() == Cosmetic.CLOUDLY) {
			online.mistylauncher.client.cosmetics.CloudHatLayer.draw(stack, buffers, model.head, ageInTicks, overlay);
		}
		graphics.flush();
		com.mojang.blaze3d.platform.Lighting.setupFor3DItems();
		stack.popPose();
		graphics.disableScissor();
		*///?}
	}

	//? if <1.21.2 {
	/*private static net.minecraft.client.model.PlayerModel<net.minecraft.world.entity.LivingEntity> wide;
	private static net.minecraft.client.model.PlayerModel<net.minecraft.world.entity.LivingEntity> slim;

	// the game's player model (wide or slim arms), made once
	private static net.minecraft.client.model.PlayerModel<net.minecraft.world.entity.LivingEntity> model(final boolean slimArms) {
		if (slimArms) {
			if (slim == null) {
				slim = new net.minecraft.client.model.PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM), true);
			}
			return slim;
		}
		if (wide == null) {
			wide = new net.minecraft.client.model.PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER), false);
		}
		return wide;
	}

	// standing still, the arms swaying a little, the head where it looks; each layer of the skin
	// (hat, jacket, sleeves, trousers) follows its part
	private void pose(final net.minecraft.client.model.PlayerModel<net.minecraft.world.entity.LivingEntity> model, final float ageInTicks) {
		// a model is a baby's until told otherwise (the game sets it each frame)
		model.young = false;
		model.setAllVisible(true);
		model.head.resetPose();
		model.body.resetPose();
		model.rightArm.resetPose();
		model.leftArm.resetPose();
		model.rightLeg.resetPose();
		model.leftLeg.resetPose();
		model.head.yRot = this.headYaw * Mth.DEG_TO_RAD;
		model.head.xRot = this.headPitch * Mth.DEG_TO_RAD;
		net.minecraft.client.model.AnimationUtils.bobArms(model.rightArm, model.leftArm, ageInTicks);
		model.hat.copyFrom(model.head);
		model.jacket.copyFrom(model.body);
		model.rightSleeve.copyFrom(model.rightArm);
		model.leftSleeve.copyFrom(model.leftArm);
		model.rightPants.copyFrom(model.rightLeg);
		model.leftPants.copyFrom(model.leftLeg);
	}
	*///?}

	@Override
	//? if >=1.21.9 {
	protected void onDrag(final net.minecraft.client.input.MouseButtonEvent event, final double dragX, final double dragY) {
	//?} else {
	/*protected void onDrag(final double mouseX, final double mouseY, final double dragX, final double dragY) {
	*///?}
		// both ways reversed from 1.0.4, where players found them inverted
		this.rotationX = Mth.clamp(this.rotationX - (float)dragY * 1.5F, -35.0F, 35.0F);
		this.rotationY -= (float)dragX * 2.5F;
		this.draggedAt = Util.getMillis();
	}

	@Override
	public void playDownSound(final SoundManager soundManager) {
	}

	@Override
	protected void updateWidgetNarration(final NarrationElementOutput output) {
	}
}
