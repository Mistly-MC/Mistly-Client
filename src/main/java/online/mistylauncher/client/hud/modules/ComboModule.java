package online.mistylauncher.client.hud.modules;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import online.mistylauncher.client.config.MistyConfig;
import online.mistylauncher.client.hud.Combo;
import online.mistylauncher.client.hud.TextModule;
import online.mistylauncher.client.ui.Ui;

/**
 * Players hit in a row without being hit back (see Combo). Behind the gear, "Record": a second
 * line "Record : 7" once a combo is over, the best of the last minute, gone after a minute.
 */
public final class ComboModule extends TextModule {
	private static final String RECORD = "record";
	private static final int LINE = 10;

	public ComboModule() {
		super("combo", new MistyConfig.Module(true, 3, 4, 82), 58);
	}

	@Override
	public List<Setting> settings() {
		return List.of(new Setting(RECORD, false));
	}

	@Override
	protected Component text(final Minecraft minecraft, final boolean preview) {
		int combo = Combo.combo();
		return value(Integer.toString(preview && combo == 0 ? 3 : combo), "combo");
	}

	/** The record line, null when it is off or there is none to show. */
	private Component record(final boolean preview) {
		if (!this.setting(RECORD)) {
			return null;
		}
		int record = Combo.record();
		if (record == 0 && preview) {
			record = 7;
		}
		return record == 0 ? null : Component.translatable("misty.module.combo.recordLine").withColor(Ui.ACCENT & 0xFFFFFF)
			.append(Component.literal(" : " + record).withColor(Ui.TEXT & 0xFFFFFF));
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		Component record = this.record(preview);
		return Math.max(super.width(minecraft, preview), record == null ? 0 : minecraft.font.width(record) + 10);
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return super.height(minecraft, preview) + (this.record(preview) == null ? 0 : LINE);
	}

	@Override
	public void draw(final GuiGraphicsExtractor graphics, final Minecraft minecraft, final boolean preview) {
		Component record = this.record(preview);
		if (record == null) {
			super.draw(graphics, minecraft, preview);
			return;
		}
		int width = this.width(minecraft, preview);
		background(graphics, width, this.height(minecraft, preview));
		Component text = this.text(minecraft, preview);
		graphics.text(minecraft.font, text, (width - minecraft.font.width(text)) / 2, 3, Ui.TEXT, false);
		graphics.text(minecraft.font, record, (width - minecraft.font.width(record)) / 2, 3 + LINE + 1, Ui.TEXT, false);
	}
}
