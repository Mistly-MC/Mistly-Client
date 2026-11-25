package online.mistylauncher.client.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
//? if >=1.20.5 {
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import online.mistylauncher.client.hud.Ping;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}
import org.spongepowered.asm.mixin.Mixin;

/** Answers to the ping requests of the Ping module (the game's debug chart still gets them). */
@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
	//? if >=1.20.5 {
	@Inject(method = "handlePongResponse", at = @At("HEAD"))
	private void misty$pong(final ClientboundPongResponsePacket packet, final CallbackInfo ci) {
		Ping.pong(packet.time());
	}
	//?}
	// before 1.20.5 there is no ping request while playing: nothing to answer
}
