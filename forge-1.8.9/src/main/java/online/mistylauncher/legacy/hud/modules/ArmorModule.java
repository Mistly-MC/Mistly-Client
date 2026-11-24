package online.mistylauncher.legacy.hud.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.HudModule;

/** The armor worn, helmet to boots, with what is left of each piece's durability. */
public final class ArmorModule extends HudModule {
	private static final int ROW = 17;
	private static final int PADDING = 2;

	public ArmorModule() {
		super("armor", new Config.Module(true, 5, -2, 0));
	}

	/** Helmet first; in the editor, a sample set when nothing is worn. */
	private static ItemStack[] worn(final Minecraft minecraft, final boolean preview) {
		ItemStack[] pieces = new ItemStack[4];
		boolean any = false;
		if (minecraft.thePlayer != null) {
			for (int i = 0; i < 4; i++) {
				pieces[i] = minecraft.thePlayer.inventory.armorInventory[3 - i];
				any |= pieces[i] != null;
			}
		}
		if (!any && preview) {
			pieces = new ItemStack[] {new ItemStack(Items.diamond_helmet), new ItemStack(Items.diamond_chestplate),
				new ItemStack(Items.diamond_leggings), new ItemStack(Items.diamond_boots)};
		}
		return pieces;
	}

	private static String durability(final ItemStack stack) {
		return stack.isItemStackDamageable() ? Integer.toString(stack.getMaxDamage() - stack.getItemDamage()) : "";
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		for (ItemStack piece : worn(minecraft, false)) {
			if (piece != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		int widest = 0;
		for (ItemStack piece : worn(minecraft, preview)) {
			if (piece != null) {
				widest = Math.max(widest, minecraft.fontRendererObj.getStringWidth(durability(piece)));
			}
		}
		return 16 + 4 + widest + 2 * PADDING;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return 4 * ROW + 2 * PADDING;
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		background(this.width(minecraft, preview), this.height(minecraft, preview));
		ItemStack[] pieces = worn(minecraft, preview);
		RenderHelper.enableGUIStandardItemLighting();
		for (int i = 0; i < 4; i++) {
			if (pieces[i] != null) {
				minecraft.getRenderItem().renderItemAndEffectIntoGUI(pieces[i], PADDING, PADDING + i * ROW);
			}
		}
		RenderHelper.disableStandardItemLighting();
		for (int i = 0; i < 4; i++) {
			if (pieces[i] != null) {
				minecraft.fontRendererObj.drawStringWithShadow(durability(pieces[i]), PADDING + 20, PADDING + i * ROW + 4, TEXT);
			}
		}
	}
}
