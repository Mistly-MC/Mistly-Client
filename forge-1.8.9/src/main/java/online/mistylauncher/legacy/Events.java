package online.mistylauncher.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import online.mistylauncher.legacy.features.Clicks;
import online.mistylauncher.legacy.features.Combat;
import online.mistylauncher.legacy.features.CubeCraft;
import online.mistylauncher.legacy.features.Fullbright;
import online.mistylauncher.legacy.features.Media;
import online.mistylauncher.legacy.features.Sidebar;
import online.mistylauncher.legacy.features.Visuals;
import online.mistylauncher.legacy.features.WorldLines;
import online.mistylauncher.legacy.features.Zoom;
import online.mistylauncher.legacy.hud.Hud;
import online.mistylauncher.legacy.hud.HudEditor;
import online.mistylauncher.legacy.hud.modules.SaturationModule;
import online.mistylauncher.legacy.menu.MistyTitle;
import online.mistylauncher.legacy.net.Badge;
import online.mistylauncher.legacy.net.MistyNetwork;
import online.mistylauncher.legacy.ui.MistyButton;
import online.mistylauncher.legacy.ui.SettingsScreen;

/** What the mod listens to in the game. */
public final class Events {
	// ── the HUD ──

	/** Before the game's HUD: its sidebar taken away when hidden or shown by the Leaderboard module. */
	@SubscribeEvent
	public void overlayBefore(final RenderGameOverlayEvent.Pre event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
			Sidebar.takeAway(minecraft, Config.get().hideScoreboard || Hud.replacesScoreboard());
		} else if (event.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
			Badge.beforePlayerList();
		}
	}

	/** After: the sidebar given back, the Mistly HUD (not with F3 nor while the editor is open). */
	@SubscribeEvent
	public void overlayAfter(final RenderGameOverlayEvent.Post event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (event.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
			Badge.afterPlayerList();
			return;
		}
		if (event.type == RenderGameOverlayEvent.ElementType.FOOD && minecraft.thePlayer != null) {
			// the hunger bar was just drawn: its top is where the next row starts, 10 lower
			int width = event.resolution.getScaledWidth();
			int height = event.resolution.getScaledHeight();
			SaturationModule.overlay(minecraft.thePlayer, height - GuiIngameForge.right_height + 10, width / 2 + 91);
			return;
		}
		if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
			return;
		}
		if (!minecraft.gameSettings.showDebugInfo && !(minecraft.currentScreen instanceof HudEditor)) {
			Hud.drawAll(event.resolution.getScaledWidth(), event.resolution.getScaledHeight(), false);
		}
		Sidebar.giveBack(minecraft);
	}

	// ── names and chat ──

	/** The name above a Mistly player's head, with the cloud; your own in third person. */
	@SubscribeEvent
	public void nameTag(final RenderLivingEvent.Specials.Pre<?> event) {
		if (!(event.entity instanceof AbstractClientPlayer)) {
			return;
		}
		AbstractClientPlayer player = (AbstractClientPlayer)event.entity;
		if (Badge.ownNameTag(player, event.renderer.getRenderManager(), event.x, event.y, event.z)
			|| Badge.nameTag(player, event.renderer.getRenderManager(), event.x, event.y, event.z)) {
			event.setCanceled(true);
		}
	}

	/** A chat line (not the line over the hotbar): kills and game ends read in it, the cloud before it. */
	@SubscribeEvent
	public void chat(final ClientChatReceivedEvent event) {
		if (event.type == 2 || event.message == null) {
			return;
		}
		String text = event.message.getUnformattedText();
		Combat.chat(text);
		CubeCraft.chat(event.message.getFormattedText());
		event.message = Badge.chat(event.message);
	}

	// ── screens ──

	/** The Mistly main menu in place of the game's (unless turned off in the settings). */
	@SubscribeEvent
	public void open(final GuiOpenEvent event) {
		if (event.gui instanceof GuiMainMenu && Config.get().customMenu) {
			event.gui = new MistyTitle();
		}
	}

	/** A way into the Mistly settings from the pause menu, in the top-right corner. */
	@SubscribeEvent
	public void pauseMenu(final GuiScreenEvent.InitGuiEvent.Post event) {
		if (event.gui instanceof GuiIngameMenu) {
			final GuiIngameMenu menu = (GuiIngameMenu)event.gui;
			event.buttonList.add(new MistyButton(menu.width - 30, 8, 22, 22, net.minecraft.client.resources.I18n.format("misty.settings.title"), new Runnable() {
				@Override
				public void run() {
					Minecraft.getMinecraft().displayGuiScreen(new SettingsScreen(menu));
				}
			}).cloud());
		}
	}

	/** The Mistly buttons put on the game's screens run their own action. */
	@SubscribeEvent
	public void button(final GuiScreenEvent.ActionPerformedEvent.Pre event) {
		if (event.button instanceof MistyButton) {
			((MistyButton)event.button).press();
			event.setCanceled(true);
		}
	}

	// ── the game ──

	@SubscribeEvent
	public void tick(final TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}
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
		if (minecraft.currentScreen == null && Keys.FULLBRIGHT.isPressed() && minecraft.thePlayer != null) {
			Config config = Config.get();
			config.fullbright = !config.fullbright;
			Config.save();
			minecraft.ingameGUI.setRecordPlaying(new ChatComponentTranslation(config.fullbright ? "misty.fullbright.on" : "misty.fullbright.off").getFormattedText(), false);
		}
	}

	/** Each click, for the CPS; the wheel while zooming changes the zoom. */
	@SubscribeEvent
	public void mouse(final MouseEvent event) {
		if (Minecraft.getMinecraft().currentScreen != null) {
			return;
		}
		if (event.buttonstate) {
			Clicks.pressed(event.button);
		}
		if (event.dwheel != 0 && Zoom.scroll(event.dwheel)) {
			event.setCanceled(true);
		}
	}

	/** The local player hits something: reach, combo, kill sound, hit particles. */
	@SubscribeEvent
	public void attack(final AttackEntityEvent event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (event.entityPlayer == minecraft.thePlayer && event.target != null) {
			Combat.attacked(minecraft, event.target);
		}
	}

	/** Zoom: the field of view divided by the magnification. */
	@SubscribeEvent
	public void fov(final EntityViewRenderEvent.FOVModifier event) {
		event.setFOV(Zoom.fov(event.getFOV()));
	}

	/** Hitboxes and chunk borders, once the world is drawn. */
	@SubscribeEvent
	public void world(final RenderWorldLastEvent event) {
		WorldLines.draw(event.partialTicks);
	}

	/** Low fire: the flames over the screen drawn lower. */
	@SubscribeEvent
	public void overlay(final RenderBlockOverlayEvent event) {
		if (event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE && Visuals.fire(Minecraft.getMinecraft())) {
			event.setCanceled(true);
		}
	}

	/** Short swords: the swords' models get their hand size from the setting. */
	@SubscribeEvent
	public void models(final ModelBakeEvent event) {
		Visuals.models(event);
	}
}
