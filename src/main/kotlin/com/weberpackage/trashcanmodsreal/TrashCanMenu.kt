package com.weberpackage.trashcanmodsreal

import net.minecraft.network.chat.Component
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.MenuType

open class TrashCanMenu(
    containerId: Int,
    playerInventory: Inventory,
    private val contents: SimpleContainer,
    rows: Int
) : ChestMenu(menuTypeForRows(rows), containerId, playerInventory, contents, rows) {

    override fun removed(player: Player) {
        super.removed(player)

        val deletedCount = (0 until contents.containerSize).sumOf { slot ->
            contents.getItem(slot).count
        }
        contents.clearContent()

        if (deletedCount > 0) {
            player.sendSystemMessage(Component.translatable("message.trashcanmodsreal.items_deleted", deletedCount))
        }
    }
}

class TrashCanMenuDiamond(containerId: Int, playerInventory: Inventory, contents: SimpleContainer) :
    TrashCanMenu(containerId, playerInventory, contents, 3)

private fun menuTypeForRows(rows: Int): MenuType<*> = when (rows) {
    1 -> MenuType.GENERIC_9x1
    2 -> MenuType.GENERIC_9x2
    3 -> MenuType.GENERIC_9x3
    else -> error("Trash can menu rows must be between 1 and 3")
}
