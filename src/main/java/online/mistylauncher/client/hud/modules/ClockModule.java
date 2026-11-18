package online.mistylauncher.client.hud.modules;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.hud.TextModule;

/**
 * Time of the computer: no need to leave the game to know how late it is. Its gear sets the
 * format: the seconds, 12 hours with AM/PM, an "h" between the hours and the minutes ("22h31";
 * with the seconds "22h31m05s").
 */
public final class ClockModule extends TextModule {
	private static final String SECONDS = "seconds";
	private static final String TWELVE_HOURS = "ampm";
	private static final String LETTER_H = "letterH";

	public ClockModule() {
		super("clock", new MistyConfig.Module(false, 1, 0, 4), 40);
	}

	@Override
	public List<Setting> settings() {
		return List.of(new Setting(SECONDS, false), new Setting(TWELVE_HOURS, false), new Setting(LETTER_H, false));
	}

	/** The format the three settings make: "HH:mm", "HH:mm:ss", "h:mm a", "HH'h'mm", "HH'h'mm'm'ss's'"… */
	private DateTimeFormatter format() {
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
		return DateTimeFormatter.ofPattern(pattern.toString(), Locale.ENGLISH);
	}

	@Override
	protected Component text(final Minecraft minecraft, final boolean preview) {
		return Component.literal(LocalTime.now().format(this.format()));
	}
}
