package com.weberpackage.everydayenhancements.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import kotlin.math.*

class FpsCounterConfigScreen(private val parent: Screen) : Screen(modTitle()) {

    private enum class Page { SELECT_COUNTER, EDIT_FPS, EDIT_CPS, EDIT_BIOME, EDIT_DEATH, EDIT_ARMOR, EDIT_COORDINATES, EDIT_REACH }

    private var page = Page.SELECT_COUNTER
    private var settingsScroll = 0
    private val editingCps get() = page == Page.EDIT_CPS
    private val editingReach get() = page == Page.EDIT_REACH
    private val style
        get() = when (page) {
            Page.EDIT_CPS -> FpsCounterConfig.cps
            Page.EDIT_BIOME -> FpsCounterConfig.biome
            Page.EDIT_DEATH -> FpsCounterConfig.death
            Page.EDIT_ARMOR -> FpsCounterConfig.armor
            Page.EDIT_COORDINATES -> FpsCounterConfig.coordinates
            else -> FpsCounterConfig.fps
        }

    private lateinit var enabledButton: Button
    private lateinit var scaleButton: Button
    private lateinit var rainbowButton: Button
    private lateinit var shadowButton: Button
    private lateinit var positionButton: Button
    private lateinit var textColorButton: Button
    private lateinit var backgroundButton: Button
    private lateinit var marginButton: Button
    private lateinit var opacitySlider: OpacitySlider
    private var previewY = 0
    private var previewHeight = 46

    override fun init() {
        val columnWidth = minOf(150, (width - 40) / 2)
        val leftX = width / 2 - columnWidth - 3
        val rightX = width / 2 + 3
        val fullWidth = columnWidth * 2 + 6
        val fullX = width / 2 - fullWidth / 2

        if (page == Page.SELECT_COUNTER) {
            val colWidth = minOf(155, (width - 48) / 2)
            val gap = 6
            val left = (width - colWidth * 2 - gap) / 2
            val right = left + colWidth + gap
            val row = minOf(38, ((height - 150) / 5).coerceAtLeast(28))
            val visibleRows = ((height - 100) / row).coerceAtLeast(1)
            val start = 38
            fun setting(text: Component, column: Int, index: Int, action: () -> Unit) {
                if (index !in settingsScroll until (settingsScroll + visibleRows)) return
                val x = if (column == 0) left else right
                addRenderableWidget(Button.builder(text) { action() }
                    .bounds(x, start + (index - settingsScroll) * row, colWidth, 21).build())
            }
            setting(
                Component.translatable("options.everydayenhancements.tab_fps"),
                0,
                0
            ) {
                page = Page.EDIT_FPS; rebuildWidgets()
            }
            setting(Component.translatable("options.everydayenhancements.tab_cps"), 1, 0) {
                page = Page.EDIT_CPS; rebuildWidgets()
            }
            setting(Component.translatable("options.everydayenhancements.tab_biome"), 0, 1) {
                page = Page.EDIT_BIOME; rebuildWidgets()
            }
            setting(Component.translatable("options.everydayenhancements.tab_death"), 1, 1) {
                page = Page.EDIT_DEATH; rebuildWidgets()
            }
            setting(Component.translatable("options.everydayenhancements.tab_armor"), 0, 2) {
                page = Page.EDIT_ARMOR; rebuildWidgets()
            }
            setting(Component.translatable("options.everydayenhancements.tab_coordinates"), 1, 2) {
                page = Page.EDIT_COORDINATES; rebuildWidgets()
            }
            setting(Component.translatable("options.everydayenhancements.tab_reach"), 0, 3) {
                page = Page.EDIT_REACH; rebuildWidgets()
            }
            setting(featureFullbrightText(), 1, 3) {
                FpsCounterConfig.fullbright = !FpsCounterConfig.fullbright
                FpsCounterConfig.save()
                rebuildWidgets()
            }
            setting(featureSpawnText(), 0, 4) {
                FpsCounterConfig.spawnOverlay = !FpsCounterConfig.spawnOverlay
                FpsCounterConfig.save()
                rebuildWidgets()
            }
            addRenderableWidget(Button.builder(Component.translatable("gui.done")) { minecraft.gui.setScreen(parent) }
                .bounds(left, height - 30, colWidth * 2 + gap, 21).build())
            return
        }

        if (editingReach) {
            addRenderableWidget(
                ReachSlider(width / 2 - 150, height / 2 - 12, 300, 20)
            )
            addRenderableWidget(
                Button.builder(Component.translatable("options.everydayenhancements.back")) {
                    page = Page.SELECT_COUNTER
                    rebuildWidgets()
                }.bounds(width / 2 - 100, height / 2 + 28, 200, 20).build()
            )
            addRenderableWidget(
                Button.builder(Component.translatable("gui.done")) {
                    minecraft.gui.setScreen(parent)
                }.bounds(width / 2 - 100, height / 2 + 56, 200, 20).build()
            )
            return
        }

        val startY = 38
        val rowStep = minOf(25, ((height - 170).coerceAtLeast(105)) / 7)
        val controlHeight = minOf(20, rowStep - 3)
        enabledButton = button(enabledText(), fullX, startY, fullWidth) {
            style.enabled = !style.enabled
            saveAndRefresh()
        }
        scaleButton = button(scaleText(), fullX, startY + rowStep, fullWidth) {
            val current = FpsCounterConfig.scales.indexOf(style.textScalePercent)
            style.textScalePercent = FpsCounterConfig.scales[(current + 1) % FpsCounterConfig.scales.size]
            saveAndRefresh()
        }
        rainbowButton = button(rainbowText(), leftX, startY + rowStep * 2, columnWidth) {
            style.rainbow = !style.rainbow
            saveAndRefresh()
        }
        shadowButton = button(shadowText(), rightX, startY + rowStep * 2, columnWidth) {
            style.shadow = !style.shadow
            saveAndRefresh()
        }
        positionButton = button(positionText(), fullX, startY + rowStep * 3, fullWidth) {
            style.positionIndex = (style.positionIndex + 1) % FpsCounterConfig.positions.size
            saveAndRefresh()
        }
        textColorButton = button(textColorText(), leftX, startY + rowStep * 4, columnWidth) {
            openColorPicker(background = false)
        }
        backgroundButton = button(backgroundText(), rightX, startY + rowStep * 4, columnWidth) {
            openColorPicker(background = true)
        }
        marginButton = button(marginText(), fullX, startY + rowStep * 5, fullWidth) {
            val current = FpsCounterConfig.margins.indexOf(style.backgroundMargin)
            style.backgroundMargin = FpsCounterConfig.margins[(current + 1) % FpsCounterConfig.margins.size]
            saveAndRefresh()
        }
        opacitySlider = addRenderableWidget(OpacitySlider(fullX, startY + rowStep * 6, fullWidth, controlHeight, style))
        listOf(
            enabledButton,
            scaleButton,
            rainbowButton,
            shadowButton,
            positionButton,
            textColorButton,
            backgroundButton,
            marginButton
        ).forEach { it.height = controlHeight }
        previewY = opacitySlider.y + opacitySlider.height + 4
        previewHeight = if (page == Page.EDIT_ARMOR) 92 else 46

        addRenderableWidget(
            Button.builder(Component.translatable("options.everydayenhancements.back")) {
                page = Page.SELECT_COUNTER
                rebuildWidgets()
            }.bounds(leftX, (previewY + previewHeight + 4).coerceAtMost(height - 24), columnWidth, 20).build()
        )
        addRenderableWidget(
            Button.builder(Component.translatable("gui.done")) {
                minecraft.gui.setScreen(parent)
            }.bounds(rightX, (previewY + previewHeight + 4).coerceAtMost(height - 24), columnWidth, 20).build()
        )
        refreshLabels()
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
        graphics.centeredText(font, title, width / 2, 18, 0xFFFFFFFF.toInt())
        if (page == Page.EDIT_REACH) {
            graphics.centeredText(
                font,
                Component.translatable("options.everydayenhancements.reach_singleplayer_only"),
                width / 2,
                height / 2 - 30,
                0xFFFFFFFF.toInt()
            )
        } else if (page != Page.SELECT_COUNTER) {
            drawPreview(graphics)
        }
    }

    override fun onClose() {
        minecraft.gui.setScreen(parent)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (page == Page.SELECT_COUNTER) {
            val row = minOf(38, ((height - 150) / 5).coerceAtLeast(28))
            val visibleRows = ((height - 100) / row).coerceAtLeast(1)
            val next = (settingsScroll - verticalAmount.toInt()).coerceIn(0, (5 - visibleRows).coerceAtLeast(0))
            if (next != settingsScroll) {
                settingsScroll = next
                rebuildWidgets()
                return true
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    private fun button(text: Component, x: Int, y: Int, width: Int, action: () -> Unit): Button =
        addRenderableWidget(Button.builder(text) { action() }.bounds(x, y, width, 20).build())

    private fun saveAndRefresh() {
        FpsCounterConfig.save()
        refreshLabels()
    }

    private fun refreshLabels() {
        if (!::enabledButton.isInitialized) return
        enabledButton.message = enabledText()
        scaleButton.message = scaleText()
        rainbowButton.message = rainbowText()
        shadowButton.message = shadowText()
        positionButton.message = positionText()
        textColorButton.message = textColorText()
        backgroundButton.message = backgroundText()
        marginButton.message = marginText()
        opacitySlider.updateMessage()
    }

    private fun openColorPicker(background: Boolean) {
        val currentColor = if (background) style.backgroundColor else style.textColor
        val title = Component.translatable(
            if (background) "options.everydayenhancements.background_color_title"
            else "options.everydayenhancements.text_color_title"
        )
        minecraft.gui.setScreen(ColorPickerScreen(this, title, currentColor) { color ->
            if (background) style.backgroundColor = color else style.textColor = color
            FpsCounterConfig.save()
            refreshLabels()
        })
    }

    private fun drawPreview(graphics: GuiGraphicsExtractor) {
        val minecraft = Minecraft.getInstance()
        val previewWidth = settingsGridWidth()
        val x = (width - previewWidth) / 2
        val y = previewY
        graphics.fill(x, y, x + previewWidth, y + previewHeight, 0x90000000.toInt())
        graphics.text(
            font,
            Component.translatable("options.everydayenhancements.preview"),
            x + 8,
            y + 3,
            0xFFE0E0E0.toInt()
        )
        if (page == Page.EDIT_ARMOR) {
            WorldHudFeatures.drawArmorPreview(graphics, minecraft, x + previewWidth / 2, y + 18, style)
            return
        }
        val sample = when (page) {
            Page.EDIT_CPS -> "CPS: ${CpsCounter.clicksPerSecond()}"
            Page.EDIT_BIOME -> "Biome: Plains"
            Page.EDIT_DEATH -> "Death: 120, 64, -30 (42 blocks)"
            Page.EDIT_ARMOR -> "Diamond Chestplate: 85%"
            Page.EDIT_COORDINATES -> "XYZ: 120, 64, -30"
            else -> "FPS: ${minecraft.fps}"
        }
        val sampleWidth = ceil(font.width(sample) * style.textScalePercent / 100.0).toInt()
        val sampleHeight = ceil(font.lineHeight * style.textScalePercent / 100.0).toInt()
        drawPreviewCounter(
            graphics,
            sample,
            x + (previewWidth - sampleWidth) / 2,
            y + (24 - sampleHeight) / 2,
            style
        )
    }

    private fun drawPreviewCounter(
        graphics: GuiGraphicsExtractor,
        text: String,
        x: Int,
        y: Int,
        settings: CounterStyleSettings
    ) {
        val scale = settings.textScalePercent / 100.0f
        val margin = settings.backgroundMargin
        val color = displayColor(settings)
        val textWidth = ceil(font.width(text) * scale).toInt()
        val textHeight = ceil(font.lineHeight * scale).toInt()
        if (settings.backgroundColor ushr 24 != 0) {
            graphics.fill(x, y, x + textWidth + margin * 2, y + textHeight + margin * 2, settings.backgroundColor)
        }
        val pose = graphics.pose()
        pose.pushMatrix()
        try {
            pose.translate((x + margin).toFloat(), (y + margin).toFloat())
            pose.scale(scale, scale)
            graphics.text(font, text, 0, 0, color, settings.shadow)
        } finally {
            pose.popMatrix()
        }
    }

    private fun settingsGridWidth(): Int = minOf(155, (width - 48) / 2) * 2 + 6

    private fun displayColor(settings: CounterStyleSettings): Int = if (settings.rainbow) {
        val hue = (System.currentTimeMillis() % 3000L).toFloat() / 3000.0f
        hsvToColor(hue, 1f, 1f)
    } else {
        settings.textColor
    }

    private fun enabledText() = Component.translatable(
        if (style.enabled) "options.everydayenhancements.enabled" else "options.everydayenhancements.disabled"
    )

    private fun featureFullbrightText() =
        Component.translatable(if (FpsCounterConfig.fullbright) "options.everydayenhancements.fullbright_on" else "options.everydayenhancements.fullbright_off")

    private fun featureSpawnText() =
        Component.translatable(if (FpsCounterConfig.spawnOverlay) "options.everydayenhancements.spawn_overlay_on" else "options.everydayenhancements.spawn_overlay_off")

    private fun scaleText() = Component.translatable(
        "options.everydayenhancements.text_scale", style.textScalePercent
    )

    private fun rainbowText() = Component.translatable(
        if (style.rainbow) "options.everydayenhancements.rainbow_on" else "options.everydayenhancements.rainbow_off"
    )

    private fun shadowText() = Component.translatable(
        if (style.shadow) "options.everydayenhancements.shadow_on" else "options.everydayenhancements.shadow_off"
    )

    private fun positionText() = Component.translatable(
        "options.everydayenhancements.position", FpsCounterConfig.positions[style.positionIndex]
    )

    private fun textColorText() = Component.translatable(
        "options.everydayenhancements.text_color", rgbHex(style.textColor)
    )

    private fun backgroundText() = Component.translatable(
        "options.everydayenhancements.background_color", rgbHex(style.backgroundColor)
    )

    private fun marginText() = Component.translatable(
        "options.everydayenhancements.background_margin", style.backgroundMargin
    )

}

private class OpacitySlider(x: Int, y: Int, width: Int, height: Int, private val settings: CounterStyleSettings) :
    AbstractSliderButton(x, y, width, height, Component.empty(), 1.0 - (settings.backgroundColor ushr 24) / 255.0) {
    init {
        updateMessage()
    }

    public override fun updateMessage() {
        message =
            Component.translatable("options.everydayenhancements.background_transparency", (value * 100).roundToInt())
    }

    override fun applyValue() {
        val alpha = ((1.0 - value) * 255).roundToInt().coerceIn(0, 255)
        settings.backgroundColor = (alpha shl 24) or (settings.backgroundColor and 0xFFFFFF)
        FpsCounterConfig.save()
    }
}

private fun modTitle(): Component {
    return Component.literal("Everyday Enhancements")
}

private class ReachSlider(x: Int, y: Int, width: Int, height: Int) : AbstractSliderButton(
    x,
    y,
    width,
    height,
    Component.empty(),
    (FpsCounterConfig.extraReach / MAX_EXTRA_REACH).coerceIn(0.0, 1.0)
) {
    init {
        updateMessage()
    }

    override fun updateMessage() {
        val extra = (value * MAX_EXTRA_REACH * 4.0).roundToInt() / 4.0
        message = Component.translatable("options.everydayenhancements.extra_reach", extra)
        if (message == Component.translatable("options.everydayenhancements.extra_reach", 2.0)) {
            message = Component.translatable("options.everydayenhancements.extra_reach_default")
        }
    }

    override fun applyValue() {
        FpsCounterConfig.extraReach = (value * MAX_EXTRA_REACH * 4.0).roundToInt() / 4.0
        FpsCounterConfig.save()
        updateMessage()
    }

    companion object {
        private const val MAX_EXTRA_REACH = 16.0
    }
}

private class ColorPickerScreen(
    private val parent: Screen,
    title: Component,
    initialColor: Int,
    private val onSelected: (Int) -> Unit
) : Screen(title) {
    private val alpha = (initialColor ushr 24) and 0xFF
    private val initialHsv = colorToHsv(initialColor)
    private var hue = initialHsv.first
    private var saturation = initialHsv.second
    private var value = initialHsv.third

    override fun init() {
        addRenderableWidget(
            Button.builder(Component.translatable("gui.done")) {
                onSelected((alpha shl 24) or (hsvToColor(hue, saturation, value) and 0xFFFFFF))
                minecraft.gui.setScreen(parent)
            }.bounds(width / 2 - 60, height - 35, 120, 20).build()
        )
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
        drawColorWheel(graphics)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (super.mouseClicked(event, doubleClick)) return true
        return updateColorFromPointer(event.x(), event.y())
    }

    private fun drawColorWheel(graphics: GuiGraphicsExtractor) {
        val centerX = width / 2
        val centerY = height / 2 - 10
        val radius = 78.0
        val segmentSize = 7
        for (step in 0 until 72) {
            val h = step / 72.0f
            val angle = h * 2.0 * PI
            val x = (centerX + cos(angle) * radius).toInt()
            val y = (centerY + sin(angle) * radius).toInt()
            graphics.fill(
                x - segmentSize / 2,
                y - segmentSize / 2,
                x + segmentSize / 2,
                y + segmentSize / 2,
                hsvToColor(h, 1f, 1f)
            )
        }

        val squareSize = 100
        val squareX = centerX - squareSize / 2
        val squareY = centerY - squareSize / 2
        val cell = squareSize / 20
        for (row in 0 until 20) {
            for (column in 0 until 20) {
                val s = (column + 0.5f) / 20.0f
                val v = 1.0f - (row + 0.5f) / 20.0f
                val color = hsvToColor(hue, s, v)
                graphics.fill(
                    squareX + column * cell,
                    squareY + row * cell,
                    squareX + (column + 1) * cell,
                    squareY + (row + 1) * cell,
                    color
                )
            }
        }

        val selectorX = squareX + (saturation * squareSize).toInt()
        val selectorY = squareY + ((1f - value) * squareSize).toInt()
        graphics.fill(selectorX - 3, selectorY - 3, selectorX + 4, selectorY + 4, 0xFF000000.toInt())
        graphics.fill(selectorX - 2, selectorY - 2, selectorX + 3, selectorY + 3, 0xFFFFFFFF.toInt())

        val hueAngle = hue * 2.0 * PI
        val hueX = (centerX + cos(hueAngle) * radius).toInt()
        val hueY = (centerY + sin(hueAngle) * radius).toInt()
        graphics.fill(hueX - 5, hueY - 5, hueX + 6, hueY + 6, 0xFF000000.toInt())
        graphics.fill(hueX - 3, hueY - 3, hueX + 4, hueY + 4, 0xFFFFFFFF.toInt())

        val color = hsvToColor(hue, saturation, value)
        val swatchX = centerX + 100
        val swatchY = centerY - 15
        graphics.fill(swatchX, swatchY, swatchX + 35, swatchY + 35, 0xFFFFFFFF.toInt())
        graphics.fill(swatchX + 2, swatchY + 2, swatchX + 33, swatchY + 33, color)
        graphics.text(font, "#${rgbHex(color)}", swatchX - 12, swatchY + 45, 0xFFFFFFFF.toInt())
    }

    private fun updateColorFromPointer(pointerX: Double, pointerY: Double): Boolean {
        val centerX = width / 2.0
        val centerY = height / 2.0 - 10.0
        val squareSize = 100.0
        val squareLeft = centerX - squareSize / 2.0
        val squareTop = centerY - squareSize / 2.0

        if (pointerX in squareLeft..(squareLeft + squareSize) && pointerY in squareTop..(squareTop + squareSize)) {
            saturation = ((pointerX - squareLeft) / squareSize).toFloat().coerceIn(0f, 1f)
            value = (1.0 - (pointerY - squareTop) / squareSize).toFloat().coerceIn(0f, 1f)
            return true
        }

        val distance = hypot(pointerX - centerX, pointerY - centerY)
        if (distance in 66.0..89.0) {
            hue = (atan2(pointerY - centerY, pointerX - centerX) / (2.0 * PI)).toFloat()
            if (hue < 0f) hue += 1f
            return true
        }
        return false
    }
}

private fun colorToHsv(color: Int): Triple<Float, Float, Float> {
    val red = ((color ushr 16) and 0xFF) / 255f
    val green = ((color ushr 8) and 0xFF) / 255f
    val blue = (color and 0xFF) / 255f
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val delta = max - min
    var hue = when {
        delta == 0f -> 0f
        max == red -> ((green - blue) / delta) % 6f
        max == green -> (blue - red) / delta + 2f
        else -> (red - green) / delta + 4f
    } / 6f
    if (hue < 0f) hue += 1f
    val saturation = if (max == 0f) 0f else delta / max
    return Triple(hue, saturation, max)
}

private fun hsvToColor(hue: Float, saturation: Float, value: Float): Int {
    val normalizedHue = ((hue % 1f) + 1f) % 1f
    val chroma = value * saturation
    val segment = normalizedHue * 6f
    val x = chroma * (1f - abs(segment % 2f - 1f))
    val (red, green, blue) = when (segment.toInt()) {
        0 -> Triple(chroma, x, 0f)
        1 -> Triple(x, chroma, 0f)
        2 -> Triple(0f, chroma, x)
        3 -> Triple(0f, x, chroma)
        4 -> Triple(x, 0f, chroma)
        else -> Triple(chroma, 0f, x)
    }
    val offset = value - chroma
    val r = ((red + offset) * 255f).toInt().coerceIn(0, 255)
    val g = ((green + offset) * 255f).toInt().coerceIn(0, 255)
    val b = ((blue + offset) * 255f).toInt().coerceIn(0, 255)
    return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}

private fun rgbHex(color: Int): String = "%06X".format(color and 0xFFFFFF)
