package com.carpentersblocks.fabric.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import com.carpentersblocks.block.coverable.CoverableBlock;

/** Breaking a coverable block sounds like its base cover (the break event fires while the cover is still there). */
@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {

    @Shadow
    private ClientLevel level;

    @WrapOperation(method = "levelEvent", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType carpentersblocks$coverBreakSound(BlockState state, Operation<SoundType> original,
            @Local(argsOnly = true) BlockPos pos) {
        if (state.getBlock() instanceof CoverableBlock block) {
            return block.getSoundType(state, level, pos, null);
        }
        return original.call(state);
    }
}
