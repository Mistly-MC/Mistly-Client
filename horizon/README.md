# Misty Client for Horizon

Misty Client for [Horizon](https://playhorizon.dev), Minecraft: Java Edition 26.3 rewritten in Rust. Horizon has no Java, so the Fabric mod can't run in it: this is the same idea as a Horizon **native mod**, a [horizon-mixins](https://github.com/horizon-rust/horizon-mixins) bundle that is compiled into the game.

- **HUD** in a corner of the screen: FPS, ping (coloured by latency), CPS, reach (coloured by distance), combo, coordinates, keystrokes and clock
- **Misty cape**: the cape equipped in the Misty launcher (Misty cape or early cape), on your player in third person

Settings: Options > Mods > Misty Client > Settings. Each module turns on or off, and buttons choose the corner, the colours and whether the cape is shown.

## Status

First version. It applies with horizon-mixins and the game compiles with it (`cargo check -p horizon-client` on Nightly 2026-11-13, `9f42064`), but it has not been played yet. Compared with the Fabric mod:

- The HUD sits in a corner, like Horizon's own HUD mods, instead of the free editor (Right Shift) of the Fabric mod.
- The combo counts the hits on other players, and a hit received or 3 seconds without hitting ends it. Horizon only tells a mod about the attack, not whether it landed.
- The cape is drawn on your own player only. Seeing the capes of the other Misty players needs the Misty network (proving the Minecraft account), which is not in this version yet. A Mojang cape still draws under the Misty cape.
- No armor module, Misty main menu, Misty badge, elytra or hat yet.
- Horizon's own cosmetics rules don't allow capes in Horizon's cosmetics service. This cape is drawn by this mod on your screen only and asks nothing of Horizon's service.

## How it fits together

| File | What it is |
|---|---|
| `mixins/misty_client/horizon-mixin.toml` | The bundle: one module and the hooks |
| `mixins/misty_client/runtime.rs` | The mod's logic and state, added to the game as `game::misty_client`; also this crate's library, for its tests |
| `mixins/misty_client/*.rs` | One small helper per hook |
| `mixins/misty_client/data/cape/` | The cape textures, compiled into the game (the same files as the Fabric mod's) |
| `compatibility.json` | The Horizon Nightly and horizon-mixins commit it is made for |

The hooks it uses: `mods`, `settings` and `settings_click` (Options > Mods), `hud` (the HUD), `native_client_attack` and `native_client_hurt` (combo and reach), `native_client_tick_post` and `client_resources_reloaded` (the cape texture, sent to the game's mod image slot), `player_render_listened` and `mob_layer` (the cape on the player).

It reads two things that are not stable hook points: the player list, for the ping, and the cape chosen in the launcher, from `client.json` in Misty's data folder (`MISTY_LAUNCHER_DIR`, else `%LOCALAPPDATA%\Misty`). So it is pinned to one Horizon Nightly (`horizon_revision`).

## Build

```sh
cargo test     # the logic, without Horizon
```

To build the game with it, give this folder to the Horizon launcher (`horizon-launcher native-build horizon/mixins --trust-native-code`), or apply it by hand on the pinned Horizon commit with horizon-mixins (`plan`, `apply`, then `cargo build -p horizon-client`), as described in Horizon's [native mods](https://github.com/horizon-rust/horizon/blob/main/docs/native-mods.md).

## License

[Misty License](../LICENSE.txt), like the rest of Misty Client.
