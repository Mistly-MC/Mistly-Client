# Misty Client

Mod Fabric (Java) qui ajoute les fonctionnalités en jeu de Misty : menu, HUD, badge, cosmétiques. Repo : Misty-MC/Misty-Client.
Le launcher (Misty-MC/Misty-Launcher) le télécharge depuis les **releases GitHub** de ce repo : publier une release lance `.github/workflows/release.yml`, qui compile chaque version et attache les jars et `misty-client.json` (la liste des jars et des versions de Minecraft de chacun) à la release. Le launcher l'inclut aussi comme sous-module `misty-client/`, seulement pour ses tests.

## Compiler

JDK 25.

```bash
./gradlew build      # build/libs/misty-client-<version>+mc<minecraft>.jar, un jar par version de Minecraft
./gradlew runClient  # lancer le jeu avec le mod
```

- Multi-version avec **Stonecutter** : une seule source, écrite pour la version `vcsVersion` (26.3, voir `settings.gradle.kts`). Les autres versions (1.21.11, 26.1.2, 26.2) en sont dérivées. Le code propre à une version passe par les commentaires conditionnels de Stonecutter, jamais par une copie du fichier.
- Réglages par version : `stonecutter.properties.toml`. `mod.mc_releases` dit sur quelles versions de Minecraft le launcher lance chaque jar (copié dans `misty-client.json` par `.github/scripts/manifest.py`).
- Version du mod : `mod.version` dans `stonecutter.properties.toml`.
- Si une fonctionnalité ne marche pas sur une version, la désactiver pour cette version seulement (voir `compat/`), sans faire planter le jeu.

## Conventions

- Package : `online.mistylauncher.client`, organisé en `config/`, `net/`, `cosmetics/`, `social/`, `hud/`, `menu/`, `ui/`, `features/`, `mixin/`.
- Textes en jeu : clés de traduction dans `src/main/resources/assets/misty/lang/` (en_us, fr_fr, es_es, it_it, ru_ru). Ajouter chaque nouvelle clé dans **tous** ces fichiers.
- `textures/cape/misty.png` et `textures/cape/early.png` sont aussi lus par le launcher, et un test du launcher vérifie qu'elles sont identiques. Si tu les modifies, préviens que le launcher doit être mis à jour.
- Réseau Misty (`net/`) : ne jamais faire sortir le token Minecraft du jeu, et respecter les délais anti-429 (pas de nouvelle tentative en boucle auprès de Mojang).

## Commits et branches

- Branches : `main` (publiée), `EnderMythex` et `xtheox` (branches perso). Travailler sur sa branche, PR vers `main`. Demander avant tout push ou merge sur `main`.
- Message : sujet en anglais à l'impératif, sans point final (`Add a Combo HUD module that counts landed hits`), corps optionnel qui explique pourquoi. Terminer par `Co-Authored-By: Claude <noreply@anthropic.com>` quand Claude a écrit le changement.
- Les joueurs reçoivent un changement quand une **release** est publiée ici (demander avant de publier). L'entrée du changelog va dans `CHANGELOG.md` du **launcher**, section `### Mistly Client (in game)`.
- Licence : Misty License 1.0 (`LICENSE.txt`, la même que le launcher : vente interdite). Elle est copiée dans chaque jar sous le nom `LICENSE_misty-client.txt`.
