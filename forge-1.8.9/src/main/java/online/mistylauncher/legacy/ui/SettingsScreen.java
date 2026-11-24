package online.mistylauncher.legacy.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.Keys;
import online.mistylauncher.legacy.MistlyClient;
import online.mistylauncher.legacy.Presets;
import online.mistylauncher.legacy.features.Combat;
import online.mistylauncher.legacy.features.Outfit;
import online.mistylauncher.legacy.features.Visuals;
import online.mistylauncher.legacy.features.Zoom;
import online.mistylauncher.legacy.hud.Hud;
import online.mistylauncher.legacy.hud.HudEditor;
import online.mistylauncher.legacy.hud.HudModule;
import online.mistylauncher.legacy.hud.modules.KillsModule;
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
	/** The switches of the HUD category before its modules. */
	private static final int HUD_SWITCHES = 9;

	private enum Category {
		GENERAL("misty.settings.general", "options"),
		HUD("misty.settings.hud", "hud"),
		ZOOM("misty.settings.zoom", "zoom"),
		FULLBRIGHT("misty.settings.fullbright", "fullbright"),
		PVP("misty.settings.pvp", "pvp"),
		SERVER("misty.settings.server", "multiplayer"),
		COSMETICS("misty.settings.cosmetics", "network"),
		PRESETS("misty.settings.presets", "presets");

		final String key;
		final ResourceLocation icon;

		Category(final String key, final String icon) {
			this.key = key;
			this.icon = Ui.icon(icon);
		}
	}

	/** Kept when the window is resized or the screen comes back. */
	private static Category category = Category.GENERAL;
	/** HUD category: its grid scrolls when its rows do not all fit. */
	private static int hudScroll;

	private int previewX;
	private int previewY;
	private int previewW;
	private int previewH;
	private int hudMaxScroll;
	private int hudGridY;
	private int hudGridH;
	private int presetsX;
	private int presetsY;
	private int presetsW;
	private int presetsButtonW;

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

	/** A boolean setting read and changed in place. */
	private abstract static class Value implements MistyToggle.Value {
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
		List<HudModule> hudModules = new ArrayList<HudModule>();
		for (HudModule module : Hud.modules()) {
			if (module.inHudCategory()) {
				hudModules.add(module);
			}
		}
		int hudItems = HUD_SWITCHES + hudModules.size();
		int space = footerY - 8 - (fy + HEADER + 26);
		int hudColumns = space - (hudItems + 1) / 2 * (ROW + GAP) - 8 < 54 ? 3 : 2;
		int rows;
		switch (category) {
			case HUD:
				rows = (hudItems + hudColumns - 1) / hudColumns;
				break;
			case ZOOM:
				rows = 3;
				break;
			case PVP:
				rows = 2;
				break;
			case COSMETICS:
				rows = 3;
				break;
			case PRESETS:
				rows = Presets.COUNT;
				break;
			default:
				rows = 1;
				break;
		}
		boolean tight = category == Category.HUD && rows * (ROW + GAP) - GAP > space;
		int hudRow = tight ? 18 : ROW;
		int hudGap = tight ? 2 : GAP;
		final int hudStep = hudRow + hudGap;
		int hudVisible = Math.max(1, (space - 8 + hudGap) / hudStep);
		this.hudMaxScroll = category == Category.HUD ? Math.max(0, rows - hudVisible) : 0;
		hudScroll = Math.max(0, Math.min(hudScroll, this.hudMaxScroll));
		int optionsH = category == Category.HUD ? Math.min(rows, hudVisible) * hudStep : rows * (ROW + GAP);
		this.previewX = x;
		this.previewY = fy + HEADER + 26;
		this.previewW = inner;
		int left = space - optionsH - 8;
		this.previewH = left < 30 ? 0 : Math.min(left, 130);
		int y = this.previewH == 0 ? this.previewY : this.previewY + this.previewH + 8;
		int half = (inner - GAP) / 2;

		switch (category) {
			case GENERAL:
				this.option(x, y, half, ROW, "customMenu", new Value() {
					public boolean get() {
						return config.customMenu;
					}

					public void set(final boolean value) {
						config.customMenu = value;
					}
				});
				break;
			case HUD: {
				int column = (inner - (hudColumns - 1) * GAP) / hudColumns;
				int first = this.buttonList.size();
				List<MistyToggle> grid = new ArrayList<MistyToggle>();
				grid.add(this.toggle("hud", new Value() {
					public boolean get() {
						return config.hud;
					}

					public void set(final boolean value) {
						config.hud = value;
					}
				}));
				grid.add(this.toggle("hitboxes", new Value() {
					public boolean get() {
						return config.hitboxes;
					}

					public void set(final boolean value) {
						config.hitboxes = value;
					}
				}).gear(this.sliders("misty.option.hitboxes", this.palette("misty.option.hitboxColor", new Palette.Value() {
					public int get() {
						return config.hitboxColor;
					}

					public void set(final int value) {
						config.hitboxColor = value;
					}
				}))));
				grid.add(this.toggle("motionBlur", new Value() {
					public boolean get() {
						return config.motionBlur;
					}

					public void set(final boolean value) {
						config.motionBlur = value;
						Visuals.retryBlur();
					}
				}).gear(this.sliders("misty.option.motionBlur", new HudModule.Slider() {
					public MistySlider create(final int sx, final int sy, final int sw, final int sh) {
						return new MistySlider(sx, sy, sw, sh, new MistySlider.Model() {
							public double get() {
								return (Visuals.blurStrength() - Visuals.BLUR_MIN) / (double)(Visuals.BLUR_MAX - Visuals.BLUR_MIN);
							}

							public void set(final double value) {
								config.motionBlurStrength = Visuals.BLUR_MIN + (int)Math.round(value * (Visuals.BLUR_MAX - Visuals.BLUR_MIN));
							}

							public String label() {
								return I18n.format("misty.option.motionBlurStrength", Visuals.blurStrength() * 10 + " %");
							}
						});
					}
				})));
				grid.add(this.toggle("particles", new Value() {
					public boolean get() {
						return config.particles;
					}

					public void set(final boolean value) {
						config.particles = value;
					}
				}).gear(this.sliders("misty.option.particles", new HudModule.Slider() {
					public MistySlider create(final int sx, final int sy, final int sw, final int sh) {
						return new MistySlider(sx, sy, sw, sh, new MistySlider.Model() {
							private final int steps = (Combat.PARTICLES_MAX - Combat.PARTICLES_MIN) / Combat.PARTICLES_STEP;

							public double get() {
								return (Combat.particlePercent() - Combat.PARTICLES_MIN) / (double)(Combat.PARTICLES_MAX - Combat.PARTICLES_MIN);
							}

							public void set(final double value) {
								config.particleMultiplier = Combat.PARTICLES_MIN + (int)Math.round(value * this.steps) * Combat.PARTICLES_STEP;
							}

							public String label() {
								return I18n.format("misty.option.particleMultiplier", Combat.particlePercent() + " %");
							}
						});
					}
				})));
				grid.add(this.toggle("glintColor", new Value() {
					public boolean get() {
						return config.glint;
					}

					public void set(final boolean value) {
						config.glint = value;
					}
				}).gear(this.sliders("misty.option.glintColor", this.glintSliders())));
				grid.add(this.toggle("hideScoreboard", new Value() {
					public boolean get() {
						return config.hideScoreboard;
					}

					public void set(final boolean value) {
						config.hideScoreboard = value;
					}
				}));
				grid.add(this.toggle("chunkViewer", new Value() {
					public boolean get() {
						return config.chunkViewer;
					}

					public void set(final boolean value) {
						config.chunkViewer = value;
					}
				}).gear(this.sliders("misty.option.chunkViewer", this.palette("misty.option.chunkBorderColor", new Palette.Value() {
					public int get() {
						return config.chunkBorderColor;
					}

					public void set(final int value) {
						config.chunkBorderColor = value;
					}
				}), this.palette("misty.option.chunkGridColor", new Palette.Value() {
					public int get() {
						return config.chunkGridColor;
					}

					public void set(final int value) {
						config.chunkGridColor = value;
					}
				}))));
				// one or the other: turning one on turns the other off
				grid.add(this.toggle("itemPhysics", new Value() {
					public boolean get() {
						return config.itemPhysics;
					}

					public void set(final boolean value) {
						config.itemPhysics = value;
						if (value) {
							config.items2D = false;
						}
					}
				}));
				grid.add(this.toggle("items2D", new Value() {
					public boolean get() {
						return config.items2D;
					}

					public void set(final boolean value) {
						config.items2D = value;
						if (value) {
							config.itemPhysics = false;
						}
					}
				}));
				for (final HudModule module : hudModules) {
					MistyToggle toggle = new MistyToggle(0, 0, 0, 0, module.name(), new Value() {
						public boolean get() {
							return module.state().enabled;
						}

						public void set(final boolean value) {
							module.state().enabled = value;
						}
					});
					// a module with its own settings: the gear before the switch opens them
					if (module.hasGear()) {
						toggle.gear(new Runnable() {
							@Override
							public void run() {
								SettingsScreen.this.mc.displayGuiScreen(ModuleSettingsScreen.of(module, SettingsScreen.this));
							}
						});
					}
					grid.add(toggle);
				}
				for (int i = 0; i < grid.size(); i++) {
					MistyToggle toggle = grid.get(i);
					toggle.xPosition = x + i % hudColumns * (column + GAP);
					toggle.yPosition = y + (i / hudColumns - hudScroll) * hudStep;
					toggle.width = column;
					toggle.height = hudRow;
					// the rows scrolled away are hidden (and cannot be clicked)
					toggle.visible = toggle.yPosition >= y && toggle.yPosition < y + hudVisible * hudStep;
					this.add(toggle);
				}
				this.hudGridY = y;
				this.hudGridH = Math.min(rows, hudVisible) * hudStep - hudGap;
				break;
			}
			case ZOOM:
				this.option(x, y, half, ROW, "zoom", new Value() {
					public boolean get() {
						return config.zoom;
					}

					public void set(final boolean value) {
						config.zoom = value;
					}
				});
				this.option(x + half + GAP, y, half, ROW, "zoomReset", new Value() {
					public boolean get() {
						return config.zoomReset;
					}

					public void set(final boolean value) {
						config.zoomReset = value;
					}
				});
				this.option(x, y + ROW + GAP, half, ROW, "zoomSmooth", new Value() {
					public boolean get() {
						return config.zoomSmooth;
					}

					public void set(final boolean value) {
						config.zoomSmooth = value;
					}
				});
				this.option(x + half + GAP, y + ROW + GAP, half, ROW, "zoomScroll", new Value() {
					public boolean get() {
						return config.zoomScroll;
					}

					public void set(final boolean value) {
						config.zoomScroll = value;
					}
				});
				this.add(new MistySlider(x, y + 2 * (ROW + GAP), inner, ROW, new MistySlider.Model() {
					public double get() {
						return (Zoom.level() - Zoom.MIN) / (Zoom.MAX - Zoom.MIN);
					}

					public void set(final double value) {
						config.zoomLevel = Math.round((Zoom.MIN + (float)value * (Zoom.MAX - Zoom.MIN)) * 10.0F) / 10.0F;
					}

					public String label() {
						return I18n.format("misty.option.zoomLevel", String.format("×%.1f", Zoom.level()));
					}
				}));
				break;
			case FULLBRIGHT:
				this.option(x, y, inner, ROW, "fullbright", new Value() {
					public boolean get() {
						return config.fullbright;
					}

					public void set(final boolean value) {
						config.fullbright = value;
					}
				});
				break;
			case PVP:
				this.option(x, y, half, ROW, "lowFire", new Value() {
					public boolean get() {
						return config.lowFire;
					}

					public void set(final boolean value) {
						config.lowFire = value;
					}
				});
				this.option(x + half + GAP, y, inner - half - GAP, ROW, "shortSword", new Value() {
					public boolean get() {
						return config.shortSword;
					}

					public void set(final boolean value) {
						config.shortSword = value;
					}
				});
				this.option(x, y + ROW + GAP, half, ROW, "killSound", new Value() {
					public boolean get() {
						return config.killSound;
					}

					public void set(final boolean value) {
						config.killSound = value;
						if (value) {
							Combat.ting();
						}
					}
				});
				this.option(x + half + GAP, y + ROW + GAP, inner - half - GAP, ROW, "tntCountdown", new Value() {
					public boolean get() {
						return config.tntCountdown;
					}

					public void set(final boolean value) {
						config.tntCountdown = value;
					}
				});
				break;
			case SERVER: {
				// one line per server: its options open on a click
				final HudModule kills = this.module(KillsModule.class);
				this.add(new MistyButton(x, y, inner, ROW, "CubeCraft", new Runnable() {
					@Override
					public void run() {
						SettingsScreen.this.mc.displayGuiScreen(new ModuleSettingsScreen("CubeCraft", Arrays.asList(
							new ModuleSettingsScreen.Toggle(I18n.format("misty.option.autoGG"), I18n.format("misty.option.autoGG.tip"), new Value() {
								public boolean get() {
									return config.autoGG;
								}

								public void set(final boolean value) {
									config.autoGG = value;
								}
							}),
							new ModuleSettingsScreen.Toggle(I18n.format("misty.option.luckyKills"), I18n.format("misty.option.luckyKills.tip"), new Value() {
								public boolean get() {
									return kills.state().enabled;
								}

								public void set(final boolean value) {
									kills.state().enabled = value;
								}
							})), java.util.Collections.<HudModule.Slider>emptyList(), SettingsScreen.this));
					}
				}));
				break;
			}
			case COSMETICS:
				this.option(x, y, half, ROW, "cosmetics", new Value() {
					public boolean get() {
						return config.cape;
					}

					public void set(final boolean value) {
						config.cape = value;
					}
				});
				this.option(x + half + GAP, y, inner - half - GAP, ROW, "network", new Value() {
					public boolean get() {
						return config.network;
					}

					public void set(final boolean value) {
						config.network = value;
					}
				});
				this.option(x, y + ROW + GAP, half, ROW, "nametagBadge", new Value() {
					public boolean get() {
						return config.nametagBadge;
					}

					public void set(final boolean value) {
						config.nametagBadge = value;
					}
				});
				this.option(x + half + GAP, y + ROW + GAP, inner - half - GAP, ROW, "ownNametag", new Value() {
					public boolean get() {
						return config.ownNametag;
					}

					public void set(final boolean value) {
						config.ownNametag = value;
					}
				});
				this.add(new MistyButton(x, y + 2 * (ROW + GAP), inner, ROW, I18n.format("misty.wardrobe.open"), new Runnable() {
					@Override
					public void run() {
						SettingsScreen.this.mc.displayGuiScreen(new WardrobeScreen(SettingsScreen.this));
					}
				}));
				break;
			case PRESETS: {
				// five slots, each with its name and date, "Load" and "Save"
				this.presetsX = x;
				this.presetsY = y;
				this.presetsW = inner;
				int buttonW = Math.min(80, (inner - 2 * GAP) / 4);
				this.presetsButtonW = buttonW;
				for (int slot = 1; slot <= Presets.COUNT; slot++) {
					final int n = slot;
					int rowY = y + (slot - 1) * (ROW + GAP);
					MistyButton load = new MistyButton(x + inner - 2 * buttonW - GAP - 2, rowY + 2, buttonW, ROW - 4, I18n.format("misty.preset.load"), new Runnable() {
						@Override
						public void run() {
							if (Presets.load(n)) {
								SettingsScreen.this.rebuild();
							}
						}
					});
					load.enabled = Presets.savedAt(n) != null;
					this.add(load);
					this.add(new MistyButton(x + inner - buttonW - 2, rowY + 2, buttonW, ROW - 4, I18n.format("misty.preset.save"), new Runnable() {
						@Override
						public void run() {
							Presets.save(n);
							SettingsScreen.this.rebuild();
						}
					}).primary());
				}
				break;
			}
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

	private HudModule module(final Class<? extends HudModule> type) {
		for (HudModule module : Hud.modules()) {
			if (type.isInstance(module)) {
				return module;
			}
		}
		throw new IllegalStateException(type.getName());
	}

	private MistyToggle toggle(final String key, final MistyToggle.Value value) {
		MistyToggle toggle = new MistyToggle(0, 0, 0, 0, I18n.format("misty.option." + key), value);
		toggle.tooltip(I18n.format("misty.option." + key + ".tip"));
		return toggle;
	}

	private void option(final int x, final int y, final int width, final int height, final String key, final MistyToggle.Value value) {
		this.add(new MistyToggle(x, y, width, height, I18n.format("misty.option." + key), value).tooltip(I18n.format("misty.option." + key + ".tip")));
	}

	/** A palette slider, for a gear. */
	private HudModule.Slider palette(final String key, final Palette.Value value) {
		return new HudModule.Slider() {
			@Override
			public MistySlider create(final int x, final int y, final int width, final int height) {
				return Palette.slider(x, y, width, height, key, value);
			}
		};
	}

	/** Opens a gear of sliders ({@code title}: the setting's name key). */
	private Runnable sliders(final String title, final HudModule.Slider... sliders) {
		return new Runnable() {
			@Override
			public void run() {
				SettingsScreen.this.mc.displayGuiScreen(ModuleSettingsScreen.sliders(I18n.format(title), Arrays.asList(sliders), SettingsScreen.this));
			}
		};
	}

	/** The glint's colour: a palette colour, or after the palette a custom one (red, green, blue). */
	private HudModule.Slider[] glintSliders() {
		final Config config = Config.get();
		HudModule.Slider color = new HudModule.Slider() {
			@Override
			public MistySlider create(final int x, final int y, final int width, final int height) {
				return new MistySlider(x, y, width, height, new MistySlider.Model() {
					public double get() {
						return Math.max(0, Math.min(Palette.COLORS.length, config.glintColor)) / (double)Palette.COLORS.length;
					}

					public void set(final double value) {
						config.glintColor = (int)Math.round(value * Palette.COLORS.length);
					}

					public String label() {
						if (config.glintColor >= Palette.COLORS.length) {
							return I18n.format("misty.option.color", I18n.format("misty.color.custom"));
						}
						return Palette.label("misty.option.color", config.glintColor);
					}
				});
			}
		};
		return new HudModule.Slider[] {color, channel("misty.option.red", 16), channel("misty.option.green", 8), channel("misty.option.blue", 0)};
	}

	/** One channel of the custom glint colour (0 to 255); moving it picks the custom colour. */
	private static HudModule.Slider channel(final String key, final int shift) {
		final Config config = Config.get();
		return new HudModule.Slider() {
			@Override
			public MistySlider create(final int x, final int y, final int width, final int height) {
				return new MistySlider(x, y, width, height, new MistySlider.Model() {
					public double get() {
						return (config.glintCustom >> shift & 255) / 255.0;
					}

					public void set(final double value) {
						int channel = (int)Math.round(value * 255);
						config.glintCustom = config.glintCustom & ~(255 << shift) | channel << shift;
						config.glintColor = Palette.COLORS.length;
					}

					public String label() {
						return I18n.format(key, config.glintCustom >> shift & 255);
					}
				});
			}
		};
	}

	@Override
	protected boolean scrolled(final int mouseX, final int mouseY, final int direction) {
		// the HUD grid scrolls by a row
		if (category == Category.HUD && this.hudMaxScroll > 0) {
			int scrolled = Math.max(0, Math.min(this.hudMaxScroll, hudScroll - direction));
			if (scrolled != hudScroll) {
				hudScroll = scrolled;
				this.rebuild();
			}
			return true;
		}
		return false;
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
		if (category == Category.PRESETS) {
			for (int slot = 1; slot <= Presets.COUNT; slot++) {
				Ui.card(this.presetsX, this.presetsY + (slot - 1) * (ROW + GAP), this.presetsW, ROW, Ui.GLASS, Ui.BORDER);
			}
		}
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

		// the HUD grid's scroll bar, when not all its rows fit
		if (category == Category.HUD && this.hudMaxScroll > 0) {
			int barX = this.rightX() + this.rightW() - PAD / 2 - 1;
			int barH = Math.max(10, this.hudGridH / (this.hudMaxScroll + 1));
			int barY = this.hudGridY + (this.hudGridH - barH) * hudScroll / this.hudMaxScroll;
			Ui.fill(barX, this.hudGridY, barX + 2, this.hudGridY + this.hudGridH, Ui.alpha(Ui.ACCENT, 0.2F));
			Ui.fill(barX, barY, barX + 2, barY + barH, Ui.ACCENT);
		}

		// the presets' names and dates, before their buttons
		if (category == Category.PRESETS) {
			for (int slot = 1; slot <= Presets.COUNT; slot++) {
				int rowY = this.presetsY + (slot - 1) * (ROW + GAP) + (ROW - 8) / 2;
				String at = Presets.savedAt(slot);
				String own = Presets.name(slot);
				String name = own != null ? own : I18n.format("misty.preset.name", slot);
				Ui.text(name, this.presetsX + 8, rowY, at == null ? Ui.MUTED : Ui.TEXT);
				String state = at == null ? I18n.format("misty.preset.empty") : I18n.format("misty.preset.saved", at);
				int stateX = this.presetsX + 8 + Ui.width(name) + 8;
				int room = this.presetsX + this.presetsW - 2 * this.presetsButtonW - GAP - 8 - stateX;
				if (Ui.width(state) > room && at != null) {
					state = at;
				}
				if (Ui.width(state) <= room) {
					Ui.text(state, stateX, rowY, Ui.DIM);
				}
			}
		}
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
			case PVP:
				this.previewPvp(x, y, w, h, seconds);
				break;
			case COSMETICS:
				this.previewCosmetics(x, y, w, h, seconds);
				break;
			default:
				MistyBackground.drawIn(x, y, w, h, 1.0F);
				break;
		}
		Ui.border(x - 1, y - 1, w + 2, h + 2, 0x4D9CC0FF);
	}

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
		if (Keys.FULLBRIGHT.getKeyCode() != 0) {
			Ui.keyCap(x + 6, y + h - 18, GameSettings.getKeyDisplayString(Keys.FULLBRIGHT.getKeyCode()), (int)seconds % 2 == 0);
		}
		Ui.endScissor();
	}

	/** PvP: the first-person view while burning, a sword in hand, as the switches leave them. */
	private void previewPvp(final int x, final int y, final int w, final int h, final float seconds) {
		Config config = Config.get();
		MistyBackground.drawIn(x, y, w, h, 1.3F);
		Ui.scissor(x, y, w, h);
		int unit = Math.max(2, h / 24);
		int flames = Math.round(h * (config.lowFire ? 0.12F : 0.34F));
		for (int fx = x; fx < x + w; fx += unit * 3) {
			float flicker = 0.75F + 0.25F * (float)Math.sin(seconds * 9.0F + fx * 0.7F);
			int top = y + h - Math.round(flames * flicker);
			Ui.fill(fx, top, Math.min(x + w, fx + unit * 3), y + h, 0xD0FF7A1A);
			Ui.fill(fx + unit, top + unit * 2, Math.min(x + w, fx + unit * 2), y + h, 0xE0FFD040);
		}
		int bob = Math.round((float)Math.sin(seconds * 2.0F) * unit);
		int size = config.shortSword ? 3 : 4;
		int sx = x + w - 10 * unit;
		int sy = y + h - 4 * unit + bob;
		for (int i = 0; i < 4 * size; i++) {
			Ui.fill(sx - i * unit / 2, sy - i * unit, sx - i * unit / 2 + unit + 1, sy - i * unit + unit, i < size ? 0xFF6B4A2B : 0xFFBFE8F0);
		}
		Ui.fill(sx - unit * 2, sy - size * unit, sx + unit * 2, sy - size * unit + unit, 0xFF8A8F99);
		Ui.endScissor();
	}

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

	/** Unused buttons are left to the parent class. */
	@Override
	protected void actionPerformed(final GuiButton button) {
		super.actionPerformed(button);
	}
}
