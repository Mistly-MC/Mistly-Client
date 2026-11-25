package online.mistylauncher.client.mixin;

//? if >=1.20.5 {
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import online.mistylauncher.client.hud.HudManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the Misty HUD with the game's own, under the chat and the player list (hidden with F1 too).
 * The in-game HUD is {@code Hud} since 26.2, {@code Gui} before.
 */
//? if >=26.2 {
@Mixin(net.minecraft.client.gui.Hud.class)
//?} else {
/*@Mixin(net.minecraft.client.gui.Gui.class)
*///?}
abstract class HudMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	// before 1.21.6 the HUD is drawn in layers, the sidebar's from a lambda: the Misty HUD is drawn
	// as the sidebar's layer starts (this hook is declared before the one that may leave it out)
	//? if >=26.2 {
	@Inject(method = "extractRenderState", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/Hud;extractScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	//?} else if >=1.21.6 {
	/*@Inject(method = "extractRenderState", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/Gui;extractScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	*///?} else {
	/*@Inject(method = "extractScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"))
	*///?}
	private void misty$hud(final GuiGraphicsExtractor graphics, final DeltaTracker deltaTracker, final CallbackInfo ci) {
		HudManager.render(graphics, this.minecraft);
	}

	/** The leaderboard on the right (the sidebar) hidden from the HUD category; the mod still reads it. */
	@Inject(method = "extractScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"), cancellable = true)
	private void misty$scoreboard(final GuiGraphicsExtractor graphics, final DeltaTracker deltaTracker, final CallbackInfo ci) {
		if (online.mistylauncher.client.MistyClient.config().hideScoreboard || HudManager.replacesScoreboard()) {
			ci.cancel();
		}
	}

	/** The Saturation module: the saturation over the drumsticks, once the game drew the hunger bar. */
	@Inject(method = "extractFood", at = @At("TAIL"))
	private void misty$saturation(final GuiGraphicsExtractor graphics, final net.minecraft.world.entity.player.Player player, final int yLine, final int xRight,
		final CallbackInfo ci) {
		online.mistylauncher.client.hud.modules.SaturationModule.overlay(graphics, player, yLine, xRight);
	}

	/** The Effects module replaces the game's effect icons at the top right. */
	@Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
	private void misty$effects(final GuiGraphicsExtractor graphics, final DeltaTracker deltaTracker, final CallbackInfo ci) {
		if (HudManager.hidesGameEffects()) {
			ci.cancel();
		}
	}
}
//?} else {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import online.mistylauncher.client.hud.HudManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The HUD before 1.20.5, all in one method: the Misty HUD just before the chat (so under it and the
// player list), the sidebar, the hunger bar (in the player's health) and the effects
@Mixin(Gui.class)
abstract class HudMixin {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	private int screenWidth;
	@Shadow
	private int screenHeight;

	@Shadow
	protected abstract Player getCameraPlayer();

	@Inject(method = "render", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;III)V"))
	private void misty$hud(final GuiGraphicsExtractor graphics, final float partialTick, final CallbackInfo ci) {
		HudManager.render(graphics, this.minecraft);
	}

	// the leaderboard on the right hidden from the HUD category; the mod still reads it
	@Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true)
	private void misty$scoreboard(final GuiGraphicsExtractor graphics, final Objective objective, final CallbackInfo ci) {
		if (online.mistylauncher.client.MistyClient.config().hideScoreboard || HudManager.replacesScoreboard()) {
			ci.cancel();
		}
	}

	// the Saturation module, once the game drew the player's health and hunger (no hunger bar on a mount)
	@Inject(method = "renderPlayerHealth", at = @At("TAIL"))
	private void misty$saturation(final GuiGraphicsExtractor graphics, final CallbackInfo ci) {
		Player player = this.getCameraPlayer();
		if (player != null && !(player.getVehicle() instanceof LivingEntity)) {
			online.mistylauncher.client.hud.modules.SaturationModule.overlay(graphics, player, this.screenHeight - 39, this.screenWidth / 2 + 91);
		}
	}

	// the Effects module replaces the game's effect icons at the top right
	@Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
	private void misty$effects(final GuiGraphicsExtractor graphics, final CallbackInfo ci) {
		if (HudManager.hidesGameEffects()) {
			ci.cancel();
		}
	}
}
*///?}
