package com.weberpackage.everydayenhancements.mixin.client

import com.weberpackage.everydayenhancements.client.CpsCounter
import com.weberpackage.everydayenhancements.client.ZoomFeature
import net.minecraft.client.Minecraft
import net.minecraft.client.MouseHandler
import net.minecraft.client.input.MouseButtonInfo
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(MouseHandler::class)
abstract class TrashCanMouseMixin {
    @Inject(method = ["onButton"], at = [At("HEAD")])
    private fun recordClick(window: Long, buttonInfo: MouseButtonInfo, action: Int, callback: CallbackInfo) {
        if (action != 1) return // GLFW_PRESS

        val client = Minecraft.getInstance()
        if (client.level == null || client.gui.screen() != null) return

        CpsCounter.record(buttonInfo.button())
    }

    @Inject(method = ["onScroll"], at = [At("HEAD")], cancellable = true)
    private fun adjustZoom(window: Long, horizontal: Double, vertical: Double, callback: CallbackInfo) {
        if (!ZoomFeature.active) return

        val client = Minecraft.getInstance()
        if (client.level == null || client.gui.screen() != null) return

        ZoomFeature.adjustFromScroll(vertical)
        callback.cancel()
    }
}
