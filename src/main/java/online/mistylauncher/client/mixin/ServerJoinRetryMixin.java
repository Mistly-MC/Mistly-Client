package online.mistylauncher.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.exceptions.AuthenticationException;
//? if >=26.3 {
import com.mojang.authlib.minecraft.SessionService;
//?} else {
/*import com.mojang.authlib.minecraft.MinecraftSessionService;
*///?}
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import online.mistylauncher.client.MistyClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Joining a server first tells Mojang ("join"), and Mojang answers 429 when the account joined
 * too often lately (several servers in a minute): the game then gives up with "Failed to log in:
 * Status: 429". Instead, the join is tried again a few times after short waits (3 s, 6 s, 10 s:
 * 19 s in all, under the 30 s a server gives to log in). This runs on a background thread, with
 * the "Logging in…" screen shown.
 */
@Mixin(ClientHandshakePacketListenerImpl.class)
abstract class ServerJoinRetryMixin {
	private static final long[] WAITS_MS = { 3_000, 6_000, 10_000 };

	//? if >=26.3 {
	@WrapOperation(
		method = "authenticateServer",
		at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/SessionService;joinServer(Ljava/util/UUID;Ljava/lang/String;Ljava/lang/String;)V")
	)
	private void misty$retryOn429(final SessionService service,
	//?} else {
	/*@WrapOperation(
		method = "authenticateServer",
		at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;joinServer(Ljava/util/UUID;Ljava/lang/String;Ljava/lang/String;)V")
	)
	private void misty$retryOn429(final MinecraftSessionService service,
	*///?}
		final UUID profile, final String token, final String serverId, final Operation<Void> original) throws AuthenticationException {
		for (int attempt = 0; ; attempt++) {
			try {
				original.call(service, profile, token, serverId);
				return;
			} catch (Exception e) {
				boolean limited = e instanceof AuthenticationException && e.getMessage() != null && e.getMessage().contains("429");
				if (!limited || attempt >= WAITS_MS.length) {
					throw sneaky(e);
				}
				MistyClient.LOG.info("Mojang answered 429 to the server join, trying again in {} s", WAITS_MS[attempt] / 1000);
				try {
					Thread.sleep(WAITS_MS[attempt]);
				} catch (InterruptedException interrupted) {
					Thread.currentThread().interrupt();
					throw sneaky(e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> RuntimeException sneaky(final Throwable throwable) throws T {
		throw (T)throwable;
	}
}
