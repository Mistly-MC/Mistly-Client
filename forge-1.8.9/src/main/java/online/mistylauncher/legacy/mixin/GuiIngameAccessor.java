package online.mistylauncher.legacy.mixin;

import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The title shown in the middle of the screen, read by the CubeCraft game detection. */
@Mixin(GuiIngame.class)
public interface GuiIngameAccessor {
	@Accessor("displayedTitle")
	String misty$displayedTitle();
}
