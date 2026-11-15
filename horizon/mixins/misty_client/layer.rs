// The Misty cape on the local player, after its vanilla model: six faces
// textured from the cape uploaded in the game's image slot.
fn misty_client_cape(
    game: &crate::game::Game<'_>,
    mob: &crate::game::native_mob_draw::NativeMobRender<'_>,
    who: Option<&crate::game::native_mob_draw::NativePlayerRender<'_>>,
    draw: &mut crate::game::native_block_entity_draw::BlockEntityDraw,
) {
    use crate::game::misty_client as m;
    if !who.is_some_and(|w| w.local) || !game.native_on(m::ID) {
        return;
    }
    if !m::settings(|key| game.native_setting(m::ID, key)).cape {
        return;
    }
    // the cape equipped now, once its texture is in the slot
    let Some(cape) = m::uploaded_cape() else {
        return;
    };
    if m::equipped_cape(std::time::Instant::now()) != Some(cape) {
        return;
    }
    let sprite = horizon_client::hud::MOD_IMAGE_SPRITE;
    let Some([slot, _]) = game.native_sprite_size(sprite) else {
        return;
    };
    for face in m::cape_faces(mob.walk_speed, mob.walk_position, m::sneaking()) {
        game.native_draw_quad(draw, sprite, face.corners, m::slot_uv(face.uv, slot), 0xFF_FFFF);
    }
}
