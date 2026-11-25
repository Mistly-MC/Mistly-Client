package online.mistylauncher.client.ui;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import online.mistylauncher.client.MistyClient;
import org.jspecify.annotations.Nullable;

/** The options of one server (a line of the Server category): one switch per option. */
public final class ServerSettingsScreen extends MistyScreen {
	private static final int WIDTH = 260;
	private static final int ROW = 20;
	private static final int GAP = 4;
	private static final int PAD = 10;
	private static final int HEADER = 22;

	/** One option: its key ({@code misty.option.<key>} and {@code .tip}) and its value. */
	public record Option(String key, BooleanSupplier value, Consumer<Boolean> change) {
	}

	private final List<Option> options;

	public ServerSettingsScreen(final Component server, final List<Option> options, final @Nullable Screen parent) {
		super(Component.translatable("misty.module.settings", server), parent);
		this.options = options;
	}

	@Override
	protected float veil() {
		return 0.55F;
	}

	private int frameW() {
		return Math.min(this.width - 20, WIDTH);
	}

	private int frameH() {
		return HEADER + PAD + this.options.size() * (ROW + GAP) + PAD + 18 + PAD;
	}

	private int frameX() {
		return (this.width - this.frameW()) / 2;
	}

	private int frameY() {
		return (this.height - this.frameH()) / 2;
	}

	@Override
	protected void init() {
		int x = this.frameX() + PAD;
		int inner = this.frameW() - 2 * PAD;
		int y = this.frameY() + HEADER + PAD;
		for (Option option : this.options) {
			MistyToggle toggle = new MistyToggle(x, y, inner, ROW, Component.translatable("misty.option." + option.key()), option.value(), option.change());
			online.mistylauncher.client.compat.Versions.tooltip(toggle, Component.translatable("misty.option." + option.key() + ".tip"));
			this.addRenderableWidget(toggle);
			y += ROW + GAP;
		}
		this.addRenderableWidget(new MistyButton(x, y + PAD - GAP, inner, 18, CommonComponents.GUI_DONE, this::onClose).primary());
	}

	@Override
	public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		int fx = this.frameX();
		int fy = this.frameY();
		int fw = this.frameW();
		Ui.card(graphics, fx, fy, fw, this.frameH(), 0xF00B1120, 0x33FFFFFF);
		graphics.fill(fx + 1, fy + 1, fx + fw - 1, fy + HEADER, 0xFF111A30);
		graphics.fill(fx + 1, fy + HEADER, fx + fw - 1, fy + HEADER + 1, Ui.LINE);
	}

	@Override
	public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		int fx = this.frameX();
		int fy = this.frameY();
		Ui.logo(graphics, fx + 8, fy + 4, 14);
		graphics.text(this.font, this.title, fx + 26, fy + (HEADER - 8) / 2, Ui.TEXT, false);
	}

	@Override
	public void removed() {
		MistyClient.config().save();
	}
}
