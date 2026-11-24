package online.mistylauncher.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.MouseEvent;
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

	@SubscribeEvent
	public void tick(final TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		Minecraft minecraft = Minecraft.getMinecraft();
		Zoom.tick(minecraft);
		Fullbright.tick(minecraft);
		Combat.tick(minecraft);
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
