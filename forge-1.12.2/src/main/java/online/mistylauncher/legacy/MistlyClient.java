package online.mistylauncher.legacy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import online.mistylauncher.legacy.features.Splash;
import online.mistylauncher.legacy.net.Badge;
import online.mistylauncher.legacy.net.MistyNetwork;

/**
 * Mistly Client for Forge 1.12.2: the Mistly menus, HUD, settings and cosmetics of the Fabric
 * versions. Client only: it joins any server, with or without the mod. It hooks into the game with
 * mixins (the {@code mixin} package, see {@link Events}); Forge loads it and its key bindings.
 */
@Mod(modid = MistlyClient.ID, name = "Mistly Client", useMetadata = true, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.12.2]")
public final class MistlyClient {
	public static final String ID = "mistlyclient";
	/** The version of the releases (the same as the Fabric versions'), written in mcmod.info by the build. */
	private static String version = "?";

	public static String version() {
		return version;
	}

	/** Before the game loads its resources (the swords' models are made during that load): the settings. */
	@Mod.EventHandler
	public void preInit(final FMLPreInitializationEvent event) {
		version = event.getModMetadata().version;
		Config.load();
	}

	@Mod.EventHandler
	public void init(final FMLInitializationEvent event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		Keys.register();
		Badge.install(minecraft.fontRenderer);
		MistyNetwork.start(minecraft);
		Splash.configure();
		Presets.starter();
	}
}
