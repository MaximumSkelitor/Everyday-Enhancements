package com.weberpackage.trashcanmodsreal.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.fabricmc.loader.api.FabricLoader
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

class FpsCounterConfigScreen(private val parent: Screen) : Screen(modTitle()) {

    private enum class Page { SELECT_COUNTER, EDIT_FPS, EDIT_CPS, EDIT_REACH }

    private var page = Page.SELECT_COUNTER
    private val editingCps get() = page == Page.EDIT_CPS
    private val editingReach get() = page == Page.EDIT_REACH
    private val style get() = if (editingCps) FpsCounterConfig.cps else FpsCounterConfig.fps

    private lateinit var enabledButton: Button
    private lateinit var scaleButton: Button
    private lateinit var rainbowButton: Button
    private lateinit var shadowButton: Button
    private lateinit var positionButton: Button
    private lateinit var textColorButton: Button
    private lateinit var backgroundButton: Button
    private lateinit var marginButton: Button

    override fun init() {
        val columnWidth = minOf(150, (width - 40) / 2)
        val leftX = width / 2 - columnWidth - 3
        val rightX = width / 2 + 3
        val fullWidth = columnWidth * 2 + 6
        val fullX = width / 2 - fullWidth / 2

        if (page == Page.SELECT_COUNTER) {
            addRenderableWidget(
                Button.builder(Component.translatable("options.trashcanmodsreal.tab_fps")) {
                    page = Page.EDIT_FPS
                    rebuildWidgets()
                }.bounds(width / 2 - 100, height / 2 - 24, 200, 20).build()
            )
            addRenderableWidget(
                Button.builder(Component.translatable("options.trashcanmodsreal.tab_cps")) {
                    page = Page.EDIT_CPS
                    rebuildWidgets()
                }.bounds(width / 2 - 100, height / 2 + 6, 200, 20).build()
            )
            addRenderableWidget(
                Button.builder(Component.translatable("options.trashcanmodsreal.tab_reach")) {
                    page = Page.EDIT_REACH
                    rebuildWidgets()
                }.bounds(width / 2 - 100, height / 2 + 36, 200, 20).build()
            )
            return
        }

        if (editingReach) {
            addRenderableWidget(
                ReachSlider(width / 2 - 150, height / 2 - 12, 300, 20)
            )
            addRenderableWidget(
                Button.builder(Component.translatable("options.trashcanmodsreal.back")) {
                    page = Page.SELECT_COUNTER
                    rebuildWidgets()
                }.bounds(width / 2 - 100, height / 2 + 28, 200, 20).build()
            )
            addRenderableWidget(
                Button.builder(Component.translatable("gui.done")) {
                    minecraft?.gui?.setScreen(parent)
                }.bounds(width / 2 - 100, height / 2 + 56, 200, 20).build()
            )
            return
        }

        val startY = 58
        enabledButton = button(enabledText(), fullX, startY + 25, fullWidth) {
            style.enabled = !style.enabled
            saveAndRefresh()
        }
        scaleButton = button(scaleText(), fullX, startY + 50, fullWidth) {
            val current = FpsCounterConfig.scales.indexOf(style.textScalePercent)
            style.textScalePercent = FpsCounterConfig.scales[(current + 1) % FpsCounterConfig.scales.size]
            saveAndRefresh()
        }
        rainbowButton = button(rainbowText(), leftX, startY + 75, columnWidth) {
            style.rainbow = !style.rainbow
            saveAndRefresh()
        }
        shadowButton = button(shadowText(), rightX, startY + 75, columnWidth) {
            style.shadow = !style.shadow
            saveAndRefresh()
        }
        positionButton = button(positionText(), fullX, startY + 100, fullWidth) {
            style.positionIndex = (style.positionIndex + 1) % FpsCounterConfig.positions.size
            saveAndRefresh()
        }
        textColorButton = button(textColorText(), leftX, startY + 125, columnWidth) {
            openColorPicker(background = false)
        }
        backgroundButton = button(backgroundText(), rightX, startY + 125, columnWidth) {
            openColorPicker(background = true)
        }
        marginButton = button(marginText(), fullX, startY + 150, fullWidth) {
            val current = FpsCounterConfig.margins.indexOf(style.backgroundMargin)
            style.backgroundMargin = FpsCounterConfig.margins[(current + 1) % FpsCounterConfig.margins.size]
            saveAndRefresh()
        }

        addRenderableWidget(
            Button.builder(Component.translatable("options.trashcanmodsreal.back")) {
                page = Page.SELECT_COUNTER
                rebuildWidgets()
            }.bounds(leftX, startY + 180, columnWidth, 20).build()
        )
        addRenderableWidget(
            Button.builder(Component.translatable("gui.done")) {
                minecraft?.gui?.setScreen(parent)
            }.bounds(rightX, startY + 180, columnWidth, 20).build()
        )
        refreshLabels()
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
        graphics.centeredText(font, title, width / 2, 18, 0xFFFFFFFF.toInt())
        if (page == Page.EDIT_REACH) {
            graphics.centeredText(
                font,
                Component.translatable("options.trashcanmodsreal.reach_singleplayer_only"),
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
    }

    private fun openColorPicker(background: Boolean) {
        val currentColor = if (background) style.backgroundColor else style.textColor
        val title = Component.translatable(
            if (background) "options.trashcanmodsreal.background_color_title"
            else "options.trashcanmodsreal.text_color_title"
        )
        minecraft.gui.setScreen(ColorPickerScreen(this, title, currentColor) { color ->
            if (background) style.backgroundColor = color else style.textColor = color
            FpsCounterConfig.save()
            refreshLabels()
        })
    }

    private fun drawPreview(graphics: GuiGraphicsExtractor) {
        val minecraft = Minecraft.getInstance()
        val previewWidth = minOf(340, width - 24)
        val x = (width - previewWidth) / 2
        val y = 268
        graphics.fill(x, y, x + previewWidth, y + 72, 0xA0000000.toInt())
        graphics.text(
            font,
            Component.translatable("options.trashcanmodsreal.preview"),
            x + 8,
            y + 5,
            0xFFE0E0E0.toInt()
        )
        val sample = if (editingCps) "CPS: ${CpsCounter.clicksPerSecond()}" else "FPS: ${minecraft.getFps()}"
        drawPreviewCounter(graphics, sample, x + 12, y + 30, style)
    }

    private fun drawPreviewCounter(graphics: GuiGraphicsExtractor, text: String, x: Int, y: Int, settings: CounterStyleSettings) {
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

    private fun displayColor(settings: CounterStyleSettings): Int = if (settings.rainbow) {
        val hue = (System.currentTimeMillis() % 3000L).toFloat() / 3000.0f
        hsvToColor(hue, 1f, 1f)
    } else {
        settings.textColor
    }

    private fun enabledText() = Component.translatable(
        if (style.enabled) "options.trashcanmodsreal.enabled" else "options.trashcanmodsreal.disabled"
    )

    private fun scaleText() = Component.translatable(
        "options.trashcanmodsreal.text_scale", style.textScalePercent
    )

    private fun rainbowText() = Component.translatable(
        if (style.rainbow) "options.trashcanmodsreal.rainbow_on" else "options.trashcanmodsreal.rainbow_off"
    )

    private fun shadowText() = Component.translatable(
        if (style.shadow) "options.trashcanmodsreal.shadow_on" else "options.trashcanmodsreal.shadow_off"
    )

    private fun positionText() = Component.translatable(
        "options.trashcanmodsreal.position", FpsCounterConfig.positions[style.positionIndex]
    )

    private fun textColorText() = Component.translatable(
        "options.trashcanmodsreal.text_color", rgbHex(style.textColor)
    )

    private fun backgroundText() = Component.translatable(
        "options.trashcanmodsreal.background_color", rgbHex(style.backgroundColor)
    )

    private fun marginText() = Component.translatable(
        "options.trashcanmodsreal.background_margin", style.backgroundMargin
    )
}

private fun modTitle(): Component {
    val version = FabricLoader.getInstance()
        .getModContainer("trashcanmodsreal")
        .orElseThrow()
        .metadata
        .version
        .friendlyString
    return Component.literal("Trash Can Mod - v$version")
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
        message = Component.translatable("options.trashcanmodsreal.extra_reach", extra)
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
                minecraft?.gui?.setScreen(parent)
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
            graphics.fill(x - segmentSize / 2, y - segmentSize / 2, x + segmentSize / 2, y + segmentSize / 2, hsvToColor(h, 1f, 1f))
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
