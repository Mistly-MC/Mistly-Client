package online.mistylauncher.legacy.ui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.features.Outfit;
import online.mistylauncher.legacy.menu.MistyBackground;

/**
 * The wardrobe, as in the Fabric versions: the categories on the left, the cosmetics of the
 * chosen one in the middle, your outfit in 3D on the right (drag to turn). A click equips.
 */
public final class WardrobeScreen extends MistyScreen {
	private static final int HEADER = 22;
	private static final int CATEGORY_ROW = 22;
	private static final int GAP = 8;

	private enum Category {
		CAPES("misty.wardrobe.capes", "cape", 0xFF9CC0FF, false, Outfit.NONE, Outfit.CAPE, Outfit.EARLY_CAPE),
		HATS("misty.wardrobe.hats", "hats", 0xFF8FE3C8, true, Outfit.NONE, Outfit.CLOUDLY),
		WINGS("misty.wardrobe.wings", "wings", 0xFFC9A8FF, false),
		PETS("misty.wardrobe.pets", "pets", 0xFFFFC46B, false),
		EMOTES("misty.wardrobe.emotes", "emotes", 0xFFFF9CB7, false);

		final String key;
		final ResourceLocation icon;
		final int color;
		final boolean onHead;
		final String[] items;
		final boolean open;

		Category(final String key, final String icon, final int color, final boolean onHead, final String... items) {
			this.key = key;
			this.icon = Ui.icon(icon);
			this.color = color;
			this.onHead = onHead;
			this.items = items;
			this.open = items.length > 0;
		}
	}

	private Category category = Category.CAPES;

	public WardrobeScreen(final GuiScreen parent) {
		super(parent);
	}

	/**
	 * The early player cape is for the first Mistly players: the Mistly network says so at sign-in
	 * (it is also open once the launcher equipped it, kept in the settings).
	 */
	private static boolean locked(final String cosmetic) {
		if (!Outfit.EARLY_CAPE.equals(cosmetic)) {
			return false;
		}
		if (Outfit.EARLY_CAPE.equals(Outfit.back()) && !Config.get().earlyCape) {
			Config.get().earlyCape = true;
			Config.save();
		}
		return !Config.get().earlyCape && !online.mistylauncher.legacy.net.MistyNetwork.early();
	}

	@Override
	protected float veil() {
		return 0.65F;
	}

	private int frameW() {
		return Math.min(this.width - 20, 580);
	}

	private int frameH() {
		return Math.min(this.height - 20, 330);
	}

	private int frameX() {
		return (this.width - this.frameW()) / 2;
	}

	private int frameY() {
		return (this.height - this.frameH()) / 2;
	}

	private int leftW() {
		return Math.max(84, Math.min(110, this.frameW() / 5));
	}

	private int rightW() {
		return Math.max(110, Math.min(210, this.frameW() * 2 / 5));
	}

	private int middleX() {
		return this.frameX() + this.leftW();
	}

	private int middleW() {
		return this.frameW() - this.leftW() - this.rightW();
	}

	private int rightX() {
		return this.frameX() + this.frameW() - this.rightW();
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		int fy = this.frameY();
		int fh = this.frameH();
		Category[] all = Category.values();
		for (int i = 0; i < all.length; i++) {
			this.add(new CategoryRow(this.frameX() + 1, fy + HEADER + 4 + i * CATEGORY_ROW, this.leftW() - 2, all[i]));
		}
		if (this.category.open) {
			int room = this.middleW() - 2 * 10;
			int cardW = Math.min(76, (room - GAP) / 2);
			int cardH = Math.min(96, fh - HEADER - 50);
			int columns = Math.max(1, (room + GAP) / (cardW + GAP));
			String[] items = this.category.items;
			int rows = (items.length + columns - 1) / columns;
			int fitH = (fh - HEADER - 34 - (rows - 1) * GAP) / Math.max(1, rows);
			cardH = Math.max(52, Math.min(cardH, fitH));
			for (int i = 0; i < items.length; i++) {
				int x = this.middleX() + 10 + i % columns * (cardW + GAP);
				int y = fy + HEADER + 10 + i / columns * (cardH + GAP);
				this.add(new Card(x, y, cardW, cardH, this.category.onHead, items[i]));
			}
		}
		int previewH = fh - HEADER - 62;
		int previewW = Math.min(this.rightW() - 20, previewH * 3 / 5);
		this.add(new PlayerPreview(this.rightX() + (this.rightW() - previewW) / 2, fy + HEADER + 12, previewW, previewH));
		this.add(new MistyButton(this.rightX() + 10, fy + fh - 26, this.rightW() - 20, 18, I18n.format("gui.done"), new Runnable() {
			@Override
			public void run() {
				WardrobeScreen.this.mc.displayGuiScreen(WardrobeScreen.this.parent);
			}
		}).primary());
	}

	@Override
	protected void drawBackground() {
		// the launcher's landscape behind the wardrobe (in the menu and in game), lightly veiled
		MistyBackground.draw(this.width, this.height);
		Ui.fill(0, 0, this.width, this.height, Ui.alpha(Ui.INK, 0.35F));
	}

	@Override
	protected void drawUnder(final int mouseX, final int mouseY) {
		int fx = this.frameX();
		int fy = this.frameY();
		int fw = this.frameW();
		int fh = this.frameH();
		Ui.card(fx, fy, fw, fh, 0xF00B1120, 0x33FFFFFF);
		Ui.fill(fx + 1, fy + 1, fx + fw - 1, fy + HEADER, 0xFF111A30);
		Ui.fill(fx + 1, fy + HEADER, fx + fw - 1, fy + HEADER + 1, Ui.LINE);
		Ui.fill(this.middleX(), fy + 1, this.middleX() + 1, fy + fh - 1, Ui.LINE);
		Ui.fill(this.rightX(), fy + 1, this.rightX() + 1, fy + fh - 1, Ui.LINE);
		// the outfit stands on a pedestal of light, in a slightly lighter corner
		Ui.fill(this.rightX() + 1, fy + HEADER + 1, fx + fw - 1, fy + fh - 1, 0x40111A30);
		int center = this.rightX() + this.rightW() / 2;
		int feet = fy + fh - 56;
		for (int i = 0; i < 6; i++) {
			int half = 50 - i * 8;
			Ui.fill(center - half, feet + i, center + half, feet + i + 1, Ui.alpha(Ui.FILL, 0.10F + i * 0.05F));
		}
	}

	@Override
	protected void drawOver(final int mouseX, final int mouseY) {
		int fy = this.frameY();
		int fh = this.frameH();
		int textY = fy + (HEADER - 8) / 2;
		Ui.text(I18n.format("misty.wardrobe.title"), this.frameX() + 10, textY, Ui.TEXT);

		// middle: the category and how many you have
		String name = I18n.format(this.category.key);
		Ui.text(name, this.middleX() + 10, textY, Ui.TEXT);
		if (this.category.open) {
			int owned = 0;
			for (String item : this.category.items) {
				owned += locked(item) ? 0 : 1;
			}
			Ui.text(" [" + owned + "/" + this.category.items.length + "]", this.middleX() + 10 + Ui.width(name), textY, Ui.DIM);
			if (!Config.get().cape) {
				Ui.text(I18n.format("misty.wardrobe.off"), this.middleX() + 10, fy + fh - 16, 0xFFFFB46B);
			}
		} else {
			// a category to come: its icon, big, and a word
			int cx = this.middleX() + this.middleW() / 2;
			int cy = fy + fh / 2 - 10;
			Ui.blit(this.category.icon, cx - 16, cy - 24, 32, 16, 0xFFFFFFFF);
			Ui.centered(I18n.format("misty.wardrobe.coming"), cx, cy + 16, Ui.TEXT);
			Ui.centered(I18n.format("misty.wardrobe.coming.text"), cx, cy + 28, Ui.DIM);
		}

		// right: your outfit
		int center = this.rightX() + this.rightW() / 2;
		Ui.centered(I18n.format("misty.wardrobe.outfit"), center, textY, Ui.ACCENT);
		Ui.centered(SelfSkin.name(), center, fy + fh - 38, Ui.MUTED);
	}

	/** A category in the left column: its icon and its name, coloured; the coming ones dimmed. */
	private final class CategoryRow extends MistyButton {
		private final Category row;

		CategoryRow(final int x, final int y, final int width, final Category row) {
			super(x, y, width, CATEGORY_ROW, I18n.format(row.key), null);
			this.row = row;
		}

		@Override
		public void press() {
			WardrobeScreen.this.category = this.row;
			WardrobeScreen.this.rebuild();
		}

		@Override
		protected void drawContents(final int mouseX, final int mouseY) {
			int x = this.xPosition;
			int y = this.yPosition;
			boolean chosen = WardrobeScreen.this.category == this.row;
			if (chosen || this.hovered) {
				Ui.fill(x, y, x + this.width, y + this.height, chosen ? 0xFF16223F : 0xFF111A30);
			}
			if (chosen) {
				Ui.fill(x, y, x + 2, y + this.height, this.row.color);
			}
			float alpha = this.row.open ? 1.0F : 0.55F;
			Ui.blit(this.row.icon, x + 8, y + 3, 16, 16, Ui.alpha(0xFFFFFFFF, alpha));
			Ui.text(this.displayString, x + 29, y + 7, Ui.alpha(this.row.color, alpha));
		}
	}

	/** One cosmetic: its picture, its name, "equipped" when worn. */
	private static final class Card extends MistyButton {
		private final boolean onHead;
		private final String cosmetic;

		Card(final int x, final int y, final int width, final int height, final boolean onHead, final String cosmetic) {
			super(x, y, width, height, Outfit.name(cosmetic), null);
			this.onHead = onHead;
			this.cosmetic = cosmetic;
		}

		@Override
		public void press() {
			if (!locked(this.cosmetic)) {
				Outfit.equip(this.onHead, this.cosmetic);
			}
		}

		@Override
		protected void drawContents(final int mouseX, final int mouseY) {
			int x = this.xPosition;
			int y = this.yPosition;
			int w = this.width;
			int h = this.height;
			boolean worn = (this.onHead ? Outfit.hat() : Outfit.back()).equals(this.cosmetic);
			boolean locked = locked(this.cosmetic);
			boolean hot = this.hovered;
			// picture area on top (lighter at the bottom, like a stand), name and state under it
			int pictureH = h - 30;
			Ui.card(x, y, w, h, worn ? 0xFF16223F : hot ? 0xFF131D36 : 0xFF0F172B, worn ? Ui.ACCENT : hot ? Ui.alpha(Ui.ACCENT, 0.6F) : Ui.BORDER);
			Ui.gradient(x + 1, y + 1, x + w - 1, y + pictureH, 0x00000000, 0x30FFFFFF);

			int cx = x + w / 2;
			int zoom = Ui.clamp((pictureH - 8) / 16, 1, 4);
			if (Outfit.CAPE.equals(this.cosmetic) || Outfit.EARLY_CAPE.equals(this.cosmetic)) {
				// front of the cape: 10 × 16 at (1, 1) of the 64 × 32 texture
				ResourceLocation texture = Outfit.CAPE.equals(this.cosmetic) ? Outfit.MISTY_TEXTURE : Outfit.EARLY_TEXTURE;
				Ui.blit(texture, cx - 5 * zoom, y + (pictureH - 16 * zoom) / 2, 1.0F, 1.0F, 10 * zoom, 16 * zoom, 10, 16, 64, 32, locked ? 0xFF6F7C99 : 0xFFFFFFFF);
			} else if (Outfit.CLOUDLY.equals(this.cosmetic)) {
				cloud(cx, y + pictureH / 2, Ui.clamp(pictureH / 18, 1, 3));
			} else {
				int s = Math.min(24, pictureH - 12);
				int top = y + (pictureH - s) / 2;
				for (int i = 0; i < s; i++) {
					Ui.fill(cx - s / 2 + i, top + i, cx - s / 2 + i + 2, top + i + 2, Ui.DIM);
					Ui.fill(cx + s / 2 - i - 2, top + i, cx + s / 2 - i, top + i + 2, Ui.DIM);
				}
			}
			if (locked) {
				// a padlock in the corner
				int lx = x + w - 13;
				int ly = y + 3;
				Ui.fill(lx + 2, ly, lx + 8, ly + 1, Ui.MUTED);
				Ui.fill(lx + 1, ly + 1, lx + 2, ly + 4, Ui.MUTED);
				Ui.fill(lx + 8, ly + 1, lx + 9, ly + 4, Ui.MUTED);
				Ui.fill(lx, ly + 4, lx + 10, ly + 10, 0xFFFFC46B);
				Ui.fill(lx + 4, ly + 6, lx + 6, ly + 8, Ui.INK);
			}
			if (worn) {
				// a small check in the corner
				Ui.panel(x + w - 13, y + 3, 10, 10, Ui.FILL);
				Ui.fill(x + w - 11, y + 8, x + w - 10, y + 10, Ui.INK);
				Ui.fill(x + w - 10, y + 9, x + w - 9, y + 11, Ui.INK);
				Ui.fill(x + w - 9, y + 5, x + w - 8, y + 10, Ui.INK);
				Ui.fill(x + w - 8, y + 5, x + w - 7, y + 7, Ui.INK);
			}

			float nameScale = Math.min(1.0F, (w - 6) / (float)Ui.width(this.displayString));
			Ui.centeredScaled(this.displayString, cx, y + h - 24, nameScale, worn ? 0xFFFFFFFF : Ui.TEXT);
			String state = locked ? I18n.format("misty.wardrobe.locked") : worn ? I18n.format("misty.wardrobe.equipped") : I18n.format("misty.wardrobe.equip");
			Ui.centeredScaled(state, cx, y + h - 12, 0.75F, locked ? 0xFFFFC46B : worn ? Ui.ACCENT : Ui.DIM);
		}

		/** Cloudly in the card: a pixel cloud, {@code px} screen pixels per cloud pixel. */
		private static void cloud(final int cx, final int cy, final int px) {
			int[][] rows = {{6, 4}, {4, 8}, {3, 10}, {1, 14}, {0, 16}, {0, 16}, {1, 14}};
			for (int r = 0; r < rows.length; r++) {
				int color = r < 2 ? 0xFFFFFFFF : r < 5 ? 0xFFF1F6FF : 0xFFD6E2FF;
				Ui.fill(cx - 8 * px + rows[r][0] * px, cy - 4 * px + r * px, cx - 8 * px + (rows[r][0] + rows[r][1]) * px, cy - 4 * px + (r + 1) * px, color);
			}
		}
	}
}
