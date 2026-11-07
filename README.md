# Trash Can Mod

A Fabric mod for Minecraft **26.3** with iron, gold, and diamond trash cans, configurable FPS/CPS displays, zoom, and singleplayer-only extended reach.

## Build locally

Install JDK 25, then run the Gradle wrapper from the project folder:

```text
Windows:  .\gradlew.bat build
macOS/Linux: ./gradlew build
```

The distributable mod jar is written to `build/libs/` (the file without `-sources` in its name). The source jar is for developers and is not the one to install.

## Install

Install Fabric Loader for Minecraft 26.3 and Fabric API, then put the mod jar from `build/libs/` into the Minecraft `mods` folder. Mod Menu is optional; it adds the in-game settings entry.

## GitHub releases

Pushing a version tag beginning with `v` builds the mod and creates a GitHub Release with the installable jar attached. For example, after pushing the project to GitHub:

```text
git tag v1.0.0
git push origin v1.0.0
```

The Actions workflow creates release `v1.0.0` and attaches `trashcanmodsreal-1.0.0.jar`. Users can download that jar from the release page. To publish another build, update `mod_version` in `gradle.properties`, commit the change, and push a new tag with the matching version (for example, `v1.0.1`).
