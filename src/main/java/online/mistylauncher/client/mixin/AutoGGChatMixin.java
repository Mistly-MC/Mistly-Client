package online.mistylauncher.client.mixin;

import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import online.mistylauncher.client.features.AutoGG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Auto GG: the server's system messages (the end of a game is told there). */
@Mixin(ChatListener.class)
abstract class AutoGGChatMixin {
	@Inject(method = "handleSystemMessage", at = @At("HEAD"))
	private void misty$autoGG(final Component message, final boolean flag, final CallbackInfo ci) {
		// since 26.1 the flag tells a message from the server (the action bar has its own method);
		// before, it tells the action bar
		//? if >=26.1 {
		boolean chat = flag;
		//?} else {
		/*boolean chat = !flag;
		*///?}
		if (chat) {
			AutoGG.chat(message);
		}
	}
}
