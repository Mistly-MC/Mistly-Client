package online.mistylauncher.client.compat;

//? if <1.20.2 {
/*import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

// The game's PlayerSkin (1.20.2) for the versions before it: a player's textures, kept together
public record PlayerSkin(ResourceLocation texture, @Nullable String textureUrl, @Nullable ResourceLocation capeTexture,
	@Nullable ResourceLocation elytraTexture, Model model, boolean secure) {
	public enum Model {
		SLIM, WIDE
	}
}
*///?}
