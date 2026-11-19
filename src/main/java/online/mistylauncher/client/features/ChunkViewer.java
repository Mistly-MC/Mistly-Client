package online.mistylauncher.client.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.ui.Palette;

/**
 * Chunk viewer: the chunk borders, as F3 + G shows them, but always on, in the colours picked
 * behind the switch's gear: the chunk's border (cyan in the game) and the grid inside it (yellow).
 * The switch sets the game's own "chunk borders" debug entry, so F3 + G and the switch agree.
 */
public final class ChunkViewer {
	/** The game's colours, swapped for the picked ones. */
	private static final int GAME_BORDER = 0xFF009B9B;
	private static final int GAME_GRID = 0xFFFFFF00;

	private ChunkViewer() {
	}

	public static boolean shown() {
		return Minecraft.getInstance().debugEntries.getStatus(DebugScreenEntries.CHUNK_BORDERS) == DebugScreenEntryStatus.ALWAYS_ON;
	}

	public static void show(final boolean shown) {
		Minecraft.getInstance().debugEntries.setStatus(DebugScreenEntries.CHUNK_BORDERS, shown ? DebugScreenEntryStatus.ALWAYS_ON : DebugScreenEntryStatus.NEVER);
	}

	/** The colour a line of the chunk borders is drawn in. */
	public static int color(final int game) {
		if (game == GAME_BORDER) {
			return Palette.color(MistyClient.config().chunkBorderColor);
		} else if (game == GAME_GRID) {
			return Palette.color(MistyClient.config().chunkGridColor);
		}
		return game;
	}
}
