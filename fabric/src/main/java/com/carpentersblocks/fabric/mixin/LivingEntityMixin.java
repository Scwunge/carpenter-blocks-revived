package com.carpentersblocks.fabric.mixin;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.door.CarpentersHatchBlock;

/**
 * Walking on a coverable block is as slippery as its top face, and an open hidden hatch can be climbed. NeoForge has
 * block hooks for these instead.
 */
@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

    @Shadow
    private Optional<BlockPos> lastClimbablePos;

    private LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @ModifyReturnValue(method = "onClimbable", at = @At("RETURN"))
    private boolean carpentersblocks$hatchLadder(boolean climbable) {
        if (climbable || isSpectator()) {
            return climbable;
        }
        BlockPos pos = blockPosition();
        BlockState state = getInBlockState();
        if (state.getBlock() instanceof CarpentersHatchBlock hatch
                && hatch.isLadder(state, level(), pos, (LivingEntity) (Object) this)) {
            lastClimbablePos = Optional.of(pos);
            return true;
        }
        return false;
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
