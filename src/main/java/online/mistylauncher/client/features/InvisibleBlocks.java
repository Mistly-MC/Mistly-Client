package online.mistylauncher.client.features;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import online.mistylauncher.client.MistyClient;

/**
 * Invisible blocks: barriers and light blocks around you are shown by their marker (the red sign,
 * the light bulb), as the game does in creative with one of them in hand, but always and in any
 * game mode. The markers come from the blocks the world picks at random around you each tick
 * (the ones that make torches smoke), so they appear within a moment and fade when you go away.
 */
public final class InvisibleBlocks {
	private InvisibleBlocks() {
	}

	/** The world picked {@code pos} for its ambient particles, it holds {@code state}. */
	public static void picked(final ClientLevel level, final BlockPos pos, final BlockState state) {
		if (MistyClient.config().invisibleBlocks && (state.is(Blocks.BARRIER) || state.is(Blocks.LIGHT))) {
			level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, state), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
		}
	}
}
