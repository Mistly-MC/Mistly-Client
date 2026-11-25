package online.mistylauncher.legacy.features;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;

/**
 * Joining a server first tells Mojang ("join"), and Mojang answers 429 when the account joined too
 * often lately (several servers in a minute): the game then gives up with "Failed to log in:
 * Status: 429". Instead, as in the Fabric versions, the join is tried again a few times after short
 * waits (3 s, 6 s, 10 s: 19 s in all, under the 30 s a server gives to log in). It runs on the
 * game's own connecting thread, with the "Logging in…" screen shown (see NetHandlerLoginClientMixin).
 */
public final class JoinRetry {
	private static final long[] WAITS_MS = {3_000, 6_000, 10_000};

	private JoinRetry() {
	}

	/** The game's session service (the Mistly network proves the account with it, without retries). */
	public static MinecraftSessionService original() {
		return Minecraft.getMinecraft().getSessionService();
	}

	/** The game's join, tried again after a 429. */
	public static void join(final MinecraftSessionService service, final GameProfile profile, final String token, final String serverId)
		throws AuthenticationException {
		for (int attempt = 0; ; attempt++) {
			try {
				service.joinServer(profile, token, serverId);
				return;
			} catch (AuthenticationException e) {
				boolean limited = e.getMessage() != null && e.getMessage().contains("429");
				if (!limited || attempt >= WAITS_MS.length) {
					throw e;
				}
				System.out.println("[Mistly Client] Mojang answered 429 to the server join, trying again in " + WAITS_MS[attempt] / 1000 + " s");
				try {
					Thread.sleep(WAITS_MS[attempt]);
				} catch (InterruptedException interrupted) {
					Thread.currentThread().interrupt();
					throw e;
				}
			}
		}
	}
}
