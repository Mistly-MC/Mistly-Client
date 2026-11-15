// A click on the mod's settings page: change the setting and keep it.
fn misty_client_click(game: &mut crate::game::Game<'_>, mod_id: &str, button: usize) {
    use crate::game::misty_client as m;
    if mod_id != m::ID {
        return;
    }
    let mut settings = m::settings(|key| game.native_setting(m::ID, key));
    settings.click(button);
    for (key, value) in settings.stored() {
        game.native_set_setting(m::ID, key, &value);
    }
}
