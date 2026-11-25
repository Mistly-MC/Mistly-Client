package online.mistylauncher.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
//? if >=1.17 {
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
//?} else {
/*import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
*///?}
import online.mistylauncher.client.features.AutoGG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Auto GG: the titles of the server ("VICTORY", "GAME OVER"…). */
@Mixin(ClientPacketListener.class)
abstract class AutoGGTitleMixin {
	//? if >=1.17 {
	@Inject(method = "setTitleText", at = @At("HEAD"))
	private void misty$autoGG(final ClientboundSetTitleTextPacket packet, final CallbackInfo ci) {
		// the packet first arrives on the network thread, then again on the game's: once
		if (Minecraft.getInstance().isSameThread()) {
			AutoGG.title(packet.text());
		}
	}
	//?} else {
	/*// before 1.17 one packet carried every kind of title: the big ones only
	@Inject(method = "handleSetTitles", at = @At("HEAD"))
	private void misty$autoGG(final ClientboundSetTitlesPacket packet, final CallbackInfo ci) {
		if (Minecraft.getInstance().isSameThread() && packet.getType() == ClientboundSetTitlesPacket.Type.TITLE) {
			AutoGG.title(packet.getText());
		}
	}
	*///?}
}
