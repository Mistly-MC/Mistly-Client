// The local player was hurt: the combo ends.
fn misty_client_hurt(player: bool) {
    let _ = player;
    crate::game::misty_client::on_hurt();
}
