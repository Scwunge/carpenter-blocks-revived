package com.carpentersblocks.neoforge.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;

import org.spongepowered.asm.mixin.Mixin;

import com.carpentersblocks.block.coverable.CoverableBlock;

/**
 * Adds NeoForge's plant hook to coverable blocks. Common code can't declare it because it returns NeoForge's
 * TriState; Fabric reaches {@link CoverableBlock#sustainsPlant} through plant mixins instead.
 */
@Mixin(CoverableBlock.class)
abstract class CoverableBlockMixin {

    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPos, Direction facing,
            BlockState plant) {
        Boolean sustains = ((CoverableBlock) (Object) this).sustainsPlant(state, level, soilPos, facing,
                CoverableBlock.plantTypeOf(plant));
        return sustains == null ? TriState.DEFAULT : sustains ? TriState.TRUE : TriState.FALSE;
    }
}
