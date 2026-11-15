// The mod's row in Options > Mods.
fn misty_client_listed(mods: &mut Vec<crate::game::native_hooks::NativeMod>) {
    use crate::game::misty_client as m;
    mods.push(crate::game::native_hooks::NativeMod {
        id: m::ID.into(),
        title: format!("{} {}", m::NAME, m::VERSION),
        description: m::DESCRIPTION.into(),
        keys: Vec::new(),
    });
}
