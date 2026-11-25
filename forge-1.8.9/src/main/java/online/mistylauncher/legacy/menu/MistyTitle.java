package online.mistylauncher.legacy.menu;

import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiModList;
import online.mistylauncher.legacy.MistlyClient;
import online.mistylauncher.legacy.features.Outfit;
import online.mistylauncher.legacy.ui.MistyButton;
import online.mistylauncher.legacy.ui.PlayerPreview;
import online.mistylauncher.legacy.ui.SelfSkin;
import online.mistylauncher.legacy.ui.SettingsScreen;
import online.mistylauncher.legacy.ui.Ui;
import online.mistylauncher.legacy.ui.WardrobeScreen;

/**
 * The Mistly main menu, as in the Fabric versions, over the launcher's night landscape:
 * <ul>
 * <li>the title in the top-left corner;</li>
 * <li>the entries along a glowing path of diamonds, each with an icon, its name and a line
 * saying where it leads (Mods in place of Realms: the Forge mods list);</li>
 * <li>you in the bottom-left corner (your head opens the wardrobe);</li>
 * <li>on the right, you in 3D with your cosmetics, and the wardrobe button.</li>
 * </ul>
 */
public final class MistyTitle extends GuiScreen {
	private static final String COPYRIGHT = "Copyright Mojang AB. Do not distribute!";
	private static final int ENTRY_W = 176;
	private static final int ENTRIES = 5;
	/** Space kept for the title at the top and for you at the bottom. */
	private static final int TITLE = 66;
	private static final int BOTTOM = 56;
	private static final int ICON = 20;
	/** You in 3D on the right of the menu (hidden on narrow windows). */
	private static final int CHARACTER_W = 120;
	private static final int PANEL_MIN_SCREEN = 420;
	/** Right edge of the menu entries (with their slide). */
	private static final int MENU_RIGHT = 54 + 22 + ENTRY_W + 12;

	/** Distance between two entries: they close up on low windows. */
	private int step() {
		return Ui.clamp((this.height - TITLE - BOTTOM) / ENTRIES, 24, 34);
	}

	private int entryHeight() {
		return this.step() - 6;
	}

	/** Y of the first entry: under the title, centred in what is left above you. */
	private int entriesTop() {
		int free = this.height - TITLE - BOTTOM - ENTRIES * this.step();
		return TITLE + 8 + Math.max(0, free / 2);
	}

	/** X of an entry: the path bulges in the middle, like an arc. */
	private static int entryX(final int index) {
		return 54 + Math.round((float)Math.sin(Math.PI * index / (ENTRIES - 1)) * 22.0F);
	}

	private boolean panel() {
		return this.width >= PANEL_MIN_SCREEN;
	}

	private int characterHeight() {
		return Math.min(200, this.height - 124);
	}

	private int characterCenter() {
		int free = this.width - MENU_RIGHT;
		return MENU_RIGHT + Math.max(CHARACTER_W / 2, free * 9 / 20);
	}

	private int characterTop() {
		return (this.height - this.characterHeight()) / 2 - 4;
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		final GuiScreen self = this;
		int top = this.entriesTop();
		int step = this.step();
		int entryH = this.entryHeight();
		this.buttonList.add(new Portal(entryX(0), top, entryH, "menu.singleplayer", "misty.title.singleplayer", "singleplayer", new Runnable() {
			@Override
			public void run() {
				MistyTitle.this.mc.displayGuiScreen(new GuiSelectWorld(self));
			}
		}));
		this.buttonList.add(new Portal(entryX(1), top + step, entryH, "menu.multiplayer", "misty.title.multiplayer", "multiplayer", new Runnable() {
			@Override
			public void run() {
				MistyTitle.this.mc.displayGuiScreen(new GuiMultiplayer(self));
			}
		}));
		this.buttonList.add(new Portal(entryX(2), top + 2 * step, entryH, "fml.menu.mods", "misty.title.mods", "presets", new Runnable() {
			@Override
			public void run() {
				MistyTitle.this.mc.displayGuiScreen(new GuiModList(self));
			}
		}));
		this.buttonList.add(new Portal(entryX(3), top + 3 * step, entryH, "menu.options", "misty.title.options", "options", new Runnable() {
			@Override
			public void run() {
				MistyTitle.this.mc.displayGuiScreen(new GuiOptions(self, MistyTitle.this.mc.gameSettings));
			}
		}));
		this.buttonList.add(new Portal(entryX(4), top + 4 * step, entryH, "menu.quit", "misty.title.quit", "quit", new Runnable() {
			@Override
			public void run() {
				MistyTitle.this.mc.shutdown();
			}
		}));

		Runnable wardrobe = new Runnable() {
			@Override
			public void run() {
				MistyTitle.this.mc.displayGuiScreen(new WardrobeScreen(self));
			}
		};
		// you, bottom left: your head opens the wardrobe (drawn by the screen, the button only reacts)
		this.buttonList.add(new MistyButton(14, this.height - 50, 36, 36, I18n.format("misty.wardrobe.title"), wardrobe) {
			@Override
			protected void drawContents(final int mouseX, final int mouseY) {
			}
		}.tooltip(I18n.format("misty.wardrobe.title")));

		// on the right: you in 3D, standing on the landscape, and the wardrobe under your feet
		if (this.panel()) {
			int center = this.characterCenter();
			int characterY = this.characterTop();
			int characterH = this.characterHeight();
			this.buttonList.add(new PlayerPreview(center - CHARACTER_W / 2, characterY, CHARACTER_W, characterH));
			this.buttonList.add(new MistyButton(center - 50, characterY + characterH + 18, 100, 18, I18n.format("misty.wardrobe.open"), wardrobe).primary());
		}

		// top right: patch notes, language, Mistly settings
		int iconY = 10;
		int iconX = this.width - 10 - 2 * ICON - 4;
		this.buttonList.add(new MistyButton(iconX - ICON - 4, iconY, ICON, ICON, I18n.format("misty.patchNotes.title"), new Runnable() {
			@Override
			public void run() {
				MistyTitle.this.mc.displayGuiScreen(new online.mistylauncher.legacy.ui.PatchNotesScreen(self));
			}
		}).texture(Ui.icon("patch_notes")));
		this.buttonList.add(new MistyButton(iconX, iconY, ICON, ICON, I18n.format("options.language"), new Runnable() {
			@Override
			public void run() {
				MistyTitle.this.mc.displayGuiScreen(new GuiLanguage(self, MistyTitle.this.mc.gameSettings, MistyTitle.this.mc.getLanguageManager()));
			}
		}).texture(Ui.icon("language")));
		this.buttonList.add(new MistyButton(iconX + ICON + 4, iconY, ICON, ICON, I18n.format("misty.settings.title"), new Runnable() {
			@Override
			public void run() {
				MistyTitle.this.mc.displayGuiScreen(new SettingsScreen(self));
			}
		}).cloud());
	}

	@Override
	protected void actionPerformed(final net.minecraft.client.gui.GuiButton button) {
		if (button instanceof MistyButton) {
			((MistyButton)button).press();
		}
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
		this.background();
		for (net.minecraft.client.gui.GuiButton button : this.buttonList) {
			button.drawButton(this.mc, mouseX, mouseY);
		}
		this.foreground();
		for (net.minecraft.client.gui.GuiButton button : this.buttonList) {
			if (button instanceof MistyButton && ((MistyButton)button).tooltip != null && ((MistyButton)button).hovered(mouseX, mouseY)) {
				this.drawHoveringText(java.util.Collections.singletonList(((MistyButton)button).tooltip), mouseX, mouseY);
				net.minecraft.client.renderer.GlStateManager.disableLighting();
				break;
			}
		}
	}

	/** Under the widgets: the landscape, a veil on the left for the texts, the path, the pedestal. */
	private void background() {
		MistyBackground.draw(this.width, this.height);

		// darker on the left, where the menu is
		int veil = Math.min(330, this.width / 2);
		for (int i = 0; i < 12; i++) {
			int x0 = veil * i / 12;
			int x1 = veil * (i + 1) / 12;
			Ui.fill(x0, 0, x1, this.height, Ui.alpha(Ui.INK, 0.62F * (1.0F - i / 12.0F)));
		}

		// the path: from under the title, through a diamond in front of each entry
		int top = this.entriesTop();
		int step = this.step();
		int entryH = this.entryHeight();
		int previousX = 30;
		int previousY = top - 20;
		for (int i = 0; i < ENTRIES; i++) {
			int nodeX = entryX(i) - 18;
			int nodeY = top + i * step + entryH / 2;
			line(previousX, previousY, nodeX, nodeY);
			previousX = nodeX;
			previousY = nodeY;
		}
		for (int i = 0; i < ENTRIES; i++) {
			Ui.diamond(entryX(i) - 18, top + i * step + entryH / 2, 5, Ui.alpha(Ui.ACCENT, 0.9F), 0xFF0E1830);
		}

		if (this.panel()) {
			// a pedestal of light under the feet
			int center = this.characterCenter();
			int feet = this.characterTop() + this.characterHeight() - 8;
			for (int i = 0; i < 6; i++) {
				int half = 46 - i * 7;
				Ui.fill(center - half, feet + i, center + half, feet + i + 1, Ui.alpha(Ui.FILL, 0.10F + i * 0.05F));
			}
		}
	}

	/** Over the widgets: title, you, versions, copyright. */
	private void foreground() {
		// title, top left
		Ui.logo(20, 16, 30);
		Ui.scaled("MISTLY", 57, 20, 3.0F, Ui.SLATE);
		Ui.scaled("MISTLY", 56, 19, 3.0F, 0xFFFFFFFF);
		Ui.text("C L I E N T", 58, 46, Ui.ACCENT);
		Ui.scaled("§l" + I18n.format("misty.title.portals"), 40.0F, this.entriesTop() - 12.0F, 0.75F, Ui.MUTED);

		// you, bottom left: your head in a frame, your name, what you wear
		String name = SelfSkin.name();
		int frameX = 14;
		int frameY = this.height - 50;
		Ui.card(frameX, frameY, 36, 36, 0xE00E1527, Ui.alpha(Ui.ACCENT, 0.7F));
		SelfSkin.face(frameX + 4, frameY + 4, 28);
		Ui.text(name, frameX + 44, frameY + 9, Ui.TEXT);
		Ui.text(I18n.format("misty.title.wearing", Outfit.describe()), frameX + 44, frameY + 21, Ui.DIM);

		// your name above your head, like in the game, and what you wear under your feet
		if (this.panel()) {
			int center = this.characterCenter();
			int top = this.characterTop();
			int nameW = Ui.width(name) + 8;
			Ui.fill(center - nameW / 2, top - 12, center + nameW / 2, top - 1, 0x66000000);
			Ui.centered(name, center, top - 10, 0xFFFFFFFF);
			Ui.centered(Outfit.describe(), center, top + this.characterHeight() + 5, Ui.MUTED);
		}

		// versions above the copyright, bottom right
		String versions = "Mistly Client " + MistlyClient.version() + " · Minecraft 1.8.9";
		Ui.text(versions, this.width - 4 - Ui.width(versions), this.height - 23, Ui.DIM);
		this.drawString(this.fontRendererObj, COPYRIGHT, this.width - 4 - Ui.width(COPYRIGHT), this.height - 10, 0xFFFFFFFF);
	}

	/** Glowing line between two points: a soft halo and a bright core. */
	private static void line(final int x0, final int y0, final int x1, final int y1) {
		int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
		for (int s = 0; s <= steps; s++) {
			float t = (float)s / Math.max(1, steps);
			int x = Math.round(Ui.lerp(t, x0, x1));
			int y = Math.round(Ui.lerp(t, y0, y1));
			Ui.fill(x - 1, y - 1, x + 2, y + 2, Ui.alpha(Ui.ACCENT, 0.10F));
			Ui.fill(x, y, x + 1, y + 1, Ui.alpha(Ui.ACCENT, 0.65F));
		}
	}

	@Override
	protected void keyTyped(final char typed, final int key) {
		// Escape does nothing on the main menu
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	/**
	 * An entry of the menu: a glass blade pointing right, an icon in a diamond, its name and where
	 * it leads. It slides a little to the right under the mouse.
	 */
	private static final class Portal extends MistyButton {
		private final String subtitle;
		private final ResourceLocation icon;
		private float slide;

		Portal(final int x, final int y, final int height, final String key, final String subtitleKey, final String icon, final Runnable onPress) {
			super(x, y, ENTRY_W, height, I18n.format(key), onPress);
			this.subtitle = I18n.format(subtitleKey);
			this.icon = Ui.icon(icon);
		}

		@Override
		protected void drawContents(final int mouseX, final int mouseY) {
			boolean hot = this.enabled && this.hovered;
			this.slide = Ui.lerp(0.25F, this.slide, hot ? 6.0F : 0.0F);
			int x = this.xPosition + Math.round(this.slide);
			int y = this.yPosition;
			int h = this.height;
			int w = ENTRY_W;

			// the blade: straight on the left, a point on the right
			int body = hot ? 0xE81A2A55 : 0xC40B1226;
			int edge = hot ? Ui.ACCENT : 0x40FFFFFF;
			for (int r = 0; r < h; r++) {
				int inset = Math.abs(r * 2 - (h - 1)) * 5 / h;
				int right = x + w - 6 + (5 - inset);
				Ui.fill(x + 8, y + r, right, y + r + 1, body);
				Ui.fill(right, y + r, right + 1, y + r + 1, edge);
			}
			Ui.fill(x + 8, y, x + w - 6, y + 1, Ui.alpha(edge, 0.8F));
			Ui.fill(x + 8, y + h - 1, x + w - 6, y + h, Ui.alpha(edge, 0.8F));
			if (hot) {
				// glow under the blade
				Ui.fill(x + 14, y + h, x + w - 12, y + h + 2, Ui.alpha(Ui.ACCENT, 0.25F));
			}

			// icon in a diamond, overlapping the blade's left end
			int cx = x + 12;
			int cy = y + h / 2;
			int radius = Math.min(13, h / 2 + 1);
			Ui.diamond(cx, cy, radius, hot ? Ui.ACCENT : 0xFF6F86B8, 0xFF101A33);
			int size = radius >= 12 ? 16 : 12;
			Ui.blit(this.icon, cx - size / 2, cy - size / 2, size, 16, 0xFFFFFFFF);

			// name and where it leads (short entries: the name alone, centred)
			int textColor = hot ? 0xFFFFFFFF : Ui.TEXT;
			boolean withSubtitle = h >= 22;
			int nameY = withSubtitle ? y + (h - 17) / 2 : y + (h - 8) / 2;
			Ui.text("§l" + this.displayString, x + 30, nameY, textColor);
			if (withSubtitle) {
				Ui.scaled(this.subtitle, x + 30, nameY + 11, 0.75F, hot ? Ui.ACCENT : Ui.MUTED);
			}
		}
	}
}
