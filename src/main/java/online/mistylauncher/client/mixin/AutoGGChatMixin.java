package online.mistylauncher.client.mixin;

//? if >=1.19 {
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
			online.mistylauncher.client.features.CubeCraftGame.chat(message);
		}
	}
}
//?} else {
/*import java.util.UUID;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import online.mistylauncher.client.features.AutoGG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Auto GG before 1.19: the server's messages are the chat lines of the system type
@Mixin(Gui.class)
abstract class AutoGGChatMixin {
	@Inject(method = "handleChat", at = @At("HEAD"))
	private void misty$autoGG(final ChatType type, final Component message, final UUID sender, final CallbackInfo ci) {
		if (type == ChatType.SYSTEM) {
			AutoGG.chat(message);
			online.mistylauncher.client.features.CubeCraftGame.chat(message);
		}
	}
}
*///?}
