package online.mistylauncher.legacy.hud.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.TextModule;

/** Your block coordinates. */
public final class CoordsModule extends TextModule {
	public CoordsModule() {
		super("coords", new Config.Module(false, 0, 2, 77));
	}

	@Override
	protected String text(final Minecraft minecraft, final boolean preview) {
		if (minecraft.thePlayer == null) {
			return "0 64 0";
		}
		return MathHelper.floor_double(minecraft.thePlayer.posX) + " " + MathHelper.floor_double(minecraft.thePlayer.posY) + " "
			+ MathHelper.floor_double(minecraft.thePlayer.posZ);
	}
}
