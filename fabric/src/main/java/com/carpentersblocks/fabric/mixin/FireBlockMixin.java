package com.carpentersblocks.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import com.carpentersblocks.block.coverable.CoverableBlock;

/**
 * Makes coverable blocks burn like their covers. Vanilla looks fire values up by block state alone; these call
 * sites know the position, so the cover can be found. NeoForge has block hooks for this instead.
 */
@Mixin(FireBlock.class)
abstract class FireBlockMixin {

    @WrapOperation(method = "checkBurnOut", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/FireBlock;getBurnOdds(Lnet/minecraft/world/level/block/state/BlockState;)I"))
    private int carpentersblocks$coverBurnOdds(FireBlock fire, BlockState state, Operation<Integer> original,
            @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        if (state.getBlock() instanceof CoverableBlock block) {
            return block.getFlammability(state, level, pos, null);
        }
        return original.call(fire, state);
    }

    @WrapOperation(method = "getIgniteOdds(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)I",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/FireBlock;getIgniteOdds(Lnet/minecraft/world/level/block/state/BlockState;)I"))
    private int carpentersblocks$coverIgniteOdds(FireBlock fire, BlockState state, Operation<Integer> original,
            @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) BlockPos firePos,
            @Local Direction direction) {
        if (state.getBlock() instanceof CoverableBlock block) {
            return block.getFireSpreadSpeed(state, level, firePos.relative(direction), direction.getOpposite());
        }
        return original.call(fire, state);
    }
}
