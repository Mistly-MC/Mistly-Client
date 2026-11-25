package online.mistylauncher.legacy.hud.modules;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.HudModule;
import online.mistylauncher.legacy.ui.Ui;

/** Your block coordinates, one axis per line, and the direction you face on the right. */
public final class CoordsModule extends HudModule {
	private static final String[] AXES = {"X", "Y", "Z"};
	private static final String[] FACING = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
	private static final int ROW = 10;
	private static final int PADDING = 5;
	private static final String DIRECTION = "direction";

	public CoordsModule() {
		super("coords", new Config.Module(true, 0, 4, 36));
	}

	@Override
	public List<Setting> settings() {
		return Collections.singletonList(new Setting(DIRECTION, true));
	}

	private static int[] position(final Minecraft minecraft) {
		if (minecraft.player == null) {
			return new int[] {128, 64, -256};
		}
		return new int[] {MathHelper.floor(minecraft.player.posX), MathHelper.floor(minecraft.player.posY),
			MathHelper.floor(minecraft.player.posZ)};
	}

	private static String facing(final Minecraft minecraft) {
		if (minecraft.player == null) {
			return "N";
		}
		// yaw 0 looks south and grows clockwise: eight slices of 45°
		int slice = Math.round(minecraft.player.rotationYaw / 45.0F);
		return FACING[((slice % 8) + 8) % 8];
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		int widest = 0;
		for (int value : position(minecraft)) {
			widest = Math.max(widest, Ui.width(Integer.toString(value)));
		}
		// without the direction, only as wide as the numbers
		return this.setting(DIRECTION) ? Math.max(72, PADDING + 10 + widest + 8 + Ui.width("NW") + PADDING) : PADDING + 10 + widest + PADDING;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return 3 * ROW + 2 * PADDING - 2;
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		int width = this.width(minecraft, preview);
		background(width, this.height(minecraft, preview));
		int[] position = position(minecraft);
		for (int i = 0; i < 3; i++) {
			int y = PADDING + i * ROW;
			Ui.text(AXES[i], PADDING, y, Ui.ACCENT);
			Ui.text(Integer.toString(position[i]), PADDING + 10, y, Ui.TEXT);
		}
		if (this.setting(DIRECTION)) {
			String facing = facing(minecraft);
			Ui.text(facing, width - PADDING - Ui.width(facing), PADDING + ROW, Ui.MUTED);
		}
	}
}
