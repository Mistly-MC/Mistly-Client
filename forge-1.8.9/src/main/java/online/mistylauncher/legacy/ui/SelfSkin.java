package online.mistylauncher.legacy.ui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;

/**
 * Your own skin for the Mistly menus (title screen, wardrobe, settings). The game of 1.8.9 only
 * loads it in a world: here it is asked to Mojang once, in the background; until it comes (or
 * offline), the default skin of your account is shown.
 */
public final class SelfSkin {
	private static volatile ResourceLocation skin;
	private static volatile boolean slim;
	private static boolean started;

	private SelfSkin() {
	}

	private static GameProfile profile() {
		return Minecraft.getMinecraft().getSession().getProfile();
	}

	private static UUID id() {
		GameProfile profile = profile();
		return profile.getId() != null ? profile.getId() : UUID.nameUUIDFromBytes(("OfflinePlayer:" + profile.getName()).getBytes());
	}

	public static String name() {
		return Minecraft.getMinecraft().getSession().getUsername();
	}

	/** The skin's texture: yours, or your account's default one while it is not loaded. */
	public static ResourceLocation texture() {
		start();
		ResourceLocation loaded = skin;
		return loaded != null ? loaded : DefaultPlayerSkin.getDefaultSkin(id());
	}

	/** Slim arms (the "Alex" model). */
	public static boolean slim() {
		return skin != null ? slim : "slim".equals(DefaultPlayerSkin.getSkinType(id()));
	}

	private static synchronized void start() {
		if (started) {
			return;
		}
		started = true;
		final Minecraft minecraft = Minecraft.getMinecraft();
		if (profile().getId() == null) {
			return;
		}
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					GameProfile filled = minecraft.getSessionService().fillProfileProperties(profile(), false);
					Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = minecraft.getSessionService().getTextures(filled, false);
					final MinecraftProfileTexture texture = textures.get(MinecraftProfileTexture.Type.SKIN);
					if (texture == null) {
						return;
					}
					minecraft.addScheduledTask(new Runnable() {
						@Override
						public void run() {
							minecraft.getSkinManager().loadSkin(texture, MinecraftProfileTexture.Type.SKIN, new SkinManager.SkinAvailableCallback() {
								@Override
								public void skinAvailable(final MinecraftProfileTexture.Type type, final ResourceLocation location, final MinecraftProfileTexture loaded) {
									slim = "slim".equals(loaded.getMetadata("model"));
									skin = location;
								}
							});
						}
					});
				} catch (RuntimeException e) {
					// offline, or Mojang refused (429): the default skin stays
				}
			}
		}, "Mistly skin");
		thread.setDaemon(true);
		thread.start();
	}

	/** Your face (and its hat layer), {@code size} pixels wide. */
	public static void face(final int x, final int y, final int size) {
		ResourceLocation texture = texture();
		Ui.blit(texture, x, y, 8.0F, 8.0F, size, size, 8, 8, 64, 64, 0xFFFFFFFF);
		Ui.blit(texture, x, y, 40.0F, 8.0F, size, size, 8, 8, 64, 64, 0xFFFFFFFF);
	}
}
