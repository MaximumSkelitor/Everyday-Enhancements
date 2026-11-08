<img alt="Everyday Enhancements logo" height="100" src="src/main/resources/assets/everydayenhancements/icon.png" width="100"/>

# Everyday Enhancements

A Fabric mod for Minecraft Java Edition **26.3**. It adds three trash cans and client-side FPS, CPS, zoom, Fullbright, mob spawnability overlay, world HUD features, and singleplayer reach.

## Download the latest release [here](https://github.com/MaximumSkelitor/Everyday-Enhancements/releases)

## Install

1. Install **Fabric Loader** for Minecraft 26.3 **(0.20.1)**.
2. Install **Fabric API** for Minecraft 26.3.
3. Download the mod jar above and put it in your Minecraft `mods` folder.
4. Launch Minecraft with your Fabric profile.

**Mod Menu is optional, but recommended.** Install it to configure the mod from the Mods screen. Keybinds can also be changed under **Options > Controls > Key Binds > Everyday Enhancements**. Get [Mod Menu](https://github.com/TerraformersMC/ModMenu).

## Features

### Trash cans

- Iron Trash Can: 9 slots
- Gold Trash Can: 18 slots
- Diamond Trash Can: 27 slots

Items left in a trash can are deleted when its screen closes. The game reports how many items were deleted. This is
permanent.

### FPS and CPS counters

Toggle the counters with **F8** (FPS) and **F9** (CPS) by default. Configure each counter separately in **Mod Menu**
with a live preview, scale, text and background colors, rainbow text, shadow, position, and background margin.

### Zoom

Hold **C** by default to zoom in. Scroll while holding the key to adjust magnification. Releasing the key smoothly
returns to normal view. The key can be rebound in Controls.

### Extended reach

Configure extra reach in the mod's Mod Menu settings (up to 8 additional blocks). It applies only in a private
singleplayer world and is disabled on multiplayer servers and when a singleplayer world is opened to LAN.

### Fullbright

Toggle Fullbright with **B** by default. Rebind the key in Controls or toggle it from Mod Menu.

### Mob spawnability overlay

Toggle the nearby hostile mob spawnability overlay with **N** by default. Nearby block tops are marked with a green **Y** or red **X**, based on block support, clear space above, and block light. Rebind the key in Controls or toggle the overlay in Mod Menu.

### World HUD

Display biome, death location, armor durability, and XYZ coordinates. Each display can be enabled and styled separately
in **Mod Menu**, with configurable scale, colors, rainbow text, shadow, position, and background.

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

The GitHub Actions workflow builds and publishes a release whenever a tag beginning with `v` is pushed. The release description comes from `CHANGELOG.md`. The part after `v` becomes the mod version, and Minecraft version is appended to the jar filename. For example, `v1.2` for Minecraft 26.3 produces `everyday-enhancements-1.2+mc.26.3.jar`.
