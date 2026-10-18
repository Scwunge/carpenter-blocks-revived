package com.carpentersblocks.fabric.mixin;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.carpentersblocks.block.coverable.CoverableBlock;

/** Lets a coverable block resist explosions like its cover. NeoForge has a block hook for this; Fabric doesn't. */
@Mixin(ExplosionDamageCalculator.class)
abstract class ExplosionDamageCalculatorMixin {

    @Inject(method = "getBlockExplosionResistance", at = @At("HEAD"), cancellable = true)
    private void carpentersblocks$coverResistance(Explosion explosion, BlockGetter level, BlockPos pos,
            BlockState state, FluidState fluid, CallbackInfoReturnable<Optional<Float>> cir) {
        if (state.getBlock() instanceof CoverableBlock block) {
            cir.setReturnValue(Optional.of(Math.max(block.getExplosionResistance(state, level, pos, explosion),
                    fluid.getExplosionResistance())));
        }
    }
}
