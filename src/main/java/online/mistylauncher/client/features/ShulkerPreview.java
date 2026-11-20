package online.mistylauncher.client.features;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import online.mistylauncher.client.MistyClient;
import org.jspecify.annotations.Nullable;

/**
 * Shulker preview: with Shift held over a shulker box, its tooltip shows what is inside as a
 * picture (its 27 slots with the items, like the inventory), instead of the game's 5 text lines.
 */
public final class ShulkerPreview {
	private static final int COLUMNS = 9;
	private static final int SLOTS = 27;
	private static final int SLOT = 18;
	/** Room around the slots for the frame in the shulker's color. */
	private static final int BORDER = 3;

	private ShulkerPreview() {
	}

	/** The preview is on and Shift is held: the shulkers' text lines give way to the picture. */
	public static boolean active() {
		//? if >=1.21.9 {
		return MistyClient.config().shulkerPreview && Minecraft.getInstance().hasShiftDown();
		//?} else {
		/*return MistyClient.config().shulkerPreview && net.minecraft.client.gui.screens.Screen.hasShiftDown();
		*///?}
	}

	/** The picture for {@code stack} when it is a shulker box with something inside. */
	public static Optional<TooltipComponent> tooltip(final ItemStack stack) {
		if (!active() || !(stack.getItem() instanceof BlockItem item) || !(item.getBlock() instanceof ShulkerBoxBlock shulker)) {
			return Optional.empty();
		}
		ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
		if (contents == null) {
			return Optional.empty();
		}
		NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		contents.copyInto(items);
		if (items.stream().allMatch(ItemStack::isEmpty)) {
			return Optional.empty();
		}
		return Optional.of(new Contents(items, shulker.getColor()));
	}

	/** What a shulker holds, slot by slot, and its color (null: the plain purple one). */
	public record Contents(NonNullList<ItemStack> items, @Nullable DyeColor color) implements TooltipComponent {
	}

	/** Draws the contents: a frame in the shulker's color, the slots and the items with their count. */
	public static final class Picture implements ClientTooltipComponent {
		private final Contents contents;

		public Picture(final Contents contents) {
			this.contents = contents;
		}

		@Override
		public int getWidth(final Font font) {
			return COLUMNS * SLOT + 2 * BORDER;
		}

		private static int height() {
			return SLOTS / COLUMNS * SLOT + 2 * BORDER + 2;
		}

		// the tooltip's height and its picture got the font and the size in 1.21.2
		//? if >=1.21.2 {
		@Override
		public int getHeight(final Font font) {
			return height();
		}

		@Override
		public void extractImage(final Font font, final int x, final int y, final int w, final int h, final GuiGraphicsExtractor graphics) {
			this.draw(font, x, y, graphics);
		}
		//?} else {
		/*@Override
		public int getHeight() {
			return height();
		}

		@Override
		public void extractImage(final Font font, final int x, final int y, final GuiGraphicsExtractor graphics) {
			this.draw(font, x, y, graphics);
		}
		*///?}

		private void draw(final Font font, final int x, final int y, final GuiGraphicsExtractor graphics) {
			int width = this.getWidth(font);
			int height = height() - 2;
			// the frame: the shulker's color (purple for the plain one), darkened
			int color = this.contents.color() == null ? 0xFF8F5E93 : this.contents.color().getTextureDiffuseColor() | 0xFF000000;
			graphics.fill(x, y, x + width, y + height, darken(color, 0.55F));
			graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, darken(color, 0.85F));
			for (int i = 0; i < SLOTS; i++) {
				int slotX = x + BORDER + i % COLUMNS * SLOT;
				int slotY = y + BORDER + i / COLUMNS * SLOT;
				// a slot as in the inventory: dark top-left edge, light bottom-right edge
				graphics.fill(slotX, slotY, slotX + SLOT, slotY + SLOT, 0xFF373737);
				graphics.fill(slotX + 1, slotY + 1, slotX + SLOT, slotY + SLOT, 0xFFFFFFFF);
				graphics.fill(slotX + 1, slotY + 1, slotX + SLOT - 1, slotY + SLOT - 1, 0xFF8B8B8B);
				ItemStack stack = this.contents.items().get(i);
				if (!stack.isEmpty()) {
					graphics.item(stack, slotX + 1, slotY + 1);
					graphics.itemDecorations(font, stack, slotX + 1, slotY + 1);
				}
			}
		}

		private static int darken(final int color, final float factor) {
			int r = Math.round((color >> 16 & 0xFF) * factor);
			int g = Math.round((color >> 8 & 0xFF) * factor);
			int b = Math.round((color & 0xFF) * factor);
			return 0xFF000000 | r << 16 | g << 8 | b;
		}
	}
}
