// The local player attacked an entity: a hit on another player counts in the
// combo, and its reach is the distance from the eyes to the target's hitbox.
fn misty_client_attack(game: &mut crate::game::Game<'_>, target: u32, player: bool) {
    use crate::game::misty_client as m;
    if !player || !game.native_on(m::ID) {
        return;
    }
    let eye = game.native_client_state().and_then(|s| s.eye_position);
    let reach = eye.and_then(|eye| {
        let entity = game.native_client_entities().into_iter().find(|e| e.id == target)?;
        Some(m::distance_to_box(eye, entity.bounds[0], entity.bounds[1]))
    });
    m::on_attack_player(reach, std::time::Instant::now());
}
