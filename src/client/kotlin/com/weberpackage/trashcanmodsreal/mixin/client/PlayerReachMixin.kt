package com.weberpackage.trashcanmodsreal.mixin.client

import com.weberpackage.trashcanmodsreal.client.FpsCounterConfig
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(Player::class)
abstract class PlayerReachMixin {
    @Inject(method = ["blockInteractionRange"], at = [At("RETURN")], cancellable = true)
    private fun addBlockReach(callback: CallbackInfoReturnable<Double>) {
        if (isPrivateSingleplayerWorld()) {
            callback.returnValue = callback.returnValue + FpsCounterConfig.extraReach
        }
    }

    @Inject(method = ["entityInteractionRange"], at = [At("RETURN")], cancellable = true)
    private fun addEntityReach(callback: CallbackInfoReturnable<Double>) {
        if (isPrivateSingleplayerWorld()) {
            callback.returnValue = callback.returnValue + FpsCounterConfig.extraReach
        }
    }

    private fun isPrivateSingleplayerWorld(): Boolean {
        val client = Minecraft.getInstance()
        return client.hasSingleplayerServer() && client.singleplayerServer?.isPublished == false
    }
}
