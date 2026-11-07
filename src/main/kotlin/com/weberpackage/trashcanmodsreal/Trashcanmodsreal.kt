package com.weberpackage.trashcanmodsreal

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour

class Trashcanmodsreal : ModInitializer {

    override fun onInitialize() {
        TRASH_CAN_IRON = registerTrashCan("trash_can_iron", 1)
        TRASH_CAN_GOLD = registerTrashCan("trash_can_gold", 2)
        TRASH_CAN_DIAMOND = registerTrashCan("trash_can_diamond", 3)

        val blockEntityKey = ResourceKey.create(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("trashcanmodsreal", "trash_can")
        )
        TRASH_CAN_BLOCK_ENTITY_TYPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            blockEntityKey,
            BlockEntityType(
                ::TrashCanBlockEntity,
                setOf(TRASH_CAN_IRON, TRASH_CAN_GOLD, TRASH_CAN_DIAMOND)
            )
        )

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register { output ->
            output.accept(TRASH_CAN_IRON.asItem())
            output.accept(TRASH_CAN_GOLD.asItem())
            output.accept(TRASH_CAN_DIAMOND.asItem())
        }
    }

    private fun registerTrashCan(path: String, rows: Int): TrashCanBlock {
        val id = Identifier.fromNamespaceAndPath("trashcanmodsreal", path)
        val blockKey = ResourceKey.create(Registries.BLOCK, id)
        val itemKey = ResourceKey.create(Registries.ITEM, id)
        val block = Blocks.register(
            blockKey,
            { properties -> TrashCanBlock(properties, rows) },
            BlockBehaviour.Properties.of().strength(2.0f).setId(blockKey)
        ) as TrashCanBlock
        Registry.register(
            BuiltInRegistries.ITEM,
            itemKey,
            BlockItem(block, Item.Properties().setId(itemKey))
        )
        return block
    }

    companion object {
        lateinit var TRASH_CAN_IRON: TrashCanBlock
            private set

        lateinit var TRASH_CAN_GOLD: TrashCanBlock
            private set

        lateinit var TRASH_CAN_DIAMOND: TrashCanBlock
            private set

        lateinit var TRASH_CAN_BLOCK_ENTITY_TYPE: BlockEntityType<TrashCanBlockEntity>
            private set
    }
}
