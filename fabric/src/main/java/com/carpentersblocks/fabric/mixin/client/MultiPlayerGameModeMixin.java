package com.carpentersblocks.fabric.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import com.carpentersblocks.block.coverable.CoverableBlock;

/** Digging a coverable block sounds like its base cover. */
@Mixin(MultiPlayerGameMode.class)
abstract class MultiPlayerGameModeMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(method = "continueDestroyBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType carpentersblocks$coverDigSound(BlockState state, Operation<SoundType> original,
            @Local(argsOnly = true) BlockPos pos) {
        if (state.getBlock() instanceof CoverableBlock block && minecraft.level != null) {
            return block.getSoundType(state, minecraft.level, pos, null);
        }
        return original.call(state);
    }
}
