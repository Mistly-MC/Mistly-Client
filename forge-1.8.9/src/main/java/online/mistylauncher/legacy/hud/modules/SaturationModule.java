package online.mistylauncher.legacy.hud.modules;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.Hud;
import online.mistylauncher.legacy.hud.HudModule;
import online.mistylauncher.legacy.hud.TextModule;
import online.mistylauncher.legacy.ui.MistySlider;
import online.mistylauncher.legacy.ui.Palette;

/**
 * Saturation, as AppleSkin shows it on the hunger bar (as in the Fabric versions):
 * <ul>
 * <li>the saturation, the hidden food reserve the game spends before the drumsticks, as golden
 * outlines over them (each holds 2 points, drawn by quarters);</li>
 * <li>with food in hand, what eating it would give, blinking.</li>
 * </ul>
 * Behind the gear: the food preview, "As text" to also show it in a box, and its colour.
 */
public final class SaturationModule extends TextModule {
	private static final String COLOR = "color";
	private static final String TEXT = "text";
	private static final String PREVIEW = "preview";
	/** Yellow, the colour of the saturation in AppleSkin. */
	private static final int DEFAULT_COLOR = 3;
	private static final int ICON = 9;
	/** AppleSkin's saturation outlines (Unlicense): a quarter, a half, three quarters, full; 9 × 9 each. */
	private static final ResourceLocation OUTLINES = new ResourceLocation("misty", "textures/gui/saturation.png");
	private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");
	private static final long BLINK_MS = 1600;

	public SaturationModule() {
		super("saturation", new Config.Module(false, 6, 4, -40), 60);
	}

	@Override
	public List<Setting> settings() {
		return Arrays.asList(new Setting(PREVIEW, true), new Setting(TEXT, false));
	}

	@Override
	public List<HudModule.Slider> sliders() {
		return Collections.<HudModule.Slider>singletonList(new HudModule.Slider() {
			@Override
			public MistySlider create(final int x, final int y, final int width, final int height) {
				return Palette.slider(x, y, width, height, "misty.option.color", new Palette.Value() {
					@Override
					public int get() {
						return SaturationModule.this.state().value(COLOR, DEFAULT_COLOR);
					}

					@Override
					public void set(final int value) {
						SaturationModule.this.state().setValue(COLOR, value);
					}
				});
			}
		});
	}

	private int color() {
		return Palette.color(this.state().value(COLOR, DEFAULT_COLOR));
	}

	/** The box only "as text"; the bar overlay does not need it. */
	@Override
	public boolean available(final Minecraft minecraft) {
		return minecraft.thePlayer != null && this.setting(TEXT);
	}

	@Override
	protected String value(final Minecraft minecraft, final boolean preview) {
		float saturation = minecraft.thePlayer == null ? 5.0F : minecraft.thePlayer.getFoodStats().getSaturationLevel();
		return String.format(Locale.ROOT, "%.1f", saturation);
	}

	@Override
	protected String unit() {
		return "saturation";
	}

	@Override
	protected int valueColor(final Minecraft minecraft, final boolean preview) {
		return this.color();
	}

	/** The module, when it is switched on. */
	private static SaturationModule enabled() {
		if (!Config.get().hud) {
			return null;
		}
		for (HudModule module : Hud.modules()) {
			if (module instanceof SaturationModule && module.state().enabled) {
				return (SaturationModule)module;
			}
		}
		return null;
	}

	/** What the food in hand gives (its food points and saturation), or null. */
	private static float[] held(final EntityPlayer player) {
		ItemStack stack = player.getHeldItem();
		if (stack == null || !(stack.getItem() instanceof ItemFood)) {
			return null;
		}
		ItemFood food = (ItemFood)stack.getItem();
		// golden apples can always be eaten
		if (!player.canEat(false) && stack.getItem() != Items.golden_apple) {
			return null;
		}
		int nutrition = food.getHealAmount(stack);
		return new float[] {nutrition, nutrition * food.getSaturationModifier(stack) * 2.0F};
	}

	/** AppleSkin's blink: fades in, stays, fades out, stays off, 0 to 1. */
	private static float blink() {
		float t = (System.currentTimeMillis() % BLINK_MS) / (float)BLINK_MS;
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

	/** After the game drew the hunger bar ({@code xRight}: its right end, {@code yLine}: its top). */
	public static void overlay(final EntityPlayer player, final int yLine, final int xRight) {
		SaturationModule module = enabled();
		if (module == null) {
			return;
		}
		int food = player.getFoodStats().getFoodLevel();
		float saturation = player.getFoodStats().getSaturationLevel();
		float[] held = module.setting(PREVIEW) ? held(player) : null;
		if (held == null) {
			outlines(xRight, yLine, saturation, 0.0F, 1.0F);
			return;
		}
		int fed = Math.min(20, food + (int)held[0]);
		float sated = Math.max(0.0F, Math.min(saturation + held[1], fed));
		float alpha = blink();
		for (int i = food / 2; i < (fed + 1) / 2; i++) {
			int x = drumstick(xRight, i);
			online.mistylauncher.legacy.ui.Ui.blit(ICONS, x, yLine, 16, 27, ICON, ICON, ICON, ICON, 256, 256, white(alpha * 0.25F));
			online.mistylauncher.legacy.ui.Ui.blit(ICONS, x, yLine, i * 2 + 1 == fed ? 61 : 52, 27, ICON, ICON, ICON, ICON, 256, 256, white(alpha));
		}
		outlines(xRight, yLine, saturation, 0.0F, 1.0F);
		outlines(xRight, yLine, sated, saturation, alpha);
	}

	/** The saturation outlines from {@code from} to {@code to} points, right to left, 2 points a drumstick. */
	private static void outlines(final int xRight, final int yLine, final float to, final float from, final float alpha) {
		float level = Math.max(0.0F, Math.min(20.0F, to));
		int first = (int)Math.max(0.0F, from / 2.0F);
		int last = (int)Math.ceil(level / 2.0F);
		for (int i = first; i < last; i++) {
			float held = level / 2.0F - i;
			int quarter = held >= 1.0F ? 3 : held > 0.5F ? 2 : held > 0.25F ? 1 : 0;
			online.mistylauncher.legacy.ui.Ui.blit(OUTLINES, drumstick(xRight, i), yLine, quarter * ICON, 0.0F, ICON, ICON, ICON, ICON, 4 * ICON, ICON, white(alpha));
		}
	}
}
