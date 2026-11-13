// Each client tick: send the cape chosen in the Misty launcher to the game's
// image slot when it changed (client.json is read every 2 seconds).
fn misty_client_tick(game: &mut crate::game::Game<'_>) {
    use crate::game::misty_client as m;
    if !game.native_on(m::ID) {
        return;
    }
    let Some(cape) = m::equipped_cape(std::time::Instant::now()) else {
        return;
    };
    if !m::cape_to_upload(cape) {
        return;
    }
    let png: &[u8] = match cape {
        m::Cape::Misty => horizon_client::native_data!("misty_client", "cape/misty.png"),
        m::Cape::Early => horizon_client::native_data!("misty_client", "cape/early.png"),
    };
    // The texture, as RGBA pixels.
    let decode = |bytes: &[u8]| -> Option<(u32, u32, Vec<u8>)> {
        let mut decoder = png::Decoder::new(std::io::Cursor::new(bytes));
        decoder.set_transformations(png::Transformations::EXPAND | png::Transformations::STRIP_16);
        let mut reader = decoder.read_info().ok()?;
        let mut buffer = vec![0; reader.output_buffer_size()?];
        let info = reader.next_frame(&mut buffer).ok()?;
        let pixels = &buffer[..info.buffer_size()];
        let rgba = match info.color_type {
            png::ColorType::Rgba => pixels.to_vec(),
            png::ColorType::Rgb => pixels.chunks_exact(3).flat_map(|p| [p[0], p[1], p[2], 0xFF]).collect(),
            _ => return None,
        };
        Some((info.width, info.height, rgba))
    };
    if let Some((width, height, rgba)) = decode(png)
        && game.native_mod_image(width, height, &rgba)
    {
        m::cape_uploaded(Some(cape));
    }
}
