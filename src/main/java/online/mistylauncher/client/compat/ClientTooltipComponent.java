package online.mistylauncher.client.compat;

//? if <1.17 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;

// The game's ClientTooltipComponent (1.17) for 1.16.5: how such a picture is drawn (by the mod's
// tooltip hook, see ClientTooltipComponentMixin)
public interface ClientTooltipComponent {
	int getHeight();

	int getWidth(Font font);

	default void renderImage(final Font font, final int x, final int y, final PoseStack pose, final ItemRenderer items, final int blitOffset) {
	}
}
*///?}
