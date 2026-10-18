package com.carpentersblocks.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import com.carpentersblocks.block.coverable.CoverableBlock;

/** Footsteps on a coverable block sound like its top face. */
@Mixin(Entity.class)
abstract class EntityMixin {

    @Shadow
    public abstract Level level();

    @WrapOperation(method = "playStepSound", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType carpentersblocks$coverStepSound(BlockState state, Operation<SoundType> original,
            @Local(argsOnly = true) BlockPos pos) {
        if (state.getBlock() instanceof CoverableBlock block) {
            return block.getSoundType(state, level(), pos, (Entity) (Object) this);
        }
        return original.call(state);
    }
}
