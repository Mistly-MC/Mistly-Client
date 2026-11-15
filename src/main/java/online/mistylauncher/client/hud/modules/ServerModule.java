package online.mistylauncher.client.hud.modules;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.hud.TextModule;
import online.mistylauncher.client.ui.Ui;

/** The address of the server played on ("IP cubecraft.net"); nothing in a solo world. */
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

	@Override
	protected Component text(final Minecraft minecraft, final boolean preview) {
		String address = address(minecraft);
		// the editor shows an example while not on a server
		return Component.literal("IP ").withColor(Ui.ACCENT & 0xFFFFFF).append(Component.literal(address == null ? "play.cubecraft.net" : address).withColor(Ui.TEXT & 0xFFFFFF));
	}
}
