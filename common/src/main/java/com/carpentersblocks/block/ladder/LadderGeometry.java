package com.carpentersblocks.block.ladder;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;

import com.carpentersblocks.block.barrier.Box;

/**
 * Geometry of the Carpenter's Ladder in pixels, from the original's renderer. A ladder on a wall faces out of it; one
 * standing free faces up when it runs along z and down when it runs along x, as the original stored it.
 */
public final class LadderGeometry {

    private LadderGeometry() {}

    /** Ladders in a row join when they share a facing; this is which neighbors along the row do. */
    public record Joins(boolean low, boolean high) {
        public static final Joins NONE = new Joins(false, false);
    }

    /** The covered parts and the iron parts that keep their own texture. */
    public record Parts(List<Box> frame, List<Box> iron) {}

    public static boolean freestanding(Direction facing) {
        return facing.getAxis() == Direction.Axis.Y;
    }

    /** The axis a row of ladders runs along. */
    public static Direction.Axis along(Direction facing) {
        return switch (facing) {
            case NORTH, SOUTH, DOWN -> Direction.Axis.X;
            default -> Direction.Axis.Z;
        };
    }

    public static Box outline(Direction facing) {
        return switch (facing) {
            case DOWN -> new Box(0, 0, 6, 16, 16, 10);
            case UP -> new Box(6, 0, 0, 10, 16, 16);
            default -> new Box(0, 0, 0, 16, 16, 3).attached(facing);
        };
    }

    public static Parts parts(LadderType type, Direction facing, Joins joins) {
        return switch (type) {
            case DEFAULT -> new Parts(classic(facing, joins), List.of());
            case RAIL -> rail(facing);
            case POLE -> new Parts(pole(facing), List.of());
        };
    }

    private static final int[] RUNGS = { 2, 6, 10, 14 };

    /**
     * The original's "classic" default ladder: stiles at the ends of a row and rungs running through. Wall ladders
     * keep all their parts 3 pixels deep (the original drew rungs 2 deep on north and west walls only).
     */
    private static List<Box> classic(Direction facing, Joins joins) {
        boolean free = freestanding(facing);
        double stileIn = free ? 6 : 0;
        double stileOut = free ? 10 : 3;
        double rungIn = free ? 7 : 0;
        double rungOut = free ? 9 : 3;
        List<Box> local = new ArrayList<>();
        if (!joins.low()) {
            local.add(new Box(0, 0, stileIn, 2, 16, stileOut));
        }
        if (!joins.high()) {
            local.add(new Box(14, 0, stileIn, 16, 16, stileOut));
        }
        double from = joins.low() ? 0 : 2;
        double to = joins.high() ? 16 : 14;
        for (int y : RUNGS) {
            local.add(new Box(from, y, rungIn, to, y + 1, rungOut));
        }
        List<Box> boxes = new ArrayList<>(local.size());
        for (Box box : local) {
            boxes.add(place(box, facing));
        }
        return boxes;
    }

    /**
     * A box drawn along x with depth on z (from the support for wall ladders) placed for {@code facing}, keeping
     * "low along the row" on the low side of the world axis, as the classic ladder's joins need.
     */
    static Box place(Box b, Direction facing) {
        return switch (facing) {
            case SOUTH, DOWN -> b;
            case NORTH -> new Box(b.x1(), b.y1(), 16 - b.z2(), b.x2(), b.y2(), 16 - b.z1());
            case EAST, UP -> new Box(b.z1(), b.y1(), b.x1(), b.z2(), b.y2(), b.x2());
            case WEST -> new Box(16 - b.z2(), b.y1(), b.x1(), 16 - b.z1(), b.y2(), b.x2());
        };
    }

    /** How the original turned a freestanding ladder's south-drawn parts: as is along z, to the west along x. */
    private static Direction freeTurn(Direction facing) {
        return facing == Direction.UP ? Direction.SOUTH : Direction.WEST;
    }

    private static Parts rail(Direction facing) {
        List<Box> frame = new ArrayList<>();
        List<Box> iron = new ArrayList<>();
        if (freestanding(facing)) {
            Direction turn = freeTurn(facing);
            for (Direction side : new Direction[] { turn, turn.getOpposite() }) {
                iron.add(new Box(5, 0, 3, 6, 16, 4).facing(side));
                iron.add(new Box(6, 0, 2, 7, 16, 4).facing(side));
                iron.add(new Box(10, 0, 3, 11, 16, 4).facing(side));
                iron.add(new Box(9, 0, 2, 10, 16, 4).facing(side));
            }
            for (int y : RUNGS) {
                frame.add(new Box(7, y, 0, 9, y + 1, 16).facing(turn));
            }
        } else {
            iron.add(new Box(2, 0, 1, 4, 16, 2).facing(facing));
            iron.add(new Box(3, 0, 2, 4, 16, 3).facing(facing));
            iron.add(new Box(12, 0, 1, 14, 16, 2).facing(facing));
            iron.add(new Box(12, 0, 2, 13, 16, 3).facing(facing));
            for (int y : RUNGS) {
                frame.add(new Box(0, y - 1, 0, 16, y + 1, 1).facing(facing));
            }
        }
        return new Parts(frame, iron);
    }

    private static List<Box> pole(Direction facing) {
        List<Box> boxes = new ArrayList<>();
        if (freestanding(facing)) {
            Direction turn = freeTurn(facing);
            boxes.add(new Box(6, 0, 6, 10, 16, 10));
            // Pegs on alternate sides, each with a lip
            boxes.add(new Box(7, 2, 10, 9, 3, 15).facing(turn));
            boxes.add(new Box(7, 3, 14, 9, 4, 15).facing(turn));
            boxes.add(new Box(7, 12, 1, 9, 13, 6).facing(turn));
            boxes.add(new Box(7, 13, 1, 9, 14, 2).facing(turn));
        } else {
            for (int y : RUNGS) {
                boxes.add(new Box(2, y, 0, 3, y + 1, 2).facing(facing));
                boxes.add(new Box(13, y, 0, 14, y + 1, 2).facing(facing));
                boxes.add(new Box(2, y, 2, 14, y + 1, 3).facing(facing));
            }
        }
        return boxes;
    }
}
