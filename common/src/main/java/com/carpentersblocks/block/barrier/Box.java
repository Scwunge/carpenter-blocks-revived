package com.carpentersblocks.block.barrier;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A box in pixels (0 to 16), drawn for a south-facing block and turned to other facings as the original's
 * {@code BlockHandlerBase.rotateBounds}.
 */
public record Box(double x1, double y1, double z1, double x2, double y2, double z2) {

    /** From the original's block units (0 to 1). */
    public static Box units(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new Box(x1 * 16, y1 * 16, z1 * 16, x2 * 16, y2 * 16, z2 * 16);
    }

    /** This box turned from facing south to facing {@code dir}; vertical directions leave it as it is. */
    public Box facing(Direction dir) {
        return switch (dir) {
            case NORTH -> new Box(16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1);
            case EAST -> new Box(z1, y1, 16 - x2, z2, y2, 16 - x1);
            case WEST -> new Box(16 - z2, y1, x1, 16 - z1, y2, x2);
            default -> this;
        };
    }

    /**
     * This box, drawn for a block on the south face of its support (against z = 0), turned to a block on the
     * {@code facing} face, as the original's direction-aware {@code setBlockBounds}. Unlike {@link #facing}, up and
     * down lay it flat.
     */
    public Box attached(Direction facing) {
        return switch (facing) {
            case UP -> new Box(x1, z1, y1, x2, z2, y2);
            case DOWN -> new Box(x1, 16 - z2, y1, x2, 16 - z1, y2);
            default -> facing(facing);
        };
    }

    public boolean isEmpty() {
        return x1 >= x2 || y1 >= y2 || z1 >= z2;
    }

    public VoxelShape shape() {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

    public static VoxelShape shape(List<Box> boxes) {
        VoxelShape shape = Shapes.empty();
        for (Box box : boxes) {
            shape = Shapes.or(shape, box.shape());
        }
        return shape.optimize();
    }
}
