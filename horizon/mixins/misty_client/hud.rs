// Misty's HUD in its corner: one box of lines (FPS, ping, CPS, reach, combo,
// coordinates, clock), then the keystrokes block.
fn misty_client_hud(
    game: &mut crate::game::Game<'_>,
    batch: &mut horizon_client::hud::HudBatch,
    corners: &mut horizon_client::extras_hud::Corners,
) {
    use crate::game::misty_client as m;
    // Turned off in Options > Mods: nothing until the next build leaves it out.
    if !game.native_on(m::ID) {
        return;
    }
    // the cape leans when the player sneaks
    let sneak = game.native_mapping("key.sneak").is_some_and(|(down, _)| down);
    m::set_sneaking(sneak);

    use horizon_client::extras::Corner;
    let settings = {
        let game = &*game;
        m::settings(|key| game.native_setting(m::ID, key))
    };
    let corner = [Corner::TopLeft, Corner::TopRight, Corner::BottomLeft, Corner::BottomRight][settings.corner];
    if !game.native_corner_shown(corner) {
        return;
    }
    let now = std::time::Instant::now();
    let (combo, reach) = m::combat(now);
    let ping = if settings.shows(m::Module::Ping) {
        m::ping(now, || {
            // this player's line in the player list
            let name = game.remote.as_ref()?.profile_name.clone();
            let (players, _, _) = game.remote_tab_players()?;
            players.into_iter().find(|p| p.name == name).map(|p| p.latency)
        })
    } else {
        None
    };
    let view = m::View {
        fps: game.native_frame_rate().0,
        ping,
        cps: game.native_clicks(),
        reach,
        combo,
        coords: game.native_player().map(|p| [p.x, p.y, p.z]),
        clock: chrono::Local::now().format("%H:%M").to_string(),
    };

    let lines = m::lines(&view, &settings);
    if !lines.is_empty() {
        let w = lines.iter().map(|(text, _)| game.native_text_width(text)).fold(0.0_f32, f32::max) + 6.0;
        let h = lines.len() as f32 * m::LINE + 3.0;
        let (x, y) = corners.place(corner, w, h);
        game.native_fill(batch, [x, y, w, h], m::BACKGROUND);
        for (i, (text, rgb)) in lines.iter().enumerate() {
            game.native_text(batch, text, [x + 3.0, y + 2.0 + i as f32 * m::LINE], *rgb, true);
        }
    }

    if settings.shows(m::Module::Keys) {
        let mut held = [false; 6];
        let mut labels: [String; 4] = Default::default();
        let names = ["key.forward", "key.left", "key.back", "key.right", "key.attack", "key.use"];
        for (i, name) in names.iter().enumerate() {
            let (down, label) = game.native_mapping(name).unwrap_or_default();
            held[i] = down;
            if i < 4 {
                labels[i] = label;
            }
        }
        let (w, h) = m::KEYS_SIZE;
        let (x, y) = corners.place(corner, w, h);
        let game = &*game;
        let labels = [labels[0].as_str(), labels[1].as_str(), labels[2].as_str(), labels[3].as_str()];
        for shape in m::keys(held, labels, &|text| game.native_text_width(text)) {
            match shape {
                m::Shape::Fill([dx, dy, w, h], argb) => game.native_fill(batch, [x + dx, y + dy, w, h], argb),
                m::Shape::Text(text, [dx, dy], rgb) => game.native_text(batch, &text, [x + dx, y + dy], rgb, false),
            }
        }
    }
}
