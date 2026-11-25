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
	fun remplace(nouveau: String, ancien: String, depuis: String, jusque: String? = null) {
		replacements.regex(current.parsed < depuis && (jusque == null || current.parsed >= jusque)) {
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
	renomme("isSameItemSameComponents", "isSameItemSameTags", "1.20.5", "1.17")
	// before 1.17: the item, then its NBT
	remplace("ItemStack\\.isSameItemSameComponents\\(kinds\\.get\\(i\\)\\.icon\\(\\), stack\\)",
		"(ItemStack.isSame(kinds.get(i).icon(), stack) && ItemStack.tagMatches(kinds.get(i).icon(), stack))", "1.17")
	remplace("player\\.getEyePosition\\(\\)", "player.getEyePosition(1.0F)", "1.17")
	// hitboxes drawn from the box's six coordinates: the colour comes 5 arguments later
	remplace("Lnet/minecraft/world/phys/AABB;FFFF\\)V", "DDDDDDFFFF)V", "1.17")
	remplace("target = BOX, ordinal = 0\\), index = 3\\)", "target = BOX, ordinal = 0), index = 8)", "1.17")
	remplace("target = BOX, ordinal = 0\\), index = 4\\)", "target = BOX, ordinal = 0), index = 9)", "1.17")
	remplace("target = BOX, ordinal = 0\\), index = 5\\)", "target = BOX, ordinal = 0), index = 10)", "1.17")
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
	remplace("\\.withColor\\((?!net\\.minecraft\\.ChatFormatting)([^()]*(?:\\([^()]*\\)[^()]*)*)\\)", ".withStyle(s -> s.withColor($1))", "1.20.3", "1.17")
	// a style's color was a TextColor before 1.17
	remplace("\\.withColor\\((?!net\\.minecraft\\.ChatFormatting)([^()]*(?:\\([^()]*\\)[^()]*)*)\\)",
		".withStyle(s -> s.withColor(net.minecraft.network.chat.TextColor.fromRgb($1)))", "1.17")
	// the mouse wheel only turned up or down before 1.20.2; widgets had no right nor bottom before 1.20.3
	remplace("(final double \\w+, final double \\w+), final double scrollX, final double scrollY\\)", "$1, final double scrollY)", "1.20.2")
	remplace("(\\w+, \\w+), scrollX, scrollY\\)", "$1, scrollY)", "1.20.2")
	remplace("this\\.getRight\\(\\)", "(this.getX() + this.getWidth())", "1.20.3", "1.19.3")
	remplace("this\\.getBottom\\(\\)", "(this.getY() + this.getHeight())", "1.20.3", "1.19.3")
	remplace("this\\.getRight\\(\\)", "(this.x + this.getWidth())", "1.19.3")
	remplace("this\\.getBottom\\(\\)", "(this.y + this.getHeight())", "1.19.3")

	// Before 1.19.4: widgets drew themselves in renderButton, had no isHovered() nor the item
	// display contexts; screens drew with render (no renderWithTooltip), swords had no tag
	remplace("this\\.isHovered\\(\\)", "this.isHovered", "1.19.4")
	remplace("effect\\.isInfiniteDuration\\(\\)", "false", "1.19.4")
	remplace("stack\\.is\\(ItemTags\\.SWORDS\\)", "stack.getItem() instanceof net.minecraft.world.item.SwordItem", "1.19.4")
	remplace("Lnet/minecraft/world/item/ItemDisplayContext;", "Lnet/minecraft/client/renderer/block/model/ItemTransforms\\\$TransformType;", "1.19.4")
	remplace("net\\.minecraft\\.world\\.item\\.ItemDisplayContext", "net.minecraft.client.renderer.block.model.ItemTransforms.TransformType", "1.19.4")
	renomme("ItemDisplayContext", "net.minecraft.client.renderer.block.model.ItemTransforms.TransformType", "1.19.4")
	// Before 1.19.3: widgets had x and y fields, no tooltips of their own; screens no rebuildWidgets;
	// the game's own math (no Axis, no JOML) and registries in Registry
	remplace("protected void updateWidgetNarration\\(", "public void updateNarration(", "1.19.3", "1.17")
	remplace("widget\\.getY\\(\\)", "widget.y", "1.19.3")
	remplace("this\\.getX\\(\\)", "this.x", "1.19.3")
	remplace("this\\.getY\\(\\)", "this.y", "1.19.3")
	remplace("this\\.rebuildWidgets\\(\\)", "this.init(this.minecraft, this.width, this.height)", "1.19.3")
	remplace("import net\\.minecraft\\.client\\.gui\\.components\\.Tooltip;", "", "1.19.3")
	remplace("com\\.mojang\\.math\\.Axis\\.", "com.mojang.math.Vector3f.", "1.19.3")
	remplace("import com\\.mojang\\.math\\.Axis;", "import com.mojang.math.Vector3f;", "1.19.3")
	remplace("\\bAxis\\.(XP|YP|ZP|XN|YN|ZN)\\b", "Vector3f.$1", "1.19.3")
	remplace("net\\.minecraft\\.core\\.registries\\.BuiltInRegistries", "net.minecraft.core.Registry", "1.19.3")
	remplace("import net\\.minecraft\\.client\\.gui\\.screens\\.CreditsAndAttributionScreen;", "", "1.19.3")
	remplace("new CreditsAndAttributionScreen\\((\\w+)\\)", "new net.minecraft.client.gui.screens.WinScreen(false, () -> Versions.setScreen($1))", "1.19.3")
	// Before 1.18: log4j instead of slf4j, an older Gson
	remplace("import org\\.slf4j\\.Logger;", "import org.apache.logging.log4j.Logger;", "1.18")
	remplace("import org\\.slf4j\\.LoggerFactory;", "import org.apache.logging.log4j.LogManager;", "1.18")
	remplace("\\bLoggerFactory\\.getLogger\\(", "LogManager.getLogger(", "1.18")
	remplace("\\bJsonParser\\.parseString\\(", "new com.google.gson.JsonParser().parse(", "1.18")
	// Before 1.17 (1.16.5): no shaders (fixed GL state), screens added their buttons with addButton,
	// widgets had no narration, entities fields instead of getters, the old item and key constants
	remplace("(?:com\\.mojang\\.blaze3d\\.systems\\.)?RenderSystem\\.setShaderColor\\(", "com.mojang.blaze3d.systems.RenderSystem.color4f(", "1.17")
	remplace("(?:com\\.mojang\\.blaze3d\\.systems\\.)?RenderSystem\\.setShaderTexture\\(0, ", "net.minecraft.client.Minecraft.getInstance().getTextureManager().bind(", "1.17")
	remplace("\\baddRenderableWidget\\b", "addButton", "1.17")
	remplace("isHoveredOrFocused\\(\\)", "isHovered()", "1.17")
	remplace("\\bMth\\.DEG_TO_RAD\\b", "((float)(Math.PI / 180.0))", "1.17")
	remplace("(\\w+)\\.is\\((Items\\.\\w+)\\)", "$1.getItem() == $2", "1.17")
	remplace("InputConstants\\.KEY_RSHIFT", "org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT", "1.17")
	remplace("InputConstants\\.KEY_([A-Z0-9])\\b", "org.lwjgl.glfw.GLFW.GLFW_KEY_$1", "1.17")
	remplace("InputConstants\\.MOUSE_BUTTON_(\\w+)", "org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_$1", "1.17")
	remplace("(?:net\\.minecraft\\.(?:util|client\\.renderer)\\.)?(?:LightCoordsUtil|LightTexture)\\.FULL_BRIGHT", "0xF000F0", "1.17")
	remplace("\\.getYRot\\(\\)", ".yRot", "1.17")
	remplace("\\.getXRot\\(\\)", ".xRot", "1.17")
	remplace("([\\w.]+)\\.getBlock([XYZ])\\(\\)", "net.minecraft.util.Mth.floor($1.get$2())", "1.17")
	remplace("\\.getInventory\\(\\)", ".inventory", "1.17")
	remplace("\\.isRemoved\\(\\)", ".removed", "1.17")
	remplace("tnt\\.getFuse\\(\\)", "tnt.getLife()", "1.17")
	remplace("net\\.minecraft\\.world\\.item\\.BlockItem\\.getBlockEntityData\\((\\w+)\\)", "$1.getTagElement(\"BlockEntityTag\")", "1.17")
	remplace("getModel\\(stack, entity\\.level\\(\\), null, entity\\.getId\\(\\)\\)", "getModel(stack, entity.level, null)", "1.17")
	remplace("import net\\.minecraft\\.client\\.renderer\\.entity\\.EntityRendererProvider;", "", "1.17")
	remplace("EntityRendererProvider\\.Context", "net.minecraft.client.renderer.entity.EntityRenderDispatcher", "1.17")
	// the player renderer had two constructors: the one with the arms' kind
	remplace("method = \"<init>\", at = @At\\(\"TAIL\"\\)\\)(\\s+@SuppressWarnings\\(\"unchecked\"\\)\\s+private void misty\\\$hatLayer)",
		"method = \"<init>(Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;Z)V\", at = @At(\"TAIL\"))$1", "1.17")
	remplace("import net\\.minecraft\\.client\\.gui\\.narration\\.NarrationElementOutput;", "", "1.17")
	remplace("@Override\\s+protected void updateWidgetNarration\\(final NarrationElementOutput output\\) \\{\\s*(?:this\\.defaultButtonNarrationText\\(output\\);\\s*)?\\}", "", "1.17")
	// models were made directly (no baked layers), with the GL light of the GUI
	remplace("Minecraft\\.getInstance\\(\\)\\.getEntityModels\\(\\)\\.bakeLayer\\(net\\.minecraft\\.client\\.model\\.geom\\.ModelLayers\\.PLAYER(?:_SLIM)?\\), ", "0.0F, ", "1.17")
	remplace("com\\.mojang\\.blaze3d\\.platform\\.Lighting\\.setupForEntityInInventory\\(\\);", "com.mojang.blaze3d.platform.Lighting.setupFor3DItems();", "1.17")
	remplace("import net\\.minecraft\\.core\\.Holder;", "", "1.17")
	remplace("import net\\.minecraft\\.client\\.gui\\.components\\.PlainTextButton;", "", "1.17")
	remplace("com\\.mojang\\.blaze3d\\.systems\\.RenderSystem\\.resetTextureMatrix\\(\\);", "", "1.17")
	remplace("net\\.minecraft\\.world\\.inventory\\.tooltip\\.TooltipComponent", "online.mistylauncher.client.compat.TooltipComponent", "1.17")
	remplace("net\\.minecraft\\.client\\.gui\\.screens\\.inventory\\.tooltip\\.ClientTooltipComponent", "online.mistylauncher.client.compat.ClientTooltipComponent", "1.17")
	remplace("effect\\.unwrapKey\\(\\)", "net.minecraft.core.Registry.MOB_EFFECT.getResourceKey(effect)", "1.17")
	remplace("effect\\.value\\(\\)\\.", "effect.", "1.17")
	remplace("(?:net\\.minecraft\\.core\\.)?Holder<(?:net\\.minecraft\\.world\\.effect\\.)?MobEffect>", "net.minecraft.world.effect.MobEffect", "1.17")

	// Before 1.19: text components were classes (TextComponent, TranslatableComponent), the random
	// source java's, images read from streams, and the user's profile id in its game profile
	remplace("(?<![\\w.])(?:net\\.minecraft\\.network\\.chat\\.)?Component\\.translatable\\(", "new net.minecraft.network.chat.TranslatableComponent(", "1.19")
	remplace("(?<![\\w.])(?:net\\.minecraft\\.network\\.chat\\.)?Component\\.literal\\(", "new net.minecraft.network.chat.TextComponent(", "1.19")
	remplace("(?<![\\w.])(?:net\\.minecraft\\.network\\.chat\\.)?Component\\.empty\\(\\)", "net.minecraft.network.chat.TextComponent.EMPTY.copy()", "1.19")
	remplace("connection\\.sendChat\\(", "minecraft.player.chat(", "1.19")
	// options were plain fields (no OptionInstance)
	remplace("at = @At\\(value = \"INVOKE\", target = \"Ljava/lang/Double;doubleValue\\(\\)D\", ordinal = 0\\)",
		"at = @At(value = \"FIELD\", target = \"Lnet/minecraft/client/Options;sensitivity:D\")", "1.19")
	remplace("addMessage\\(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;\\)V",
		"addMessage(Lnet/minecraft/network/chat/Component;I)V", "1.19")
	remplace("Lnet/minecraft/client/gui/components/ChatComponent;render\\(Lnet/minecraft/client/gui/GuiGraphicsExtractor;III\\)V",
		"Lnet/minecraft/client/gui/components/ChatComponent;render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V", "1.19")
	remplace("MOB_EFFECT\\.wrapAsHolder\\(effect\\.getEffect\\(\\)\\)",
		"MOB_EFFECT.getOrCreateHolder(net.minecraft.core.Registry.MOB_EFFECT.getResourceKey(effect.getEffect()).orElseThrow())", "1.19")
	remplace("return MobEffectUtil\\.formatDuration\\(effect, 1\\.0F\\);", "return new net.minecraft.network.chat.TextComponent(MobEffectUtil.formatDuration(effect, 1.0F));", "1.19")
	remplace("minecraft\\.getSkinManager\\(\\)\\.getInsecureSkinLocation\\(profile\\)",
		"(body != null ? minecraft.getSkinManager().registerTexture(body, com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN) : net.minecraft.client.resources.DefaultPlayerSkin.getDefaultSkin(Versions.id(profile)))", "1.19")
	remplace("final net\\.minecraft\\.client\\.renderer\\.entity\\.ItemRenderer items\\)", "final net.minecraft.client.renderer.entity.ItemRenderer items, final int blitOffset)", "1.19.3")
	remplace("minecraft\\.getFps\\(\\)", "online.mistylauncher.client.compat.Versions.fps()", "1.19")
	remplace("stack\\.copyWithCount\\(1\\)", "online.mistylauncher.client.compat.Versions.one(stack)", "1.19")
	remplace("target\\.getBoundingBox\\(\\)\\.distanceToSqr\\(player\\.getEyePosition\\(\\)\\)",
		"online.mistylauncher.client.compat.Versions.distanceToSqr(target.getBoundingBox(), player.getEyePosition())", "1.19", "1.17")
	remplace("target\\.getBoundingBox\\(\\)\\.distanceToSqr\\(player\\.getEyePosition\\(\\)\\)",
		"online.mistylauncher.client.compat.Versions.distanceToSqr(target.getBoundingBox(), player.getEyePosition(1.0F))", "1.17")
	remplace("import net\\.minecraft\\.util\\.RandomSource;", "", "1.19")
	remplace("\\bRandomSource\\.create\\(\\)", "new java.util.Random()", "1.19")
	remplace("\\bRandomSource\\b", "java.util.Random", "1.19")
	remplace("NativeImage\\.read\\(([^;]*)\\);", "NativeImage.read(new java.io.ByteArrayInputStream($1));", "1.19")
	remplace("getUser\\(\\)\\.getProfileId\\(\\)", "getUser().getGameProfile().getId()", "1.19")
	remplace("user\\.getProfileId\\(\\)", "user.getGameProfile().getId()", "1.19")

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
	remplace("import net\\.minecraft\\.client\\.gui\\.components\\.PlayerFaceExtractor;", "", "1.20")

	// The GUI is "extracted" instead of "rendered" since 26.1 (the most precise names first)
	renomme("PlayerFaceExtractor\\.extractRenderState", "PlayerFaceRenderer.draw")
	renomme("net\\.minecraft\\.util\\.LightCoordsUtil", "net.minecraft.client.renderer.LightTexture")
	renomme("extractRenderStateWithTooltipAndSubtitles", "renderWithTooltipAndSubtitles", jusque = "1.21.9")
	renomme("extractRenderStateWithTooltipAndSubtitles", "renderWithTooltip", "1.21.9", "1.19.4")
	renomme("extractRenderStateWithTooltipAndSubtitles", "render", "1.19.4")
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
	renomme("extractWidgetRenderState", "renderWidget", jusque = "1.19.4")
	renomme("extractWidgetRenderState", "renderButton", "1.19.4")
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
	renomme("extractContents", "renderWidget", "1.21.11", "1.19.4")
	renomme("extractContents", "renderButton", "1.19.4")
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
