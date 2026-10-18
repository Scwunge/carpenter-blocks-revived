package com.carpentersblocks.block.stairs;

import static com.carpentersblocks.block.stairs.StairsShape.EXT_NEG_NE;
import static com.carpentersblocks.block.stairs.StairsShape.EXT_NEG_NW;
import static com.carpentersblocks.block.stairs.StairsShape.EXT_NEG_SE;
import static com.carpentersblocks.block.stairs.StairsShape.EXT_NEG_SW;
import static com.carpentersblocks.block.stairs.StairsShape.EXT_POS_NE;
import static com.carpentersblocks.block.stairs.StairsShape.EXT_POS_NW;
import static com.carpentersblocks.block.stairs.StairsShape.EXT_POS_SE;
import static com.carpentersblocks.block.stairs.StairsShape.EXT_POS_SW;
import static com.carpentersblocks.block.stairs.StairsShape.INT_NEG_NE;
import static com.carpentersblocks.block.stairs.StairsShape.INT_NEG_NW;
import static com.carpentersblocks.block.stairs.StairsShape.INT_NEG_SE;
import static com.carpentersblocks.block.stairs.StairsShape.INT_NEG_SW;
import static com.carpentersblocks.block.stairs.StairsShape.INT_POS_NE;
import static com.carpentersblocks.block.stairs.StairsShape.INT_POS_NW;
import static com.carpentersblocks.block.stairs.StairsShape.INT_POS_SE;
import static com.carpentersblocks.block.stairs.StairsShape.INT_POS_SW;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.WEST;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

/**
 * Joins newly placed stairs with the stairs around them, as the original {@code StairsTransform}. Neighbors are
 * looked up through a function returning the shape of the Carpenter's Stairs in a direction, or {@code null}.
 */
public final class StairsTransform {

    private StairsTransform() {}

    /** The shape placed stairs take to connect with their neighbors. */
    public static StairsShape transform(StairsShape stairs, Function<Direction, @Nullable StairsShape> neighbors) {
        StairsShape xn = neighbors.apply(WEST);
        StairsShape xp = neighbors.apply(EAST);
        StairsShape yn = neighbors.apply(Direction.DOWN);
        StairsShape yp = neighbors.apply(Direction.UP);
        StairsShape zn = neighbors.apply(NORTH);
        StairsShape zp = neighbors.apply(SOUTH);

        // Continue a column of side stairs
        if (yn != null && yn.type() == StairsShape.Type.SIDE) {
            return yn;
        }
        if (yp != null && yp.type() == StairsShape.Type.SIDE) {
            return yp;
        }

        // Corners between two neighbors
        if (zn != null) {
            if (xn != null) {
                if (zn.faces(WEST) && xn.faces(NORTH)) {
                    return pick(xn.isPositive() && zn.isPositive(), INT_POS_NW, INT_NEG_NW);
                }
                if (zn.faces(EAST) && xn.faces(SOUTH)) {
                    return pick(xn.isPositive() && zn.isPositive(), EXT_POS_SE, EXT_NEG_SE);
                }
            }
            if (xp != null) {
                if (zn.faces(EAST) && xp.faces(NORTH)) {
                    return pick(xp.isPositive() && zn.isPositive(), INT_POS_NE, INT_NEG_NE);
                }
                if (zn.faces(WEST) && xp.faces(SOUTH)) {
                    return pick(xp.isPositive() && zn.isPositive(), EXT_POS_SW, EXT_NEG_SW);
                }
            }
        }
        if (zp != null) {
            if (xn != null) {
                if (zp.faces(WEST) && xn.faces(SOUTH)) {
                    return pick(xn.isPositive() && zp.isPositive(), INT_POS_SW, INT_NEG_SW);
                }
                if (zp.faces(EAST) && xn.faces(NORTH)) {
                    return pick(xn.isPositive() && zp.isPositive(), EXT_POS_NE, EXT_NEG_NE);
                }
            }
            if (xp != null) {
                if (zp.faces(EAST) && xp.faces(SOUTH)) {
                    return pick(xp.isPositive() && zp.isPositive(), INT_POS_SE, INT_NEG_SE);
                }
                if (zp.faces(WEST) && xp.faces(NORTH)) {
                    return pick(xp.isPositive() && zp.isPositive(), EXT_POS_NW, EXT_NEG_NW);
                }
            }
        }

        // Corners against a single neighbor
        if (xn != null) {
            if (stairs.faces(WEST)) {
                if (xn.faces(SOUTH) && !xn.faces(EAST)) {
                    return pick(xn.isPositive(), INT_POS_SW, INT_NEG_SW);
                }
                if (xn.faces(NORTH) && !xn.faces(EAST)) {
                    return pick(xn.isPositive(), INT_POS_NW, INT_NEG_NW);
                }
            }
            if (stairs.faces(EAST)) {
                if (xn.faces(SOUTH) && !xn.faces(EAST)) {
                    return pick(xn.isPositive(), EXT_POS_SE, EXT_NEG_SE);
                }
                if (xn.faces(NORTH) && !xn.faces(EAST)) {
                    return pick(xn.isPositive(), EXT_POS_NE, EXT_NEG_NE);
                }
            }
        }
        if (xp != null) {
            if (stairs.faces(WEST)) {
                if (xp.faces(SOUTH) && !xp.faces(WEST)) {
                    return pick(xp.isPositive(), EXT_POS_SW, EXT_NEG_SW);
                }
                if (xp.faces(NORTH) && !xp.faces(WEST)) {
                    return pick(xp.isPositive(), EXT_POS_NW, EXT_NEG_NW);
                }
            }
            if (stairs.faces(EAST)) {
                if (xp.faces(SOUTH) && !xp.faces(WEST)) {
                    return pick(xp.isPositive(), INT_POS_SE, INT_NEG_SE);
                }
                if (xp.faces(NORTH) && !xp.faces(WEST)) {
                    return pick(xp.isPositive(), INT_POS_NE, INT_NEG_NE);
                }
            }
        }
        if (zn != null) {
            if (stairs.faces(NORTH)) {
                if (zn.faces(EAST) && !zn.faces(SOUTH)) {
                    return pick(zn.isPositive(), INT_POS_NE, INT_NEG_NE);
                }
                if (zn.faces(WEST) && !zn.faces(SOUTH)) {
                    return pick(zn.isPositive(), INT_POS_NW, INT_NEG_NW);
                }
            }
            if (stairs.faces(SOUTH)) {
                if (zn.faces(EAST) && !zn.faces(SOUTH)) {
                    return pick(zn.isPositive(), EXT_POS_SE, EXT_NEG_SE);
                }
                if (zn.faces(WEST) && !zn.faces(SOUTH)) {
                    return pick(zn.isPositive(), EXT_POS_SW, EXT_NEG_SW);
                }
            }
        }
        if (zp != null) {
            if (stairs.faces(NORTH)) {
                if (zp.faces(EAST) && !zp.faces(NORTH)) {
                    return pick(zp.isPositive(), EXT_POS_NE, EXT_NEG_NE);
                }
                if (zp.faces(WEST) && !zp.faces(NORTH)) {
                    return pick(zp.isPositive(), EXT_POS_NW, EXT_NEG_NW);
                }
            }
            if (stairs.faces(SOUTH)) {
                if (zp.faces(EAST) && !zp.faces(NORTH)) {
                    return pick(zp.isPositive(), INT_POS_SE, INT_NEG_SE);
                }
                if (zp.faces(WEST) && !zp.faces(NORTH)) {
                    return pick(zp.isPositive(), INT_POS_SW, INT_NEG_SW);
                }
            }
        }

        return stairs;
    }

    /**
     * New shapes for the horizontal neighbors so they turn into corners meeting the placed stairs. Neighbors that
     * stay as they are are left out.
     */
    public static Map<Direction, StairsShape> transformAdjacent(StairsShape stairs,
            Function<Direction, @Nullable StairsShape> neighbors) {
        Map<Direction, StairsShape> changes = new EnumMap<>(Direction.class);
        boolean positive = stairs.isPositive();

        // Later rules win, as in the original where each overwrote the neighbor's data
        if (stairs.faces(WEST)) {
            adjust(changes, neighbors, NORTH, positive, NORTH, EXT_POS_NW, EXT_NEG_NW, SOUTH, INT_POS_SW, INT_NEG_SW);
            adjust(changes, neighbors, SOUTH, positive, SOUTH, EXT_POS_SW, EXT_NEG_SW, NORTH, INT_POS_NW, INT_NEG_NW);
        }
        if (stairs.faces(EAST)) {
            adjust(changes, neighbors, NORTH, positive, NORTH, EXT_POS_NE, EXT_NEG_NE, SOUTH, INT_POS_SE, INT_NEG_SE);
            adjust(changes, neighbors, SOUTH, positive, SOUTH, EXT_POS_SE, EXT_NEG_SE, NORTH, INT_POS_NE, INT_NEG_NE);
        }
        if (stairs.faces(NORTH)) {
            adjust(changes, neighbors, WEST, positive, WEST, EXT_POS_NW, EXT_NEG_NW, EAST, INT_POS_NE, INT_NEG_NE);
            adjust(changes, neighbors, EAST, positive, EAST, EXT_POS_NE, EXT_NEG_NE, WEST, INT_POS_NW, INT_NEG_NW);
        }
        if (stairs.faces(SOUTH)) {
            adjust(changes, neighbors, WEST, positive, WEST, EXT_POS_SW, EXT_NEG_SW, EAST, INT_POS_SE, INT_NEG_SE);
            adjust(changes, neighbors, EAST, positive, EAST, EXT_POS_SE, EXT_NEG_SE, WEST, INT_POS_SW, INT_NEG_SW);
        }
        return changes;
    }

    /**
     * If the neighbor in {@code side} is the same way up, turns it exterior when it faces {@code outward} and then
     * interior when it faces {@code inward}, the second check winning as in the original.
     */
    private static void adjust(Map<Direction, StairsShape> changes, Function<Direction, @Nullable StairsShape> neighbors,
            Direction side, boolean positive, Direction outward, StairsShape extPos, StairsShape extNeg,
            Direction inward, StairsShape intPos, StairsShape intNeg) {
        StairsShape neighbor = neighbors.apply(side);
        if (neighbor == null || neighbor.isPositive() != positive) {
            return;
        }
        if (neighbor.faces(outward)) {
            changes.put(side, pick(positive, extPos, extNeg));
        }
        if (neighbor.faces(inward)) {
            changes.put(side, pick(positive, intPos, intNeg));
        }
    }

    private static StairsShape pick(boolean positive, StairsShape ifPositive, StairsShape ifNegative) {
        return positive ? ifPositive : ifNegative;
    }
}
