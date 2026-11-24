package online.mistylauncher.legacy.net;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import online.mistylauncher.legacy.Config;
import org.lwjgl.opengl.GL11;

/**
 * The Mistly cloud in front of the Mistly players' names: above their head, in the player list
 * and in the chat, as in the Fabric versions. The cloud is a character of the game's font
 * ({@code U+E000}, drawn in {@code textures/font/unicode_page_e0.png}), so it flows with the text.
 */
public final class Badge {
	/** The cloud, white whatever the colour of the text around it, and a space. */
	public static final String ICON = "§f§r ";
	/** A chat line is "name, then one of these, then the message" on about every server. */
	private static final String SEPARATORS = ":>»→›";
	/** The name must start within this many characters (ranks and prefixes come before it). */
	private static final int NAME_WITHIN = 48;

	/** The player list's names while it is drawn with the clouds, to put them back after. */
	private static final Map<NetworkPlayerInfo, IChatComponent> LISTED = new HashMap<NetworkPlayerInfo, IChatComponent>();

	private Badge() {
	}

	/**
	 * Gives the cloud its width in the font (the game reads the widths of its own characters only),
	 * again after each reload of the resources, which reads them again.
	 */
	public static void install(final FontRenderer font) {
		width(font);
		IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
		if (manager instanceof IReloadableResourceManager) {
			((IReloadableResourceManager)manager).registerReloadListener(new IResourceManagerReloadListener() {
				@Override
				public void onResourceManagerReload(final IResourceManager reloaded) {
					width(font);
				}
			});
		}
	}

	private static void width(final FontRenderer font) {
		try {
			byte[] widths = ReflectionHelper.getPrivateValue(FontRenderer.class, font, "glyphWidth", "field_78287_e");
			// columns 0 to 15 of its cell: 8 pixels wide on the screen
			widths[0xE000] = 0x0F;
		} catch (RuntimeException e) {
			System.err.println("[Mistly Client] badge not installed: " + e);
		}
	}

	/** Above the head: also its own switch (the player list and the chat follow the network). */
	private static boolean shown() {
		return Config.get().network && Config.get().nametagBadge;
	}

	// ── above the head ──

	/**
	 * The name above a Mistly player's head, with the cloud before it: drawn as the game draws it
	 * (its distance, its team's rules, the score under it). False: left to the game (not a
	 * Mistly player, crouching, nothing to draw).
	 */
	public static boolean nameTag(final AbstractClientPlayer player, final RenderManager manager, final double x, final double y, final double z) {
		if (!shown() || player.isSneaking() || !MistyPlayers.isMisty(player.getUniqueID())) {
			return false;
		}
		if (!canRenderName(player, manager)) {
			return true;
		}
		double distance = player.getDistanceSqToEntity(manager.livingPlayer);
		if (distance >= 64.0 * 64.0) {
			return true;
		}
		float scale = 0.02666667F;
		double labelY = y - (player.isChild() ? player.height / 2.0F : 0.0F);
		GlStateManager.alphaFunc(516, 0.1F);
		FontRenderer font = manager.getFontRenderer();
		// as the player renderer: the score of the objective shown under the names, close by
		if (distance < 100.0) {
			Scoreboard scoreboard = player.getWorldScoreboard();
			ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(2);
			if (objective != null) {
				Score score = scoreboard.getValueFromObjective(player.getName(), objective);
				label(player, manager, score.getScorePoints() + " " + objective.getDisplayName(), x, labelY, z);
				labelY += font.FONT_HEIGHT * 1.15F * scale;
			}
		}
		label(player, manager, ICON + player.getDisplayName().getFormattedText(), x, labelY, z);
		return true;
	}

	/** The game's rules for a player's name: its team's visibility, invisible, ridden, F1. */
	private static boolean canRenderName(final AbstractClientPlayer entity, final RenderManager manager) {
		EntityPlayerSP self = Minecraft.getMinecraft().thePlayer;
		if (entity != self && self != null) {
			Team team = entity.getTeam();
			Team own = self.getTeam();
			if (team != null) {
				switch (team.getNameTagVisibility()) {
					case ALWAYS:
						return true;
					case NEVER:
						return false;
					case HIDE_FOR_OTHER_TEAMS:
						return own == null || team.isSameTeam(own);
					case HIDE_FOR_OWN_TEAM:
						return own == null || !team.isSameTeam(own);
					default:
						return true;
				}
			}
		}
		return Minecraft.isGuiEnabled() && entity != manager.livingPlayer && (self == null || !entity.isInvisibleToPlayer(self)) && entity.riddenByEntity == null;
	}

	/** A line over the head, as the game draws one (Render.renderLivingLabel). */
	private static void label(final AbstractClientPlayer entity, final RenderManager manager, final String text, final double x, final double y, final double z) {
		FontRenderer font = manager.getFontRenderer();
		float scale = 0.016666668F * 1.6F;
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x, (float)y + entity.height + 0.5F, (float)z);
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-manager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(manager.playerViewX, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-scale, -scale, scale);
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		int half = font.getStringWidth(text) / 2;
		GlStateManager.disableTexture2D();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer buffer = tessellator.getWorldRenderer();
		buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(-half - 1, -1, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		buffer.pos(-half - 1, 8, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		buffer.pos(half + 1, 8, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		buffer.pos(half + 1, -1, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		font.drawString(text, -half, 0, 553648127);
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		font.drawString(text, -half, 0, -1);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	// ── the player list ──

	/** Before the player list is drawn: the Mistly players' names get the cloud. */
	public static void beforePlayerList() {
		LISTED.clear();
		NetHandlerPlayClient connection = Minecraft.getMinecraft().getNetHandler();
		if (!Config.get().network || connection == null) {
			return;
		}
		for (NetworkPlayerInfo info : connection.getPlayerInfoMap()) {
			if (MistyPlayers.isMisty(info.getGameProfile().getId())) {
				IChatComponent own = info.getDisplayName();
				LISTED.put(info, own);
				String name = own != null ? own.getFormattedText() : ScorePlayerTeam.formatPlayerName(info.getPlayerTeam(), info.getGameProfile().getName());
				info.setDisplayName(new ChatComponentText(ICON + name));
			}
		}
	}

	/** After: the names are given back as the server set them. */
	public static void afterPlayerList() {
		for (Map.Entry<NetworkPlayerInfo, IChatComponent> listed : LISTED.entrySet()) {
			listed.getKey().setDisplayName(listed.getValue());
		}
		LISTED.clear();
	}

	// ── the chat ──

	/** A chat line about to be shown: the cloud before it when a Mistly player speaks. */
	public static IChatComponent chat(final IChatComponent message) {
		if (!Config.get().network || !speakerIsMisty(message.getUnformattedText())) {
			return message;
		}
		return new ChatComponentText(ICON).appendSibling(message);
	}

	/**
	 * Servers format the chat themselves: the speaker is the first player of the list whose name
	 * stands before a separator ("Name: hello", "[VIP] Name » hello").
	 */
	private static boolean speakerIsMisty(final String line) {
		NetHandlerPlayClient connection = Minecraft.getMinecraft().getNetHandler();
		if (connection == null) {
			return false;
		}
		int first = Integer.MAX_VALUE;
		boolean misty = false;
		for (NetworkPlayerInfo info : connection.getPlayerInfoMap()) {
			UUID id = info.getGameProfile().getId();
			int at = nameBeforeSeparator(line, info.getGameProfile().getName());
			if (at >= 0 && at < first) {
				first = at;
				misty = MistyPlayers.isMisty(id);
			}
		}
		return misty;
	}

	/** Position of {@code name} as a whole word followed by a separator, or -1. */
	private static int nameBeforeSeparator(final String line, final String name) {
		if (name == null || name.isEmpty()) {
			return -1;
		}
		int at = line.indexOf(name);
		if (at < 0 || at > NAME_WITHIN) {
			return -1;
		}
		int end = at + name.length();
		if (at > 0 && isNameChar(line.charAt(at - 1)) || end < line.length() && isNameChar(line.charAt(end))) {
			return -1;
		}
		for (int i = end; i < line.length() && i < end + 6; i++) {
			if (SEPARATORS.indexOf(line.charAt(i)) >= 0) {
				return at;
			}
		}
		return -1;
	}

	private static boolean isNameChar(final char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}
}
