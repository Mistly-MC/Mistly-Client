package online.mistylauncher.legacy.features;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import net.minecraft.client.Minecraft;

/**
 * The loading screen in the Mistly colours. On 1.8.9 it is Forge's (it draws while the mods load,
 * before any of them runs), set by {@code config/splash.properties}: the night background, the
 * Mistly cloud in place of the Mojang logo, the bars in the accent blue. Written here, it shows
 * from the next start. A splash.properties the player changed is left alone.
 */
public final class Splash {
	/** Marks the file as written by Mistly Client (Forge keeps unknown keys when it saves it). */
	private static final String MARK = "mistlyClient";
	/** Folder of the game where the cloud is put for Forge (its "resourcePackPath"). */
	private static final String PACK = "mistly-splash";
	private static final String LOGO = "assets/misty/textures/gui/logo.png";

	private Splash() {
	}

	public static void configure() {
		File game = Minecraft.getMinecraft().gameDir;
		File file = new File(game, "config/splash.properties");
		Properties properties = new Properties();
		if (file.isFile()) {
			try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.ISO_8859_1)) {
				properties.load(reader);
			} catch (IOException e) {
				return;
			}
			// Forge's own defaults (white, Mojang logo) are replaced; anything else was chosen by the player
			boolean ours = properties.containsKey(MARK);
			boolean defaults = "textures/gui/title/mojang.png".equals(properties.getProperty("logoTexture", "textures/gui/title/mojang.png"))
				&& "0xFFFFFF".equalsIgnoreCase(properties.getProperty("background", "0xFFFFFF"));
			if (!ours && !defaults) {
				return;
			}
		}
		try {
			File logo = new File(game, PACK + "/" + LOGO);
			Files.createDirectories(logo.getParentFile().toPath());
			InputStream in = Splash.class.getResourceAsStream("/" + LOGO);
			if (in == null) {
				return;
			}
			try {
				Files.copy(in, logo.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} finally {
				in.close();
			}
			properties.setProperty(MARK, "1");
			properties.setProperty("resourcePackPath", PACK);
			properties.setProperty("logoTexture", "misty:textures/gui/logo.png");
			properties.setProperty("background", "0x0A0F1D");
			properties.setProperty("font", "0xEEF3FF");
			properties.setProperty("barBorder", "0x9CC0FF");
			properties.setProperty("bar", "0x6F9CFF");
			properties.setProperty("barBackground", "0x111A30");
			Files.createDirectories(file.getParentFile().toPath());
			try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.ISO_8859_1)) {
				properties.store(writer, "Splash screen properties (Mistly colours, written by Mistly Client)");
			}
		} catch (IOException | RuntimeException e) {
			// the game's loading screen stays as it is
		}
	}
}
