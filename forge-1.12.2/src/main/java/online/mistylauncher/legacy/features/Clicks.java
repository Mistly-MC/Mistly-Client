package online.mistylauncher.legacy.features;

import java.util.ArrayDeque;
import java.util.Deque;

/** Clicks of the last second, left and right, for the CPS module. */
public final class Clicks {
	private static final long WINDOW_MS = 1000;
	private static final Deque<Long> LEFT = new ArrayDeque<Long>();
	private static final Deque<Long> RIGHT = new ArrayDeque<Long>();

	private Clicks() {
	}

	/** A mouse button went down: 0 left, 1 right. */
	public static void pressed(final int button) {
		long now = System.currentTimeMillis();
		if (button == 0) {
			LEFT.addLast(now);
		} else if (button == 1) {
			RIGHT.addLast(now);
		}
	}

	private static int count(final Deque<Long> clicks) {
		long since = System.currentTimeMillis() - WINDOW_MS;
		while (!clicks.isEmpty() && clicks.peekFirst() < since) {
			clicks.removeFirst();
		}
		return clicks.size();
	}

	public static int left() {
		return count(LEFT);
	}

	public static int right() {
		return count(RIGHT);
	}
}
