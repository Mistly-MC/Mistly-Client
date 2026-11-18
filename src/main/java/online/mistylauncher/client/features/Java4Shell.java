package online.mistylauncher.client.features;

import java.lang.management.ManagementFactory;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.compat.Versions;
import org.jspecify.annotations.Nullable;

/**
 * Java4Shell (CVE-2026-114938): Java's fast Base64 decoder, the one that uses AVX-512 instructions,
 * has a flaw that a server can use to run code on the player's computer, just by being pinged in
 * the server list or joined. Mistly Launcher turns that decoder off ({@code -XX:-UseBASE64Intrinsics})
 * since 0.3.9. A game started without it (an older launcher, another launcher) on a processor that
 * uses AVX-512 is exposed: the menu says so, and the multiplayer screen asks before it opens (the
 * server list pings every server as soon as it is shown; see OpenScreenMixin).
 */
public final class Java4Shell {
	private static Boolean exposed;
	/** The player chose to go on anyway: asked once per game. */
	private static boolean accepted;

	private Java4Shell() {
	}

	/** Whether this game still uses the flawed decoder. */
	public static boolean exposed() {
		if (exposed == null) {
			exposed = check();
			if (exposed) {
				MistyClient.LOG.warn("Java4Shell (CVE-2026-114938): this Java uses the AVX-512 Base64 decoder; start the game with -XX:-UseBASE64Intrinsics (Mistly Launcher 0.3.9 or later does)");
			}
		}
		return exposed;
	}

	private static boolean check() {
		// development: -Dmisty.debug.java4shell=true shows the warning on any computer
		if (Boolean.getBoolean("misty.debug.java4shell")) {
			return true;
		}
		try {
			com.sun.management.HotSpotDiagnosticMXBean vm = ManagementFactory.getPlatformMXBean(com.sun.management.HotSpotDiagnosticMXBean.class);
			boolean decoder = Boolean.parseBoolean(vm.getVMOption("UseBASE64Intrinsics").getValue());
			// UseAVX = 3: the processor's AVX-512 instructions are in use (the only ones affected)
			int avx = Integer.parseInt(vm.getVMOption("UseAVX").getValue());
			return decoder && avx >= 3;
		} catch (Throwable e) {
			// not HotSpot, not an x86 processor (no UseAVX option on ARM), or no jdk.management:
			// nothing to say for sure, so no warning
			return false;
		}
	}

	/**
	 * The screen to open instead of {@code next} (the multiplayer screen): a warning first if the game
	 * is exposed, which goes on to {@code next} or back to {@code parent}.
	 */
	public static Screen guard(final @Nullable Screen parent, final Screen next) {
		if (!exposed() || accepted) {
			return next;
		}
		MistyClient.LOG.info("Java4Shell: warning shown before the server list");
		return new ConfirmScreen(ok -> {
			accepted = ok;
			Versions.setScreen(ok ? next : parent);
		}, Component.translatable("misty.java4shell.title"), Component.translatable("misty.java4shell.message"),
			Component.translatable("misty.java4shell.continue"), Component.translatable("gui.back"));
	}
}
