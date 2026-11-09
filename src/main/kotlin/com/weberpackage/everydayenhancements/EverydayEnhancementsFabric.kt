package com.weberpackage.everydayenhancements

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
import net.minecraft.world.item.CreativeModeTabs

class EverydayEnhancementsFabric : ModInitializer {
    override fun onInitialize() {
        EverydayEnhancements.initialize()
        
        // Register trash cans in the Functional Blocks creative tab
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register { output ->
            output.accept(EverydayEnhancements.TRASH_CAN_IRON.asItem())
            output.accept(EverydayEnhancements.TRASH_CAN_GOLD.asItem())
            output.accept(EverydayEnhancements.TRASH_CAN_DIAMOND.asItem())
        }
    }
}
