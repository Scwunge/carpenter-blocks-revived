package com.carpentersblocks.fabric;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.block.coverable.CoverableBlock;

/** Shared by the plant mixins: whether {@code soil} (a block under a plant) lets the plant grow. */
public final class PlantSupport {

    private PlantSupport() {}

    /** {@code null} when the soil is not a coverable block, or it leaves the plant to vanilla's rules. */
    @Nullable
    public static Boolean sustains(BlockState soil, BlockGetter level, BlockPos soilPos, Block plant) {
        if (soil.getBlock() instanceof CoverableBlock block) {
            return block.sustainsPlant(soil, level, soilPos, Direction.UP,
                    CoverableBlock.plantTypeOf(plant.defaultBlockState()));
        }
        return null;
    }
}
