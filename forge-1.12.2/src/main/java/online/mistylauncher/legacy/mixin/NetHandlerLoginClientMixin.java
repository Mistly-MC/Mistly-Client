package online.mistylauncher.legacy.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.network.NetHandlerLoginClient;
import online.mistylauncher.legacy.features.JoinRetry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Joining a server: Mojang's join tried again after a 429 (see JoinRetry). */
@Mixin(NetHandlerLoginClient.class)
abstract class NetHandlerLoginClientMixin {
	@Redirect(method = "handleEncryptionRequest", at = @At(value = "INVOKE",
		target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;joinServer(Lcom/mojang/authlib/GameProfile;Ljava/lang/String;Ljava/lang/String;)V",
		remap = false))
	private void misty$join(final MinecraftSessionService service, final GameProfile profile, final String token, final String serverId)
		throws AuthenticationException {
		JoinRetry.join(service, profile, token, serverId);
	}
}
