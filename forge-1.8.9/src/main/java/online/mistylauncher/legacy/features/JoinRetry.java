package online.mistylauncher.legacy.features;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * Joining a server first tells Mojang ("join"), and Mojang answers 429 when the account joined too
 * often lately (several servers in a minute): the game then gives up with "Failed to log in:
 * Status: 429". Instead, as in the Fabric versions, the join is tried again a few times after short
 * waits (3 s, 6 s, 10 s: 19 s in all, under the 30 s a server gives to log in). It runs on the
 * game's own connecting thread, with the "Logging in…" screen shown.
 *
 * <p>The game's session service is wrapped: every other call goes to it unchanged.
 */
public final class JoinRetry {
	private static final long[] WAITS_MS = {3_000, 6_000, 10_000};
	private static MinecraftSessionService original;

	private JoinRetry() {
	}

	/** The game's own session service (the Mistly network proves the account with it, without retries). */
	public static MinecraftSessionService original() {
		return original != null ? original : Minecraft.getMinecraft().getSessionService();
	}

	public static void install(final Minecraft minecraft) {
		final MinecraftSessionService service = minecraft.getSessionService();
		if (original != null || service == null) {
			return;
		}
		MinecraftSessionService wrapped = (MinecraftSessionService)Proxy.newProxyInstance(JoinRetry.class.getClassLoader(), new Class<?>[] {MinecraftSessionService.class},
			new InvocationHandler() {
				@Override
				public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
					if (!"joinServer".equals(method.getName())) {
						return call(service, method, args);
					}
					for (int attempt = 0; ; attempt++) {
						try {
							return call(service, method, args);
						} catch (AuthenticationException e) {
							boolean limited = e.getMessage() != null && e.getMessage().contains("429");
							if (!limited || attempt >= WAITS_MS.length) {
								throw e;
							}
							System.out.println("[Mistly Client] Mojang answered 429 to the server join, trying again in " + WAITS_MS[attempt] / 1000 + " s");
							Thread.sleep(WAITS_MS[attempt]);
						}
					}
				}
			});
		try {
			ReflectionHelper.setPrivateValue(Minecraft.class, minecraft, wrapped, "sessionService", "field_152355_az");
			original = service;
		} catch (RuntimeException e) {
			System.err.println("[Mistly Client] join retry not installed: " + e);
		}
	}

	private static Object call(final MinecraftSessionService service, final Method method, final Object[] args) throws Throwable {
		try {
			return method.invoke(service, args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
