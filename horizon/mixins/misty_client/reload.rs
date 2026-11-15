// The resources were reloaded: the image slot was cleared, so the cape is sent again.
fn misty_client_reloaded(game: &mut crate::game::Game<'_>) {
    let _ = game;
    crate::game::misty_client::cape_uploaded(None);
}
