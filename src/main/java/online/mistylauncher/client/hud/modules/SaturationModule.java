package online.mistylauncher.client.hud.modules;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.compat.Gui2D;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.hud.HudManager;
import online.mistylauncher.client.hud.TextModule;
import online.mistylauncher.client.ui.EffectSettingsScreen;
import online.mistylauncher.client.ui.Palette;

/**
 * Saturation, as AppleSkin shows it on the hunger bar:
 * <ul>
 * <li>the saturation, the hidden food reserve the game spends before the drumsticks, as golden
 * outlines over them (each holds 2 points, drawn by quarters);</li>
 * <li>with food in hand, what eating it would give, blinking: the drumsticks it fills and the
 * saturation it adds.</li>
 * </ul>
 * Behind the gear: the food preview, and "As text" to also show the saturation in a box ("5.0
 * saturation") placed like any module, in the colour chosen there.
 */
public final class SaturationModule extends TextModule {
	private static final String COLOR = "color";
	private static final String TEXT = "text";
	private static final String PREVIEW = "preview";
	/** Yellow, the colour of the saturation in AppleSkin. */
	private static final int DEFAULT_COLOR = 3;
	private static final int ICON = 9;
	/** AppleSkin's saturation outlines (Unlicense): a quarter, a half, three quarters, full; 9 × 9 each. */
	private static final Identifier OUTLINES = MistyClient.id("textures/gui/saturation.png");
	private static final Identifier FOOD_EMPTY = Identifier.withDefaultNamespace("hud/food_empty");
	private static final Identifier FOOD_HALF = Identifier.withDefaultNamespace("hud/food_half");
	private static final Identifier FOOD_FULL = Identifier.withDefaultNamespace("hud/food_full");
	/** One blink of the food preview, as AppleSkin's (32 ticks). */
	private static final long BLINK_MS = 1600;

	public SaturationModule() {
		super("saturation", new MistyConfig.Module(false, 6, 4, -40), 60);
	}

	@Override
	public List<Setting> settings() {
		return List.of(new Setting(PREVIEW, true), new Setting(TEXT, false));
	}

	@Override
	public List<EffectSettingsScreen.Slider> sliders() {
		return List.of((x, y, w, h) -> new Palette.Slider(x, y, w, h, "misty.option.color",
			() -> this.state().value(COLOR, DEFAULT_COLOR), value -> this.state().setValue(COLOR, value)));
	}

	private int color() {
		return Palette.color(this.state().value(COLOR, DEFAULT_COLOR));
	}

	/** The box only "as text"; the bar overlay does not need it. */
	@Override
	public boolean available(final Minecraft minecraft) {
		return minecraft.player != null && this.setting(TEXT);
	}

	@Override
	protected Component text(final Minecraft minecraft, final boolean preview) {
		float saturation = minecraft.player == null ? 5.0F : minecraft.player.getFoodData().getSaturationLevel();
		return value(String.format(Locale.ROOT, "%.1f", saturation), this.color(), "saturation");
	}

	/** The module, when it is switched on. */
	private static SaturationModule enabled() {
		if (!MistyClient.config().hud) {
			return null;
		}
		for (var module : HudManager.modules()) {
			if (module instanceof SaturationModule saturation && saturation.state().enabled) {
				return saturation;
			}
		}
		return null;
	}

	/** What the food in hand gives, or null: nothing to eat, or the player can't eat it now. */
	private static FoodProperties held(final Player player) {
		for (ItemStack stack : new ItemStack[] {player.getMainHandItem(), player.getOffhandItem()}) {
			FoodProperties food = stack.get(DataComponents.FOOD);
			if (food != null) {
				return player.canEat(food.canAlwaysEat()) ? food : null;
			}
		}
		return null;
	}

	/** AppleSkin's blink: fades in, stays, fades out, stays off, 0 to 1. */
	private static float blink() {
		float t = (System.currentTimeMillis() % BLINK_MS) / (float) BLINK_MS;
		float wave = t < 0.5F ? -0.5F + t * 4.0F : 1.5F - (t - 0.5F) * 4.0F;
		return Math.max(0.0F, Math.min(1.0F, wave));
	}

	private static int white(final float alpha) {
		return Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F) << 24 | 0xFFFFFF;
	}

	/** Left of drumstick {@code i} (0: the rightmost), as the game lays out the hunger bar. */
	private static int drumstick(final int xRight, final int i) {
		return xRight - i * 8 - 9;
	}

	/**
	 * After the game drew the hunger bar ({@code xRight}: its right end, {@code yLine}: its top):
	 * the food in hand blinking over it, then the saturation's golden outlines.
	 */
	public static void overlay(final GuiGraphicsExtractor graphics, final Player player, final int yLine, final int xRight) {
		SaturationModule module = enabled();
		if (module == null) {
			return;
		}
		int food = player.getFoodData().getFoodLevel();
		float saturation = player.getFoodData().getSaturationLevel();
		FoodProperties held = module.setting(PREVIEW) ? held(player) : null;
		if (held == null) {
			outlines(graphics, xRight, yLine, saturation, 0.0F, 1.0F);
			return;
		}
		// eating it: the food goes up (20 at most), then the saturation, never above the food
		int fed = Math.min(20, food + held.nutrition());
		float sated = Math.max(0.0F, Math.min(saturation + held.saturation(), fed));
		float alpha = blink();
		for (int i = food / 2; i < (fed + 1) / 2; i++) {
			int x = drumstick(xRight, i);
			Gui2D.sprite(graphics, FOOD_EMPTY, x, yLine, ICON, ICON, white(alpha * 0.25F));
			Gui2D.sprite(graphics, i * 2 + 1 == fed ? FOOD_HALF : FOOD_FULL, x, yLine, ICON, ICON, white(alpha));
		}
		outlines(graphics, xRight, yLine, saturation, 0.0F, 1.0F);
		outlines(graphics, xRight, yLine, sated, saturation, alpha);
	}

	/**
	 * The saturation outlines from {@code from} to {@code to} points, right to left, 2 points a
	 * drumstick: a full outline per 2, the last one by quarters (AppleSkin's icons).
	 */
	private static void outlines(final GuiGraphicsExtractor graphics, final int xRight, final int yLine, final float to, final float from,
		final float alpha) {
		float level = Math.max(0.0F, Math.min(20.0F, to));
		int first = (int) Math.max(0.0F, from / 2.0F);
		int last = (int) Math.ceil(level / 2.0F);
		for (int i = first; i < last; i++) {
			float held = level / 2.0F - i;
			int quarter = held >= 1.0F ? 3 : held > 0.5F ? 2 : held > 0.25F ? 1 : 0;
			Gui2D.blit(graphics, OUTLINES, drumstick(xRight, i), yLine, quarter * ICON, 0.0F, ICON, ICON, ICON, ICON, 4 * ICON, ICON,
				white(alpha));
		}
	}
}
