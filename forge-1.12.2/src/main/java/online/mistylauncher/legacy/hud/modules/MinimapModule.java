package online.mistylauncher.legacy.hud.modules;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.hud.HudModule;
import online.mistylauncher.legacy.ui.Ui;

/**
 * Mini-map: the ground seen from above, 64 × 64 blocks around the player, in the colours of the
 * game's maps (shaded by the relief, like them), north up, with an arrow where the player looks.
 * Drawn again twice a second.
 */
public final class MinimapModule extends HudModule {
	private static final int SIZE = 64;
	private static final int BORDER = 2;
	private static final long REFRESH_MS = 500;
	private DynamicTexture texture;
	private ResourceLocation location;
	private long drawnAt;

	public MinimapModule() {
		super("minimap", new Config.Module(false, 2, -4, 70));
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
		WorldClient world = minecraft.world;
		if (this.texture == null) {
			this.texture = new DynamicTexture(SIZE, SIZE);
			this.location = minecraft.getTextureManager().getDynamicTextureLocation("mistly_minimap", this.texture);
		}
		int[] pixels = this.texture.getTextureData();
		int x0 = MathHelper.floor(minecraft.player.posX) - SIZE / 2;
		int z0 = MathHelper.floor(minecraft.player.posZ) - SIZE / 2;
		for (int px = 0; px < SIZE; px++) {
			int x = x0 + px;
			// the height of the block north of the first row, for its shade
			int north = world.getHeight(new BlockPos(x, 0, z0 - 1)).getY();
			for (int pz = 0; pz < SIZE; pz++) {
				int z = z0 + pz;
				int height = world.getHeight(new BlockPos(x, 0, z)).getY();
				BlockPos pos = new BlockPos(x, height - 1, z);
				IBlockState state = world.getBlockState(pos);
				MapColor color = state.getMapColor(world, pos);
				int argb;
				if (color == MapColor.AIR || height <= 0) {
					argb = 0xFF0B1120;
				} else {
					// as the game's maps: lighter going up, darker going down
					int shade = height > north ? 2 : height < north ? 0 : 1;
					argb = 0xFF000000 | color.getMapColor(shade);
				}
				pixels[pz * SIZE + px] = argb;
				north = height;
			}
		}
		this.texture.updateDynamicTexture();
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		int size = SIZE + 2 * BORDER;
		Ui.panel(0, 0, size, size, BACKGROUND);
		if (minecraft.world == null || minecraft.player == null) {
			// the editor: an empty map with its arrow
			Ui.fill(BORDER, BORDER, BORDER + SIZE, BORDER + SIZE, 0xFF1D3F86);
			arrow(180.0F);
			return;
		}
		long now = System.currentTimeMillis();
		if (this.texture == null || now - this.drawnAt > REFRESH_MS) {
			this.drawnAt = now;
			this.paint(minecraft);
		}
		Ui.blit(this.location, BORDER, BORDER, 0.0F, 0.0F, SIZE, SIZE, SIZE, SIZE, SIZE, SIZE, 0xFFFFFFFF);
		arrow(minecraft.player.rotationYaw);
	}

	/** The player in the middle: a white dot and a line towards where they look. */
	private static void arrow(final float yaw) {
		int c = BORDER + SIZE / 2;
		// yaw 0 looks south (down on the map), and turns clockwise
		float dx = -MathHelper.sin(yaw * (float)Math.PI / 180.0F);
		float dz = MathHelper.cos(yaw * (float)Math.PI / 180.0F);
		for (int i = 1; i <= 5; i++) {
			int x = c + Math.round(dx * i);
			int y = c + Math.round(dz * i);
			Ui.fill(x, y, x + 1, y + 1, Ui.ACCENT);
		}
		Ui.fill(c - 1, c - 1, c + 2, c + 2, 0xFF000000);
		Ui.fill(c, c, c + 1, c + 1, 0xFFFFFFFF);
	}
}
