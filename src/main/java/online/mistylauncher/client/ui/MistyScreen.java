package online.mistylauncher.client.ui;

import online.mistylauncher.client.compat.Versions;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/** Base of the Misty screens: the game (or the menu panorama) stays visible behind a night veil. */
public abstract class MistyScreen extends Screen {
	protected final @Nullable Screen parent;

	protected MistyScreen(final Component title, final @Nullable Screen parent) {
		super(title);
		this.parent = parent;
	}

	/** Screen shown again when this one closes (none: back to the game). */
	public final @Nullable Screen parent() {
		return this.parent;
	}

	/** Opacity of the veil over what is behind (0..1). */
	protected abstract float veil();

	/** Whether what is behind is blurred (as much as the game's "menu background blur" option says). */
	protected boolean blur() {
		return true;
	}

	// a method of the game's screens since 1.20.2; before, drawn from the screen mixin
	//? if >=1.20.2 {
	@Override
	//?}
	public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		//? if >=1.21.6 {
		if (this.minecraft.level == null) {
			this.extractPanorama(graphics, a);
		}
		if (this.blur()) {
			this.extractBlurredBackground(graphics);
		}
		//?} else if >=1.20.5 {
		/*if (this.minecraft.level == null) {
			this.extractPanorama(graphics, a);
		}
		if (this.blur()) {
			this.extractBlurredBackground(a);
		}
		*///?} else {
		/*// no panorama nor blur for the screens before 1.20.5: the Mistly landscape out of a world
		if (this.minecraft.level == null) {
			online.mistylauncher.client.menu.MistyBackground.draw(graphics, this.width, this.height);
		}
		*///?}
		graphics.fill(0, 0, this.width, this.height, Ui.alpha(Ui.INK, this.veil()));
		Versions.extractDeferredSubtitles();
	}

	@Override
	public void onClose() {
		Versions.setScreen(this.parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
