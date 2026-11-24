package online.mistylauncher.legacy.hud.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.features.Media;
import online.mistylauncher.legacy.hud.HudModule;
import online.mistylauncher.legacy.ui.Ui;
import org.lwjgl.opengl.GL11;

/**
 * The song playing on the computer: cover, title, artist, progress. Read by the launcher from
 * Windows (Spotify, Deezer, a browser…), nothing to sign in to.
 */
public final class MusicModule extends HudModule {
	private static final int WIDTH = 168;
	private static final int HEIGHT = 40;
	private static final int COVER = 32;
	/** A title too long for the module scrolls, this many pixels per second, with a pause at each end. */
	private static final float SCROLL_SPEED = 22.0F;
	private static final float SCROLL_PAUSE = 1.6F;
	private static final Media.Song SAMPLE = new Media.Song("Night Sky", "Mistly", "Spotify", true, 74_000, 196_000, 0, 0);

	public MusicModule() {
		super("music", new Config.Module(true, 5, -4, 0));
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return Media.song() != null;
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		return WIDTH;
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return HEIGHT;
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		Media.Song song = Media.song();
		boolean sample = song == null;
		if (sample) {
			if (!preview) {
				return;
			}
			song = SAMPLE;
		}
		background(WIDTH, HEIGHT);
		// cover (or a note on the night sky)
		int cx = 4;
		int cy = 4;
		ResourceLocation cover = sample ? null : Media.cover();
		if (cover != null) {
			// the whole image shrunk into the square (its middle square when it is not one)
			int w = Media.coverWidth();
			int h = Media.coverHeight();
			int side = Math.min(w, h);
			Ui.blit(cover, cx, cy, (w - side) / 2.0F, (h - side) / 2.0F, COVER, COVER, side, side, w, h, 0xFFFFFFFF);
		} else {
			Ui.gradient(cx, cy, cx + COVER, cy + COVER, 0xFF1D3F86, 0xFF0E1A38);
			Ui.centered("♪", cx + COVER / 2, cy + 12, Ui.ACCENT);
		}
		// title (scrolling when too long), artist and app
		int tx = cx + COVER + 6;
		int room = WIDTH - tx - 5;
		scrolling(song.title, tx, 5, room, Ui.TEXT);
		String under = song.artist.isEmpty() ? song.app : song.artist;
		Ui.text(Ui.font().trimStringToWidth(under, room), tx, 15, Ui.MUTED);
		// progress bar and time
		long position = sample ? song.position : song.positionNow();
		int barY = 30;
		Ui.fill(tx, barY, tx + room, barY + 2, Ui.TRACK);
		if (song.duration > 0) {
			int done = (int)(room * Math.min(1.0, (double)position / song.duration));
			Ui.fill(tx, barY, tx + done, barY + 2, song.playing ? Ui.FILL : Ui.DIM);
		}
		String time = clock(position) + (song.duration > 0 ? " / " + clock(song.duration) : "");
		String state = song.playing ? "" : "❚❚ ";
		Ui.scaled(state + time, tx, barY + 3.5F, 0.6F, Ui.DIM);
	}

	/** Text that slides when it does not fit, pausing at both ends. */
	private static void scrolling(final String text, final int x, final int y, final int room, final int color) {
		int width = Ui.width(text);
		if (width <= room) {
			Ui.text(text, x, y, color);
			return;
		}
		int overflow = width - room;
		float travel = overflow / SCROLL_SPEED;
		float cycle = 2 * (travel + SCROLL_PAUSE);
		float t = (System.currentTimeMillis() % (long)(cycle * 1000)) / 1000.0F;
		float offset;
		if (t < SCROLL_PAUSE) {
			offset = 0;
		} else if (t < SCROLL_PAUSE + travel) {
			offset = (t - SCROLL_PAUSE) * SCROLL_SPEED;
		} else if (t < 2 * SCROLL_PAUSE + travel) {
			offset = overflow;
		} else {
			offset = overflow - (t - 2 * SCROLL_PAUSE - travel) * SCROLL_SPEED;
		}
		// cut to its room: the HUD is scaled and moved, so the cut is made in the screen's pixels
		java.nio.FloatBuffer matrix = org.lwjgl.BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrix);
		float scale = matrix.get(0);
		int left = Math.round(matrix.get(12) + x * scale);
		int top = Math.round(matrix.get(13) + (y - 1) * scale);
		Ui.scissor(left, top, Math.round(room * scale), Math.round(11 * scale));
		GlStateManager.pushMatrix();
		Ui.text(text, x - Math.round(offset), y, color);
		GlStateManager.popMatrix();
		Ui.endScissor();
	}

	private static String clock(final long millis) {
		long seconds = Math.max(0, millis / 1000);
		return seconds / 60 + ":" + String.format("%02d", seconds % 60);
	}
}
