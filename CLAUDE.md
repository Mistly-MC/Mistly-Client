# Misty Client

Mod Fabric (Java) qui ajoute les fonctionnalités en jeu de Misty : menu, HUD, badge, cosmétiques. Repo : Misty-MC/Misty-Client.
Il est inclus dans Misty-MC/Misty-Launcher comme sous-module `misty-client/`, et le launcher intègre ses jars dans `Misty.exe`.

## Compiler

JDK 25.

```bash
./gradlew build      # build/libs/misty-client-<version>+mc<minecraft>.jar, un jar par version de Minecraft
./gradlew runClient  # lancer le jeu avec le mod
```

- Multi-version avec **Stonecutter** : une seule source, écrite pour la version `vcsVersion` (26.3, voir `settings.gradle.kts`). Les autres versions (1.21.11, 26.1.2, 26.2) en sont dérivées. Le code propre à une version passe par les commentaires conditionnels de Stonecutter, jamais par une copie du fichier.
- Réglages par version : `stonecutter.properties.toml`. Le launcher lit `mod.mc_releases` (`misty-rust/build.rs`), donc ajouter une version de Minecraft demande aussi de vérifier le launcher.
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
- Après un push ici, le launcher ne voit pas le changement tant que son sous-module n'est pas mis à jour (`git submodule update --remote misty-client` dans Misty-Launcher, puis commit). L'entrée du changelog va dans `CHANGELOG.md` du **launcher**, section `### Misty Client (in game)`.
- Licence : Misty License 1.0 (`LICENSE.txt`, la même que le launcher : vente interdite). Elle est copiée dans chaque jar sous le nom `LICENSE_misty-client.txt`.
