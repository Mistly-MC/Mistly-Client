package online.mistylauncher.legacy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import online.mistylauncher.legacy.features.Cape;
import online.mistylauncher.legacy.features.Renderers;
import online.mistylauncher.legacy.features.Splash;
import online.mistylauncher.legacy.net.Badge;
import online.mistylauncher.legacy.net.MistyNetwork;

/**
 * Mistly Client for Forge 1.8.9: the Mistly menus, HUD, settings and cosmetics of the Fabric
 * versions. Client only: it joins any server, with or without the mod.
 */
@Mod(modid = MistlyClient.ID, name = "Mistly Client", useMetadata = true, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.8.9]")
public final class MistlyClient {
	public static final String ID = "mistlyclient";
	/** The version of the releases (the same as the Fabric versions'), written in mcmod.info by the build. */
	private static String version = "?";

	public static String version() {
		return version;
	}

	/**
	 * Before the game loads its resources: the settings, the events (the swords' models are made
	 * during that load) and the renderers of dropped items and TNT.
	 */
	@Mod.EventHandler
	public void preInit(final FMLPreInitializationEvent event) {
		version = event.getModMetadata().version;
		Config.load();
		MinecraftForge.EVENT_BUS.register(new Events());
		Renderers.register();
	}

	@Mod.EventHandler
	public void init(final FMLInitializationEvent event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		Keys.register();
		Badge.install(minecraft.fontRendererObj);
		online.mistylauncher.legacy.features.JoinRetry.install(minecraft);
		MistyNetwork.start(minecraft);
		Splash.configure();
		Presets.starter();
	}

	/** The player renderers exist once every mod is loaded: the cosmetics' layers go on them then. */
	@Mod.EventHandler
	public void postInit(final FMLPostInitializationEvent event) {
		Cape.addLayers();
	}
}
