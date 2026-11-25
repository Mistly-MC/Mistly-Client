package online.mistylauncher.legacy.hud.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.HudModule;
import online.mistylauncher.legacy.ui.Ui;

/**
 * The active effects, as on Lunar: for each one its icon, its name with its level ("Speed II") and
 * right under it the time left (blinking in its last 10 seconds).
 */
public final class EffectsModule extends HudModule {
	private static final String NAME = "name";
	private static final String DURATION = "duration";
	private static final String BLINK = "blink";
	private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/container/inventory.png");
	private static final int ICON = 18;
	private static final int PAD = 3;
	private static final int GAP = 2;
	/** The time blinks during the last seconds of an effect (10 s). */
	private static final int BLINK_TICKS = 200;

	public EffectsModule() {
		super("effects", new Config.Module(true, 2, -4, 4));
	}

	@Override
	public List<Setting> settings() {
		return Arrays.asList(new Setting(NAME, true), new Setting(DURATION, true), new Setting(BLINK, true));
	}

	/** One line: the icon (its index in the inventory's picture), the name and the time left. */
	private static final class Line {
		final int icon;
		final String name;
		final String duration;
		final boolean ending;
		final boolean ambient;

		Line(final int icon, final String name, final String duration, final boolean ending, final boolean ambient) {
			this.icon = icon;
			this.name = name;
			this.duration = duration;
			this.ending = ending;
			this.ambient = ambient;
		}
	}

	private static List<Line> lines(final Minecraft minecraft, final boolean preview) {
		List<Line> lines = new ArrayList<Line>();
		if (minecraft.player != null) {
			for (PotionEffect effect : minecraft.player.getActivePotionEffects()) {
				Potion potion = effect.getPotion();
				if (potion == null || !potion.hasStatusIcon()) {
					continue;
				}
				boolean ending = !effect.getIsPotionDurationMax() && effect.getDuration() <= BLINK_TICKS;
				lines.add(new Line(potion.getStatusIconIndex(), name(potion.getName(), effect.getAmplifier()), time(Potion.getPotionDurationString(effect, 1.0F)),
					ending, effect.getIsAmbient()));
			}
		}
		// the editor shows two effects while there are none
		if (lines.isEmpty() && preview) {
			lines.add(new Line(net.minecraft.init.MobEffects.SPEED.getStatusIconIndex(), name(net.minecraft.init.MobEffects.SPEED.getName(), 1), "1:30", false, false));
			lines.add(new Line(net.minecraft.init.MobEffects.STRENGTH.getStatusIconIndex(), name(net.minecraft.init.MobEffects.STRENGTH.getName(), 0), "0:45", false, false));
		}
		return lines;
	}

	/** "Speed II": the level in roman numerals from II to X, as in the inventory. */
	private static String name(final String key, final int amplifier) {
		String name = I18n.format(key);
		if (amplifier >= 1 && amplifier <= 9) {
			name += " " + I18n.format("enchantment.level." + (amplifier + 1));
		}
		return name;
	}

	/** "01:27" becomes "1:27", as on Lunar. */
	private static String time(final String time) {
		return time.length() == 5 && time.charAt(0) == '0' && time.charAt(2) == ':' ? time.substring(1) : time;
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return !lines(minecraft, false).isEmpty();
	}

	private boolean text() {
		return this.setting(NAME) || this.setting(DURATION);
	}

	private int lineHeight() {
		return this.setting(NAME) && this.setting(DURATION) ? 20 : ICON;
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		int text = 0;
		if (this.text()) {
			for (Line line : lines(minecraft, preview)) {
				if (this.setting(NAME)) {
					text = Math.max(text, Ui.width(line.name));
				}
				if (this.setting(DURATION)) {
					text = Math.max(text, Ui.width(line.duration));
				}
			}
			text += 4;
		}
		return PAD + ICON + text + PAD;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		int count = Math.max(1, lines(minecraft, preview).size());
		return PAD + count * this.lineHeight() + (count - 1) * GAP + PAD;
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		List<Line> lines = lines(minecraft, preview);
		background(this.width(minecraft, preview), this.height(minecraft, preview));
		boolean name = this.setting(NAME);
		boolean duration = this.setting(DURATION);
		// blinking: off a quarter of a second, every half second
		boolean blinkOff = this.setting(BLINK) && System.currentTimeMillis() % 500 < 250;
		int lineHeight = this.lineHeight();
		int y = PAD;
		for (Line line : lines) {
			int iconY = y + (lineHeight - ICON) / 2;
			Ui.blit(ICONS, PAD, iconY, line.icon % 8 * 18, 198 + line.icon / 8 * 18, ICON, ICON, 18, 18, 256, 256, 0xFFFFFFFF);
			int textX = PAD + ICON + 4;
			int timeColor = line.ending ? 0xFFFF5A6E : Ui.MUTED;
			boolean showTime = duration && !(line.ending && blinkOff);
			if (name && duration) {
				Ui.text(line.name, textX, y + 1, line.ambient ? 0xFF9CC0FF : Ui.TEXT);
				if (showTime) {
					Ui.text(line.duration, textX, y + 11, timeColor);
				}
			} else if (name) {
				Ui.text(line.name, textX, y + (lineHeight - 8) / 2, Ui.TEXT);
			} else if (showTime) {
				Ui.text(line.duration, textX, y + (lineHeight - 8) / 2, line.ending ? timeColor : Ui.TEXT);
			}
			y += lineHeight + GAP;
		}
	}
}
