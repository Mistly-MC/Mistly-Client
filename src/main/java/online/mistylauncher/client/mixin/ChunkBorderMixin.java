package online.mistylauncher.client.mixin;

//? if >=1.21.11 {
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import online.mistylauncher.client.features.ChunkViewer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Chunk viewer: the chunk borders' lines in the picked colours. */
@Mixin(ChunkBorderRenderer.class)
abstract class ChunkBorderMixin {
	@ModifyArg(
		method = "emitGizmos",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/gizmos/Gizmos;line(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;IF)Lnet/minecraft/gizmos/GizmoProperties;"),
		index = 2
	)
	private int misty$color(final int color) {
		return ChunkViewer.color(color);
	}
}
//?} else {
/*// no gizmos before 1.21.11: nothing to hook (the option is hidden)
@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.renderer.debug.ChunkBorderRenderer.class)
abstract class ChunkBorderMixin {
}
*///?}
