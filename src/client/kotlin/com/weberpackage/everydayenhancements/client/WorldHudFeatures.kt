package com.weberpackage.everydayenhancements.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraft.resources.Identifier
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.resources.model.sprite.SpriteId

object WorldHudFeatures {
    private var previousHealth = Float.MAX_VALUE
    private var deathPosition: BlockPos? = null
    private var deathDimension: ResourceKey<Level>? = null

    fun tick(client: Minecraft) {
        val player = client.player ?: run {
            previousHealth = Float.MAX_VALUE
            return
        }
        val health = player.health
        if (previousHealth > 0f && health <= 0f) {
            deathPosition = player.blockPosition()
            deathDimension = client.level?.dimension()
        }
        previousHealth = health
    }

    fun render(graphics: GuiGraphicsExtractor, client: Minecraft) {
        val player = client.player ?: return
        val level = client.level ?: return
        val biome = level.getBiome(player.blockPosition()).unwrapKey()
            .map { it.toString().substringAfterLast('/') }
            .orElse("unknown")
            .substringAfter(':')
            .replace('_', ' ')
            .replace(']', ' ')
        val offsets = IntArray(4)
        listOf(FpsCounterConfig.fps to "FPS: ${client.getFps()}", FpsCounterConfig.cps to "CPS: ${CpsCounter.clicksPerSecond()}")
            .filter { it.first.enabled }
            .forEach { (settings, text) -> offsets[settings.positionIndex] += counterHeight(client, settings, listOf(text)) + 2 }
        drawStyled(graphics, client, FpsCounterConfig.biome, listOf("Biome: $biome"), offsets)
        val pos = player.blockPosition()
        drawStyled(graphics, client, FpsCounterConfig.coordinates, listOf("XYZ: ${pos.x}, ${pos.y}, ${pos.z}"), offsets)

        val armor = armorItems(client)
        if (armor.isNotEmpty()) drawArmor(graphics, client, armor, offsets)

        val marker = deathPosition ?: return
        if (deathDimension != level.dimension()) return
        val distance = player.blockPosition().distManhattan(marker)
        drawStyled(graphics, client, FpsCounterConfig.death, listOf("Death: ${marker.x}, ${marker.y}, ${marker.z} ($distance blocks)"), offsets)
    }

    fun armorItems(client: Minecraft): List<Pair<net.minecraft.world.item.ItemStack, Int>> {
        val player = client.player ?: return emptyList()
        return listOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
            .mapNotNull { slot ->
            val stack = player.getItemBySlot(slot)
            if (stack.isEmpty() || !stack.isDamageableItem()) return@mapNotNull null
            val remaining = stack.getMaxDamage() - stack.getDamageValue()
            stack to remaining
        }
    }

    fun drawArmorPreview(graphics: GuiGraphicsExtractor, client: Minecraft, x: Int, y: Int, settings: CounterStyleSettings) {
        val samples = listOf(
            497 to "diamond_helmet",
            592 to "diamond_chestplate",
            555 to "diamond_leggings",
            481 to "diamond_boots"
        )
        val margin = settings.backgroundMargin
        val width = client.font.width("592") + 34 + margin * 2
        val rowHeight = 18
        val height = samples.size * rowHeight + margin * 2
        val left = x - width / 2
        if (settings.backgroundColor ushr 24 != 0) graphics.fill(left, y, left + width, y + height, settings.backgroundColor)
        samples.forEachIndexed { index, (durability, piece) ->
            val rowY = y + margin + index * rowHeight
            graphics.text(client.font, durability.toString(), left + margin, rowY + 1, settings.textColor, settings.shadow)
            val iconX = left + width - margin - 20
            val sprite = graphics.getSprite(
                SpriteId(TextureAtlas.LOCATION_ITEMS, Identifier.withDefaultNamespace("item/$piece"))
            )
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, iconX, rowY, 16, 16)
        }
    }

    private fun drawStyled(graphics: GuiGraphicsExtractor, client: Minecraft, settings: CounterStyleSettings, lines: List<String>, offsets: IntArray) {
        if (!settings.enabled) return
        val scale = settings.textScalePercent / 100f
        val margin = settings.backgroundMargin
        val lineHeight = kotlin.math.ceil(client.font.lineHeight * scale).toInt()
        val width = lines.maxOf { kotlin.math.ceil(client.font.width(it) * scale).toInt() } + margin * 2
        val height = lines.size * lineHeight + margin * 2
        val corner = settings.positionIndex
        val left = corner % 2 == 1
        val bottom = corner >= 2
        val x = if (left) 4 else graphics.guiWidth() - width - 4
        val offset = offsets[corner]
        val y = if (bottom) graphics.guiHeight() - height - 4 - offset else 4 + offset
        offsets[corner] += height + 2
        if (settings.backgroundColor ushr 24 != 0) graphics.fill(x, y, x + width, y + height, settings.backgroundColor)
        val pose = graphics.pose()
        pose.pushMatrix()
        try {
            pose.translate((x + margin).toFloat(), (y + margin).toFloat())
            pose.scale(scale, scale)
            lines.forEachIndexed { index, line ->
                graphics.text(client.font, line, 0, index * client.font.lineHeight, settings.textColor, settings.shadow)
            }
        } finally {
            pose.popMatrix()
        }
    }

    private fun counterHeight(client: Minecraft, settings: CounterStyleSettings, lines: List<String>): Int {
        val scale = settings.textScalePercent / 100f
        return lines.size * kotlin.math.ceil(client.font.lineHeight * scale).toInt() + settings.backgroundMargin * 2
    }

    private fun drawArmor(
        graphics: GuiGraphicsExtractor,
        client: Minecraft,
        armor: List<Pair<net.minecraft.world.item.ItemStack, Int>>,
        offsets: IntArray
    ) {
        val settings = FpsCounterConfig.armor
        if (!settings.enabled) return
        val scale = settings.textScalePercent / 100f
        val margin = settings.backgroundMargin
        val rowHeight = maxOf(18, kotlin.math.ceil(client.font.lineHeight * scale).toInt())
        val width = (client.font.width("9999") + 28) + margin * 2
        val height = armor.size * rowHeight + margin * 2
        val corner = settings.positionIndex
        val left = corner % 2 == 1
        val bottom = corner >= 2
        val x = if (left) 4 else graphics.guiWidth() - width - 4
        val y = if (bottom) graphics.guiHeight() - height - 4 - offsets[corner] else 4 + offsets[corner]
        offsets[corner] += height + 2
        drawArmorRows(graphics, client, armor, x, y, settings)
    }

    private fun drawArmorRows(graphics: GuiGraphicsExtractor, client: Minecraft, armor: List<Pair<net.minecraft.world.item.ItemStack, Int>>, x: Int, y: Int, settings: CounterStyleSettings) {
        val scale = settings.textScalePercent / 100f
        val margin = settings.backgroundMargin
        val rowHeight = maxOf(18, kotlin.math.ceil(client.font.lineHeight * scale).toInt())
        val width = client.font.width("9999") + 28 + margin * 2
        val height = armor.size * rowHeight + margin * 2
        if (settings.backgroundColor ushr 24 != 0) graphics.fill(x, y, x + width, y + height, settings.backgroundColor)
        armor.forEachIndexed { index, (stack, durability) ->
            val rowY = y + margin + index * rowHeight
            val pose = graphics.pose()
            pose.pushMatrix()
            try {
                pose.translate((x + margin).toFloat(), rowY.toFloat())
                pose.scale(scale, scale)
                graphics.text(client.font, durability.toString(), 0, 1, settings.textColor, settings.shadow)
            } finally {
                pose.popMatrix()
            }
            graphics.item(stack, x + width - margin - 20, rowY)
        }
    }
}
