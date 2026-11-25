package online.mistylauncher.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import online.mistylauncher.legacy.features.Clicks;
import online.mistylauncher.legacy.features.Combat;
import online.mistylauncher.legacy.features.CubeCraft;
import online.mistylauncher.legacy.features.Fullbright;
import online.mistylauncher.legacy.features.Media;
import online.mistylauncher.legacy.features.Sidebar;
import online.mistylauncher.legacy.features.Visuals;
import online.mistylauncher.legacy.features.Zoom;
import online.mistylauncher.legacy.hud.Hud;
import online.mistylauncher.legacy.hud.HudEditor;
import online.mistylauncher.legacy.hud.modules.SaturationModule;
import online.mistylauncher.legacy.net.Badge;
import online.mistylauncher.legacy.net.MistyNetwork;
import online.mistylauncher.legacy.ui.MistyButton;
import online.mistylauncher.legacy.ui.SettingsScreen;
import online.mistylauncher.legacy.ui.Ui;

/**
 * What the mod does at the game's moments: called by the mixins (the {@code mixin} package), each
 * one said where it hooks in.
 */
public final class Events {
	private static final ResourceLocation HANDLE = new ResourceLocation("misty", "textures/gui/slider_handle.png");
	private static final ResourceLocation HANDLE_HOT = new ResourceLocation("misty", "textures/gui/slider_handle_highlighted.png");

	private Events() {
	}

	// ── the HUD ──

	/** Before the game's HUD: its sidebar taken away when hidden or shown by the Leaderboard module. */
	public static void hudBefore() {
		Sidebar.takeAway(Minecraft.getMinecraft(), Config.get().hideScoreboard || Hud.replacesScoreboard());
	}

	/** After: the Mistly HUD (not with F3 nor while the editor is open), the sidebar given back. */
	public static void hudAfter() {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (!minecraft.gameSettings.showDebugInfo && !(minecraft.currentScreen instanceof HudEditor)) {
			ScaledResolution resolution = new ScaledResolution(minecraft);
			Hud.drawAll(resolution.getScaledWidth(), resolution.getScaledHeight(), false);
		}
		Sidebar.giveBack(minecraft);
	}

	/** The hunger bar was just drawn: its top is where the next row starts, 10 lower. */
	public static void food(final int width, final int height) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft.player != null) {
			SaturationModule.overlay(minecraft.player, height - GuiIngameForge.right_height + 10, width / 2 + 91);
		}
	}

	// ── names and chat ──

	/** The name above a Mistly player's head, with the cloud; your own in third person. True: drawn. */
	public static boolean nameTag(final Entity entity, final RenderManager manager, final double x, final double y, final double z) {
		if (!(entity instanceof AbstractClientPlayer)) {
			return false;
		}
		AbstractClientPlayer player = (AbstractClientPlayer)entity;
		return Badge.ownNameTag(player, manager, x, y, z) || Badge.nameTag(player, manager, x, y, z);
	}

	/** A chat line (not the line over the hotbar): kills and game ends read in it, the cloud before it. */
	public static ITextComponent chat(final ITextComponent message) {
		if (message == null) {
			return null;
		}
		Combat.chat(message.getUnformattedText());
		CubeCraft.chat(message.getFormattedText());
		return Badge.chat(message);
	}

	// ── screens ──

	/** A way into the Mistly settings from the pause menu, in the top-right corner. */
	public static MistyButton pauseButton(final GuiIngameMenu menu) {
		return new MistyButton(menu.width - 30, 8, 22, 22, net.minecraft.client.resources.I18n.format("misty.settings.title"), new Runnable() {
			@Override
			public void run() {
				Minecraft.getMinecraft().displayGuiScreen(new SettingsScreen(menu));
			}
		}).cloud();
	}

	/**
	 * The game's menus outside a world (options, worlds, servers…), as in the Fabric versions: over
	 * the launcher's landscape instead of the dirt. True: drawn. With the Mistly menu turned off,
	 * the game's own background stays.
	 */
	public static boolean background(final GuiScreen screen) {
		if (!Config.get().customMenu || Minecraft.getMinecraft().world != null || screen instanceof online.mistylauncher.legacy.ui.MistyScreen) {
			return false;
		}
		online.mistylauncher.legacy.menu.MistyBackground.draw(screen.width, screen.height);
		return true;
	}

	/** The game's buttons write their text in the Mistly colours (white under the mouse), not yellow. */
	public static int buttonText(final GuiButton button, final boolean hovered, final int color) {
		if (button.packedFGColour != 0 || !button.enabled) {
			return color;
		}
		return hovered ? 0xFFFFFF : 0xEEF3FF;
	}

	/** The game's sliders get the Mistly handle, over the game's (their text is written after). */
	public static void sliderHandle(final GuiButton slider, final double value, final boolean hovered) {
		if (!slider.visible) {
			return;
		}
		int x = slider.x + (int)(Math.max(0.0, Math.min(1.0, value)) * (slider.width - 8));
		Ui.blit(hovered ? HANDLE_HOT : HANDLE, x, slider.y, 0.0F, 0.0F, 8, 20, 8, 20, 8, 20, 0xFFFFFFFF);
	}

	// ── the game ──

	/** Once the game's tick is done. */
	public static void tick() {
		Minecraft minecraft = Minecraft.getMinecraft();
		Zoom.tick(minecraft);
		Fullbright.tick(minecraft);
		Combat.tick(minecraft);
		CubeCraft.tick(minecraft);
		Media.tick(minecraft);
		Visuals.tick(minecraft);
		MistyNetwork.tick(minecraft);
		if (minecraft.currentScreen == null && Keys.EDITOR.isPressed()) {
			minecraft.displayGuiScreen(new HudEditor());
		}
		if (minecraft.currentScreen == null && Keys.FULLBRIGHT.isPressed() && minecraft.player != null) {
			Config config = Config.get();
			config.fullbright = !config.fullbright;
			Config.save();
			minecraft.ingameGUI.setOverlayMessage(new TextComponentTranslation(config.fullbright ? "misty.fullbright.on" : "misty.fullbright.off").getFormattedText(), false);
		}
	}

	/** A mouse button pressed in game (0 left, 1 right…), for the CPS. */
	public static void mouseButton(final int button, final boolean pressed) {
		if (pressed && button >= 0 && Minecraft.getMinecraft().currentScreen == null) {
			Clicks.pressed(button);
		}
	}

	/** The wheel in game: true when the zoom takes it (the hotbar then stays as it is). */
	public static boolean wheel(final int dwheel) {
		return Minecraft.getMinecraft().currentScreen == null && Zoom.scroll(dwheel);
	}

	/** The local player hits something: reach, combo, kill sound, hit particles. */
	public static void attack(final EntityPlayer player, final Entity target) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (player == minecraft.player && target != null) {
			Combat.attacked(minecraft, target);
		}
	}
}
