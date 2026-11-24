package online.mistylauncher.legacy.hud.modules;

import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.TextModule;

/** The time on your computer. */
public final class ClockModule extends TextModule {
	public ClockModule() {
		super("clock", new Config.Module(false, 2, -2, 70));
	}

	@Override
	protected String text(final Minecraft minecraft, final boolean preview) {
		return new SimpleDateFormat("HH:mm").format(new Date());
	}
}
