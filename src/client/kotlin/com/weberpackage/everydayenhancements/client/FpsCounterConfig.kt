package com.weberpackage.everydayenhancements.client

import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.util.Properties

data class CounterStyleSettings(
    var enabled: Boolean = true,
    var textScalePercent: Int = 100,
    var rainbow: Boolean = false,
    var shadow: Boolean = true,
    var positionIndex: Int = 0,
    var textColor: Int = 0xFFFFFFFF.toInt(),
    var backgroundColor: Int = 0xA0000000.toInt(),
    var backgroundMargin: Int = 4
)

object FpsCounterConfig {
    val fps = CounterStyleSettings()
    val cps = CounterStyleSettings()
    val biome = CounterStyleSettings()
    val death = CounterStyleSettings()
    val armor = CounterStyleSettings()
    val coordinates = CounterStyleSettings()
    var extraReach = 2.0
    var fullbright = false
    var spawnOverlay = false

    val positions = listOf("Top Right", "Top Left", "Bottom Right", "Bottom Left")
    val scales = listOf(75, 100, 125, 150, 200)
    val margins = listOf(0, 2, 4, 8, 12)
    val backgroundOpacities = listOf(0, 25, 50, 75, 100)
    private val legacyTextColors = listOf(
        0xFFFFFFFF.toInt(), 0xFFFF5555.toInt(), 0xFFFFFF55.toInt(), 0xFF55FF55.toInt(),
        0xFF55FFFF.toInt(), 0xFF5555FF.toInt(), 0xFFFF55FF.toInt()
    )
    private val legacyBackgroundColors = listOf(
        0xA0000000.toInt(), 0xA0404040.toInt(), 0xA0102840.toInt(), 0x00000000
    )

    private val configFile
        get() = FabricLoader.getInstance().configDir.resolve("everydayenhancements.properties")

    fun load() {
        if (!Files.exists(configFile)) return

        val properties = Properties()
        Files.newInputStream(configFile).use(properties::load)
        loadStyle(properties, "fps", fps, properties.getProperty("showFps", "true").toBoolean())
        loadStyle(properties, "cps", cps, properties.getProperty("showCps", "true").toBoolean())
        loadStyle(properties, "biome", biome, true)
        loadStyle(properties, "death", death, true)
        loadStyle(properties, "armor", armor, true)
        loadStyle(properties, "coordinates", coordinates, true)
        extraReach = properties.getProperty("extraReach", "2.0")
            .toDoubleOrNull()?.coerceIn(0.0, 8.0) ?: 2.0
        fullbright = properties.getProperty("fullbright", "false").toBoolean()
        spawnOverlay = properties.getProperty("spawnOverlay", "false").toBoolean()
    }

    fun save() {
        Files.createDirectories(configFile.parent)
        val properties = Properties().apply {
            saveStyle(this, "fps", fps)
            saveStyle(this, "cps", cps)
            saveStyle(this, "biome", biome)
            saveStyle(this, "death", death)
            saveStyle(this, "armor", armor)
            saveStyle(this, "coordinates", coordinates)
            setProperty("extraReach", extraReach.toString())
            setProperty("fullbright", fullbright.toString())
            setProperty("spawnOverlay", spawnOverlay.toString())
        }
        Files.newOutputStream(configFile).use { output ->
            properties.store(output, "Everyday Enhancements client settings")
        }
    }

    private fun loadStyle(properties: Properties, prefix: String, style: CounterStyleSettings, legacyEnabled: Boolean) {
        style.enabled = properties.getProperty("$prefix.enabled", legacyEnabled.toString()).toBoolean()
        style.textScalePercent = properties.getProperty(
            "$prefix.textScalePercent", properties.getProperty("textScalePercent", "100")
        ).toIntOrNull()
            ?.takeIf { it in scales } ?: 100
        style.rainbow =
            properties.getProperty("$prefix.rainbow", properties.getProperty("rainbow", "false")).toBoolean()
        style.shadow = properties.getProperty("$prefix.shadow", properties.getProperty("shadow", "true")).toBoolean()
        style.positionIndex = properties.getProperty(
            "$prefix.positionIndex", properties.getProperty("positionIndex", "0")
        ).toIntOrNull()
            ?.coerceIn(positions.indices) ?: 0
        val oldTextColor = properties.getProperty("textColorIndex", "0").toIntOrNull()
            ?.coerceIn(legacyTextColors.indices) ?: 0
        val oldBackgroundColor = properties.getProperty("backgroundColorIndex", "0").toIntOrNull()
            ?.coerceIn(legacyBackgroundColors.indices) ?: 0
        style.textColor = properties.getProperty("$prefix.textColor")?.toLongOrNull(16)?.toInt()
            ?: legacyTextColors[oldTextColor]
        style.backgroundColor = properties.getProperty("$prefix.backgroundColor")?.toLongOrNull(16)?.toInt()
            ?: legacyBackgroundColors[oldBackgroundColor]
        style.backgroundMargin = properties.getProperty(
            "$prefix.backgroundMargin", properties.getProperty("backgroundMargin", "4")
        ).toIntOrNull()
            ?.takeIf { it in margins } ?: 4
        val defaultOpacity = (style.backgroundColor ushr 24) * 100 / 255
        val opacity = properties.getProperty("$prefix.backgroundOpacity")?.toIntOrNull()
            ?.coerceIn(0, 100) ?: defaultOpacity
        style.backgroundColor = (opacity * 255 / 100 shl 24) or (style.backgroundColor and 0xFFFFFF)
    }

    private fun saveStyle(properties: Properties, prefix: String, style: CounterStyleSettings) {
        properties.setProperty("$prefix.enabled", style.enabled.toString())
        properties.setProperty("$prefix.textScalePercent", style.textScalePercent.toString())
        properties.setProperty("$prefix.rainbow", style.rainbow.toString())
        properties.setProperty("$prefix.shadow", style.shadow.toString())
        properties.setProperty("$prefix.positionIndex", style.positionIndex.toString())
        properties.setProperty("$prefix.textColor", style.textColor.toUInt().toString(16))
        properties.setProperty("$prefix.backgroundColor", style.backgroundColor.toUInt().toString(16))
        properties.setProperty("$prefix.backgroundMargin", style.backgroundMargin.toString())
        properties.setProperty("$prefix.backgroundOpacity", ((style.backgroundColor ushr 24) * 100 / 255).toString())
    }
}
