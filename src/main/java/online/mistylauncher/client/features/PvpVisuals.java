package online.mistylauncher.client.features;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.config.MistyConfig;

/**
 * PvP visuals in first person: low fire (the flames when burning cover less of the screen), low
 * shield (the shield held lower, so it hides less) and short swords (swords drawn smaller in hand).
 * Done by moving what the game draws, without any texture: they work with every resource pack.
 */
public final class PvpVisuals {
	/** How far the flames go down (the screen is about 2 high). */
	private static final float FIRE_DROP = 0.3F;
	/** How far the shield goes down in the hand. */
	private static final float SHIELD_DROP = 0.25F;
	/** Size of a sword in hand. */
	private static final float SWORD_SCALE = 0.75F;

	private PvpVisuals() {
	}

	/** Before the flames of a burning player are drawn over the screen (inside a pushed pose). */
	public static void fire(final PoseStack pose) {
		if (MistyClient.config().lowFire) {
			pose.translate(0.0F, -FIRE_DROP, 0.0F);
		}
	}

	/** Before the item held in first person is drawn, in the hand's pose (popped right after). */
	public static void heldItem(final ItemStack stack, final PoseStack pose) {
		MistyConfig config = MistyClient.config();
		if (config.lowShield && stack.is(Items.SHIELD)) {
			pose.translate(0.0F, -SHIELD_DROP, 0.0F);
		} else if (config.shortSword && stack.is(ItemTags.SWORDS)) {
			pose.scale(SWORD_SCALE, SWORD_SCALE, SWORD_SCALE);
		}
	}
}
