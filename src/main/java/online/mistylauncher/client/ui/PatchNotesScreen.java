package online.mistylauncher.client.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import online.mistylauncher.client.MistyClient;
import org.jspecify.annotations.Nullable;

/**
 * The patch notes of Mistly Client: the notes of its last releases on GitHub (what each version
 * brought), newest first, scrolled with the mouse wheel. Read once per game session.
 */
public final class PatchNotesScreen extends MistyScreen {
	private static final String RELEASES = "https://api.github.com/repos/Misty-MC/Mistly-Client/releases?per_page=15";
	private static final int WIDTH = 360;
	private static final int HEADER = 22;
	private static final int PAD = 10;
	private static final int LINE = 10;

	/** One release: its version, its date (yyyy-mm-dd) and its notes, line by line. */
	private record Release(String version, String date, List<String> notes) {
	}

	/** The releases read from GitHub (null: not yet, or not reachable). */
	private static volatile @Nullable List<Release> releases;
	private static volatile boolean failed;
	private static volatile boolean loading;

	private double scroll;

	public PatchNotesScreen(final @Nullable Screen parent) {
		super(Component.translatable("misty.patchNotes.title"), parent);
	}

	@Override
	protected float veil() {
		return 0.55F;
	}

	private int frameW() {
		return Math.min(this.width - 20, WIDTH);
	}

	private int frameH() {
		return Math.min(this.height - 20, 300);
	}

	private int frameX() {
		return (this.width - this.frameW()) / 2;
	}

	private int frameY() {
		return (this.height - this.frameH()) / 2;
	}

	/** Where the notes are drawn (between the header and the button). */
	private int listTop() {
		return this.frameY() + HEADER + 6;
	}

	private int listBottom() {
		return this.frameY() + this.frameH() - PAD - 18 - 6;
	}

	@Override
	protected void init() {
		load();
		int x = this.frameX() + PAD;
		int inner = this.frameW() - 2 * PAD;
		this.addRenderableWidget(new MistyButton(x, this.frameY() + this.frameH() - PAD - 18, inner, 18, CommonComponents.GUI_DONE, this::onClose).primary());
	}

	/** Reads the releases in the background, once. */
	private static void load() {
		if (releases != null || loading) {
			return;
		}
		loading = true;
		failed = false;
		HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NORMAL).build();
		HttpRequest request = HttpRequest.newBuilder(URI.create(RELEASES)).timeout(Duration.ofSeconds(15))
			.header("Accept", "application/vnd.github+json").header("User-Agent", "Mistly-Client").GET().build();
		http.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete((response, error) -> {
			try {
				if (error != null || response.statusCode() != 200) {
					failed = true;
					return;
				}
				List<Release> read = new ArrayList<>();
				JsonArray list = JsonParser.parseString(response.body()).getAsJsonArray();
				for (JsonElement element : list) {
					JsonObject release = element.getAsJsonObject();
					if (release.get("draft").getAsBoolean() || release.get("prerelease").getAsBoolean()) {
						continue;
					}
					String date = release.has("published_at") && !release.get("published_at").isJsonNull() ? release.get("published_at").getAsString() : "";
					String body = release.has("body") && !release.get("body").isJsonNull() ? release.get("body").getAsString() : "";
					read.add(new Release(release.get("tag_name").getAsString(), date.length() >= 10 ? date.substring(0, 10) : date, notes(body)));
				}
				releases = read;
			} catch (RuntimeException e) {
				MistyClient.LOG.warn("Patch notes not readable", e);
				failed = true;
			} finally {
				loading = false;
			}
		});
	}

	/** The notes' markdown as plain lines: "- **Kills** module" → "• Kills module". */
	private static List<String> notes(final String body) {
		List<String> lines = new ArrayList<>();
		for (String line : body.replace("\r", "").split("\n")) {
			String text = line.strip().replace("**", "").replace("`", "");
			if (text.isEmpty() || text.startsWith("#") && text.replace("#", "").isBlank()) {
				continue;
			}
			if (text.startsWith("- ") || text.startsWith("* ")) {
				text = "• " + text.substring(2);
			} else if (text.startsWith("#")) {
				text = text.replaceFirst("^#+\\s*", "");
			}
			lines.add(text);
		}
		return lines;
	}

	/** The lines to draw, wrapped to the width: a version's title, then its notes. */
	private record Row(FormattedCharSequence text, int color, int indent, boolean title) {
	}

	private List<Row> rows(final int width) {
		List<Row> rows = new ArrayList<>();
		List<Release> list = releases;
		if (list == null) {
			return rows;
		}
		for (Release release : list) {
			String current = MistyClient.version();
			boolean installed = current.equals(release.version()) || current.startsWith(release.version() + "+");
			Component title = Component.literal(release.version()).withColor(Ui.ACCENT & 0xFFFFFF)
				.append(Component.literal("   " + release.date()).withColor(Ui.DIM & 0xFFFFFF));
			if (installed) {
				title = title.copy().append(Component.literal("   ").append(Component.translatable("misty.patchNotes.installed")).withColor(Ui.MUTED & 0xFFFFFF));
			}
			rows.add(new Row(title.getVisualOrderText(), Ui.TEXT, 0, true));
			for (String note : release.notes()) {
				boolean bullet = note.startsWith("• ");
				int indent = bullet ? 8 : 0;
				List<FormattedCharSequence> wrapped = this.font.split(Component.literal(bullet ? note.substring(2) : note), width - 8 - indent);
				for (int i = 0; i < wrapped.size(); i++) {
					rows.add(new Row(wrapped.get(i), Ui.TEXT, indent, false));
					if (bullet && i == 0) {
						rows.set(rows.size() - 1, new Row(wrapped.get(i), Ui.TEXT, -indent, false));
					}
				}
			}
			rows.add(new Row(FormattedCharSequence.EMPTY, 0, 0, false));
		}
		return rows;
	}

	@Override
	public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
		this.scroll -= scrollY * LINE * 3;
		return true;
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

		int x = fx + PAD;
		int width = this.frameW() - 2 * PAD;
		int top = this.listTop();
		int bottom = this.listBottom();
		if (releases == null) {
			Component state = Component.translatable(failed ? "misty.patchNotes.failed" : "misty.patchNotes.loading");
			Ui.centered(graphics, this.font, state, fx + this.frameW() / 2, (top + bottom) / 2 - 4, Ui.MUTED);
			return;
		}
		List<Row> rows = this.rows(width);
		int total = rows.size() * LINE;
		this.scroll = Mth.clamp(this.scroll, 0.0, Math.max(0, total - (bottom - top)));
		graphics.enableScissor(x, top, x + width, bottom);
		int y = top - (int)this.scroll;
		for (Row row : rows) {
			if (y + LINE > top && y < bottom) {
				if (row.indent() < 0) {
					// a bullet: the dot, then the line
					graphics.text(this.font, "•", x - row.indent() - 7, y, Ui.ACCENT, false);
					graphics.text(this.font, row.text(), x - row.indent(), y, row.color(), false);
				} else {
					graphics.text(this.font, row.text(), x + row.indent(), y, row.color(), false);
				}
			}
			y += LINE;
		}
		graphics.disableScissor();
		// the scroll bar, when everything does not fit
		if (total > bottom - top) {
			int barH = Math.max(12, (bottom - top) * (bottom - top) / total);
			int barY = top + (int)((bottom - top - barH) * this.scroll / (total - (bottom - top)));
			graphics.fill(x + width + 3, barY, x + width + 5, barY + barH, Ui.alpha(Ui.ACCENT, 0.7F));
		}
	}
}
