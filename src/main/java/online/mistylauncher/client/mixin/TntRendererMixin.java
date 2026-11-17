package online.mistylauncher.client.mixin;

import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.features.TntCountdown;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** TNT countdown: the time before a lit TNT explodes, as a name tag over it. */
@Mixin(TntRenderer.class)
abstract class TntRendererMixin {
	// the method name is split in two: stonecutter.gradle.kts renames the screens' "extractRenderState", not this one
	@Inject(method = "extract" + "RenderState(Lnet/minecraft/world/entity/item/PrimedTnt;Lnet/minecraft/client/renderer/entity/state/TntRenderState;F)V", at = @At("TAIL"))
	private void misty$countdown(final PrimedTnt tnt, final TntRenderState state, final float partialTicks, final CallbackInfo ci) {
		if (MistyClient.config().tntCountdown) {
			state.nameTag = TntCountdown.text(state.fuseRemainingInTicks);
			// just above the block
			state.nameTagAttachment = new Vec3(0.0, tnt.getBbHeight() + 0.25, 0.0);
		}
	}
}
