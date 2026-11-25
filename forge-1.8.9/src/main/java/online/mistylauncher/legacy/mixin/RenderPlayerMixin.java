package online.mistylauncher.legacy.mixin;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import online.mistylauncher.legacy.features.Cape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Every player renderer gets the cosmetics' layers (the Mistly cape and the hat) as it is made. */
@Mixin(RenderPlayer.class)
abstract class RenderPlayerMixin extends RendererLivingEntity<AbstractClientPlayer> {
	protected RenderPlayerMixin(final RenderManager manager, final ModelBase model, final float shadow) {
		super(manager, model, shadow);
	}

	// the other constructor goes through this one
	@Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/RenderManager;Z)V", at = @At("RETURN"))
	private void misty$layers(final RenderManager manager, final boolean slim, final CallbackInfo ci) {
		RenderPlayer self = (RenderPlayer)(Object)this;
		this.addLayer(new Cape.CapeLayer(self));
		this.addLayer(new Cape.HatLayer(self));
	}
}
