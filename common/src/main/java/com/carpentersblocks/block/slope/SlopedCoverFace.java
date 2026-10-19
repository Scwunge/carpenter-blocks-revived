package com.carpentersblocks.block.slope;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import com.carpentersblocks.CarpentersBlocks;

/**
 * Which face of a cover a sloped quad shows, as the original's {@code getUniqueIcon}. Kept out of the client package
 * so the server-side tests can check it; {@code CoverQuads} draws with it.
 */
public final class SlopedCoverFace {

    /** Covers that show their top on upward slopes, like grass; anything extending SnowyDirtBlock counts too. */
    public static final TagKey<Block> SLOPE_TOP_COVERS = TagKey.create(Registries.BLOCK,
            CarpentersBlocks.id("slope_top_covers"));

    private SlopedCoverFace() {}

    /**
     * The side texture by default; pillar ends when the pillar stands upright (bark when it lies down); the top for
     * directional blocks on anything but plain wedges; and the top of grass-like blocks on upward slopes.
     *
     * @param positive the slope faces up
     * @param side     the horizontal direction the slope faces
     * @param wedge    the quad belongs to a plain wedge
     */
    public static Direction of(BlockState cover, boolean positive, Direction side, boolean wedge) {
        Direction face = side;
        if (cover.hasProperty(BlockStateProperties.AXIS)) {
            Direction.Axis axis = cover.getValue(BlockStateProperties.AXIS);
            if (axis == Direction.Axis.Y) {
                face = positive ? Direction.UP : Direction.DOWN;
            } else if (side.getAxis() == axis) {
                face = Direction.UP;
            }
        } else if (!wedge && cover.getBlock() instanceof HorizontalDirectionalBlock) {
            face = Direction.UP;
        }
        if (positive && (cover.is(SLOPE_TOP_COVERS) || cover.getBlock() instanceof SnowyDirtBlock)) {
            face = Direction.UP;
        }
        return face;
    }
}
