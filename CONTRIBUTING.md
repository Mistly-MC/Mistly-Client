# Contributing to Mistly Client

Thanks for wanting to help! This repository is the Fabric mod that adds Mistly's in-game features: the Mistly menu, the HUD, the player badge and the cosmetics. The launcher lives in [Mistly-Launcher](https://github.com/Mistly-MC/Mistly-Launcher) and the website in [Mistly-Web](https://github.com/Mistly-MC/Mistly-Web).

By taking part, you agree to follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Ways to help

- **Report a bug**: open an issue with the Minecraft version, what you did, what you expected and what happened. The launcher console's **Send to the team** button gives a code you can paste in the issue.
- **Fix a translation**: the texts are in `src/main/resources/assets/misty/lang/` (`en_us.json` is the reference).
- **Fix or add a feature**: for anything bigger than a small fix, open an issue (or ask on [Discord](https://discord.gg/rdQJUgvUh3)) before you start, so that we can agree on the idea first.

## Build and run

You need JDK 25.

```bash
git clone https://github.com/Mistly-MC/Mistly-Client.git
cd Mistly-Client
./gradlew build      # one jar per Minecraft version in build/libs/
./gradlew runClient  # start the game with the mod
```

The mod is built for several Minecraft versions from one source with [Stonecutter](https://stonecutter.kikugie.dev/): code that differs between versions goes through Stonecutter's conditional comments, never through a copy of the file. To try a local build in the launcher, start it with `MISTY_CLIENT_DIR=<folder of the jars>`.

## Code style

- Package `online.mistylauncher.client`, organised in `config/`, `net/`, `cosmetics/`, `social/`, `hud/`, `menu/`, `ui/`, `features/`, `mixin/`.
- In-game texts are never hard-coded: add the key to **every** file in `lang/`.
- If a feature can't work on one Minecraft version, turn it off for that version only (see `compat/`) instead of crashing the game.
- The Mistly network code (`net/`) never sends the Minecraft token out of the game, and never retries Mojang in a loop.

## Pull requests

1. Fork the repository and create a branch from `main`.
2. Keep each pull request focused on one change.
3. Write commit messages in English, in the imperative, without a final period: `Add a Combo HUD module that counts landed hits`.
4. Describe what you changed, on which Minecraft versions you tested it, with a screenshot for visual changes.
5. A team member reviews it. Players get changes when a release is published, so only the team merges and publishes.

## License

Mistly Client is distributed under the Misty License 1.0 (`LICENSE.txt`): free to use, modify and share, but not to sell. By submitting a contribution, you agree that it is distributed under this license.
