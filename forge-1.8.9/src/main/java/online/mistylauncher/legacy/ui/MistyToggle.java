package online.mistylauncher.legacy.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/** A setting on a line: its name, and a switch on the right (with a gear before it when it has settings). */
public class MistyToggle extends MistyButton {
	private static final int SWITCH_WIDTH = 20;
	private static final int SWITCH_HEIGHT = 10;
	private static final int GEAR = 10;
	private static final ResourceLocation GEAR_ICON = Ui.icon("options");

	/** A setting's value, read and changed. */
	public interface Value {
		boolean get();

		void set(boolean value);
	}

	private final Value value;
	private Runnable gear;
	/** The mouse is over the gear (noted while drawing, read when the line is clicked). */
	private boolean gearHot;

	public MistyToggle(final int x, final int y, final int width, final int height, final String message, final Value value) {
		super(x, y, width, height, message, null);
		this.value = value;
	}

	/** Adds a gear before the switch: a click on it opens the settings instead of switching. */
	public MistyToggle gear(final Runnable open) {
		this.gear = open;
		return this;
	}

	@Override
	public void press() {
		if (this.gear != null && this.gearHot) {
			this.gear.run();
		} else {
			this.value.set(!this.value.get());
		}
	}

	@Override
	protected void drawContents(final int mouseX, final int mouseY) {
		int x = this.xPosition;
		int y = this.yPosition;
		boolean hot = this.hovered;
		boolean on = this.value.get();
		Ui.card(x, y, this.width, this.height, hot ? Ui.GLASS_HOVER : Ui.GLASS, hot ? Ui.alpha(Ui.ACCENT, 0.6F) : Ui.BORDER);
		int switchX = x + this.width - SWITCH_WIDTH - 7;
		// the name stops before the gear or the switch (cut with … in a narrow column)
		int room = (this.gear != null ? switchX - GEAR - 8 : switchX) - 4 - (x + 8);
		String name = this.displayString;
		if (Ui.width(name) > room) {
			name = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(name, room - Ui.width("…")) + "…";
		}
		Ui.text(name, x + 8, y + (this.height - 8) / 2, on ? Ui.TEXT : Ui.MUTED);

		int switchY = y + (this.height - SWITCH_HEIGHT) / 2;
		Ui.panel(switchX, switchY, SWITCH_WIDTH, SWITCH_HEIGHT, on ? Ui.FILL : Ui.TRACK);
		int knobX = on ? switchX + SWITCH_WIDTH - SWITCH_HEIGHT + 2 : switchX + 2;
		Ui.panel(knobX, switchY + 2, SWITCH_HEIGHT - 4, SWITCH_HEIGHT - 4, on ? 0xFFFFFFFF : Ui.MUTED);

		if (this.gear != null) {
			int gearX = switchX - GEAR - 6;
			int gearY = y + (this.height - GEAR) / 2;
			this.gearHot = hot && mouseX >= gearX - 2 && mouseX < gearX + GEAR + 2 && mouseY >= gearY - 2 && mouseY < gearY + GEAR + 2;
			if (this.gearHot) {
				Ui.panel(gearX - 2, gearY - 2, GEAR + 4, GEAR + 4, Ui.alpha(Ui.ACCENT, 0.35F));
			}
			Ui.blit(GEAR_ICON, gearX, gearY, GEAR, 16, 0xFFFFFFFF);
		} else {
			this.gearHot = false;
		}
	}
}
