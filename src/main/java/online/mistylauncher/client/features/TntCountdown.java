package online.mistylauncher.client.features;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/**
 * TNT countdown: over each lit TNT, the seconds before it explodes ("2.35"), green when there is
 * time, then yellow, orange and red at the end. A lit TNT waits 80 ticks (4 s) in the game, but
 * servers change it, so the colour follows the time left rather than a share of it.
 */
public final class TntCountdown {
	private TntCountdown() {
	}

	/** The name tag for {@code fuseTicks} ticks left. */
	public static Component text(final float fuseTicks) {
		float seconds = Math.max(0.0F, fuseTicks / 20.0F);
		return Component.literal(String.format(Locale.ROOT, "%.2f", seconds)).withColor(color(seconds));
	}

	/** Green above 3 s, then yellow, orange, and red under 1 s. */
	private static int color(final float seconds) {
		if (seconds > 3.0F) {
			return 0x55E07A;
		} else if (seconds > 2.0F) {
			return 0xE8D44D;
		} else if (seconds > 1.0F) {
			return 0xF2994A;
		}
		return 0xFF5A6E;
	}
}
