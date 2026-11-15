package online.mistylauncher.client.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.util.Mth;
import online.mistylauncher.client.MistyClient;

/**
 * Entity hitboxes, as F3 + B shows them, but always on: the switch of the Misty settings sets the
 * game's own "entity hitboxes" debug entry, so F3 + B and the switch always agree. Their color is
 * chosen behind the switch's gear.
 */
public final class Hitboxes {
	/** The colors to choose from (white: the game's), with their name key ({@code misty.color.<name>}). */
	public static final int[] COLORS = { 0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF, 0xFF6F9CFF, 0xFFAA55FF, 0xFFFF77DD, 0xFF000000 };
	public static final String[] NAMES = { "white", "red", "orange", "yellow", "green", "cyan", "blue", "purple", "pink", "black" };

	private Hitboxes() {
	}

	public static boolean shown() {
		return Minecraft.getInstance().debugEntries.getStatus(DebugScreenEntries.ENTITY_HITBOXES) == DebugScreenEntryStatus.ALWAYS_ON;
	}

	public static void show(final boolean shown) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.debugEntries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, shown ? DebugScreenEntryStatus.ALWAYS_ON : DebugScreenEntryStatus.NEVER);
	}

	/** Index of the chosen color in COLORS. */
	public static int index() {
		return Mth.clamp(MistyClient.config().hitboxColor, 0, COLORS.length - 1);
	}

	/** The color of the boxes (ARGB). */
	public static int color() {
		return COLORS[index()];
	}
}
