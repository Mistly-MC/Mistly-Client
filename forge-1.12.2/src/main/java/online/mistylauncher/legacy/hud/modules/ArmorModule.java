package online.mistylauncher.legacy.hud.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.HudModule;
import online.mistylauncher.legacy.ui.Ui;

/** The armor worn and the item held, with what is left of their durability. */
public final class ArmorModule extends HudModule {
	private static final int ROW = 17;
	private static final int WIDTH = 52;

	public ArmorModule() {
		super("armor", new Config.Module(false, 8, -4, -4));
	}

	/** Helmet to boots, then the item held when it wears out (a sword, a bow…). */
	private static List<ItemStack> items(final Minecraft minecraft) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		if (minecraft.player != null) {
			for (int i = 3; i >= 0; i--) {
				ItemStack piece = minecraft.player.inventory.armorInventory.get(i);
				if (!piece.isEmpty()) {
					items.add(piece);
				}
			}
			ItemStack held = minecraft.player.getHeldItemMainhand();
			if (!held.isEmpty() && held.isItemStackDamageable()) {
				items.add(held);
			}
		}
		return items;
	}

	/** What is drawn: what you wear, or in the editor a full diamond set. */
	private static List<ItemStack> shown(final Minecraft minecraft, final boolean preview) {
		List<ItemStack> items = items(minecraft);
		if (items.isEmpty() && preview) {
			items.add(new ItemStack(Items.DIAMOND_HELMET));
			items.add(new ItemStack(Items.DIAMOND_CHESTPLATE));
			items.add(new ItemStack(Items.DIAMOND_LEGGINGS));
			items.add(new ItemStack(Items.DIAMOND_BOOTS));
		}
		return items;
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return !items(minecraft).isEmpty();
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		return WIDTH;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return Math.max(1, shown(minecraft, preview).size()) * ROW + 3;
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		List<ItemStack> items = shown(minecraft, preview);
		background(WIDTH, this.height(minecraft, preview));
		RenderHelper.enableGUIStandardItemLighting();
		int y = 2;
		for (ItemStack stack : items) {
			minecraft.getRenderItem().renderItemAndEffectIntoGUI(stack, 3, y);
			y += ROW;
		}
		RenderHelper.disableStandardItemLighting();
		y = 2;
		for (ItemStack stack : items) {
			if (stack.isItemStackDamageable()) {
				int left = stack.getMaxDamage() - stack.getItemDamage();
				float ratio = (float)left / stack.getMaxDamage();
				int color = ratio > 0.5F ? Ui.TEXT : ratio > 0.2F ? 0xFFFFD166 : 0xFFFF5A6E;
				Ui.text(Integer.toString(left), 23, y + 4, color);
			} else if (stack.getCount() > 1) {
				Ui.text("x" + stack.getCount(), 23, y + 4, Ui.MUTED);
			}
			y += ROW;
		}
	}
}
