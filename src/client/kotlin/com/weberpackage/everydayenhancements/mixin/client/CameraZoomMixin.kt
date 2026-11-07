package com.weberpackage.everydayenhancements.mixin.client

import com.weberpackage.everydayenhancements.client.ZoomFeature
import net.minecraft.client.Camera
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.ModifyArg

@Mixin(Camera::class)
abstract class CameraZoomMixin {
    @ModifyArg(
        method = ["update"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setupPerspective(FFFFF)V"
        )],
        index = 2
    )
    private fun applyZoom(fov: Float): Float {
        if (!ZoomFeature.active) return fov

        return (fov.toDouble() / ZoomFeature.multiplier)
            .coerceAtLeast(0.0001)
            .toFloat()
    }
}
