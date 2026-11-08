package com.weberpackage.everydayenhancements.mixin.client;

import com.weberpackage.everydayenhancements.client.FpsCounterConfig;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapMixin {
    private static final Vector3f EVERYDAY_ENHANCEMENTS_WHITE = new Vector3f(1.0F, 1.0F, 1.0F);

    @Inject(method = "extract", at = @At("TAIL"))
    private void everydayenhancements$applyFullbright(LightmapRenderState state, float partialTick, CallbackInfo callback) {
        if (FpsCounterConfig.INSTANCE.getFullbright()) {
            state.nightVisionEffectIntensity = 1.0F;
            state.nightVisionColor = EVERYDAY_ENHANCEMENTS_WHITE;
            state.needsUpdate = true;
        }
    }
}
