package com.carpentersblocks.block.slope;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.config.CBConfig;

/**
 * Rail slope fill: a coverable block with a solid top under an ascending rail draws a wedge of its covers in the rail's
 * space, so the rail doesn't float over a gap. Rendering only, as the original's {@code enableRailSlopes}. Any rail
 * extending {@link BaseRailBlock} counts, modded ones included.
 */
public final class RailSlopes {

    private RailSlopes() {}

    /** The wedge to fill above the coverable block at {@code pos}, or {@code null} if there is none. */
    @Nullable
    public static Slope slopeAbove(BlockGetter level, BlockPos pos, BlockState state) {
        if (!CBConfig.enableRailSlopes) {
            return null;
        }
        BlockState above = level.getBlockState(pos.above());
        if (!(above.getBlock() instanceof BaseRailBlock rail) || !state.isFaceSturdy(level, pos, Direction.UP)) {
            return null;
        }
        return switch (above.getValue(rail.getShapeProperty())) {
            case ASCENDING_EAST -> Slope.WEDGE_POS_W;
            case ASCENDING_WEST -> Slope.WEDGE_POS_E;
            case ASCENDING_NORTH -> Slope.WEDGE_POS_S;
            case ASCENDING_SOUTH -> Slope.WEDGE_POS_N;
            default -> null;
        };
    }
}
