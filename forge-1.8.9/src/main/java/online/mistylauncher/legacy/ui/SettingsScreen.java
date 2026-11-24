package online.mistylauncher.legacy.ui;

import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.Keys;
import online.mistylauncher.legacy.MistlyClient;
import online.mistylauncher.legacy.features.Outfit;
import online.mistylauncher.legacy.features.Zoom;
import online.mistylauncher.legacy.hud.Hud;
import online.mistylauncher.legacy.hud.HudEditor;
import online.mistylauncher.legacy.hud.HudModule;
import online.mistylauncher.legacy.menu.MistyBackground;

/**
 * Settings of Mistly Client, as in the Fabric versions: the categories on the left, and for the
 * chosen one an animated preview that loops (what the setting does, shown rather than told) and
 * its settings.
 */
public final class SettingsScreen extends MistyScreen {
	private static final int HEADER = 22;
	private static final int CATEGORY_ROW = 22;
	private static final int ROW = 20;
	private static final int GAP = 4;
	private static final int PAD = 10;

	private enum Category {
		GENERAL("misty.settings.general", "options"),
		HUD("misty.settings.hud", "hud"),
		ZOOM("misty.settings.zoom", "zoom"),
		FULLBRIGHT("misty.settings.fullbright", "fullbright"),
		COSMETICS("misty.settings.cosmetics", "network");

		final String key;
		final ResourceLocation icon;

		Category(final String key, final String icon) {
			this.key = key;
			this.icon = Ui.icon(icon);
		}
	}

	/** Kept when the window is resized or the screen comes back. */
	private static Category category = Category.GENERAL;

	private int previewX;
	private int previewY;
	private int previewW;
	private int previewH;

	public SettingsScreen(final GuiScreen parent) {
		super(parent);
	}

	@Override
	protected float veil() {
		return 0.55F;
	}

	private int frameW() {
		return Math.min(this.width - 20, 600);
	}

	private int frameH() {
		return Math.min(this.height - 20, 340);
	}

	private int frameX() {
		return (this.width - this.frameW()) / 2;
	}

	private int frameY() {
		return (this.height - this.frameH()) / 2;
	}

	private int leftW() {
		return Math.max(96, Math.min(124, this.frameW() / 4));
	}

	private int rightX() {
		return this.frameX() + this.leftW();
	}

	private int rightW() {
		return this.frameW() - this.leftW();
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		final Config config = Config.get();
		int fy = this.frameY();
		int fh = this.frameH();

		Category[] all = Category.values();
		int categoryRow = Math.min(CATEGORY_ROW, (fh - HEADER - 8) / all.length);
		for (int i = 0; i < all.length; i++) {
			this.add(new CategoryRow(this.frameX() + 1, fy + HEADER + 4 + i * categoryRow, this.leftW() - 2, categoryRow, all[i]));
		}

		int x = this.rightX() + PAD;
		int inner = this.rightW() - 2 * PAD;
		int footerY = fy + fh - PAD - 18;
		List<HudModule> modules = Hud.modules();
		int hudItems = 1 + modules.size();
		int space = footerY - 8 - (fy + HEADER + 26);
		int hudColumns = space - (hudItems + 1) / 2 * (ROW + GAP) - 8 < 54 ? 3 : 2;
		int rows;
		switch (category) {
			case HUD:
				rows = (hudItems + hudColumns - 1) / hudColumns;
				break;
			case ZOOM:
				rows = 2;
				break;
			case COSMETICS:
				rows = 3;
				break;
			default:
				rows = 1;
				break;
		}
		// a tight HUD grid (small screen): slimmer lines
		boolean tight = category == Category.HUD && rows * (ROW + GAP) - GAP > space;
		int row = tight ? 18 : ROW;
		int gap = tight ? 2 : GAP;
		int optionsH = rows * (row + gap);
		// the preview takes what the settings leave (30 to 130 high; none when even 30 is too much)
		this.previewX = x;
		this.previewY = fy + HEADER + 26;
		this.previewW = inner;
		int left = space - optionsH - 8;
		this.previewH = left < 30 ? 0 : Math.min(left, 130);
		int y = this.previewH == 0 ? this.previewY : this.previewY + this.previewH + 8;
		int half = (inner - GAP) / 2;

		switch (category) {
			case GENERAL:
				this.option(x, y, half, ROW, "customMenu", new MistyToggle.Value() {
					@Override
					public boolean get() {
						return config.customMenu;
					}

					@Override
					public void set(final boolean value) {
						config.customMenu = value;
					}
				});
				break;
			case HUD: {
				int column = (inner - (hudColumns - 1) * GAP) / hudColumns;
				this.option(x, y, column, row, "hud", new MistyToggle.Value() {
					@Override
					public boolean get() {
						return config.hud;
					}

					@Override
					public void set(final boolean value) {
						config.hud = value;
					}
				});
				for (int i = 0; i < modules.size(); i++) {
					final HudModule module = modules.get(i);
					int at = i + 1;
					MistyToggle toggle = new MistyToggle(x + at % hudColumns * (column + GAP), y + at / hudColumns * (row + gap), column, row, module.name(),
						new MistyToggle.Value() {
							@Override
							public boolean get() {
								return module.state().enabled;
							}

							@Override
							public void set(final boolean value) {
								module.state().enabled = value;
							}
						});
					// a module with its own settings: the gear before the switch opens them
					if (!module.settings().isEmpty()) {
						toggle.gear(new Runnable() {
							@Override
							public void run() {
								SettingsScreen.this.mc.displayGuiScreen(new ModuleSettingsScreen(module, SettingsScreen.this));
							}
						});
					}
					this.add(toggle);
				}
				break;
			}
			case ZOOM:
				this.option(x, y, half, ROW, "zoom", new MistyToggle.Value() {
					@Override
					public boolean get() {
						return config.zoom;
					}

					@Override
					public void set(final boolean value) {
						config.zoom = value;
					}
				});
				this.option(x + half + GAP, y, inner - half - GAP, ROW, "zoomSmooth", new MistyToggle.Value() {
					@Override
					public boolean get() {
						return config.zoomSmooth;
					}

					@Override
					public void set(final boolean value) {
						config.zoomSmooth = value;
					}
				});
				this.add(new MistySlider(x, y + ROW + GAP, inner, ROW, new MistySlider.Model() {
					@Override
					public double get() {
						return (Zoom.level() - Zoom.MIN) / (Zoom.MAX - Zoom.MIN);
					}

					@Override
					public void set(final double value) {
						config.zoomLevel = Math.round((Zoom.MIN + (float)value * (Zoom.MAX - Zoom.MIN)) * 10.0F) / 10.0F;
					}

					@Override
					public String label() {
						return I18n.format("misty.option.zoomLevel", String.format("×%.1f", Zoom.level()));
					}
				}));
				break;
			case FULLBRIGHT:
				this.option(x, y, inner, ROW, "fullbright", new MistyToggle.Value() {
					@Override
					public boolean get() {
						return config.fullbright;
					}

					@Override
					public void set(final boolean value) {
						config.fullbright = value;
					}
				});
				break;
			case COSMETICS:
				this.option(x, y, half, ROW, "cosmetics", new MistyToggle.Value() {
					@Override
					public boolean get() {
						return config.cape;
					}

					@Override
					public void set(final boolean value) {
						config.cape = value;
					}
				});
				this.option(x + half + GAP, y, inner - half - GAP, ROW, "network", new MistyToggle.Value() {
					@Override
					public boolean get() {
						return config.network;
					}

					@Override
					public void set(final boolean value) {
						config.network = value;
					}
				});
				this.option(x, y + ROW + GAP, half, ROW, "nametagBadge", new MistyToggle.Value() {
					@Override
					public boolean get() {
						return config.nametagBadge;
					}

					@Override
					public void set(final boolean value) {
						config.nametagBadge = value;
					}
				});
				this.add(new MistyButton(x, y + 2 * (ROW + GAP), inner, ROW, I18n.format("misty.wardrobe.open"), new Runnable() {
					@Override
					public void run() {
						SettingsScreen.this.mc.displayGuiScreen(new WardrobeScreen(SettingsScreen.this));
					}
				}));
				break;
			default:
				break;
		}

		// footer: arrange the HUD (from the HUD category), done
		if (category == Category.HUD) {
			this.add(new MistyButton(x, footerY, half, 18, I18n.format("misty.settings.layout"), new Runnable() {
				@Override
				public void run() {
					Config.save();
					// back from the editor goes to where the settings were opened from, not into a loop
					GuiScreen back = SettingsScreen.this.parent instanceof HudEditor ? ((HudEditor)SettingsScreen.this.parent).parent() : SettingsScreen.this.parent;
					SettingsScreen.this.mc.displayGuiScreen(new HudEditor(back));
				}
			}));
		}
		this.add(new MistyButton(x + half + GAP, footerY, inner - half - GAP, 18, I18n.format("gui.done"), new Runnable() {
			@Override
			public void run() {
				SettingsScreen.this.mc.displayGuiScreen(SettingsScreen.this.parent);
			}
		}).primary());
	}

	private void option(final int x, final int y, final int width, final int height, final String key, final MistyToggle.Value value) {
		this.add(new MistyToggle(x, y, width, height, I18n.format("misty.option." + key), value).tooltip(I18n.format("misty.option." + key + ".tip")));
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
		Ui.fill(this.rightX(), fy + 1, this.rightX() + 1, fy + fh - 1, Ui.LINE);
	}

	@Override
	protected void drawOver(final int mouseX, final int mouseY) {
		int fx = this.frameX();
		int fy = this.frameY();
		int textY = fy + (HEADER - 8) / 2;
		// header: Mistly, the version, the category and what it is about
		Ui.logo(fx + 8, fy + 4, 14);
		Ui.text("MISTLY", fx + 26, textY, Ui.TEXT);
		Ui.text(MistlyClient.VERSION, fx + 26 + Ui.width("MISTLY") + 4, textY, Ui.DIM);
		int x = this.rightX() + PAD;
		Ui.text(I18n.format(category.key), x, textY, Ui.TEXT);
		Ui.text(I18n.format(category.key + ".text"), x, fy + HEADER + 9, Ui.MUTED);
		this.preview();
	}

	@Override
	public void onGuiClosed() {
		Config.save();
	}

	private void choose(final Category chosen) {
		category = chosen;
		this.rebuild();
	}

	// ── previews: short loops that show what each category does ──

	private void preview() {
		if (this.previewH == 0) {
			return;
		}
		int x = this.previewX;
		int y = this.previewY;
		int w = this.previewW;
		int h = this.previewH;
		float seconds = (System.currentTimeMillis() % 3_600_000L) / 1000.0F;
		switch (category) {
			case GENERAL:
				this.previewMenu(x, y, w, h, seconds);
				break;
			case HUD:
				this.previewHud(x, y, w, h, seconds);
				break;
			case ZOOM:
				this.previewZoom(x, y, w, h, seconds);
				break;
			case FULLBRIGHT:
				this.previewFullbright(x, y, w, h, seconds);
				break;
			default:
				this.previewCosmetics(x, y, w, h, seconds);
				break;
		}
		Ui.border(x - 1, y - 1, w + 2, h + 2, 0x4D9CC0FF);
	}

	/** The Mistly menu: the landscape, the cloud and the entries sliding in one after the other. */
	private void previewMenu(final int x, final int y, final int w, final int h, final float seconds) {
		MistyBackground.drawIn(x, y, w, h, 1.0F);
		Ui.scissor(x, y, w, h);
		Ui.fill(x, y, x + w / 2, y + h, 0x660A0F1D);
		Ui.logo(x + 8, y + 6, 12);
		Ui.fill(x + 24, y + 9, x + 52, y + 13, 0xFFFFFFFF);
		float loop = seconds % 4.0F;
		int entries = Math.min(5, (h - 26) / 11);
		for (int i = 0; i < entries; i++) {
			float t = Ui.clamp((loop - i * 0.25F) / 0.4F, 0.0F, 1.0F);
			float ease = 1.0F - (1.0F - t) * (1.0F - t);
			int bladeX = x + 12 + Math.round((1.0F - ease) * -40.0F) + Math.round((float)Math.sin(Math.PI * i / 4) * 6);
			int bladeY = y + 22 + i * 11;
			int alpha = Math.round(255 * ease);
			Ui.fill(bladeX, bladeY, bladeX + 60, bladeY + 8, alpha << 24 | 0x1A2A55);
			Ui.fill(bladeX, bladeY, bladeX + 2, bladeY + 8, alpha << 24 | 0x9CC0FF);
			Ui.fill(bladeX + 6, bladeY + 3, bladeX + 30, bladeY + 5, alpha << 24 | 0xEEF3FF);
		}
		Ui.endScissor();
	}

	/** The HUD in game: FPS that move, the keys lighting up, the coordinates walking. */
	private void previewHud(final int x, final int y, final int w, final int h, final float seconds) {
		MistyBackground.drawIn(x, y, w, h, 1.4F);
		Ui.scissor(x, y, w, h);
		int fps = 140 + Math.round((float)Math.sin(seconds * 2.3F) * 6 + (float)Math.sin(seconds * 7.1F) * 2);
		Ui.box(x + 6, y + 6, "FPS " + fps);
		Ui.box(x + 6, y + 20, "XYZ " + Math.round(seconds * 4 % 200) + " 64 -32");
		String clock = String.format("%02d:%02d", (int)(seconds / 60) % 24, (int)seconds % 60);
		Ui.box(x + w - 6 - Ui.width(clock) - 6, y + 6, clock);
		int key = (int)(seconds * 2.5F) % 4;
		boolean low = h < 56;
		int kx = low ? x + w - 6 - Ui.width(clock) - 6 - 8 - 42 : x + w - 46;
		int ky = low ? y + 4 : y + h - 32;
		Ui.keyCap(kx + 14, ky, "W", key == 0);
		Ui.keyCap(kx, ky + 14, "A", key == 1);
		Ui.keyCap(kx + 14, ky + 14, "S", key == 2);
		Ui.keyCap(kx + 28, ky + 14, "D", key == 3);
		Ui.endScissor();
	}

	/** Zoom: the landscape magnified and back, the key lit while it zooms, the level shown. */
	private void previewZoom(final int x, final int y, final int w, final int h, final float seconds) {
		float level = Zoom.level();
		float loop = seconds % 4.0F;
		float t = loop < 0.6F ? 0.0F : loop < 1.2F ? (loop - 0.6F) / 0.6F : loop < 2.8F ? 1.0F : loop < 3.4F ? 1.0F - (loop - 2.8F) / 0.6F : 0.0F;
		float ease = t * t * (3.0F - 2.0F * t);
		float zoom = 1.0F + (level - 1.0F) * ease;
		MistyBackground.drawIn(x, y, w, h, zoom);
		Ui.scissor(x, y, w, h);
		int cx = x + w / 2;
		int cy = y + h / 2;
		Ui.fill(cx - 4, cy, cx + 5, cy + 1, 0xCCFFFFFF);
		Ui.fill(cx, cy - 4, cx + 1, cy + 5, 0xCCFFFFFF);
		boolean held = loop >= 0.6F && loop < 3.4F;
		Ui.keyCap(x + 6, y + h - 18, GameSettings.getKeyDisplayString(Keys.ZOOM.getKeyCode()), held);
		String text = String.format("×%.1f", zoom);
		Ui.box(x + w - 6 - Ui.width(text) - 6, y + 6, text);
		Ui.endScissor();
	}

	/** Fullbright: a dark night, and the same lit up, the line between them sweeping across. */
	private void previewFullbright(final int x, final int y, final int w, final int h, final float seconds) {
		MistyBackground.drawIn(x, y, w, h, 1.6F);
		Ui.scissor(x, y, w, h);
		float sweep = 0.5F + 0.42F * (float)Math.sin(seconds * 1.2F);
		int split = x + Math.round(w * sweep);
		Ui.fill(x, y, split, y + h, 0xC0000000);
		Ui.fill(split, y, x + w, y + h, 0x30FFF4D6);
		Ui.fill(split, y, split + 1, y + h, 0xFFFFFFFF);
		Ui.text(I18n.format("misty.fullbright.before"), x + 6, y + 6, Ui.MUTED);
		String after = I18n.format("misty.fullbright.after");
		Ui.text(after, x + w - 6 - Ui.width(after), y + 6, 0xFFFFFFFF);
		Ui.blit(Category.FULLBRIGHT.icon, split - 8, y + h / 2 - 8, 16, 16, 0xFFFFFFFF);
		Ui.endScissor();
	}

	/** Cosmetics: your cape, swaying, and the cloud before your name, as the others see you. */
	private void previewCosmetics(final int x, final int y, final int w, final int h, final float seconds) {
		MistyBackground.drawIn(x, y, w, h, 1.0F);
		Ui.scissor(x, y, w, h);
		int cx = x + w / 2;
		int bob = Math.round((float)Math.sin(seconds * 2.0F) * 2.0F);
		int zoom = Math.max(1, Math.min(3, (h - 30) / 18));
		ResourceLocation cape = Outfit.capeTexture() != null ? Outfit.capeTexture() : Outfit.MISTY_TEXTURE;
		Ui.blit(cape, cx - 5 * zoom, y + 22 + bob, 1.0F, 1.0F, 10 * zoom, 16 * zoom, 10, 16, 64, 32, 0xFFFFFFFF);
		String name = SelfSkin.name();
		int tagW = Ui.width(name) + 22;
		int tagX = cx - tagW / 2;
		int tagY = y + 6;
		Ui.fill(tagX, tagY, tagX + tagW, tagY + 12, 0x66000000);
		Ui.logo(tagX + 3, tagY + 2, 8);
		SelfSkin.face(tagX - 14, tagY + 1, 10);
		Ui.text(name, tagX + 14, tagY + 2, 0xFFFFFFFF);
		Ui.endScissor();
	}

	/** A category in the left column: its icon and its name. */
	private final class CategoryRow extends MistyButton {
		private final Category row;

		CategoryRow(final int x, final int y, final int width, final int height, final Category row) {
			super(x, y, width, height, I18n.format(row.key), null);
			this.row = row;
		}

		@Override
		public void press() {
			SettingsScreen.this.choose(this.row);
		}

		@Override
		protected void drawContents(final int mouseX, final int mouseY) {
			int x = this.xPosition;
			int y = this.yPosition;
			boolean chosen = category == this.row;
			if (chosen || this.hovered) {
				Ui.fill(x, y, x + this.width, y + this.height, chosen ? 0xFF16223F : 0xFF111A30);
			}
			if (chosen) {
				Ui.fill(x, y, x + 2, y + this.height, Ui.ACCENT);
			}
			Ui.blit(this.row.icon, x + 8, y + (this.height - 16) / 2, 16, 16, 0xFFFFFFFF);
			Ui.text(this.displayString, x + 29, y + (this.height - 8) / 2, chosen ? 0xFFFFFFFF : Ui.TEXT);
		}
	}
}
