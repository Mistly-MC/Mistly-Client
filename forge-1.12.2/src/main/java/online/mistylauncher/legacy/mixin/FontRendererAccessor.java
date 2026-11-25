package online.mistylauncher.legacy.mixin;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The widths of the unicode glyphs, where the Mistly badge's glyph gets its own. */
@Mixin(FontRenderer.class)
public interface FontRendererAccessor {
	@Accessor("glyphWidth")
	byte[] misty$glyphWidth();
}
