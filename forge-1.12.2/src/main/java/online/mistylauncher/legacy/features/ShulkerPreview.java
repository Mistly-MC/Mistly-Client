package online.mistylauncher.legacy.features;

import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.ui.Ui;

/**
 * Shulker preview, as in the Fabric versions: Shift held over a shulker box that holds something,
 * its tooltip becomes its name over a picture of its 27 slots, in a frame of the box's colour.
 */
public final class ShulkerPreview {
	private static final int COLUMNS = 9;
	private static final int SLOTS = 27;
	private static final int SLOT = 18;
	/** Room around the slots for the frame in the shulker's colour. */
	private static final int BORDER = 3;
	private static final int NAME = 12;

	private ShulkerPreview() {
	}

	/** Draws the picture in place of the tooltip of {@code stack}: false when it is not a shulker to show. */
	public static boolean tooltip(final GuiScreen screen, final ItemStack stack, final int mouseX, final int mouseY) {
		if (!Config.get().shulkerPreview || !GuiScreen.isShiftKeyDown() || stack.isEmpty()) {
			return false;
		}
		Block block = Block.getBlockFromItem(stack.getItem());
		if (!(block instanceof BlockShulkerBox)) {
			return false;
		}
		NBTTagCompound tag = stack.getSubCompound("BlockEntityTag");
		if (tag == null || !tag.hasKey("Items", 9)) {
			return false;
		}
		NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(tag, items);
		boolean empty = true;
		for (ItemStack item : items) {
			empty &= item.isEmpty();
		}
		if (empty) {
			return false;
		}
		draw(screen, stack, items, BlockShulkerBox.getColorFromItem(stack.getItem()), mouseX, mouseY);
		return true;
	}

	private static void draw(final GuiScreen screen, final ItemStack box, final NonNullList<ItemStack> items, final EnumDyeColor dye, final int mouseX,
		final int mouseY) {
		Minecraft minecraft = Minecraft.getMinecraft();
		FontRenderer font = minecraft.fontRenderer;
		int width = COLUMNS * SLOT + 2 * BORDER;
		int height = NAME + SLOTS / COLUMNS * SLOT + 2 * BORDER;
		// where the game puts a tooltip: to the right of the mouse, kept on the screen
		int x = mouseX + 12;
		int y = mouseY - 12;
		if (x + width > screen.width - 4) {
			x = Math.max(4, mouseX - 16 - width);
		}
		y = Math.max(4, Math.min(y, screen.height - height - 4));
		int color = 0xFF000000 | (dye == null ? 0x8F5E93 : dye.getColorValue());

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 400.0F);
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		// the frame: the shulker's colour, darkened; its name on top
		Ui.fill(x, y, x + width, y + height, darken(color, 0.55F));
		Ui.fill(x + 1, y + 1, x + width - 1, y + height - 1, darken(color, 0.85F));
		font.drawStringWithShadow(box.getRarity().getColor() + box.getDisplayName(), x + BORDER + 1, y + BORDER, 0xFFFFFF);
		int top = y + BORDER + NAME;
		for (int i = 0; i < SLOTS; i++) {
			int slotX = x + BORDER + i % COLUMNS * SLOT;
			int slotY = top + i / COLUMNS * SLOT;
			// a slot as in the inventory: dark top-left edge, light bottom-right edge
			Ui.fill(slotX, slotY, slotX + SLOT, slotY + SLOT, 0xFF373737);
			Ui.fill(slotX + 1, slotY + 1, slotX + SLOT, slotY + SLOT, 0xFFFFFFFF);
			Ui.fill(slotX + 1, slotY + 1, slotX + SLOT - 1, slotY + SLOT - 1, 0xFF8B8B8B);
		}
		GlStateManager.enableDepth();
		RenderHelper.enableGUIStandardItemLighting();
		RenderItem renderer = minecraft.getRenderItem();
		for (int i = 0; i < SLOTS; i++) {
			ItemStack stack = items.get(i);
			if (!stack.isEmpty()) {
				int slotX = x + BORDER + i % COLUMNS * SLOT + 1;
				int slotY = top + i / COLUMNS * SLOT + 1;
				renderer.renderItemAndEffectIntoGUI(stack, slotX, slotY);
				renderer.renderItemOverlays(font, stack, slotX, slotY);
			}
		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
	}

	private static int darken(final int color, final float factor) {
		int r = Math.round((color >> 16 & 0xFF) * factor);
		int g = Math.round((color >> 8 & 0xFF) * factor);
		int b = Math.round((color & 0xFF) * factor);
		return 0xFF000000 | r << 16 | g << 8 | b;
	}
}
