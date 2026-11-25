# Mistly Client for Forge 1.8.9

Mistly Client for **Minecraft 1.8.9 with Forge** (11.15.1.2318), the PvP version. The Fabric mod can't run there (another loader, Java 8, other class names): this is its own mod. Like the Fabric versions, it hooks into the game with mixins (Mixin 0.7, shipped inside the jar: the players need nothing more); Forge loads it and its keys. It has the features of the Fabric versions, with the same look (night theme, textures, texts):

- **Mistly main menu**: the night landscape, the entries along the path of diamonds (Mods in place of Realms), you in 3D with your cosmetics, the wardrobe, the patch notes. It can be turned off in the settings
- **The game's menus** (options, worlds, servers…) over the Mistly landscape, with the Mistly buttons, sliders and lists (their textures in `assets/minecraft/textures/gui`)
- **Settings** (the cloud of the menu, of the pause menu, or "Settings" in the HUD editor): General, HUD, Zoom, Fullbright, PvP, Server, Cosmetics and Presets, each with its animated preview. A setting with options has a gear
- **HUD**, movable, in the Mistly style: FPS, ping, CPS, reach, combo (and its record), CubeCraft kills, the Mistly leaderboard (the server's sidebar, in its place), coordinates, keystrokes, armor, effects, clock, server (with its country's flag), music (read by the launcher), saturation (as AppleSkin), RAM, potions counter, mini-map
- **HUD editor** (Right Shift): drag a module to move it (it sticks to the edges and the centre), click it to turn it on or off, drag its corner or scroll over it to resize it, right-click it to put it back
- **HUD category**: hitboxes and chunk borders in the colours picked, motion blur, more hit particles, the enchantment glint's colour, the server's leaderboard hidden, item physics or 2D items
- **Zoom** (C): the wheel changes it, it glides, the mouse turns slower; **fullbright** (and its key)
- **PvP**: low fire, short swords, a ting on each kill (players, and mobs if chosen, behind its gear), the TNT countdown
- **Server**: CubeCraft's Auto GG and kills
- **Presets**: five slots of all the settings and the HUD, the first one ready-made ("PvP Mistly")
- **Wardrobe**: the Mistly cape, the early player cape and the Cloudly hat, written for the launcher as the Fabric versions do (`client.json`)
- **Mistly network**, as in the Fabric versions (same site, same token file): the other Mistly players see your cosmetics and badge, you see theirs, and the early player cape opens for the accounts the site counts. The account is proven with a Mojang "join", as a server does: the Minecraft token never leaves the game
- **Badge**: the Mistly cloud before the Mistly players' names, above their head (yours too in third person, if chosen), in the player list and in the chat. It is a character of the game's font (`U+E000`, in `assets/minecraft/textures/font/unicode_page_e0.png`)
- **Joining a server** after Mojang answered 429 is tried again a few times
- **Loading screen**: on 1.8.9 it is Forge's, drawn before any mod runs. Mistly Client writes its colours and the Mistly cloud in `config/splash.properties` (and the cloud in `mistly-splash/`), so it shows from the second start. A splash.properties the player changed is left alone
- Translations: English, French, Spanish, Italian, Russian

Settings are kept in `config/misty-client.json` of the game folder. Keys: Options > Controls > Mistly Client.

## Status

What 1.8.9 does not have is left out: totems, shields (low shield), shulker boxes (their preview), elytra. The enchantment glint is drawn tinted purple by the game, so some colours come out darker. A Mojang cape still draws under the Mistly cape.

With the Gradle of this folder, `runClient` stops on a check of the old loom plugin. The game is started with the `launch.cfg` of `.gradle/loom-cache` instead (`net.fabricmc.devlaunchinjector.Main`, with Java 8).

## Build

Gradle runs on Java 17 to 21 (not newer); the mod is compiled with a JDK 8 that Gradle downloads.

```bash
./gradlew build      # build/libs/misty-client-<version>+mc1.8.9.jar
./gradlew runClient  # the game with the mod
```

It is built with [architectury-loom](https://github.com/EssentialGG/architectury-loom) (Essential's fork), as [Forge1.8.9Template](https://github.com/nea89o/Forge1.8.9Template) does, so it needs neither ForgeGradle 2 nor Gradle 2. The version is the one of the releases: `mod.version` in `../stonecutter.properties.toml`, shared with the Fabric versions.

## Layout

```
src/main/java/online/mistylauncher/legacy/
├── MistlyClient.java   entry point (@Mod)
├── Config.java         settings (config/misty-client.json)
├── Presets.java        the five presets
├── Events.java         what the mod does at the game's moments (called by the mixins)
├── Keys.java           HUD editor, zoom and fullbright keys
├── features/           combat, zoom, fullbright, visuals, world lines, CubeCraft, music and flag, cosmetics
├── mixin/              the hooks into the game (mixins.mistlyclient.json)
├── hud/                HUD modules, their placement and the editor
├── menu/               the Mistly main menu and its landscape
├── net/                the Mistly network and the badge
└── ui/                 the Mistly screens and widgets
src/main/resources/assets/misty/       textures (the same files as the Fabric mod's), shaders, translations
src/main/resources/assets/minecraft/   the badge's font page, the motion blur program
```

## License

[Misty License](../LICENSE.txt), like the rest of Mistly Client. It is copied in the jar as `LICENSE_misty-client.txt`.
