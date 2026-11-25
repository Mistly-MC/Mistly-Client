package online.mistylauncher.legacy.hud.modules;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.TextModule;

/** The time on your computer: seconds, 12 hours and "h" between hours and minutes behind its gear. */
public final class ClockModule extends TextModule {
	private static final String SECONDS = "seconds";
	private static final String TWELVE_HOURS = "ampm";
	private static final String LETTER_H = "letterH";

	public ClockModule() {
		super("clock", new Config.Module(false, 1, 0, 4), 40);
	}

	@Override
	public List<Setting> settings() {
		return Arrays.asList(new Setting(SECONDS, false), new Setting(TWELVE_HOURS, false), new Setting(LETTER_H, false));
	}

	/** The pattern the three settings make: HH:mm, HH:mm:ss, h:mm a, 22h31, 22h31m05s… */
	@Override
	protected String value(final Minecraft minecraft, final boolean preview) {
		boolean seconds = this.setting(SECONDS);
		boolean twelve = this.setting(TWELVE_HOURS);
		boolean letter = this.setting(LETTER_H);
		StringBuilder pattern = new StringBuilder(twelve ? "h" : "HH");
		pattern.append(letter ? "'h'mm" : ":mm");
		if (seconds) {
			pattern.append(letter ? "'m'ss's'" : ":ss");
		}
		if (twelve) {
			pattern.append(" a");
		}
		// AM and PM in English letters whatever the game's language
		return new SimpleDateFormat(pattern.toString(), Locale.ENGLISH).format(new Date());
	}
}
