package online.mistylauncher.client.hud.modules;

import net.minecraft.world.item.Items;
import online.mistylauncher.client.config.MistyConfig;

/** Potions carried (drinkable, splash and lingering), in one slot as a single stack (or "Potions : 5"). */
public final class PotionModule extends ItemCountModule {
	public PotionModule() {
		super("potions", new MistyConfig.Module(false, 7, 112, -4),
			stack -> stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION), Items.SPLASH_POTION);
	}

	/** The next kinds of potions go to the right at first. */
	@Override
	protected boolean rightByDefault() {
		return true;
	}
}
