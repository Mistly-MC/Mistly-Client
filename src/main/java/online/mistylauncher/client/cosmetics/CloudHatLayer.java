package online.mistylauncher.client.cosmetics;

//? if >=1.21.2 {
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import online.mistylauncher.client.MistyClient;

/**
 * Cloudly: a small cloud floating over the head, bobbing gently. A layer of the player renderer,
 * built like the game's own deadmau5 ears: a player model with only these cubes, on the head, so
 * it follows every move of the head.
 */
public final class CloudHatLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private static final Identifier TEXTURE = MistyClient.id("textures/entity/cloudly.png");

	/** A cloud stays white: lit fully, whatever the light where the player stands. */
	private static final int GLOW = net.minecraft.util.LightCoordsUtil.FULL_BRIGHT;

	private final PlayerModel model;

	public CloudHatLayer(final RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
		super(parent);
		this.model = new PlayerModel(createLayer().bakeRoot(), false);
	}

	/** The cloud: a wide base and puffs, a pixel above the head (y -8 is the top of the head). */
	private static LayerDefinition createLayer() {
		MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, false);
		//? if >=1.21.9 {
		PartDefinition root = mesh.getRoot().clearRecursively();
		//?} else {
		/*PartDefinition root = mesh.getRoot();
		clear(root);
		*///?}
		PartDefinition head = root.getChild("head");
		head.addOrReplaceChild("cloud", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-5.0F, -12.5F, -4.0F, 10.0F, 3.0F, 8.0F)
			.texOffs(0, 12).addBox(-3.0F, -15.0F, -3.0F, 6.0F, 3.0F, 6.0F)
			.texOffs(26, 12).addBox(0.0F, -14.0F, 0.5F, 4.0F, 2.0F, 4.0F)
			.texOffs(0, 22).addBox(-7.0F, -12.0F, -3.0F, 3.0F, 2.0F, 6.0F)
			.texOffs(20, 22).addBox(4.0F, -12.0F, -2.5F, 3.0F, 2.0F, 5.0F),
			PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 64);
	}

	// Before 1.21.9 there is no clearRecursively(): the cubes of every part are removed by hand,
	// the parts themselves kept (the model looks them up by name)
	//? if <1.21.9 {
	/*private static void clear(final PartDefinition part) {
		for (var child : new java.util.ArrayList<>(part.getChildren())) {
			var grandChildren = new java.util.ArrayList<>(child.getValue().getChildren());
			PartDefinition emptied = part.clearChild(child.getKey());
			for (var grandChild : grandChildren) {
				emptied.addOrReplaceChild(grandChild.getKey(), grandChild.getValue());
			}
			clear(emptied);
		}
	}
	*///?}

	//? if >=1.21.9 {
	@Override
	public void submit(final PoseStack pose, final net.minecraft.client.renderer.SubmitNodeCollector collector, final int light, final AvatarRenderState state, final float yRot, final float xRot) {
	//?} else {
	/*@Override
	public void render(final PoseStack pose, final net.minecraft.client.renderer.MultiBufferSource buffers, final int light, final AvatarRenderState state, final float yRot, final float xRot) {
	*///?}
		if (((HatHolder)state).misty$hat() != Cosmetic.CLOUDLY || state.isInvisible) {
			return;
		}
		pose.pushPose();
		// floats up and down a little, slowly
		pose.translate(0.0F, (float)Math.sin(state.ageInTicks * 0.08F) * 0.03F, 0.0F);
		int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
		//? if >=26.3 {
		collector.submitModel(this.model, state, pose, RenderTypes.entityTranslucent(TEXTURE), GLOW, overlay, state.outlineColor);
		//?} else if >=1.21.9 {
		/*collector.submitModel(this.model, state, pose, RenderTypes.entityTranslucent(TEXTURE), GLOW, overlay, state.outlineColor, null);
		*///?} else {
		/*this.model.setupAnim(state);
		this.model.renderToBuffer(pose, buffers.getBuffer(RenderTypes.entityTranslucent(TEXTURE)), GLOW, overlay);
		*///?}
		pose.popPose();
	}
}
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.net.MistyPlayers;

// Cloudly before 1.21.2 (no player render state): the cloud on a head that copies the player's
public final class CloudHatLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	private static final ResourceLocation TEXTURE = MistyClient.id("textures/entity/cloudly.png");
	private static final int GLOW = net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;

	private final ModelPart head;

	public CloudHatLayer(final RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
		super(parent);
		this.head = createLayer().bakeRoot().getChild("head");
	}

	// the cloud alone, on an empty head (y -8 is the top of the head)
	private static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition head = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
		head.addOrReplaceChild("cloud", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-5.0F, -12.5F, -4.0F, 10.0F, 3.0F, 8.0F)
			.texOffs(0, 12).addBox(-3.0F, -15.0F, -3.0F, 6.0F, 3.0F, 6.0F)
			.texOffs(26, 12).addBox(0.0F, -14.0F, 0.5F, 4.0F, 2.0F, 4.0F)
			.texOffs(0, 22).addBox(-7.0F, -12.0F, -3.0F, 3.0F, 2.0F, 6.0F)
			.texOffs(20, 22).addBox(4.0F, -12.0F, -2.5F, 3.0F, 2.0F, 5.0F),
			PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void render(final PoseStack pose, final MultiBufferSource buffers, final int light, final AbstractClientPlayer player, final float limbSwing,
		final float limbSwingAmount, final float partialTicks, final float ageInTicks, final float headYaw, final float headPitch) {
		if (!MistyClient.config().cosmetics || player.isInvisible() || MistyPlayers.outfit(player.getUUID()).hat() != Cosmetic.CLOUDLY) {
			return;
		}
		pose.pushPose();
		// floats up and down a little, slowly
		pose.translate(0.0F, (float)Math.sin(ageInTicks * 0.08F) * 0.03F, 0.0F);
		this.head.copyFrom(this.getParentModel().head);
		this.head.render(pose, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), GLOW, LivingEntityRenderer.getOverlayCoords(player, 0.0F));
		pose.popPose();
	}
}
*///?}
