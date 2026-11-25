package online.mistylauncher.client.hud.modules;

import online.mistylauncher.client.compat.Versions;
import online.mistylauncher.client.compat.Gui2D;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import online.mistylauncher.client.MistyClient;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.hud.HudModule;
import online.mistylauncher.client.ui.Ui;

/**
 * Mini-map: the ground seen from above, 64 × 64 blocks around the player, in the colours of the
 * game's maps (shaded by the relief, like them), north up, with an arrow where the player looks.
 * Drawn again twice a second.
 */
public final class MinimapModule extends HudModule {
	private static final int SIZE = 64;
	private static final int BORDER = 2;
	private static final long REFRESH_MS = 500;
	private static final Identifier TEXTURE = MistyClient.id("minimap");

	private DynamicTexture texture;
	private long drawnAt;

	public MinimapModule() {
		super("minimap", new MistyConfig.Module(false, 2, -4, 70));
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		return SIZE + 2 * BORDER;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return SIZE + 2 * BORDER;
	}

	/** Paints the ground around the player in the texture. */
	private void paint(final Minecraft minecraft) {
		ClientLevel level = minecraft.level;
		LocalPlayer player = minecraft.player;
		if (this.texture == null) {
			this.texture = Versions.texture("Mistly minimap", new NativeImage(SIZE, SIZE, true));
			minecraft.getTextureManager().register(TEXTURE, this.texture);
		}
		NativeImage image = this.texture.getPixels();
		int x0 = player.getBlockX() - SIZE / 2;
		int z0 = player.getBlockZ() - SIZE / 2;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int px = 0; px < SIZE; px++) {
			int x = x0 + px;
			// the height of the block north of the first row, for its shade
			int north = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z0 - 1);
			for (int pz = 0; pz < SIZE; pz++) {
				int z = z0 + pz;
				int height = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
				pos.set(x, height - 1, z);
				BlockState state = level.getBlockState(pos);
				MapColor color = state.getMapColor(level, pos);
				int argb;
				if (color == MapColor.NONE) {
					argb = 0xFF0B1120;
				} else {
					//? if >=1.17 {
					MapColor.Brightness shade = height > north ? MapColor.Brightness.HIGH : height < north ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL;
					//?} else {
					/*// the shades were numbers before 1.17 (0 low, 1 normal, 2 high)
					int shade = height > north ? 2 : height < north ? 0 : 1;
					*///?}
					//? if >=1.21.2 {
					argb = color.calculateARGBColor(shade) | 0xFF000000;
					//?} else {
					/*int abgr = color.calculateRGBColor(shade);
					argb = 0xFF000000 | (abgr & 0xFF) << 16 | abgr & 0xFF00 | (abgr >> 16) & 0xFF;
					*///?}
				}
				Versions.setPixel(image, px, pz, argb);
				north = height;
			}
		}
		this.texture.upload();
	}

	@Override
	public void draw(final GuiGraphicsExtractor graphics, final Minecraft minecraft, final boolean preview) {
		int size = SIZE + 2 * BORDER;
		Ui.panel(graphics, 0, 0, size, size, BACKGROUND);
		if (minecraft.level == null || minecraft.player == null) {
			// the editor: an empty map with its arrow
			graphics.fill(BORDER, BORDER, BORDER + SIZE, BORDER + SIZE, 0xFF1D3F86);
			this.arrow(graphics, 180.0F);
			return;
		}
		long now = Util.getMillis();
		if (this.texture == null || now - this.drawnAt > REFRESH_MS) {
			this.drawnAt = now;
			this.paint(minecraft);
		}
		Gui2D.blit(graphics, TEXTURE, BORDER, BORDER, 0.0F, 0.0F, SIZE, SIZE, SIZE, SIZE, SIZE, SIZE);
		this.arrow(graphics, minecraft.player.getYRot());
	}

	/** The player in the middle: a white dot and a line towards where they look. */
	private void arrow(final GuiGraphicsExtractor graphics, final float yaw) {
		int c = BORDER + SIZE / 2;
		// yaw 0 looks south (down on the map), and turns clockwise
		float dx = -Mth.sin(yaw * Mth.DEG_TO_RAD);
		float dz = Mth.cos(yaw * Mth.DEG_TO_RAD);
		for (int i = 1; i <= 5; i++) {
			int x = c + Math.round(dx * i);
			int y = c + Math.round(dz * i);
			graphics.fill(x, y, x + 1, y + 1, Ui.ACCENT);
		}
		graphics.fill(c - 1, c - 1, c + 2, c + 2, 0xFF000000);
		graphics.fill(c, c, c + 1, c + 1, 0xFFFFFFFF);
	}
}
