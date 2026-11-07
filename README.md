<img alt="Everyday Enhancements logo" height="100" src="src/main/resources/assets/everydayenhancements/icon.png" width="100"/>

# Everyday Enhancements

A Fabric mod for Minecraft Java Edition **26.3**. It adds three trash cans and client-side FPS, CPS, zoom, and singleplayer reach features.

## Download

Download the installable jar from the [v1.1-SNAPSHOT release](https://github.com/MaximumSkelitor/Trash-Can-Mod/releases/tag/v1.1-SNAPSHOT):

**[Download everyday-enhancements-1.1-SNAPSHOT.jar](https://github.com/MaximumSkelitor/Trash-Can-Mod/releases/download/v1.1-SNAPSHOT/everyday-enhancements-1.1-SNAPSHOT.jar)**

## Install

1. Install **Fabric Loader** for Minecraft 26.3 (0.20.1).
2. Install **Fabric API** for Minecraft 26.3.
3. Download the mod jar above and put it in your Minecraft `mods` folder.
4. Launch Minecraft with your Fabric profile.

**Mod Menu is optional.** Install it to open the mod’s settings from the Mods screen. Without Mod Menu, the keybinds still work and can be changed under **Options → Controls → Key Binds → Everyday Enhancements**.
Install Mod Menu [here](https://github.com/TerraformersMC/ModMenu)

## Features

### Trash cans

- Iron Trash Can: 9 slots
- Gold Trash Can: 18 slots
- Diamond Trash Can: 27 slots

Items left in the trash can are deleted when its screen closes. The game reports how many items were deleted. This is permanent.

### FPS and CPS counters

Toggle the counters with **F8** (FPS) and **F9** (CPS) by default. Their positions and display settings can be customized separately in **Mod Menu → Everyday Enhancements → FPS/CPS settings**. Each counter has a preview and its own scale, colors, rainbow option, shadow, position, and background settings.

### Zoom

Hold **C** by default to zoom in. While holding it, scroll up to zoom further or down to reduce magnification. Releasing the key smoothly returns to normal view. The key can be rebound in Controls.

### Extended reach

The reach slider is in the mod’s Mod Menu settings. It adds up to 8 blocks of reach and applies only in a private singleplayer world. It is disabled on multiplayer servers and when a singleplayer world is opened to LAN.

## Build from source

Building requires JDK 25. From the project directory, run:

```powershell
.\gradlew.bat build
```

On macOS or Linux, run:

```sh
bash gradlew build
```

The installable jar is in `build/libs/`; choose the jar without `-sources` in its name.

## Releases

The GitHub Actions workflow builds and publishes a release whenever a tag beginning with `v` is pushed. The part after `v` becomes the mod version and jar filename. For example, `v1.0.1` produces `everyday-enhancements-1.0.1.jar`.
