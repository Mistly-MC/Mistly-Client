package online.mistylauncher.legacy.features;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

/**
 * What the player wears, chosen in the launcher or in the wardrobe: {@code client.json} in Misty's
 * data folder ({"cosmetic": "misty_cape", "hat": "cloudly"}), read again when it changes. The
 * wardrobe writes it with {"from": "game"}, as the Fabric versions do, so the launcher keeps it.
 */
public final class Outfit {
	public static final String NONE = "";
	public static final String CAPE = "misty_cape";
	public static final String ELYTRA = "misty_elytra";
	public static final String EARLY_CAPE = "early_cape";
	public static final String CLOUDLY = "cloudly";

	public static final ResourceLocation MISTY_TEXTURE = new ResourceLocation("misty", "textures/cape/misty.png");
	public static final ResourceLocation EARLY_TEXTURE = new ResourceLocation("misty", "textures/cape/early.png");
	private static final long REFRESH_MS = 2000;

	private static String back = NONE;
	private static String hat = NONE;
	private static long readAt;
	private static long modified = -1;

	private Outfit() {
	}

	/** Misty's data folder: the one the launcher gives the game, else %LOCALAPPDATA%\Misty. */
	private static File launcherDir() {
		String given = System.getProperty("misty.launcher.dir");
		if (given != null && !given.isEmpty()) {
			return new File(given);
		}
		String local = System.getenv("LOCALAPPDATA");
		return local == null ? null : new File(local, "Misty");
	}

	private static File file() {
		File dir = launcherDir();
		return dir == null ? null : new File(dir, "client.json");
	}

	private static void refresh() {
		long now = System.currentTimeMillis();
		if (now - readAt < REFRESH_MS) {
			return;
		}
		readAt = now;
		File file = file();
		if (file == null || !file.isFile()) {
			back = NONE;
			hat = NONE;
			modified = -1;
			return;
		}
		if (file.lastModified() == modified) {
			return;
		}
		modified = file.lastModified();
		try {
			JsonObject json = new JsonParser().parse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
			back = string(json, "cosmetic");
			hat = string(json, "hat");
		} catch (IOException | RuntimeException e) {
			// half-written: read again at the next check
			modified = -1;
		}
	}

	private static String string(final JsonObject json, final String key) {
		return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsString() : NONE;
	}

	/** What is worn on the back: {@link #CAPE}, {@link #ELYTRA}, {@link #EARLY_CAPE} or {@link #NONE}. */
	public static String back() {
		refresh();
		return back;
	}

	/** What is worn on the head: {@link #CLOUDLY} or {@link #NONE}. */
	public static String hat() {
		refresh();
		return hat;
	}

	/** The cape's texture on the back, null for none (the Misty elytra have no 1.8.9 version: their cape). */
	public static ResourceLocation capeTexture() {
		return capeTexture(back());
	}

	/** The texture of a cosmetic worn on the back, null for none. */
	public static ResourceLocation capeTexture(final String worn) {
		if (CAPE.equals(worn) || ELYTRA.equals(worn)) {
			return MISTY_TEXTURE;
		}
		return EARLY_CAPE.equals(worn) ? EARLY_TEXTURE : null;
	}

	/** Equips a cosmetic from the wardrobe: worn at once, and written for the launcher. */
	public static void equip(final boolean onHead, final String cosmetic) {
		refresh();
		if (onHead) {
			hat = cosmetic;
		} else {
			back = cosmetic;
		}
		File file = file();
		if (file == null) {
			return;
		}
		try {
			JsonObject json = new JsonObject();
			json.addProperty("cosmetic", back);
			json.addProperty("hat", hat);
			json.addProperty("from", "game");
			Files.createDirectories(file.getParentFile().toPath());
			File temporary = new File(file.getParentFile(), "client.json.tmp");
			Files.write(temporary.toPath(), json.toString().getBytes(StandardCharsets.UTF_8));
			Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			modified = file.lastModified();
		} catch (IOException | RuntimeException e) {
			// not written: worn for this session only
		}
	}

	public static String name(final String cosmetic) {
		return I18n.format("misty.cosmetic." + (NONE.equals(cosmetic) ? "none" : cosmetic));
	}

	/** "Mistly cape + Cloudly", or "Nothing". */
	public static String describe() {
		String worn = back();
		String head = hat();
		if (NONE.equals(head)) {
			return name(worn);
		}
		if (NONE.equals(worn)) {
			return name(head);
		}
		return name(worn) + " + " + name(head);
	}
}
