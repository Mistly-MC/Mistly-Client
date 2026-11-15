// Misty Client for Horizon: the mod's logic and state, added to the game as
// `game::misty_client`. It is also this crate's library, for its tests, so it
// only uses the standard library: the helpers next to it read the game and
// hand plain values in.
//
// No inner doc comments, `mod` files or `include!` here: horizon-mixins copies
// this file into the game's source as one inline module.

use std::path::PathBuf;
use std::sync::Mutex;
use std::time::{Duration, Instant, SystemTime};

pub const ID: &str = "misty_client";
pub const NAME: &str = "Misty Client";
pub const VERSION: &str = "0.1.0";
pub const DESCRIPTION: &str =
    "Misty's HUD (FPS, ping, CPS, reach, combo, coordinates, keystrokes, clock) and the Misty cape.";

/// Box behind the HUD lines (ARGB).
pub const BACKGROUND: u32 = 0x9010_1420;
/// A key that is held, in the keystrokes block (ARGB).
pub const KEY_DOWN: u32 = 0xD0FF_FFFF;
/// Text (RGB).
pub const INK: u32 = 0xFF_FFFF;
/// Text on a held key (RGB).
pub const INK_DOWN: u32 = 0x10_1420;
/// Height of one HUD line, in GUI pixels.
pub const LINE: f32 = 11.0;

/// A combo ends 3 seconds after the last hit, as in the Fabric mod.
const COMBO_TIMEOUT: Duration = Duration::from_secs(3);
/// How often the cosmetic chosen in the launcher is read again.
const COSMETIC_REFRESH: Duration = Duration::from_secs(2);

// ───────────────────────── HUD modules and settings ─────────────────────────

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Module {
    Fps,
    Ping,
    Cps,
    Reach,
    Combo,
    Coords,
    Keys,
    Clock,
}

impl Module {
    pub const ALL: [Module; 8] = [
        Module::Fps,
        Module::Ping,
        Module::Cps,
        Module::Reach,
        Module::Combo,
        Module::Coords,
        Module::Keys,
        Module::Clock,
    ];

    /// The key its setting is stored under.
    pub fn key(self) -> &'static str {
        match self {
            Module::Fps => "fps",
            Module::Ping => "ping",
            Module::Cps => "cps",
            Module::Reach => "reach",
            Module::Combo => "combo",
            Module::Coords => "coords",
            Module::Keys => "keys",
            Module::Clock => "clock",
        }
    }

    pub fn label(self) -> &'static str {
        match self {
            Module::Fps => "FPS",
            Module::Ping => "Ping",
            Module::Cps => "CPS",
            Module::Reach => "Reach",
            Module::Combo => "Combo",
            Module::Coords => "Coordinates",
            Module::Keys => "Keystrokes",
            Module::Clock => "Clock",
        }
    }

    fn on_by_default(self) -> bool {
        !matches!(self, Module::Coords | Module::Clock)
    }
}

pub const CORNERS: [&str; 4] = ["Top left", "Top right", "Bottom left", "Bottom right"];

#[derive(Clone, Copy, Debug, PartialEq)]
pub struct Settings {
    /// One per `Module::ALL`.
    pub on: [bool; 8],
    /// Index in `CORNERS`.
    pub corner: usize,
    /// Ping and reach change colour with their value.
    pub colours: bool,
    /// The Misty cape equipped in the launcher is drawn.
    pub cape: bool,
}

impl Default for Settings {
    fn default() -> Self {
        Settings { on: Module::ALL.map(Module::on_by_default), corner: 0, colours: true, cape: true }
    }
}

fn flag(value: Option<String>, default: bool) -> bool {
    match value.as_deref() {
        Some("1") | Some("true") => true,
        Some("0") | Some("false") => false,
        _ => default,
    }
}

/// The settings as the game stored them (`read` is `native_setting`).
pub fn settings(read: impl Fn(&str) -> Option<String>) -> Settings {
    let defaults = Settings::default();
    let mut s = defaults;
    for (i, m) in Module::ALL.iter().enumerate() {
        s.on[i] = flag(read(m.key()), defaults.on[i]);
    }
    s.corner = read("corner").and_then(|c| c.parse().ok()).filter(|c: &usize| *c < CORNERS.len()).unwrap_or(0);
    s.colours = flag(read("colours"), true);
    s.cape = flag(read("cape"), true);
    s
}

impl Settings {
    pub fn shows(&self, m: Module) -> bool {
        Module::ALL.iter().position(|x| *x == m).is_some_and(|i| self.on[i])
    }

    /// The buttons of the settings page (Options > Mods > Misty Client > Settings).
    pub fn labels(&self) -> Vec<String> {
        let mut labels: Vec<String> = Module::ALL
            .iter()
            .zip(self.on)
            .map(|(m, on)| format!("{}: {}", m.label(), if on { "On" } else { "Off" }))
            .collect();
        labels.push(format!("Corner: {}", CORNERS[self.corner]));
        labels.push(format!("Colours: {}", if self.colours { "On" } else { "Off" }));
        labels.push(format!("Misty cape: {}", if self.cape { "Shown" } else { "Hidden" }));
        labels
    }

    /// A click on button `button` of the page.
    pub fn click(&mut self, button: usize) {
        let n = Module::ALL.len();
        match button {
            b if b < n => self.on[b] = !self.on[b],
            b if b == n => self.corner = (self.corner + 1) % CORNERS.len(),
            b if b == n + 1 => self.colours = !self.colours,
            b if b == n + 2 => self.cape = !self.cape,
            _ => {}
        }
    }

    /// What to store, by key (`native_set_setting`).
    pub fn stored(&self) -> Vec<(&'static str, String)> {
        let bit = |b: bool| if b { "1" } else { "0" }.to_string();
        let mut out: Vec<(&'static str, String)> =
            Module::ALL.iter().zip(self.on).map(|(m, on)| (m.key(), bit(on))).collect();
        out.push(("corner", self.corner.to_string()));
        out.push(("colours", bit(self.colours)));
        out.push(("cape", bit(self.cape)));
        out
    }
}

// ───────────────────────── Colours ─────────────────────────

const GREEN: u32 = 0x55_FF55;
const YELLOW: u32 = 0xFF_FF55;
const ORANGE: u32 = 0xFF_AA00;
const RED: u32 = 0xFF_5555;

fn mix(a: u32, b: u32, t: f64) -> u32 {
    let t = t.clamp(0.0, 1.0);
    let channel = |shift: u32| {
        let (x, y) = (f64::from((a >> shift) & 0xFF), f64::from((b >> shift) & 0xFF));
        ((x + (y - x) * t).round() as u32) << shift
    };
    channel(16) | channel(8) | channel(0)
}

/// Green under 80 ms, yellow under 150, orange under 250, red above.
pub fn ping_colour(ms: i32) -> u32 {
    match ms {
        ..80 => GREEN,
        80..150 => YELLOW,
        150..250 => ORANGE,
        _ => RED,
    }
}

/// Green up to 2 blocks, yellow around 2.6, red from 3.
pub fn reach_colour(blocks: f64) -> u32 {
    if blocks <= 2.0 {
        GREEN
    } else if blocks <= 2.6 {
        mix(GREEN, YELLOW, (blocks - 2.0) / 0.6)
    } else {
        mix(YELLOW, RED, (blocks - 2.6) / 0.4)
    }
}

// ───────────────────────── Combat: combo and reach ─────────────────────────

#[derive(Default)]
struct Combat {
    combo: u32,
    last_hit: Option<Instant>,
    reach: Option<f64>,
}

static COMBAT: Mutex<Combat> = Mutex::new(Combat { combo: 0, last_hit: None, reach: None });

/// Distance from `eye` to the closest point of the box `min`..`max`, in blocks.
pub fn distance_to_box(eye: [f64; 3], min: [f64; 3], max: [f64; 3]) -> f64 {
    let mut sum = 0.0;
    for i in 0..3 {
        let d = (min[i] - eye[i]).max(0.0).max(eye[i] - max[i]);
        sum += d * d;
    }
    sum.sqrt()
}

/// The local player hit another player, `reach` blocks away when the game
/// could tell. Hits more than 3 seconds apart start a new combo.
pub fn on_attack_player(reach: Option<f64>, now: Instant) {
    let mut c = COMBAT.lock().unwrap_or_else(|e| e.into_inner());
    let going = c.last_hit.is_some_and(|t| now.duration_since(t) < COMBO_TIMEOUT);
    c.combo = if going { c.combo + 1 } else { 1 };
    c.last_hit = Some(now);
    if let Some(r) = reach.filter(|r| r.is_finite()) {
        c.reach = Some(r);
    }
}

/// The local player was hurt: the combo ends.
pub fn on_hurt() {
    let mut c = COMBAT.lock().unwrap_or_else(|e| e.into_inner());
    c.combo = 0;
    c.last_hit = None;
}

/// The current combo (0 once it timed out) and the last reach.
pub fn combat(now: Instant) -> (u32, Option<f64>) {
    let c = COMBAT.lock().unwrap_or_else(|e| e.into_inner());
    let combo = if c.last_hit.is_some_and(|t| now.duration_since(t) < COMBO_TIMEOUT) { c.combo } else { 0 };
    (combo, c.reach)
}

// ───────────────────────── Ping and sneaking ─────────────────────────

/// The ping is read from the player list, which the game rebuilds when asked:
/// once a second is enough, as the server sends it only every few seconds.
const PING_REFRESH: Duration = Duration::from_secs(1);

static PING: Mutex<(Option<Instant>, Option<i32>)> = Mutex::new((None, None));

/// The local player's ping, `read` from the player list at most once a second.
pub fn ping(now: Instant, read: impl FnOnce() -> Option<i32>) -> Option<i32> {
    let mut p = PING.lock().unwrap_or_else(|e| e.into_inner());
    if p.0.is_none_or(|t| now.duration_since(t) >= PING_REFRESH) {
        *p = (Some(now), read());
    }
    p.1
}

/// The local player holds sneak: the HUD reads it each frame, the cape leans with it.
static SNEAKING: std::sync::atomic::AtomicBool = std::sync::atomic::AtomicBool::new(false);

pub fn set_sneaking(down: bool) {
    SNEAKING.store(down, std::sync::atomic::Ordering::Relaxed);
}

pub fn sneaking() -> bool {
    SNEAKING.load(std::sync::atomic::Ordering::Relaxed)
}

// ───────────────────────── HUD lines ─────────────────────────

/// What the HUD shows this frame, read by the helper.
#[derive(Clone, Debug, Default)]
pub struct View {
    pub fps: u32,
    /// Milliseconds, None in single player or before the server sent it.
    pub ping: Option<i32>,
    /// Left and right clicks in the last second.
    pub cps: (u32, u32),
    pub reach: Option<f64>,
    pub combo: u32,
    pub coords: Option<[f64; 3]>,
    /// "14:32".
    pub clock: String,
}

/// The text lines of the HUD box, with their colour (RGB).
pub fn lines(view: &View, s: &Settings) -> Vec<(String, u32)> {
    let mut out = Vec::new();
    for m in Module::ALL {
        if !s.shows(m) {
            continue;
        }
        match m {
            Module::Fps => out.push((format!("{} FPS", view.fps), INK)),
            Module::Ping => {
                if let Some(ms) = view.ping.filter(|ms| *ms >= 0) {
                    out.push((format!("{ms} ms"), if s.colours { ping_colour(ms) } else { INK }));
                }
            }
            Module::Cps => out.push((format!("{} | {} CPS", view.cps.0, view.cps.1), INK)),
            Module::Reach => {
                let (text, colour) = match view.reach {
                    Some(r) => (format!("{r:.2} blocks"), if s.colours { reach_colour(r) } else { INK }),
                    None => ("- blocks".to_string(), INK),
                };
                out.push((text, colour));
            }
            Module::Combo => out.push((format!("{} combo", view.combo), INK)),
            Module::Coords => {
                if let Some([x, y, z]) = view.coords {
                    out.push((format!("{:.0} {:.0} {:.0}", x.floor(), y.floor(), z.floor()), INK));
                }
            }
            Module::Clock => out.push((view.clock.clone(), INK)),
            // drawn as its own block
            Module::Keys => {}
        }
    }
    out
}

/// Something to draw, relative to a block's top left corner.
#[derive(Clone, Debug, PartialEq)]
pub enum Shape {
    /// `[x, y, w, h]`, ARGB.
    Fill([f32; 4], u32),
    /// The text, where it starts, RGB.
    Text(String, [f32; 2], u32),
}

/// Size of the keystrokes block.
pub const KEYS_SIZE: (f32, f32) = (62.0, 62.0);

/// The keystrokes block: W, A S D, then the two mouse buttons. `down` is, in
/// that order, forward, left, back, right, attack, use. `width` measures text.
pub fn keys(down: [bool; 6], labels: [&str; 4], width: &dyn Fn(&str) -> f32) -> Vec<Shape> {
    let key = 20.0;
    let gap = 1.0;
    let mut out = Vec::new();
    let mut add = |x: f32, y: f32, w: f32, text: &str, held: bool| {
        out.push(Shape::Fill([x, y, w, key], if held { KEY_DOWN } else { BACKGROUND }));
        let tx = x + (w - width(text)) / 2.0;
        out.push(Shape::Text(text.to_string(), [tx, y + 6.0], if held { INK_DOWN } else { INK }));
    };
    add(key + gap, 0.0, key, labels[0], down[0]);
    add(0.0, key + gap, key, labels[1], down[1]);
    add(key + gap, key + gap, key, labels[2], down[2]);
    add(2.0 * (key + gap), key + gap, key, labels[3], down[3]);
    let half = (3.0 * key + 2.0 * gap - gap) / 2.0;
    add(0.0, 2.0 * (key + gap), half, "LMB", down[4]);
    add(half + gap, 2.0 * (key + gap), half, "RMB", down[5]);
    out
}

// ───────────────────────── The Misty cape ─────────────────────────

/// The capes Misty has, as the launcher names them in `client.json`.
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Cape {
    Misty,
    Early,
}

impl Cape {
    /// Its texture's file in the bundle's `data/` folder.
    pub fn file(self) -> &'static str {
        match self {
            Cape::Misty => "cape/misty.png",
            Cape::Early => "cape/early.png",
        }
    }
}

/// The cape in the launcher's `client.json` (`{"cosmetic": "misty_cape"}`).
/// Elytra and hats have no Horizon version yet: no cape for them.
pub fn cape_from_json(text: &str) -> Option<Cape> {
    let after = &text[text.find("\"cosmetic\"")? + "\"cosmetic\"".len()..];
    let after = after.trim_start().strip_prefix(':')?.trim_start().strip_prefix('"')?;
    match &after[..after.find('"')?] {
        "misty_cape" => Some(Cape::Misty),
        "early_cape" => Some(Cape::Early),
        _ => None,
    }
}

/// Misty's data folder, where the launcher writes `client.json`: the one the
/// launcher names (`MISTY_LAUNCHER_DIR`), else `%LOCALAPPDATA%\Misty`.
fn launcher_dir() -> Option<PathBuf> {
    if let Some(d) = std::env::var_os("MISTY_LAUNCHER_DIR") {
        return Some(PathBuf::from(d));
    }
    std::env::var_os("LOCALAPPDATA").map(|d| PathBuf::from(d).join("Misty"))
}

struct Wardrobe {
    cape: Option<Cape>,
    read_at: Option<Instant>,
    modified: Option<SystemTime>,
    /// The cape whose texture is in the game's image slot.
    uploaded: Option<Cape>,
}

static WARDROBE: Mutex<Wardrobe> = Mutex::new(Wardrobe { cape: None, read_at: None, modified: None, uploaded: None });

/// The cape equipped in the launcher, read again every 2 seconds when the
/// file changed, so a cape equipped in Misty shows without restarting.
pub fn equipped_cape(now: Instant) -> Option<Cape> {
    let mut w = WARDROBE.lock().unwrap_or_else(|e| e.into_inner());
    if w.read_at.is_none_or(|t| now.duration_since(t) >= COSMETIC_REFRESH) {
        w.read_at = Some(now);
        if let Some(path) = launcher_dir().map(|d| d.join("client.json")) {
            let modified = std::fs::metadata(&path).and_then(|m| m.modified()).ok();
            if modified != w.modified {
                w.modified = modified;
                w.cape = std::fs::read_to_string(&path).ok().as_deref().and_then(cape_from_json);
            }
        }
    }
    w.cape
}

/// Which cape texture has to be sent to the game, if any.
pub fn cape_to_upload(cape: Cape) -> bool {
    WARDROBE.lock().unwrap_or_else(|e| e.into_inner()).uploaded != Some(cape)
}

pub fn cape_uploaded(cape: Option<Cape>) {
    WARDROBE.lock().unwrap_or_else(|e| e.into_inner()).uploaded = cape;
}

/// The texture in the game's image slot, if it is the cape's.
pub fn uploaded_cape() -> Option<Cape> {
    WARDROBE.lock().unwrap_or_else(|e| e.into_inner()).uploaded
}

/// One face of the cape: its corners in blocks (top left, bottom left, bottom
/// right, top right, seen from its front) and its rectangle in the 64 × 32
/// cape texture, in pixels.
#[derive(Clone, Copy, Debug, PartialEq)]
pub struct Face {
    pub corners: [[f32; 3]; 4],
    pub uv: [f32; 4],
}

/// The cape's six faces in the player's frame (at the feet, +z the way the
/// body faces, +x its left, +y up), as vanilla's cape: 10 × 16 × 1 pixels of
/// the player model, hung from the shoulders, swinging back as the player
/// walks. `walk_speed` is 0 standing to about 1; `walk_position` drives the
/// sway; `sneaking` leans the body forward.
pub fn cape_faces(walk_speed: f32, walk_position: f32, sneaking: bool) -> [Face; 6] {
    // A player model pixel, in blocks (vanilla draws players at 15/16).
    let px = 0.9375 / 16.0;
    let (w, h, t) = (10.0 * px, 16.0 * px, 1.0 * px);
    // Hinge: the top of the back, just behind the body (4 pixels deep).
    let hinge = [0.0, if sneaking { 20.0 } else { 24.0 } * px, -2.0 * px];
    // How far the cape swings back from hanging straight down.
    let speed = walk_speed.clamp(0.0, 1.0);
    let sway = (walk_position * 0.6662).sin() * 3.0 * speed;
    let degrees = 6.0 + 70.0 * speed + sway + if sneaking { 25.0 } else { 0.0 };
    let (sin, cos) = degrees.to_radians().sin_cos();
    // Local (x, y, z): y down from the hinge (negative), z backwards (negative).
    let place = |x: f32, y: f32, z: f32| {
        // turn about x: the bottom moves backwards (-z)
        let (ry, rz) = (y * cos - z * sin, y * sin + z * cos);
        [hinge[0] + x, hinge[1] + ry, hinge[2] + rz]
    };
    let (l, r) = (w / 2.0, -w / 2.0);
    [
        // outside, facing back (seen from behind the player, +x is on the left)
        Face { corners: [place(l, 0.0, -t), place(l, -h, -t), place(r, -h, -t), place(r, 0.0, -t)], uv: [1.0, 1.0, 11.0, 17.0] },
        // inside, against the back
        Face { corners: [place(r, 0.0, 0.0), place(r, -h, 0.0), place(l, -h, 0.0), place(l, 0.0, 0.0)], uv: [12.0, 1.0, 22.0, 17.0] },
        // top
        Face { corners: [place(l, 0.0, 0.0), place(l, 0.0, -t), place(r, 0.0, -t), place(r, 0.0, 0.0)], uv: [1.0, 0.0, 11.0, 1.0] },
        // bottom
        Face { corners: [place(l, -h, -t), place(l, -h, 0.0), place(r, -h, 0.0), place(r, -h, -t)], uv: [11.0, 0.0, 21.0, 1.0] },
        // the player's left edge
        Face { corners: [place(l, 0.0, 0.0), place(l, -h, 0.0), place(l, -h, -t), place(l, 0.0, -t)], uv: [0.0, 1.0, 1.0, 17.0] },
        // the player's right edge
        Face { corners: [place(r, 0.0, -t), place(r, -h, -t), place(r, -h, 0.0), place(r, 0.0, 0.0)], uv: [11.0, 1.0, 12.0, 17.0] },
    ]
}

/// A cape texture rectangle (pixels of the 64 × 32 image, uploaded at the top
/// left of a `slot`-pixel square) in the texels `native_draw_quad` takes: 16
/// per side of the sprite.
pub fn slot_uv(uv: [f32; 4], slot: f32) -> [f32; 4] {
    uv.map(|p| p * 16.0 / slot)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn settings_round_trip() {
        let mut s = Settings::default();
        s.click(0);
        s.click(Module::ALL.len());
        s.click(Module::ALL.len() + 2);
        let stored = s.stored();
        let back = settings(|k| stored.iter().find(|(key, _)| *key == k).map(|(_, v)| v.clone()));
        assert_eq!(back, s);
        assert!(!back.shows(Module::Fps));
        assert_eq!(back.corner, 1);
        assert!(!back.cape);
    }

    #[test]
    fn labels_match_buttons() {
        let s = Settings::default();
        assert_eq!(s.labels().len(), Module::ALL.len() + 3);
        assert_eq!(s.labels()[0], "FPS: On");
    }

    #[test]
    fn colours() {
        assert_eq!(ping_colour(40), GREEN);
        assert_eq!(ping_colour(120), YELLOW);
        assert_eq!(ping_colour(200), ORANGE);
        assert_eq!(ping_colour(400), RED);
        assert_eq!(reach_colour(1.5), GREEN);
        assert_eq!(reach_colour(2.6), YELLOW);
        assert_eq!(reach_colour(3.5), RED);
    }

    #[test]
    fn reach_to_a_box() {
        assert_eq!(distance_to_box([0.0, 1.6, 0.0], [2.7, 0.0, -0.3], [3.3, 1.8, 0.3]), 2.7);
        assert_eq!(distance_to_box([0.0, 0.0, 0.0], [-1.0, -1.0, -1.0], [1.0, 1.0, 1.0]), 0.0);
    }

    #[test]
    fn combo_counts_and_ends() {
        let t = Instant::now();
        on_hurt();
        on_attack_player(Some(2.5), t);
        on_attack_player(None, t + Duration::from_millis(500));
        assert_eq!(combat(t + Duration::from_secs(1)), (2, Some(2.5)));
        assert_eq!(combat(t + Duration::from_secs(5)).0, 0);
        on_attack_player(None, t + Duration::from_secs(1));
        on_hurt();
        assert_eq!(combat(t + Duration::from_secs(1)).0, 0);
    }

    #[test]
    fn cape_from_launcher() {
        assert_eq!(cape_from_json(r#"{"cosmetic": "misty_cape", "hat": ""}"#), Some(Cape::Misty));
        assert_eq!(cape_from_json(r#"{"cosmetic":"early_cape"}"#), Some(Cape::Early));
        assert_eq!(cape_from_json(r#"{"cosmetic": "misty_elytra"}"#), None);
        assert_eq!(cape_from_json(r#"{"cosmetic": ""}"#), None);
        assert_eq!(cape_from_json("{}"), None);
    }

    #[test]
    fn cape_hangs_behind_the_back() {
        let faces = cape_faces(0.0, 0.0, false);
        for f in faces {
            for c in f.corners {
                assert!(c[2] <= 0.0, "the cape stays behind the player: {c:?}");
                assert!(c[1] > 0.4 && c[1] < 1.5, "between the shoulders and the knees: {c:?}");
            }
        }
        // walking swings the bottom further back
        let still = cape_faces(0.0, 0.0, false)[0].corners[1][2];
        let walking = cape_faces(1.0, 0.0, false)[0].corners[1][2];
        assert!(walking < still);
    }

    #[test]
    fn keys_block_fits() {
        let shapes = keys([true, false, false, false, false, false], ["W", "A", "S", "D"], &|t| t.len() as f32 * 6.0);
        for s in &shapes {
            if let Shape::Fill([x, y, w, h], _) = s {
                assert!(x + w <= KEYS_SIZE.0 + 0.01 && y + h <= KEYS_SIZE.1 + 0.01);
            }
        }
        assert_eq!(shapes[0], Shape::Fill([21.0, 0.0, 20.0, 20.0], KEY_DOWN));
    }
}
