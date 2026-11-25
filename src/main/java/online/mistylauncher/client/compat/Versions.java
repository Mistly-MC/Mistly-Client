package online.mistylauncher.client.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * What changes from one Minecraft version to the next, in one place. The rest of the mod calls
 * these instead of the game, so a new version is mostly a matter of this class.
 *
 * <p>The {@code //?} comments are Stonecutter conditions: each jar is built with the code of its
 * Minecraft version (see stonecutter.gradle.kts, which also renames what only changed name).
 */
public final class Versions {
	private Versions() {
	}

	/** Opens a screen ({@code null}: back to the game). */
	public static void setScreen(final @Nullable Screen screen) {
		//? if >=26.2 {
		Minecraft.getInstance().gui.setScreen(screen);
		//?} else {
		/*Minecraft.getInstance().setScreen(screen);
		*///?}
	}

	/** The screen open now ({@code null}: playing). */
	public static @Nullable Screen screen() {
		//? if >=26.2 {
		return Minecraft.getInstance().gui.screen();
		//?} else {
		/*return Minecraft.getInstance().screen;
		*///?}
	}

	/** Sets the overlay over the screens (the loading screen). */
	public static void setOverlay(final @Nullable Overlay overlay) {
		//? if >=26.2 {
		Minecraft.getInstance().gui.setOverlay(overlay);
		//?} else {
		/*Minecraft.getInstance().setOverlay(overlay);
		*///?}
	}

	/** The subtitles drawn above the screens. */
	public static void extractDeferredSubtitles() {
		//? if >=26.2 {
		Minecraft.getInstance().gui.hud.extractDeferredSubtitles();
		//?} else if >=26.1 {
		/*Minecraft.getInstance().gui.extractDeferredSubtitles();
		*///?} else if >=1.21.9 {
		/*Minecraft.getInstance().gui.renderDeferredSubtitles();
		*///?}
		// before 1.21.9 the subtitles are drawn with the rest of the interface
	}

	/** The interface is hidden (F1). */
	public static boolean guiHidden() {
		//? if >=26.2 {
		return Minecraft.getInstance().gui.hud.isHidden();
		//?} else {
		/*return Minecraft.getInstance().options.hideGui;
		*///?}
	}

	/** The player entity type. */
	public static EntityType<Player> playerType() {
		//? if >=26.2 {
		return net.minecraft.world.entity.EntityTypes.PLAYER;
		//?} else {
		/*return EntityType.PLAYER;
		*///?}
	}

	/** The game's Options screen, opened from a menu (not from a world). */
	public static Screen options(final Screen parent) {
		Minecraft minecraft = Minecraft.getInstance();
		//? if >=26.3 {
		return new OptionsScreen(parent, minecraft.options);
		//?} else if >=26.1 {
		/*return new OptionsScreen(parent, minecraft.options, false);
		*///?} else {
		/*return new OptionsScreen(parent, minecraft.options);
		*///?}
	}

	/** A player's id (a record since 1.21.9, a class with getters before). */
	public static java.util.UUID id(final com.mojang.authlib.GameProfile profile) {
		//? if >=1.21.9 {
		return profile.id();
		//?} else {
		/*return profile.getId();
		*///?}
	}

	/** A player's name. */
	public static String name(final com.mojang.authlib.GameProfile profile) {
		//? if >=1.21.9 {
		return profile.name();
		//?} else {
		/*return profile.getName();
		*///?}
	}

	/** A short message above the hotbar. */
	public static void overlayMessage(final Component message) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) {
			return;
		}
		//? if >=26.1 {
		minecraft.player.sendOverlayMessage(message);
		//?} else {
		/*minecraft.player.displayClientMessage(message, true);
		*///?}
	}

	/** A texture made in code (named since 1.21.5, for the debug tools). */
	public static net.minecraft.client.renderer.texture.DynamicTexture texture(final String name, final com.mojang.blaze3d.platform.NativeImage image) {
		//? if >=1.21.5 {
		return new net.minecraft.client.renderer.texture.DynamicTexture(() -> name, image);
		//?} else {
		/*return new net.minecraft.client.renderer.texture.DynamicTexture(image);
		*///?}
	}

	/** Whether the F3 screen is open. */
	public static boolean debugScreen() {
		//? if >=1.21.9 {
		return Minecraft.getInstance().debugEntries.isOverlayVisible();
		//?} else if >=1.20.2 {
		/*return Minecraft.getInstance().getDebugOverlay().showDebugScreen();
		*///?} else {
		/*return Minecraft.getInstance().options.renderDebug;
		*///?}
	}

	/** "1.21.1", "26.3"… */
	public static String minecraftVersion() {
		//? if >=1.21.6 {
		return net.minecraft.SharedConstants.getCurrentVersion().name();
		//?} else {
		/*return net.minecraft.SharedConstants.getCurrentVersion().getName();
		*///?}
	}

	/** An image pixel, in ARGB (ABGR before 1.21.2). */
	public static void setPixel(final com.mojang.blaze3d.platform.NativeImage image, final int x, final int y, final int argb) {
		//? if >=1.21.2 {
		image.setPixel(x, y, argb);
		//?} else {
		/*image.setPixelRGBA(x, y, argb & 0xFF00FF00 | (argb & 0xFF) << 16 | (argb >> 16) & 0xFF);
		*///?}
	}

	/** An image pixel, read in ARGB (stored ABGR before 1.21.2). */
	public static int getPixel(final com.mojang.blaze3d.platform.NativeImage image, final int x, final int y) {
		//? if >=1.21.2 {
		return image.getPixel(x, y);
		//?} else {
		/*int abgr = image.getPixelRGBA(x, y);
		return abgr & 0xFF00FF00 | (abgr & 0xFF) << 16 | (abgr >> 16) & 0xFF;
		*///?}
	}

	/** A dye's colour (RGB): an int since 1.21, three floats before. */
	public static int dyeColor(final net.minecraft.world.item.DyeColor dye) {
		//? if >=1.21 {
		return dye.getTextureDiffuseColor() & 0xFFFFFF;
		//?} else {
		/*float[] rgb = dye.getTextureDiffuseColors();
		return Math.round(rgb[0] * 255) << 16 | Math.round(rgb[1] * 255) << 8 | Math.round(rgb[2] * 255);
		*///?}
	}

	//? if <1.21.2 {
	/*// A model drawn in white, full: the colour given as an int since 1.21, as four floats before
	public static void renderModel(final net.minecraft.client.model.Model model, final com.mojang.blaze3d.vertex.PoseStack pose,
		final com.mojang.blaze3d.vertex.VertexConsumer buffer, final int light, final int overlay) {
		//? if >=1.21 {
		model.renderToBuffer(pose, buffer, light, overlay);
		//?} else {
		/^model.renderToBuffer(pose, buffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
		^///?}
	}
	*///?}

	/** The food of an item (null: not food): a data component since 1.20.5, the item's before. */
	public static net.minecraft.world.food.@org.jspecify.annotations.Nullable FoodProperties food(final net.minecraft.world.item.ItemStack stack) {
		//? if >=1.20.5 {
		return stack.get(net.minecraft.core.component.DataComponents.FOOD);
		//?} else {
		/*return stack.getItem().getFoodProperties();
		*///?}
	}

	/** The food points eating it gives. */
	public static int nutrition(final net.minecraft.world.food.FoodProperties food) {
		//? if >=1.20.5 {
		return food.nutrition();
		//?} else {
		/*return food.getNutrition();
		*///?}
	}

	/** The saturation eating it gives (given as a modifier before 1.20.5: nutrition × modifier × 2). */
	public static float saturation(final net.minecraft.world.food.FoodProperties food) {
		//? if >=1.20.5 {
		return food.saturation();
		//?} else {
		/*return food.getNutrition() * food.getSaturationModifier() * 2.0F;
		*///?}
	}

	/**
	 * What a container item (a shulker box) holds, put in {@code items}; false when it holds nothing
	 * (a data component since 1.20.5, its block entity's NBT before).
	 */
	public static boolean containerItems(final net.minecraft.world.item.ItemStack stack, final net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> items) {
		//? if >=1.20.5 {
		net.minecraft.world.item.component.ItemContainerContents contents = stack.get(net.minecraft.core.component.DataComponents.CONTAINER);
		if (contents == null) {
			return false;
		}
		contents.copyInto(items);
		return true;
		//?} else {
		/*net.minecraft.nbt.CompoundTag tag = net.minecraft.world.item.BlockItem.getBlockEntityData(stack);
		if (tag == null || !tag.contains("Items", 9)) {
			return false;
		}
		net.minecraft.world.ContainerHelper.loadAllItems(tag, items);
		return true;
		*///?}
	}

	/** An effect's kind, as a holder (it is one since 1.20.5). */
	public static net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect(final net.minecraft.world.effect.MobEffectInstance effect) {
		//? if >=1.20.5 {
		return effect.getEffect();
		//?} else {
		/*return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.getEffect());
		*///?}
	}

	/** The objective shown in the sidebar (the slots were numbers before 1.20.2: the sidebar's is 1). */
	public static net.minecraft.world.scores.@org.jspecify.annotations.Nullable Objective sidebar(final net.minecraft.world.scores.Scoreboard scoreboard) {
		//? if >=1.20.2 {
		return scoreboard.getDisplayObjective(net.minecraft.world.scores.DisplaySlot.SIDEBAR);
		//?} else {
		/*return scoreboard.getDisplayObjective(1);
		*///?}
	}
}
