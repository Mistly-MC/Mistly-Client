package online.mistylauncher.client.hud.modules;

import online.mistylauncher.client.compat.Gui2D;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.features.ServerFlag;
import online.mistylauncher.client.hud.TextModule;
import online.mistylauncher.client.ui.Ui;

/** The address of the server played on ("cubecraft.net") after the flag of its country (see ServerFlag); nothing in a solo world. */
public final class ServerModule extends TextModule {
	public ServerModule() {
		super("server", new MistyConfig.Module(false, 1, 0, 20), 60);
	}

	private static String address(final Minecraft minecraft) {
		ServerData server = minecraft.getCurrentServer();
		if (server == null || minecraft.isLocalServer()) {
			return null;
		}
		// as typed in the server list, without the default port
		String address = server.ip.strip().toLowerCase(Locale.ROOT);
		return address.endsWith(":25565") ? address.substring(0, address.length() - 6) : address;
	}

	@Override
	public boolean available(final Minecraft minecraft) {
		return address(minecraft) != null;
	}

	/** The flag's height on the HUD (its width keeps the flag's own shape: 15 for 5:3, 12 for 4:3). */
	private static final int FLAG_H = 9;
	private static final int HEIGHT = 14;
	private static final int PADDING = 5;

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		int flag = ServerFlag.ready() ? flagWidth() + 4 : 0;
		return Math.max(60, PADDING + flag + minecraft.font.width(this.text(minecraft, preview)) + PADDING);
	}

	@Override
	public void draw(final GuiGraphicsExtractor graphics, final Minecraft minecraft, final boolean preview) {
		Component text = this.text(minecraft, preview);
		int width = this.width(minecraft, preview);
		background(graphics, width, HEIGHT);
		int flagW = flagWidth();
		int flag = ServerFlag.ready() ? flagW + 4 : 0;
		int x = (width - flag - minecraft.font.width(text)) / 2;
		if (flag > 0) {
			// the whole flag shrunk to the height of a capital letter, with a thin dark outline
			int y = (HEIGHT - FLAG_H) / 2;
			graphics.fill(x - 1, y - 1, x + flagW + 1, y + FLAG_H + 1, 0x66000000);
			Gui2D.blit(graphics, ServerFlag.TEXTURE, x, y, 0.0F, 0.0F, flagW, FLAG_H, ServerFlag.width(), ServerFlag.height(), ServerFlag.width(), ServerFlag.height());
		}
		graphics.text(minecraft.font, text, x + flag, 3, Ui.TEXT, false);
	}

	private static int flagWidth() {
		return ServerFlag.height() <= 0 ? 12 : Math.max(1, Math.round(FLAG_H * (float)ServerFlag.width() / ServerFlag.height()));
	}

	@Override
	protected Component text(final Minecraft minecraft, final boolean preview) {
		String address = address(minecraft);
		// the editor shows an example while not on a server
		return Component.literal(address == null ? "play.cubecraft.net" : address).withColor(Ui.TEXT & 0xFFFFFF);
	}
}
