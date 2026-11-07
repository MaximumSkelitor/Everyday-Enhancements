package com.weberpackage.everydayenhancements

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class TrashCanBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(EverydayEnhancements.TRASH_CAN_BLOCK_ENTITY_TYPE, pos, state), MenuProvider {

    private val rows = (state.block as TrashCanBlock).rows
    private val contents = SimpleContainer(rows * 9)

    override fun getDisplayName(): Component = Component.translatable(
        "block.everydayenhancements.trash_can_${when (rows) {
            1 -> "iron"
            2 -> "gold"
            else -> "diamond"
        }}"
    )

    override fun createMenu(containerId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu =
        if (rows == 3) {
            TrashCanMenuDiamond(containerId, playerInventory, contents)
        } else {
            TrashCanMenu(containerId, playerInventory, contents, rows)
        }
}
