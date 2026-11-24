package online.mistylauncher.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import online.mistylauncher.legacy.features.Clicks;
import online.mistylauncher.legacy.features.Combat;
import online.mistylauncher.legacy.features.Fullbright;
import online.mistylauncher.legacy.features.Zoom;
import online.mistylauncher.legacy.hud.Hud;
import online.mistylauncher.legacy.hud.HudEditor;
import online.mistylauncher.legacy.menu.MistyTitle;
import online.mistylauncher.legacy.net.Badge;
import online.mistylauncher.legacy.net.MistyNetwork;

/** What the mod listens to in the game. */
public final class Events {
	/** The HUD, over the game's own (not with F3 nor while the editor is open). */
	@SubscribeEvent
	public void overlay(final RenderGameOverlayEvent.Post event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (event.type != RenderGameOverlayEvent.ElementType.ALL || minecraft.gameSettings.showDebugInfo
			|| minecraft.currentScreen instanceof HudEditor) {
			return;
		}
		Hud.drawAll(event.resolution.getScaledWidth(), event.resolution.getScaledHeight(), false);
	}

	/** The player list: the Mistly players' names with the cloud while it is drawn. */
	@SubscribeEvent
	public void playerListBefore(final RenderGameOverlayEvent.Pre event) {
		if (event.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
			Badge.beforePlayerList();
		}
	}

	@SubscribeEvent
	public void playerListAfter(final RenderGameOverlayEvent.Post event) {
		if (event.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
			Badge.afterPlayerList();
		}
	}

	/** The name above a Mistly player's head, with the cloud. */
	@SubscribeEvent
	public void nameTag(final RenderLivingEvent.Specials.Pre<?> event) {
		if (event.entity instanceof AbstractClientPlayer
			&& Badge.nameTag((AbstractClientPlayer)event.entity, event.renderer.getRenderManager(), event.x, event.y, event.z)) {
			event.setCanceled(true);
		}
	}

	/** A chat line: the cloud before it when a Mistly player speaks (not the line over the hotbar). */
	@SubscribeEvent
	public void chat(final ClientChatReceivedEvent event) {
		if (event.type != 2 && event.message != null) {
			event.message = Badge.chat(event.message);
		}
	}

	/** The Mistly main menu in place of the game's (unless turned off in the settings). */
	@SubscribeEvent
	public void open(final GuiOpenEvent event) {
		if (event.gui instanceof GuiMainMenu && Config.get().customMenu) {
			event.gui = new MistyTitle();
		}
	}

	@SubscribeEvent
	public void tick(final TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		Minecraft minecraft = Minecraft.getMinecraft();
		Zoom.tick(minecraft);
		Fullbright.tick(minecraft);
		Combat.tick(minecraft);
		MistyNetwork.tick(minecraft);
		if (minecraft.currentScreen == null && Keys.EDITOR.isPressed()) {
			minecraft.displayGuiScreen(new HudEditor());
		}
	}

	/** Each click, for the CPS. */
	@SubscribeEvent
	public void mouse(final MouseEvent event) {
		if (event.buttonstate && Minecraft.getMinecraft().currentScreen == null) {
			Clicks.pressed(event.button);
		}
	}

	/** The local player hits another player: reach and combo. */
	@SubscribeEvent
	public void attack(final AttackEntityEvent event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (event.entityPlayer == minecraft.thePlayer && event.target instanceof EntityPlayer) {
			Combat.attacked(minecraft, event.target);
		}
	}
}
