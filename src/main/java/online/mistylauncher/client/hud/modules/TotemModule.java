package online.mistylauncher.client.hud.modules;

import net.minecraft.world.item.Items;
import online.mistylauncher.client.config.MistyConfig;

/** Totems of undying carried, in one slot as a single stack (or "Totems : 3"). */
public final class TotemModule extends ItemCountModule {
	public TotemModule() {
		super("totems", new MistyConfig.Module(false, 7, -112, -4), stack -> stack.is(Items.TOTEM_OF_UNDYING), Items.TOTEM_OF_UNDYING);
	}
}
