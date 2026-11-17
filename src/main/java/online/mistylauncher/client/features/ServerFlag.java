package online.mistylauncher.client.features;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import online.mistylauncher.client.MistyClient;
import org.jspecify.annotations.Nullable;

/**
 * The flag of the country the server is in, for the Server module. The server's IP (the one the
 * game is connected to) is located by country.is, and its flag comes from flagcdn.com, once per
 * server joined. Nothing for local and private addresses.
 */
public final class ServerFlag {
	public static final Identifier TEXTURE = MistyClient.id("server_flag");
	/** The flag's size in the texture (flagcdn's 40 px wide flags are 40 × 30, some a bit taller). */
	private static int width;
	private static int height;

	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NORMAL).build();

	private static @Nullable ClientPacketListener connection;
	/** The country shown (ISO code, lower case), null while unknown. */
	private static volatile @Nullable String country;
	private static volatile boolean ready;

	private ServerFlag() {
	}

	/** Whether the flag of the current server is loaded in {@link #TEXTURE}. */
	public static boolean ready() {
		return ready;
	}

	public static int width() {
		return width;
	}

	public static int height() {
		return height;
	}

	/** Each client tick: a new server (or none) looks for its flag. */
	public static void tick(final Minecraft minecraft) {
		ClientPacketListener current = minecraft.getConnection();
		if (current == connection) {
			return;
		}
		connection = current;
		ready = false;
		country = null;
		if (current == null || minecraft.isLocalServer()) {
			return;
		}
		SocketAddress remote = current.getConnection().getRemoteAddress();
		if (!(remote instanceof InetSocketAddress address) || address.getAddress() == null) {
			return;
		}
		var ip = address.getAddress();
		if (ip.isLoopbackAddress() || ip.isSiteLocalAddress() || ip.isLinkLocalAddress() || ip.isAnyLocalAddress()) {
			return;
		}
		load(minecraft, current, ip.getHostAddress());
	}

	public static void load(final Minecraft minecraft, final @Nullable ClientPacketListener asked, final String ip) {
		get("https://api.country.is/" + ip, HttpResponse.BodyHandlers.ofString()).thenAccept(body -> {
			String code = JsonParser.parseString(body).getAsJsonObject().get("country").getAsString().toLowerCase(Locale.ROOT);
			if (!code.matches("[a-z]{2}")) {
				return;
			}
			get("https://flagcdn.com/w40/" + code + ".png", HttpResponse.BodyHandlers.ofByteArray()).thenAccept(png ->
				minecraft.execute(() -> {
					// still on that server
					if (connection != asked) {
						return;
					}
					try {
						NativeImage image = NativeImage.read(png);
						width = image.getWidth();
						height = image.getHeight();
						minecraft.getTextureManager().register(TEXTURE, new DynamicTexture(() -> "Mistly server flag", image));
						country = code;
						ready = true;
					} catch (Exception e) {
						MistyClient.LOG.debug("Server flag not readable", e);
					}
				}));
		});
	}

	private static <T> java.util.concurrent.CompletableFuture<T> get(final String url, final HttpResponse.BodyHandler<T> body) {
		HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).header("User-Agent", "Mistly-Client").GET().build();
		return HTTP.sendAsync(request, body).thenApply(response -> {
			if (response.statusCode() != 200) {
				throw new IllegalStateException("HTTP " + response.statusCode());
			}
			return response.body();
		});
	}
}
