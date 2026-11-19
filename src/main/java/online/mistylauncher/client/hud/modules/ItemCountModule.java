package online.mistylauncher.client.hud.modules;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.hud.HudModule;
import online.mistylauncher.client.ui.Ui;

/**
 * How many of an item the player carries (the whole inventory and the off hand), shown as one
 * inventory slot holding them all as a single stack ("as if glitched"), or as text ("Totems : 3")
 * behind the gear.
 */
abstract class ItemCountModule extends HudModule {
	private static final String TEXT = "text";
	private static final int SLOT = 18;
	private static final int PADDING = 3;
	private static final int TEXT_HEIGHT = 14;

	private final Predicate<ItemStack> counted;
	private final Item sample;

	ItemCountModule(final String id, final MistyConfig.Module defaults, final Predicate<ItemStack> counted, final Item sample) {
		super(id, defaults);
		this.counted = counted;
		this.sample = sample;
	}

	@Override
	public final List<Setting> settings() {
		return List.of(new Setting(TEXT, false));
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return minecraft.player != null;
	}

	/** The icon: the first stack carried (a potion keeps its colour), else the plain item. */
	private ItemStack icon(final Minecraft minecraft) {
		if (minecraft.player != null) {
			Inventory inventory = minecraft.player.getInventory();
			for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
				ItemStack stack = inventory.getItem(slot);
				if (!stack.isEmpty() && this.counted.test(stack)) {
					return stack.copyWithCount(1);
				}
			}
		}
		return new ItemStack(this.sample);
	}

	private int count(final Minecraft minecraft, final boolean preview) {
		if (minecraft.player == null) {
			return preview ? 3 : 0;
		}
		Inventory inventory = minecraft.player.getInventory();
		int total = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!stack.isEmpty() && this.counted.test(stack)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	private Component label(final int count) {
		return Component.translatable("misty.module." + this.id() + ".label").withColor(Ui.ACCENT & 0xFFFFFF)
			.append(Component.literal(" : " + count).withColor(Ui.TEXT & 0xFFFFFF));
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		return this.setting(TEXT) ? Math.max(50, minecraft.font.width(this.label(this.count(minecraft, preview))) + 10) : SLOT + 2 * PADDING;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return this.setting(TEXT) ? TEXT_HEIGHT : SLOT + 2 * PADDING;
	}

	@Override
	public void draw(final GuiGraphicsExtractor graphics, final Minecraft minecraft, final boolean preview) {
		Font font = minecraft.font;
		int count = this.count(minecraft, preview);
		int width = this.width(minecraft, preview);
		background(graphics, width, this.height(minecraft, preview));
		if (this.setting(TEXT)) {
			Component label = this.label(count);
			graphics.text(font, label, (width - font.width(label)) / 2, 3, Ui.TEXT, false);
			return;
		}
		// a slot as in the inventory: dark top-left edge, light bottom-right edge
		int x = PADDING;
		int y = PADDING;
		graphics.fill(x, y, x + SLOT, y + SLOT, 0xFF373737);
		graphics.fill(x + 1, y + 1, x + SLOT, y + SLOT, 0xFFFFFFFF);
		graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, 0xFF8B8B8B);
		ItemStack stack = this.icon(minecraft);
		graphics.item(stack, x + 1, y + 1);
		// the count as the game writes a stack's, even above 64 and for 1 or 0
		graphics.itemDecorations(font, stack, x + 1, y + 1, Integer.toString(count));
	}
}
