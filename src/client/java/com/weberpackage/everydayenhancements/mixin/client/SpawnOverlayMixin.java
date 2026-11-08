package com.weberpackage.everydayenhancements.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.weberpackage.everydayenhancements.client.FpsCounterConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class SpawnOverlayMixin {
    @Inject(method = "submitFeatures", at = @At("HEAD"))
    private void everydayenhancements$submitSpawnMarkers(LevelRenderState renderState, SubmitNodeCollector collector, boolean outline, CallbackInfo callback) {
        if (!FpsCounterConfig.INSTANCE.getSpawnOverlay()) return;
        Minecraft client = Minecraft.getInstance();
        Level level = client.level;
        if (level == null || client.player == null) return;
        BlockPos origin = client.player.blockPosition();
        Vec3 camera = renderState.cameraRenderState.pos;
        PoseStack pose = new PoseStack();
        int radius = 8;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radius * radius) continue;
                for (int y = -3; y <= 3; y++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || !state.isFaceSturdy(level, pos, net.minecraft.core.Direction.UP)) continue;
                    BlockPos above = pos.above();
                    if (!level.getBlockState(above).isAir() || !level.getBlockState(above.above()).isAir()) continue;
                    boolean canSpawn = level.getBrightness(LightLayer.BLOCK, above) == 0;
                    Vec3 markerPos = new Vec3(pos.getX() + 0.5 - camera.x, pos.getY() + 1.08 - camera.y, pos.getZ() + 0.5 - camera.z);
                    Component marker = Component.literal(canSpawn ? "Y" : "X")
                            .withStyle(canSpawn ? ChatFormatting.GREEN : ChatFormatting.RED);
                    collector.submitNameTag(pose, markerPos, 0, marker, false, 15728880, renderState.cameraRenderState);
                }
            }
        }
    }
}
