# Mistly Client for Forge 1.8.9

Mistly Client for **Minecraft 1.8.9 with Forge** (11.15.1.2318), the PvP version. The Fabric mod can't run there (another loader, Java 8, other class names): this is its own small mod, built from Forge events only, without mixins.

It looks like the Fabric versions: the same night theme, textures and texts.

- **Mistly main menu**: the night landscape, the entries along the path of diamonds (Mods in place of Realms), you in 3D with your cosmetics, and the wardrobe. It can be turned off in the settings
- **Settings** (the cloud of the menu, or "Settings" in the HUD editor): General, HUD, Zoom, Fullbright and Cosmetics, each with its animated preview. A module with options has a gear
- **HUD**, movable, in the Mistly style: FPS, ping and reach (coloured), CPS, combo, coordinates (with the direction), keystrokes, armor with durability, clock (seconds, 12 hours, "h" behind its gear), potions counter
- **HUD editor** (Right Shift): drag a module to move it (it sticks to the edges and the centre), click it to turn it on or off, drag its corner or scroll over it to resize it, right-click it to put it back
- **Potions counter**: one slot per kind of potion. The first slot stays where it was placed and each new kind is added on its left, or on its right (behind its gear). "As text" shows "Potions : 5" instead
- **Wardrobe**: the Mistly cape, the early player cape and the Cloudly hat, written for the launcher as the Fabric versions do (`client.json`)
- **Zoom** (C, its level and smoothness in the settings), **fullbright**
- Translations: English, French, Spanish, Italian, Russian

No damage indicators: they count as cheating on servers.

Settings are kept in `config/misty-client.json` of the game folder. Keys: Options > Controls > Mistly Client.

## Status

- **Mistly network**, as in the Fabric versions (same site, same token file): the other Mistly players see your cosmetics and badge, you see theirs, and the early player cape opens for the accounts the site counts. The account is proven with a Mojang "join", as a server does: the Minecraft token never leaves the game
- **Badge**: the Mistly cloud before the Mistly players' names, above their head, in the player list and in the chat. It is a character of the game's font (`U+E000`, in `assets/minecraft/textures/font/unicode_page_e0.png`)
- **Loading screen**: on 1.8.9 it is Forge's, drawn before any mod runs. Mistly Client writes its colours and the Mistly cloud in `config/splash.properties` (and the cloud in `mistly-splash/`), so it shows from the second start. A splash.properties the player changed is left alone

Compared with the Fabric mod, there are no server features yet. A Mojang cape still draws under the Mistly cape.

With the Gradle of this folder, `runClient` stops on a check of the old loom plugin. The game is started with the `launch.cfg` of `.gradle/loom-cache` instead (`net.fabricmc.devlaunchinjector.Main`, with Java 8).

## Build

Gradle runs on Java 17 or later; the mod is compiled with a JDK 8 that Gradle downloads.

```bash
./gradlew build      # build/libs/misty-client-<version>+mc1.8.9.jar
./gradlew runClient  # the game with the mod
```

It is built with [architectury-loom](https://github.com/EssentialGG/architectury-loom) (Essential's fork), as [Forge1.8.9Template](https://github.com/nea89o/Forge1.8.9Template) does, so it needs neither ForgeGradle 2 nor Gradle 2. The version is `version` in `gradle.properties`.

## Layout

```
src/main/java/online/mistylauncher/legacy/
├── MistlyClient.java   entry point (@Mod)
├── Config.java         settings (config/misty-client.json)
├── Events.java         the Forge events it listens to
├── Keys.java           HUD editor and zoom keys
├── features/           CPS, reach and combo, zoom, fullbright, Misty cape
├── hud/                HUD modules, their placement and the editor
└── ui/                 settings screen
src/main/resources/assets/misty/   cape textures (the same files as the Fabric mod's), translations
```

## License

[Misty License](../LICENSE.txt), like the rest of Mistly Client. It is copied in the jar as `LICENSE_misty-client.txt`.
