package com.carpentersblocks.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DeadBushBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.carpentersblocks.fabric.PlantSupport;

/**
 * Flowers, saplings, dead bushes and nether wart grow on coverable blocks with the right soil cover. NeoForge asks
 * the block through canSustainPlant instead. Dead bush and nether wart replace mayPlaceOn without calling it.
 */
@Mixin({ BushBlock.class, DeadBushBlock.class, NetherWartBlock.class })
abstract class BushBlockMixin {

    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void carpentersblocks$coverSoil(BlockState soil, BlockGetter level, BlockPos pos,
            CallbackInfoReturnable<Boolean> cir) {
        Boolean sustains = PlantSupport.sustains(soil, level, pos, (Block) (Object) this);
        if (sustains != null) {
            cir.setReturnValue(sustains);
        }
    }
}
