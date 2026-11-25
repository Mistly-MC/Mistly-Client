package online.mistylauncher.legacy.mixin;

import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import online.mistylauncher.legacy.Events;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Each chat line as it is added: read (kills, game ends), the Mistly cloud put before names. */
@Mixin(GuiNewChat.class)
abstract class GuiNewChatMixin {
	@ModifyVariable(method = "printChatMessageWithOptionalDeletion", at = @At("HEAD"), argsOnly = true)
	private IChatComponent misty$chat(final IChatComponent message) {
		return Events.chat(message);
	}
}
