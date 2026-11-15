package online.mistylauncher.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import online.mistylauncher.client.features.InvisibleBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Invisible blocks: each block the world picks around the player for its ambient particles. */
@Mixin(ClientLevel.class)
abstract class ClientLevelMixin {
	@ModifyExpressionValue(
		method = "doAnimateTick",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0)
	)
	private BlockState misty$invisibleBlocks(final BlockState state, @Local(argsOnly = true) final BlockPos.MutableBlockPos pos) {
		InvisibleBlocks.picked((ClientLevel)(Object)this, pos, state);
		return state;
	}
}
