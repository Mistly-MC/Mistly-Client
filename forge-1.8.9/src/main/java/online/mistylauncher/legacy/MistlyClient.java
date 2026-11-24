package online.mistylauncher.legacy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import online.mistylauncher.legacy.features.Cape;

/**
 * Mistly Client for Forge 1.8.9: the HUD (movable with Right Shift), zoom, fullbright and the
 * Misty cape. Client only: it joins any server, with or without the mod.
 */
@Mod(modid = MistlyClient.ID, name = "Mistly Client", version = MistlyClient.VERSION, acceptableRemoteVersions = "*",
	acceptedMinecraftVersions = "[1.8.9]")
public final class MistlyClient {
	public static final String ID = "mistlyclient";
	public static final String VERSION = "1.0.0";

	@Mod.EventHandler
	public void init(final FMLInitializationEvent event) {
		Config.load();
		Keys.register();
		MinecraftForge.EVENT_BUS.register(new Events());
		online.mistylauncher.legacy.net.Badge.install(net.minecraft.client.Minecraft.getMinecraft().fontRendererObj);
		online.mistylauncher.legacy.net.MistyNetwork.start(net.minecraft.client.Minecraft.getMinecraft());
		online.mistylauncher.legacy.features.Splash.configure();
	}

	/** The player renderers exist once every mod is loaded: the cape layer goes on them then. */
	@Mod.EventHandler
	public void postInit(final FMLPostInitializationEvent event) {
		Cape.addLayers();
	}
}
