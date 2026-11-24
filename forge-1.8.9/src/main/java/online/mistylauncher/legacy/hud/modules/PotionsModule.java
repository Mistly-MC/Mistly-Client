package online.mistylauncher.legacy.hud.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.HudModule;

/**
 * The potions carried, one inventory slot per kind (strength, speed, splash or not...) holding them
 * all as a single stack, or as text ("Potions : 5") in the settings. The first kind's slot stays
 * where it was placed; the next kinds go to its left, or to its right with "Right" in the settings.
 */
public final class PotionsModule extends HudModule {
	private static final int SLOT = 18;
	private static final int GAP = 2;
	private static final int PADDING = 3;
	private static final int LINE = 10;

	/** One kind carried: a potion of it (its look) and how many in all. */
	private static final class Kind {
		final ItemStack icon;
		int count;

		Kind(final ItemStack icon, final int count) {
			this.icon = icon;
			this.count = count;
		}
	}

	public PotionsModule() {
		super("potions", new Config.Module(false, 7, 112, -4));
	}

	/** The kinds carried, in inventory order, potions with the same metadata merged. */
	private static List<Kind> carried(final Minecraft minecraft) {
		List<Kind> kinds = new ArrayList<Kind>();
		if (minecraft.thePlayer == null) {
			return kinds;
		}
		for (ItemStack stack : minecraft.thePlayer.inventory.mainInventory) {
			if (stack == null || stack.getItem() != Items.potionitem) {
				continue;
			}
			Kind same = null;
			for (Kind kind : kinds) {
				if (kind.icon.getMetadata() == stack.getMetadata()) {
					same = kind;
					break;
				}
			}
			if (same != null) {
				same.count += stack.stackSize;
			} else {
				ItemStack icon = stack.copy();
				icon.stackSize = 1;
				kinds.add(new Kind(icon, stack.stackSize));
			}
		}
		return kinds;
	}

	/** What is drawn: the kinds carried, or in the editor a sample to place it. */
	private static List<Kind> kinds(final Minecraft minecraft, final boolean preview) {
		List<Kind> kinds = carried(minecraft);
		if (kinds.isEmpty() && preview) {
			// a splash potion of healing II
			kinds.add(new Kind(new ItemStack(Items.potionitem, 1, 16421), 3));
		}
		return kinds;
	}

	private boolean text() {
		return this.state().text;
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return !carried(minecraft).isEmpty();
	}

	/** In slots: the first kind's slot stays in place, the next kinds are added beside it. */
	@Override
	public int fixedWidth(final Minecraft minecraft, final boolean preview) {
		return this.text() ? 0 : SLOT + 2 * PADDING;
	}

	@Override
	public boolean growsRight() {
		return this.state().right;
	}

	/** "Potions : 3" for a single kind, else each kind's name: "Splash Potion of Healing : 2". */
	private static String label(final Kind kind, final boolean named) {
		String name = named ? kind.icon.getDisplayName() : I18n.format("misty.module.potions.label");
		return "§b" + name + "§f : " + kind.count;
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		List<Kind> kinds = kinds(minecraft, preview);
		if (this.text()) {
			int widest = 0;
			for (Kind kind : kinds) {
				widest = Math.max(widest, minecraft.fontRendererObj.getStringWidth(label(kind, kinds.size() > 1)));
			}
			return Math.max(50, widest + 10);
		}
		int n = Math.max(1, kinds.size());
		return n * SLOT + (n - 1) * GAP + 2 * PADDING;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		if (this.text()) {
			return Math.max(1, kinds(minecraft, preview).size()) * LINE + 4;
		}
		return SLOT + 2 * PADDING;
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		FontRenderer font = minecraft.fontRendererObj;
		List<Kind> kinds = kinds(minecraft, preview);
		int width = this.width(minecraft, preview);
		background(width, this.height(minecraft, preview));
		if (this.text()) {
			int y = 3;
			for (Kind kind : kinds) {
				String label = label(kind, kinds.size() > 1);
				font.drawStringWithShadow(label, (width - font.getStringWidth(label)) / 2, y, TEXT);
				y += LINE;
			}
			return;
		}
		// the first kind keeps its slot: the leftmost when the next ones are added on the right,
		// the rightmost when they are added on the left
		boolean right = this.growsRight();
		int y = PADDING;
		for (int i = 0; i < kinds.size(); i++) {
			int x = PADDING + (right ? i : kinds.size() - 1 - i) * (SLOT + GAP);
			// a slot as in the inventory: dark top-left edge, light bottom-right edge
			Gui.drawRect(x, y, x + SLOT, y + SLOT, 0xFF373737);
			Gui.drawRect(x + 1, y + 1, x + SLOT, y + SLOT, 0xFFFFFFFF);
			Gui.drawRect(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, 0xFF8B8B8B);
		}
		RenderHelper.enableGUIStandardItemLighting();
		for (int i = 0; i < kinds.size(); i++) {
			int x = PADDING + (right ? i : kinds.size() - 1 - i) * (SLOT + GAP);
			Kind kind = kinds.get(i);
			minecraft.getRenderItem().renderItemAndEffectIntoGUI(kind.icon, x + 1, y + 1);
			// the count as the game writes a stack's, even for 1
			minecraft.getRenderItem().renderItemOverlayIntoGUI(font, kind.icon, x + 1, y + 1, Integer.toString(kind.count));
		}
		RenderHelper.disableStandardItemLighting();
	}
}
