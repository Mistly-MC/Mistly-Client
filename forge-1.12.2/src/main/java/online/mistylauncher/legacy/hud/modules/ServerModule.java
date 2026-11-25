package online.mistylauncher.legacy.hud.modules;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ResourceLocation;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.features.Media;
import online.mistylauncher.legacy.hud.HudModule;
import online.mistylauncher.legacy.ui.Ui;

/** The address of the server played on ("cubecraft.net") after the flag of its country; nothing in a solo world. */
public final class ServerModule extends HudModule {
	/** The flag's height on the HUD (its width keeps the flag's own shape). */
	private static final int FLAG_H = 9;
	private static final int HEIGHT = 14;
	private static final int PADDING = 5;

	public ServerModule() {
		super("server", new Config.Module(false, 1, 0, 20));
	}

	private static String address(final Minecraft minecraft) {
		ServerData server = minecraft.getCurrentServerData();
		if (server == null || minecraft.isIntegratedServerRunning() || server.serverIP == null) {
			return null;
		}
		// as typed in the server list, without the default port
		String address = server.serverIP.trim().toLowerCase(Locale.ROOT);
		return address.endsWith(":25565") ? address.substring(0, address.length() - 6) : address;
	}

	private static String text(final Minecraft minecraft) {
		String address = address(minecraft);
		// the editor shows an example while not on a server
		return address == null ? "play.cubecraft.net" : address;
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return address(minecraft) != null;
	}

	private static int flagWidth() {
		return Media.flagHeight() <= 0 ? 12 : Math.max(1, Math.round(FLAG_H * (float)Media.flagWidth() / Media.flagHeight()));
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		int flag = Media.flag() != null ? flagWidth() + 4 : 0;
		return Math.max(60, PADDING + flag + Ui.width(text(minecraft)) + PADDING);
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return HEIGHT;
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		String text = text(minecraft);
		int width = this.width(minecraft, preview);
		background(width, HEIGHT);
		ResourceLocation flag = Media.flag();
		int flagW = flagWidth();
		int shift = flag != null ? flagW + 4 : 0;
		int x = (width - shift - Ui.width(text)) / 2;
		if (flag != null) {
			// the whole flag shrunk to the height of a capital letter, with a thin dark outline
			int y = (HEIGHT - FLAG_H) / 2;
			Ui.fill(x - 1, y - 1, x + flagW + 1, y + FLAG_H + 1, 0x66000000);
			Ui.blit(flag, x, y, 0.0F, 0.0F, flagW, FLAG_H, Media.flagWidth(), Media.flagHeight(), Media.flagWidth(), Media.flagHeight(), 0xFFFFFFFF);
		}
		Ui.text(text, x + shift, 3, Ui.TEXT);
	}
}
