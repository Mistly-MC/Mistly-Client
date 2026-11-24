# Mistly Client for Forge 1.8.9

Mistly Client for **Minecraft 1.8.9 with Forge** (11.15.1.2318), the PvP version. The Fabric mod can't run there (another loader, Java 8, other class names): this is its own small mod, built from Forge events only, without mixins.

- **HUD**, movable: FPS, CPS, ping (coloured by latency), reach (coloured by distance), combo, coordinates, clock, keystrokes (with the CPS on the mouse buttons), armor with durability, potions counter
- **HUD editor** (Right Shift): drag a module to move it, click it to turn it on or off, scroll over it to resize it, right-click it to put it back. "Settings" opens the rest
- **Potions counter**: one slot per kind of potion. The first slot stays where it was placed and each new kind is added on its left, or on its right ("New potions: on the right" in the settings). "Potions as text" shows "Potions : 5" instead
- **Zoom** (C), **fullbright**, and the **Misty cape** equipped in the launcher on your player
- Translations: English, French, Spanish, Italian, Russian

No damage indicators: they count as cheating on servers.

Settings are kept in `config/misty-client.json` of the game folder. Keys: Options > Controls > Mistly Client.

## Status

First version: it compiles, but it has not been played yet. Compared with the Fabric mod, there is no Misty main menu, Misty badge, Misty network (the other Misty players' capes) or server features yet. A Mojang cape still draws under the Misty cape.

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
