package online.mistylauncher.legacy.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/**
 * The patch notes of Mistly Client, as in the Fabric versions: the notes of its last releases on
 * GitHub, newest first, scrolled with the mouse wheel. Read once per game session.
 */
public final class PatchNotesScreen extends MistyScreen {
	private static final String RELEASES = "https://api.github.com/repos/Mistly-MC/Mistly-Client/releases?per_page=15";
	private static final int WIDTH = 360;
	private static final int HEADER = 22;
	private static final int PAD = 10;
	private static final int LINE = 10;

	/** One release: its version, its date (yyyy-mm-dd) and its notes, line by line. */
	private static final class Release {
		final String version;
		final String date;
		final List<String> notes;

		Release(final String version, final String date, final List<String> notes) {
			this.version = version;
			this.date = date;
			this.notes = notes;
		}
	}

	/** A line to draw: its text, its indent (negative: a bullet before it), a version's title. */
	private static final class Row {
		final String text;
		final int indent;
		final boolean title;

		Row(final String text, final int indent, final boolean title) {
			this.text = text;
			this.indent = indent;
			this.title = title;
		}
	}

	private static volatile List<Release> releases;
	private static volatile boolean failed;
	private static volatile boolean loading;

	private int scroll;

	public PatchNotesScreen(final GuiScreen parent) {
		super(parent);
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

	private int listTop() {
		return this.frameY() + HEADER + 6;
	}

	private int listBottom() {
		return this.frameY() + this.frameH() - PAD - 18 - 6;
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		load();
		int x = this.frameX() + PAD;
		int inner = this.frameW() - 2 * PAD;
		this.add(new MistyButton(x, this.frameY() + this.frameH() - PAD - 18, inner, 18, I18n.format("gui.done"), new Runnable() {
			@Override
			public void run() {
				PatchNotesScreen.this.mc.displayGuiScreen(PatchNotesScreen.this.parent);
			}
		}).primary());
	}

	/** Reads the releases in the background, once. */
	private static void load() {
		if (releases != null || loading) {
			return;
		}
		loading = true;
		failed = false;
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpURLConnection http = (HttpURLConnection)new URL(RELEASES).openConnection();
					http.setConnectTimeout(10_000);
					http.setReadTimeout(15_000);
					http.setRequestProperty("Accept", "application/vnd.github+json");
					http.setRequestProperty("User-Agent", "Mistly-Client");
					if (http.getResponseCode() != 200) {
						failed = true;
						return;
					}
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					InputStream in = http.getInputStream();
					try {
						byte[] buffer = new byte[8192];
						int n;
						while ((n = in.read(buffer)) > 0) {
							out.write(buffer, 0, n);
						}
					} finally {
						in.close();
					}
					List<Release> read = new ArrayList<Release>();
					JsonArray list = new JsonParser().parse(new String(out.toByteArray(), StandardCharsets.UTF_8)).getAsJsonArray();
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
				} catch (IOException | RuntimeException e) {
					failed = true;
				} finally {
					loading = false;
				}
			}
		}, "Mistly patch notes");
		thread.setDaemon(true);
		thread.start();
	}

	/** The notes' markdown as plain lines: "- **Kills** module" → "• Kills module". */
	private static List<String> notes(final String body) {
		List<String> lines = new ArrayList<String>();
		for (String line : body.replace("\r", "").split("\n")) {
			String text = line.trim().replace("**", "").replace("`", "");
			if (text.isEmpty() || text.startsWith("#") && text.replace("#", "").trim().isEmpty()) {
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

	private List<Row> rows(final int width) {
		List<Row> rows = new ArrayList<Row>();
		List<Release> list = releases;
		if (list == null) {
			return rows;
		}
		for (Release release : list) {
			rows.add(new Row("§9" + release.version + "§8   " + release.date, 0, true));
			for (String note : release.notes) {
				boolean bullet = note.startsWith("• ");
				int indent = bullet ? 8 : 0;
				List<String> wrapped = this.fontRendererObj.listFormattedStringToWidth(bullet ? note.substring(2) : note, width - 8 - indent);
				for (int i = 0; i < wrapped.size(); i++) {
					rows.add(new Row(wrapped.get(i), bullet && i == 0 ? -indent : indent, false));
				}
			}
			rows.add(new Row("", 0, false));
		}
		return rows;
	}

	@Override
	protected boolean scrolled(final int mouseX, final int mouseY, final int direction) {
		this.scroll -= direction * LINE * 3;
		return true;
	}

	@Override
	protected void drawUnder(final int mouseX, final int mouseY) {
		int fx = this.frameX();
		int fy = this.frameY();
		int fw = this.frameW();
		Ui.card(fx, fy, fw, this.frameH(), 0xF00B1120, 0x33FFFFFF);
		Ui.fill(fx + 1, fy + 1, fx + fw - 1, fy + HEADER, 0xFF111A30);
		Ui.fill(fx + 1, fy + HEADER, fx + fw - 1, fy + HEADER + 1, Ui.LINE);
	}

	@Override
	protected void drawOver(final int mouseX, final int mouseY) {
		int fx = this.frameX();
		int fy = this.frameY();
		Ui.logo(fx + 8, fy + 4, 14);
		Ui.text(I18n.format("misty.patchNotes.title"), fx + 26, fy + (HEADER - 8) / 2, Ui.TEXT);
		int x = fx + PAD;
		int width = this.frameW() - 2 * PAD;
		int top = this.listTop();
		int bottom = this.listBottom();
		if (releases == null) {
			Ui.centered(I18n.format(failed ? "misty.patchNotes.failed" : "misty.patchNotes.loading"), fx + this.frameW() / 2, (top + bottom) / 2 - 4, Ui.MUTED);
			return;
		}
		List<Row> rows = this.rows(width);
		int total = rows.size() * LINE;
		this.scroll = Math.max(0, Math.min(this.scroll, Math.max(0, total - (bottom - top))));
		Ui.scissor(x, top, width, bottom - top);
		int y = top - this.scroll;
		for (Row row : rows) {
			if (y + LINE > top && y < bottom) {
				if (row.indent < 0) {
					// a bullet: the dot, then the line
					Ui.text("•", x - row.indent - 7, y, Ui.ACCENT);
					Ui.text(row.text, x - row.indent, y, Ui.TEXT);
				} else {
					Ui.text(row.text, x + row.indent, y, row.title ? Ui.ACCENT : Ui.TEXT);
				}
			}
			y += LINE;
		}
		Ui.endScissor();
		if (total > bottom - top) {
			int barH = Math.max(12, (bottom - top) * (bottom - top) / total);
			int barY = top + (bottom - top - barH) * this.scroll / (total - (bottom - top));
			Ui.fill(x + width + 3, barY, x + width + 5, barY + barH, Ui.alpha(Ui.ACCENT, 0.7F));
		}
	}
}
