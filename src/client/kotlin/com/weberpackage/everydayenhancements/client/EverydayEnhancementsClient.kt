package com.weberpackage.everydayenhancements.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import com.mojang.blaze3d.platform.InputConstants

class EverydayEnhancementsClient : ClientModInitializer {

    override fun onInitializeClient() {
        FpsCounterConfig.load()

        val category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("everydayenhancements", "general")
        )
        val toggleFpsKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.everydayenhancements.toggle_fps",
                InputConstants.KEY_F8,
                category
            )
        )
        val toggleCpsKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.everydayenhancements.toggle_cps",
                InputConstants.KEY_F9,
                category
            )
        )
        val toggleZoomKey = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.everydayenhancements.toggle_zoom",
                InputConstants.KEY_C,
                category
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            WorldHudFeatures.tick(client)
            ZoomFeature.tick(
                toggleZoomKey.isDown,
                client.level != null && client.gui.screen() == null
            )

            while (toggleFpsKey.consumeClick()) {
                FpsCounterConfig.fps.enabled = !FpsCounterConfig.fps.enabled
                FpsCounterConfig.save()
                client.gui.hud.setOverlayMessage(
                    Component.translatable(
                        if (FpsCounterConfig.fps.enabled) "options.everydayenhancements.fps_enabled"
                        else "options.everydayenhancements.fps_disabled"
                    ),
                    true
                )
            }

            while (toggleCpsKey.consumeClick()) {
                FpsCounterConfig.cps.enabled = !FpsCounterConfig.cps.enabled
                FpsCounterConfig.save()
                client.gui.hud.setOverlayMessage(
                    Component.translatable(
                        if (FpsCounterConfig.cps.enabled) "options.everydayenhancements.cps_enabled"
                        else "options.everydayenhancements.cps_disabled"
                    ),
                    true
                )
            }
        }

        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("everydayenhancements", "world_hud")
        ) { graphics, _ ->
            WorldHudFeatures.render(graphics, Minecraft.getInstance())
        }

        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("everydayenhancements", "fps_counter")
        ) { graphics, _ ->
            if (FpsCounterConfig.fps.enabled) {
                val minecraft = Minecraft.getInstance()
                drawCounter(graphics, "FPS: ${minecraft.getFps()}", lineIndex(FpsCounterConfig.fps), FpsCounterConfig.fps, minecraft)
            }
        }

        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("everydayenhancements", "cps_counter")
        ) { graphics, _ ->
            if (FpsCounterConfig.cps.enabled) {
                val minecraft = Minecraft.getInstance()
                drawCounter(
                    graphics,
                    "CPS: ${CpsCounter.clicksPerSecond()}",
                    lineIndex(FpsCounterConfig.cps),
                    FpsCounterConfig.cps,
                    minecraft
                )
            }
        }
    }

    private fun lineIndex(settings: CounterStyleSettings): Int {
        val other = if (settings === FpsCounterConfig.fps) FpsCounterConfig.cps else FpsCounterConfig.fps
        return if (other.enabled && other.positionIndex == settings.positionIndex) {
            if (settings === FpsCounterConfig.fps) 0 else 1
        } else {
            0
        }
    }

    private fun drawCounter(
        graphics: net.minecraft.client.gui.GuiGraphicsExtractor,
        text: String,
        lineIndex: Int,
        settings: CounterStyleSettings,
        minecraft: Minecraft
    ) {
        val scale = settings.textScalePercent / 100.0f
        val margin = settings.backgroundMargin
        val scaledTextWidth = kotlin.math.ceil(minecraft.font.width(text) * scale).toInt()
        val scaledTextHeight = kotlin.math.ceil(minecraft.font.lineHeight * scale).toInt()
        val boxWidth = scaledTextWidth + margin * 2
        val boxHeight = scaledTextHeight + margin * 2
        val rightAligned = settings.positionIndex % 2 == 0
        val bottomAligned = settings.positionIndex >= 2
        val x = if (rightAligned) graphics.guiWidth() - boxWidth - 4 else 4
        val rowOffset = lineIndex * (boxHeight + 2)
        val y = if (bottomAligned) {
            graphics.guiHeight() - boxHeight - 4 - rowOffset
        } else {
            4 + rowOffset
        }
        val backgroundColor = settings.backgroundColor
        if (backgroundColor ushr 24 != 0) {
            graphics.fill(x, y, x + boxWidth, y + boxHeight, backgroundColor)
        }

        val textColor = if (settings.rainbow) {
            rainbowColor()
        } else {
            settings.textColor
        }

        val pose = graphics.pose()
        pose.pushMatrix()
        try {
            pose.translate((x + margin).toFloat(), (y + margin).toFloat())
            pose.scale(scale, scale)
            graphics.text(minecraft.font, text, 0, 0, textColor, settings.shadow)
        } finally {
            pose.popMatrix()
        }
    }

    private fun rainbowColor(): Int {
        val phase = (System.currentTimeMillis() % 3000L).toFloat() / 3000.0f * 6.0f
        val sector = phase.toInt()
        val fraction = phase - sector
        val value = 255
        val down = (255 * (1.0f - fraction)).toInt()
        val up = (255 * fraction).toInt()
        val (red, green, blue) = when (sector % 6) {
            0 -> Triple(value, up, 0)
            1 -> Triple(down, value, 0)
            2 -> Triple(0, value, up)
            3 -> Triple(0, down, value)
            4 -> Triple(up, 0, value)
            else -> Triple(value, 0, down)
        }
        return (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
    }
}
