package online.mistylauncher.legacy.hud.modules;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import online.mistylauncher.legacy.Config;
import online.mistylauncher.legacy.features.Combat;
import online.mistylauncher.legacy.hud.TextModule;
import online.mistylauncher.legacy.ui.Ui;

/** The players you hit in a row (hits that land); behind its gear, the record of the last minute. */
public final class ComboModule extends TextModule {
	private static final String RECORD = "record";
	private static final int LINE = 10;

	public ComboModule() {
		super("combo", new Config.Module(true, 3, 4, 82), 58);
	}

	@Override
	public List<Setting> settings() {
		return Collections.singletonList(new Setting(RECORD, false));
	}

	@Override
	protected String value(final Minecraft minecraft, final boolean preview) {
		int combo = Combat.combo();
		return Integer.toString(preview && combo == 0 ? 3 : combo);
	}

	@Override
	protected String unit() {
		return "combo";
	}

	/** The record's number, 0 when it is off or there is none to show. */
	private int record(final boolean preview) {
		if (!this.setting(RECORD)) {
			return 0;
		}
		int record = Combat.record();
		return record == 0 && preview ? 7 : record;
	}

	private static String recordLabel() {
		return I18n.format("misty.module.combo.recordLine");
	}

	@Override
	public int width(final Minecraft minecraft, final boolean preview) {
		int record = this.record(preview);
		return Math.max(super.width(minecraft, preview), record == 0 ? 0 : Ui.width(recordLabel() + " : " + record) + 10);
	}

	@Override
	public int height(final Minecraft minecraft, final boolean preview) {
		return super.height(minecraft, preview) + (this.record(preview) == 0 ? 0 : LINE);
	}

	@Override
	public void draw(final Minecraft minecraft, final boolean preview) {
		int record = this.record(preview);
		if (record == 0) {
			super.draw(minecraft, preview);
			return;
		}
		int width = this.width(minecraft, preview);
		background(width, this.height(minecraft, preview));
		String value = this.value(minecraft, preview);
		drawLine(value, Ui.TEXT, this.unit(), (width - Ui.width(value + " " + this.unit())) / 2, 3);
		String label = recordLabel();
		String count = " : " + record;
		int x = (width - Ui.width(label + count)) / 2;
		Ui.text(label, x, 3 + LINE + 1, Ui.ACCENT);
		Ui.text(count, x + Ui.width(label), 3 + LINE + 1, Ui.TEXT);
	}
}
