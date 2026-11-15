package online.mistylauncher.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import online.mistylauncher.client.features.AutoGG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Auto GG: the titles of the server ("VICTORY", "GAME OVER"…). */
@Mixin(ClientPacketListener.class)
abstract class AutoGGTitleMixin {
	@Inject(method = "setTitleText", at = @At("HEAD"))
	private void misty$autoGG(final ClientboundSetTitleTextPacket packet, final CallbackInfo ci) {
		// the packet first arrives on the network thread, then again on the game's: once
		if (Minecraft.getInstance().isSameThread()) {
			AutoGG.title(packet.text());
		}
	}
}
