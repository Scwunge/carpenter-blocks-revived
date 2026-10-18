package com.carpentersblocks.block;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

/**
 * Shapes a Carpenter's Block can take: a full cube, or a slab resting against one of the six faces.
 * Replaces the original {@code Slab} data values that were stored in the tile entity.
 */
public enum FrameShape implements StringRepresentable {

    FULL("full", null, Block.box(0, 0, 0, 16, 16, 16)),
    DOWN("down", Direction.DOWN, Block.box(0, 0, 0, 16, 8, 16)),
    UP("up", Direction.UP, Block.box(0, 8, 0, 16, 16, 16)),
    NORTH("north", Direction.NORTH, Block.box(0, 0, 0, 16, 16, 8)),
    SOUTH("south", Direction.SOUTH, Block.box(0, 0, 8, 16, 16, 16)),
    WEST("west", Direction.WEST, Block.box(0, 0, 0, 8, 16, 16)),
    EAST("east", Direction.EAST, Block.box(8, 0, 0, 16, 16, 16));

    private final String name;
    @Nullable
    private final Direction side;
    private final VoxelShape shape;

    FrameShape(String name, @Nullable Direction side, VoxelShape shape) {
        this.name = name;
        this.side = side;
        this.shape = shape;
    }

    public VoxelShape shape() {
        return shape;
    }

    /** Whether the given face of the block space is completely covered by this shape. */
    public boolean coversFace(Direction face) {
        return side == null || side == face;
    }

    /** The slab that rests against the given face. */
    public static FrameShape slabAgainst(Direction face) {
        for (FrameShape shape : values()) {
            if (shape.side == face) {
                return shape;
            }
        }
        return FULL;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
