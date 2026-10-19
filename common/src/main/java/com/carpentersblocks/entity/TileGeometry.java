package com.carpentersblocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

/** Where a Carpenter's Tile sits and which neighbor it copies, kept apart from the entity for tests. */
public final class TileGeometry {

    /** Thickness of a tile, in blocks. */
    public static final double DEPTH = 1.0 / 16;
    /** How close to an edge of the face (as a fraction) counts as clicking near it, as the original. */
    public static final double EDGE = 0.2;

    private TileGeometry() {}

    /** The tile's box in the cell {@code pos}, against the face of the block behind it that points {@code direction}. */
    public static AABB box(BlockPos pos, Direction direction) {
        AABB cell = new AABB(pos);
        return switch (direction) {
            case DOWN -> cell.setMinY(cell.maxY - DEPTH);
            case UP -> cell.setMaxY(cell.minY + DEPTH);
            case NORTH -> cell.setMinZ(cell.maxZ - DEPTH);
            case SOUTH -> cell.setMaxZ(cell.minZ + DEPTH);
            case WEST -> cell.setMinX(cell.maxX - DEPTH);
            case EAST -> cell.setMaxX(cell.minX + DEPTH);
        };
    }

    /**
     * The edge of the clicked face nearest the click when it is within {@link #EDGE} of one, else {@code null} for the
     * middle. {@code hit} is relative to the clicked block.
     */
    @Nullable
    public static Direction edge(Direction face, Vec3 hit) {
        Direction nearest = null;
        double best = Double.MAX_VALUE;
        for (Direction side : Direction.values()) {
            if (side.getAxis() == face.getAxis()) {
                continue;
            }
            double coord = side.getAxis().choose(hit.x, hit.y, hit.z);
            double distance = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 - coord : coord;
            if (distance < best) {
                best = distance;
                nearest = side;
            }
        }
        return best <= EDGE ? nearest : null;
    }
}
