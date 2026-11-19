package online.mistylauncher.client.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import online.mistylauncher.client.ui.Palette;
import online.mistylauncher.client.MistyClient;

/**
 * Entity hitboxes, as F3 + B shows them, but always on: the switch of the Misty settings sets the
 * game's own "entity hitboxes" debug entry, so F3 + B and the switch always agree. Their color is
 * chosen behind the switch's gear.
 */
public final class Hitboxes {

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
		return Palette.index(MistyClient.config().hitboxColor);
	}

	/** The color of the boxes (ARGB). */
	public static int color() {
		return Palette.color(MistyClient.config().hitboxColor);
	}
}
