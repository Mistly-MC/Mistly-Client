package online.mistylauncher.legacy.mixin;

import java.util.Locale;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderTNTPrimed;
import net.minecraft.entity.item.EntityTNTPrimed;
import online.mistylauncher.legacy.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** TNT countdown: the seconds left over lit TNT ("2.35"), green, then yellow, orange, red. */
@Mixin(RenderTNTPrimed.class)
abstract class RenderTNTPrimedMixin extends Render<EntityTNTPrimed> {
	protected RenderTNTPrimedMixin(final RenderManager manager) {
		super(manager);
	}

	@Inject(method = "doRender(Lnet/minecraft/entity/item/EntityTNTPrimed;DDDFF)V", at = @At("TAIL"))
	private void misty$countdown(final EntityTNTPrimed entity, final double x, final double y, final double z, final float yaw, final float partialTicks,
		final CallbackInfo ci) {
		if (Config.get().tntCountdown) {
			float seconds = Math.max(0.0F, (entity.fuse - partialTicks + 1.0F) / 20.0F);
			this.renderLivingLabel(entity, misty$color(seconds) + String.format(Locale.ROOT, "%.2f", seconds), x, y, z, 64);
		}
	}

	/** Green above 3 s, then yellow, orange, and red under 1 s. */
	@Unique
	private static String misty$color(final float seconds) {
		return seconds > 3.0F ? "§a" : seconds > 2.0F ? "§e" : seconds > 1.0F ? "§6" : "§c";
	}
}
