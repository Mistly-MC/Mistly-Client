package online.mistylauncher.legacy;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

/** The mod's keys, changeable in Options > Controls. */
public final class Keys {
	public static final String CATEGORY = "key.categories.mistly";
	/** Opens the HUD editor. */
	public static final KeyBinding EDITOR = new KeyBinding("key.mistly.editor", Keyboard.KEY_RSHIFT, CATEGORY);
	/** Zooms while held. */
	public static final KeyBinding ZOOM = new KeyBinding("key.mistly.zoom", Keyboard.KEY_C, CATEGORY);

	private Keys() {
	}

	static void register() {
		ClientRegistry.registerKeyBinding(EDITOR);
		ClientRegistry.registerKeyBinding(ZOOM);
	}
}
