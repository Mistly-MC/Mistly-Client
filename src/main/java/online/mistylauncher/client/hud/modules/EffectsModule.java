package online.mistylauncher.client.hud.modules;

import online.mistylauncher.client.compat.Gui2D;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.hud.HudModule;
import online.mistylauncher.client.ui.Ui;

/**
 * The active effects, as on Lunar: for each one its icon, its name with its level ("Speed II")
 * and right under it the time left. Replaces the game's icons at the top right (see the setting).
 */
public final class EffectsModule extends HudModule {
	public static final String NAME = "name";
	public static final String DURATION = "duration";
	public static final String BLINK = "blink";
	public static final String HIDE_VANILLA = "hideVanilla";

	private static final int ICON = 18;
	private static final int PAD = 3;
	private static final int GAP = 2;
	/** The time blinks during the last seconds of an effect (10 s, like the game's icons). */
	private static final int BLINK_TICKS = 200;

	public EffectsModule() {
		super("effects", new MistyConfig.Module(true, 2, -4, 4));
	}

	@Override
	public List<Setting> settings() {
		return List.of(new Setting(NAME, true), new Setting(DURATION, true), new Setting(BLINK, true), new Setting(HIDE_VANILLA, true));
	}

	/** One line: the icon, the name (with the level) and the time left. */
	private record Line(Identifier icon, Component name, Component duration, boolean ending, boolean ambient) {
	}

	private List<Line> lines(final Minecraft minecraft, final boolean preview) {
		List<Line> lines = new ArrayList<>();
		if (minecraft.player != null && minecraft.level != null) {
			//? if >=1.20.3 {
			float tickrate = minecraft.level.tickRateManager().tickrate();
			//?} else {
			/*float tickrate = 20.0F;
			*///?}
			for (MobEffectInstance effect : minecraft.player.getActiveEffects()) {
				if (!effect.showIcon()) {
					continue;
				}
				boolean ending = !effect.isInfiniteDuration() && effect.getDuration() <= BLINK_TICKS;
				lines.add(new Line(sprite(online.mistylauncher.client.compat.Versions.effect(effect)), name(online.mistylauncher.client.compat.Versions.effect(effect), effect.getAmplifier()),
					time(duration(effect, tickrate)), ending, effect.isAmbient()));
			}
		}
		// the editor shows two effects while there are none
		if (lines.isEmpty() && preview) {
			lines.add(new Line(Identifier.withDefaultNamespace("mob_effect/speed"), sample("effect.minecraft.speed", 1), Component.literal("1:30"), false, false));
			lines.add(new Line(Identifier.withDefaultNamespace("mob_effect/strength"), sample("effect.minecraft.strength", 0), Component.literal("0:45"), false, false));
		}
		return lines;
	}

	/** "01:27" becomes "1:27", as on Lunar. */
	private static Component time(final Component time) {
		String text = time.getString();
		return text.length() == 5 && text.charAt(0) == '0' && text.charAt(2) == ':' ? Component.literal(text.substring(1)) : time;
	}

	// the time left (the game's tick rate came in 1.20.3)
	private static Component duration(final MobEffectInstance effect, final float tickrate) {
		//? if >=1.20.3 {
		return MobEffectUtil.formatDuration(effect, 1.0F, tickrate);
		//?} else {
		/*return MobEffectUtil.formatDuration(effect, 1.0F);
		*///?}
	}

	private static Identifier sprite(final Holder<MobEffect> effect) {
		//? if >=26.2 {
		return net.minecraft.client.gui.Hud.getMobEffectSprite(effect);
		//?} else if >=1.21.2 {
		/*return net.minecraft.client.gui.Gui.getMobEffectSprite(effect);
		*///?} else {
		/*return effect.unwrapKey().map(key -> key.location().withPrefix("mob_effect/")).orElse(Identifier.withDefaultNamespace("mob_effect/speed"));
		*///?}
	}

	/** "Speed II": the level in roman numerals from II to X, as in the inventory. */
	private static Component name(final Holder<MobEffect> effect, final int amplifier) {
		return withLevel(effect.value().getDisplayName().copy(), amplifier);
	}

	private static Component sample(final String key, final int amplifier) {
		return withLevel(Component.translatable(key), amplifier);
	}

	private static Component withLevel(final MutableComponent name, final int amplifier) {
		if (amplifier >= 1 && amplifier <= 9) {
			name.append(" ").append(Component.translatable("enchantment.level." + (amplifier + 1)));
		}
		return name;
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return !this.lines(minecraft, false).isEmpty();
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
			Font font = minecraft.font;
			for (Line line : this.lines(minecraft, preview)) {
				if (this.setting(NAME)) {
					text = Math.max(text, font.width(line.name()));
				}
				if (this.setting(DURATION)) {
					text = Math.max(text, font.width(line.duration()));
				}
			}
			text += 4;
		}
		return PAD + ICON + text + PAD;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		int count = Math.max(1, this.lines(minecraft, preview).size());
		return PAD + count * this.lineHeight() + (count - 1) * GAP + PAD;
	}

	@Override
	public void draw(final GuiGraphicsExtractor graphics, final Minecraft minecraft, final boolean preview) {
		Font font = minecraft.font;
		List<Line> lines = this.lines(minecraft, preview);
		background(graphics, this.width(minecraft, preview), this.height(minecraft, preview));
		boolean name = this.setting(NAME);
		boolean duration = this.setting(DURATION);
		// blinking: off a quarter of a second, every half second
		boolean blinkOff = this.setting(BLINK) && Util.getMillis() % 500 < 250;
		int lineHeight = this.lineHeight();
		int y = PAD;
		for (Line line : lines) {
			int iconY = y + (lineHeight - ICON) / 2;
			//? if >=1.21.2 {
			Gui2D.sprite(graphics, line.icon(), PAD, iconY, ICON, ICON);
			//?} else {
			/*// before 1.21.2 the icons are not in the GUI atlas: their own picture, 18 × 18
			Gui2D.blit(graphics, line.icon().withPath(path -> "textures/" + path + ".png"), PAD, iconY, 0.0F, 0.0F, ICON, ICON, 18, 18, 18, 18);
			*///?}
			int textX = PAD + ICON + 4;
			int timeColor = line.ending() ? 0xFFFF5A6E : Ui.MUTED;
			boolean showTime = duration && !(line.ending() && blinkOff);
			if (name && duration) {
				graphics.text(font, line.name(), textX, y + 1, line.ambient() ? 0xFF9CC0FF : Ui.TEXT, false);
				if (showTime) {
					graphics.text(font, line.duration(), textX, y + 11, timeColor, false);
				}
			} else if (name) {
				graphics.text(font, line.name(), textX, y + (lineHeight - 8) / 2, Ui.TEXT, false);
			} else if (showTime) {
				graphics.text(font, line.duration(), textX, y + (lineHeight - 8) / 2, line.ending() ? timeColor : Ui.TEXT, false);
			}
			y += lineHeight + GAP;
		}
	}
}
