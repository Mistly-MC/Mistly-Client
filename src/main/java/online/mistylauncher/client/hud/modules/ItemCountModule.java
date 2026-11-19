package online.mistylauncher.client.hud.modules;

import java.util.ArrayList;
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
 * How many of an item the player carries (the whole inventory and the off hand), one inventory slot
 * per kind (each potion its own: strength, speed, splash or not...) holding them all as a single
 * stack, or as text ("Totems : 3", a line per kind) behind the gear. Hidden while none is carried.
 */
abstract class ItemCountModule extends HudModule {
	private static final String TEXT = "text";
	private static final int SLOT = 18;
	private static final int GAP = 2;
	private static final int PADDING = 3;
	private static final int LINE = 10;

	/** One kind carried: a stack of it (its look) and how many in all. */
	private record Kind(ItemStack icon, int count) {
	}

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

	/** Not shown while none is carried. */
	@Override
	public boolean available(final Minecraft minecraft) {
		return minecraft.player != null && !this.carried(minecraft).isEmpty();
	}

	/** The kinds carried, in inventory order, the same item with the same contents merged. */
	private List<Kind> carried(final Minecraft minecraft) {
		List<Kind> kinds = new ArrayList<>();
		if (minecraft.player == null) {
			return kinds;
		}
		Inventory inventory = minecraft.player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty() || !this.counted.test(stack)) {
				continue;
			}
			int i = 0;
			while (i < kinds.size() && !ItemStack.isSameItemSameComponents(kinds.get(i).icon(), stack)) {
				i++;
			}
			if (i < kinds.size()) {
				kinds.set(i, new Kind(kinds.get(i).icon(), kinds.get(i).count() + stack.getCount()));
			} else {
				kinds.add(new Kind(stack.copyWithCount(1), stack.getCount()));
			}
		}
		return kinds;
	}

	/** What is drawn: the kinds carried, or in the HUD editor a sample to place it. */
	private List<Kind> kinds(final Minecraft minecraft, final boolean preview) {
		List<Kind> kinds = this.carried(minecraft);
		if (kinds.isEmpty() && preview) {
			kinds.add(new Kind(new ItemStack(this.sample), 3));
		}
		return kinds;
	}

	/** "Totems : 3" for a single kind, else the kind's name: "Potion of Strength : 2". */
	private Component label(final Kind kind, final boolean named) {
		Component name = named ? kind.icon().getHoverName() : Component.translatable("misty.module." + this.id() + ".label");
		return name.copy().withColor(Ui.ACCENT & 0xFFFFFF).append(Component.literal(" : " + kind.count()).withColor(Ui.TEXT & 0xFFFFFF));
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		List<Kind> kinds = this.kinds(minecraft, preview);
		if (this.setting(TEXT)) {
			boolean named = kinds.size() > 1;
			int widest = 0;
			for (Kind kind : kinds) {
				widest = Math.max(widest, minecraft.font.width(this.label(kind, named)));
			}
			return Math.max(50, widest + 10);
		}
		int n = Math.max(1, kinds.size());
		return n * SLOT + (n - 1) * GAP + 2 * PADDING;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		if (this.setting(TEXT)) {
			return Math.max(1, this.kinds(minecraft, preview).size()) * LINE + 4;
		}
		return SLOT + 2 * PADDING;
	}

	@Override
	public void draw(final GuiGraphicsExtractor graphics, final Minecraft minecraft, final boolean preview) {
		Font font = minecraft.font;
		List<Kind> kinds = this.kinds(minecraft, preview);
		int width = this.width(minecraft, preview);
		background(graphics, width, this.height(minecraft, preview));
		if (this.setting(TEXT)) {
			boolean named = kinds.size() > 1;
			int y = 3;
			for (Kind kind : kinds) {
				Component label = this.label(kind, named);
				graphics.text(font, label, (width - font.width(label)) / 2, y, Ui.TEXT, false);
				y += LINE;
			}
			return;
		}
		int x = PADDING;
		int y = PADDING;
		for (Kind kind : kinds) {
			// a slot as in the inventory: dark top-left edge, light bottom-right edge
			graphics.fill(x, y, x + SLOT, y + SLOT, 0xFF373737);
			graphics.fill(x + 1, y + 1, x + SLOT, y + SLOT, 0xFFFFFFFF);
			graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, 0xFF8B8B8B);
			graphics.item(kind.icon(), x + 1, y + 1);
			// the count as the game writes a stack's, even above 64 and for 1
			graphics.itemDecorations(font, kind.icon(), x + 1, y + 1, Integer.toString(kind.count()));
			x += SLOT + GAP;
		}
	}
}
