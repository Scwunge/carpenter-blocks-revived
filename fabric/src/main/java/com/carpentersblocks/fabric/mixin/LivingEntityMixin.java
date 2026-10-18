package com.carpentersblocks.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.carpentersblocks.block.coverable.CoverableBlock;

/** Walking on a coverable block is as slippery as its top face. NeoForge has a block hook for this instead. */
@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

    private LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @WrapOperation(method = "travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
    private float carpentersblocks$coverFriction(Block block, Operation<Float> original) {
        if (block instanceof CoverableBlock coverable) {
            BlockPos pos = getBlockPosBelowThatAffectsMyMovement();
            return coverable.getFriction(level().getBlockState(pos), level(), pos, this);
        }
        return original.call(block);
    }
}
