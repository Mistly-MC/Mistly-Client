stonecutter active "26.3"

// Conditions in the sources: //? if >=26.2 { ... //?}
// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
	// Names that only changed name. The sources use the newest ones; the versions older than
	// `depuis` (and not older than `jusque`) get `ancien`. Whole words only, applied to the sources
	// as written (one rename does not feed the next), and one way only: the sources must stay as
	// they are, so never make a version older than 26.1 the active one.
	fun renomme(nouveau: String, ancien: String, depuis: String = "26.1", jusque: String? = null) {
		val actif = current.parsed < depuis && (jusque == null || current.parsed >= jusque)
		replacements.regex(actif) {
			replace(java.util.regex.Pattern.compile("\\b$nouveau\\b"), ancien, java.util.regex.Pattern.compile("(?!)"), "")
		}
	}

	// Same, for text that does not start and end with a letter (a method descriptor, a parameter)
	fun remplace(nouveau: String, ancien: String, depuis: String) {
		replacements.regex(current.parsed < depuis) {
			replace(java.util.regex.Pattern.compile(nouveau), ancien, java.util.regex.Pattern.compile("(?!)"), "")
		}
	}

	// Before 1.21: no DeltaTracker (the HUD and the renderer get the frame's partial tick), new
	// ResourceLocation(...) instead of its factories, and the options screens in the screens package
	// (the most precise first: they win over the renames of their parts)
	remplace("Lnet/minecraft/client/DeltaTracker;", "F", "1.21")
	remplace("final DeltaTracker deltaTracker", "final float deltaTracker", "1.21")
	remplace("import net\\.minecraft\\.client\\.DeltaTracker;", "", "1.21")
	renomme("Identifier\\.withDefaultNamespace", "new ResourceLocation", "1.21")
	renomme("Identifier\\.fromNamespaceAndPath", "new ResourceLocation", "1.21")
	remplace("net\\.minecraft\\.client\\.gui\\.screens\\.options\\.", "net.minecraft.client.gui.screens.", "1.21")

	// Before 1.20.5 the game runs on Java 17, which has no Math.clamp: the game's own instead
	remplace("\\bMath\\.clamp\\(", "net.minecraft.util.Mth.clamp(", "1.20.5")
	// before the data components of 1.20.5, a stack's NBT was its "tag"
	renomme("isSameItemSameComponents", "isSameItemSameTags", "1.20.5")
	// packets were classes with getters before 1.20.5, and name tags were drawn without the partial tick
	remplace("packet\\.text\\(\\)", "packet.getText()", "1.20.5")
	remplace("poseStack, buffer, light, partialTicks\\);", "poseStack, buffer, light);", "1.20.5")
	remplace("Lnet/minecraft/client/renderer/MultiBufferSource;IF\\)V", "Lnet/minecraft/client/renderer/MultiBufferSource;I)V", "1.20.5")
	remplace("final MultiBufferSource buffers, final int light, final float partialTicks\\)", "final MultiBufferSource buffers, final int light)", "1.20.5")

	// Before 1.20.2: no PlayerSkin (the mod's own copy), authlib 4 (getters, a profile to join),
	// the profile from the user, and colors given through a style (the argument is kept: no
	// parentheses inside it but one call's)
	renomme("net\\.minecraft\\.world\\.entity\\.player\\.PlayerSkin", "online.mistylauncher.client.compat.PlayerSkin", "1.20.2")
	remplace("PlayerPreview\\.skin\\(\\), ", "PlayerPreview.skin().texture(), ", "1.20.2")
	remplace("textures\\.value\\(\\)", "textures.getValue()", "1.20.2")
	remplace("textures\\.signature\\(\\)", "textures.getSignature()", "1.20.2")
	remplace("Minecraft\\.getInstance\\(\\)\\.getGameProfile\\(\\)", "Minecraft.getInstance().getUser().getGameProfile()", "1.20.2")
	remplace("joinServer\\(user\\.getProfileId\\(\\),", "joinServer(user.getGameProfile(),", "1.20.2")
	remplace("joinServer\\(Ljava/util/UUID;", "joinServer(Lcom/mojang/authlib/GameProfile;", "1.20.2")
	remplace("final UUID profile, final String token", "final com.mojang.authlib.GameProfile profile, final String token", "1.20.2")
	remplace("\\.withColor\\((?!net\\.minecraft\\.ChatFormatting)([^()]*(?:\\([^()]*\\)[^()]*)*)\\)", ".withStyle(s -> s.withColor($1))", "1.20.3")
	// the mouse wheel only turned up or down before 1.20.2; widgets had no right nor bottom before 1.20.3
	remplace("(final double \\w+, final double \\w+), final double scrollX, final double scrollY\\)", "$1, final double scrollY)", "1.20.2")
	remplace("(\\w+, \\w+), scrollX, scrollY\\)", "$1, scrollY)", "1.20.2")
	remplace("this\\.getRight\\(\\)", "(this.getX() + this.getWidth())", "1.20.3")
	remplace("this\\.getBottom\\(\\)", "(this.getY() + this.getHeight())", "1.20.3")

	// Before 1.20: no GuiGraphics, the game draws with a pose stack. The mod draws with its own
	// copy (compat.GuiGraphics); the game's methods it overrides or hooks get the pose stack and
	// make one, and the game's drawing it calls gets the pose stack back
	remplace("net\\.minecraft\\.client\\.gui\\.GuiGraphicsExtractor", "online.mistylauncher.client.compat.GuiGraphics", "1.20")
	remplace("(?<=extractRenderState|extractWidgetRenderState|extractContents|misty\\$\\w{1,40})\\(\\s*final GuiGraphicsExtractor graphics((?:,[^){]*)?\\)[^{;]*\\{)",
		"(final com.mojang.blaze3d.vertex.PoseStack mistyPose$1 GuiGraphics graphics = new GuiGraphics(mistyPose);", "1.20")
	remplace("Lnet/minecraft/client/gui/GuiGraphicsExtractor;", "Lcom/mojang/blaze3d/vertex/PoseStack;", "1.20")
	remplace("super\\.extractRenderState\\(graphics,", "super.render(graphics.pose(),", "1.20")
	remplace("(?<=extractRenderStateWithTooltipAndSubtitles)\\(graphics,", "(graphics.pose(),", "1.20")
	remplace("protected void (?=extractContents\\b|extractWidgetRenderState\\b)", "public void ", "1.20")
	// map colors were "material" colors, and an entity's level a field
	renomme("MapColor", "MaterialColor", "1.20")
	remplace("entity\\.level\\(\\)", "entity.level", "1.20")
	remplace("PlayerFaceExtractor\\.extractRenderState\\(graphics, ", "online.mistylauncher.client.compat.GuiGraphics.face(graphics, ", "1.20")

	// The GUI is "extracted" instead of "rendered" since 26.1 (the most precise names first)
	renomme("PlayerFaceExtractor\\.extractRenderState", "PlayerFaceRenderer.draw")
	renomme("net\\.minecraft\\.util\\.LightCoordsUtil", "net.minecraft.client.renderer.LightTexture")
	renomme("extractRenderStateWithTooltipAndSubtitles", "renderWithTooltipAndSubtitles", jusque = "1.21.9")
	renomme("extractRenderStateWithTooltipAndSubtitles", "renderWithTooltip", "1.21.9")
	renomme("extractPanorama", "renderPanorama")
	renomme("extractScoreboardSidebar", "renderScoreboardSidebar")
	renomme("extractEffects", "renderEffects")
	renomme("extractFood", "renderFood")
	renomme("extractImage", "renderImage")
	renomme("graphics\\.itemDecorations", "graphics.renderItemDecorations")
	renomme("extractBlurredBackground", "renderBlurredBackground")
	renomme("GuiGraphicsExtractor", "GuiGraphics")
	renomme("extractRenderState", "render")
	renomme("extractBackground", "renderBackground")
	renomme("extractWidgetRenderState", "renderWidget")
	renomme("PlayerFaceExtractor", "PlayerFaceRenderer")
	renomme("LightCoordsUtil", "LightTexture")
	renomme("KeyMappingHelper", "KeyBindingHelper")
	renomme("keymapping\\.v1", "keybinding.v1")
	renomme("registerKeyMapping", "registerKeyBinding")
	renomme("graphics\\.text", "graphics.drawString")
	renomme("graphics\\.item", "graphics.renderItem")
	renomme("graphics\\.entity", "graphics.submitEntityRenderState")
	// buttons draw their whole look in renderWidget before 1.21.11
	renomme("extractContents", "renderContents", jusque = "1.21.11")
	renomme("extractContents", "renderWidget", "1.21.11")
	renomme("graphics\\.outline", "graphics.renderOutline", jusque = "1.21.11")
	renomme("graphics\\.outline", "graphics.submitOutline", "1.21.11", "1.21.9")
	renomme("graphics\\.outline", "graphics.renderOutline", "1.21.9")

	// Renamed in 1.21.11
	renomme("Identifier", "ResourceLocation", "1.21.11")
	renomme("net\\.minecraft\\.util\\.Util", "net.minecraft.Util", "1.21.11")
	renomme("net\\.minecraft\\.client\\.model\\.player\\.PlayerModel", "net.minecraft.client.model.PlayerModel", "1.21.11")
	renomme("net\\.minecraft\\.client\\.renderer\\.rendertype\\.RenderTypes", "net.minecraft.client.renderer.RenderType", "1.21.11")
	renomme("RenderTypes", "RenderType", "1.21.11")

	// Renamed in 1.21.9: players were not yet "avatars", skins were in the client package, and
	// buttons were pressed without the input that pressed them
	renomme("AvatarRenderState", "PlayerRenderState", "1.21.9")
	renomme("AvatarRenderer", "PlayerRenderer", "1.21.9")
	renomme("net\\.minecraft\\.world\\.entity\\.player\\.PlayerSkin", "net.minecraft.client.resources.PlayerSkin", "1.21.9", "1.20.2")
	renomme("onPress\\(final InputWithModifiers input", "onPress(", "1.21.9")
	renomme("net\\.minecraft\\.client\\.input\\.InputWithModifiers", "java.lang.Object", "1.21.9")
}
