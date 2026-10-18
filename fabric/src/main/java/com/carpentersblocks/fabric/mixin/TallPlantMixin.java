package com.carpentersblocks.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import com.carpentersblocks.fabric.PlantSupport;

/**
 * Cactus and sugar cane accept a coverable block with sand (or, for cane, dirt) cover in place of their soil tag
 * checks. Their other rules (no solid blocks beside cactus, water beside cane) still apply.
 */
@Mixin({ CactusBlock.class, SugarCaneBlock.class })
abstract class TallPlantMixin {

    @WrapOperation(method = "canSurvive", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean carpentersblocks$coverSoil(BlockState soil, TagKey<Block> tag, Operation<Boolean> original,
            @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) BlockPos plantPos) {
        Boolean sustains = PlantSupport.sustains(soil, level, plantPos.below(), (Block) (Object) this);
        return sustains != null ? sustains : original.call(soil, tag);
    }
}
