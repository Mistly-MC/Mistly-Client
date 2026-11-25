package online.mistylauncher.legacy.mixin;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;
import online.mistylauncher.legacy.features.Visuals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Short swords: once the models are made, the swords' get their hand size from the setting. */
@Mixin(ModelManager.class)
abstract class ModelManagerMixin {
	@Shadow
	private IRegistry<ModelResourceLocation, IBakedModel> modelRegistry;

	@Inject(method = "onResourceManagerReload", at = @At("TAIL"))
	private void misty$swords(final IResourceManager manager, final CallbackInfo ci) {
		Visuals.models(this.modelRegistry);
	}
}
