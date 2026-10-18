package com.carpentersblocks.block.barrier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.core.Direction;

/**
 * The frame boxes of barriers and gates, ported from the original's {@code BlockHandlerCarpentersBarrier} and
 * {@code BlockHandlerCarpentersGate}. Boxes are drawn for the south side and turned with {@link Box#facing}.
 */
public final class BarrierGeometry {

    /** Horizontal directions in the order the original renderer visits them. */
    public static final List<Direction> SIDES = List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST,
            Direction.EAST);

    private BarrierGeometry() {}

    /**
     * What a barrier's look depends on beyond its own block state. Used as the render cache key.
     *
     * @param connectedAbove sides on which the block above and to that side would take a connection
     * @param connectsUp whether the block above takes a connection from below
     * @param barrierBelow whether another barrier is below
     * @param barrierAbove whether another barrier is above
     * @param post whether this barrier's column shows a post
     */
    public record Surroundings(Set<Direction> connectedAbove, boolean connectsUp, boolean barrierBelow,
            boolean barrierAbove, boolean post) {

        public Surroundings {
            connectedAbove = Set.copyOf(connectedAbove);
        }

        /** A lone barrier, as shown in the inventory or without a world. */
        public static final Surroundings ALONE = new Surroundings(Set.of(), false, false, false, true);
    }

    public static List<Box> barrier(BarrierType type, Set<Direction> connections, Surroundings around) {
        List<Box> boxes = new ArrayList<>();
        switch (type) {
            case PICKET -> picket(boxes, connections, around);
            case SHADOWBOX -> shadowbox(boxes, connections, around);
            case WALL -> wall(boxes, connections, around);
            default -> vanilla(boxes, type, connections, around);
        }
        return boxes;
    }

    private static void post(List<Box> boxes, Surroundings around, double radius) {
        if (around.post()) {
            boxes.add(Box.units(0.5 - radius, 0, 0.5 - radius, 0.5 + radius, 1, 0.5 + radius));
        }
    }

    /**
     * A rail running from the middle to the side. Unless {@code always}, a top rail is left out where the barrier
     * above continues the same way, and a bottom rail where another barrier is below, so stacks look like one fence.
     */
    private static void rail(List<Box> boxes, Surroundings around, Direction side, double depthRadius, double yMin,
            double yMax, boolean always) {
        boolean top = yMax - 0.5 > 0.5 - yMin;
        boolean show = always || (top ? !around.connectedAbove().contains(side) || !around.barrierAbove()
                : !around.barrierBelow());
        if (show) {
            boxes.add(Box.units(0.5 - depthRadius, yMin, 0.5, 0.5 + depthRadius, yMax, 1).facing(side));
        }
    }

    private static void vanilla(List<Box> boxes, BarrierType type, Set<Direction> connections, Surroundings around) {
        post(boxes, around, 0.125);
        double drop = type.railDrop() / 16.0;
        for (Direction side : SIDES) {
            if (!connections.contains(side)) {
                continue;
            }
            if (type == BarrierType.VANILLA_X3) {
                rail(boxes, around, side, 0.0625, 0.1875, 0.9375, true);
            } else {
                rail(boxes, around, side, 0.0625, 0.75 - drop, 0.9375, true);
                rail(boxes, around, side, 0.0625, 0.375 - drop, 0.5625, true);
            }
        }
    }

    private static void picket(List<Box> boxes, Set<Direction> connections, Surroundings around) {
        post(boxes, around, 0.0625);
        double yMin = around.barrierBelow() ? 0 : 0.0625;
        for (Direction side : SIDES) {
            if (connections.contains(side)) {
                boolean continues = around.connectedAbove().contains(side) && around.connectsUp();
                double yMax = continues ? 1 : 0.875;
                boxes.add(Box.units(0.5625, yMin, 0.6875, 0.625, yMax, 0.8125).facing(side));
                boxes.add(Box.units(0.375, yMin, 0.6875, 0.4375, yMax, 0.8125).facing(side));
                yMax = continues ? 1 : 0.8125;
                boxes.add(Box.units(0.5625, yMin, 0.9375, 0.625, yMax, 1).facing(side));
                boxes.add(Box.units(0.375, yMin, 0.9375, 0.4375, yMax, 1).facing(side));
                rail(boxes, around, side, 0.0625, 0.625, 0.6875, false);
                rail(boxes, around, side, 0.0625, 0.1875, 0.25, false);
            } else {
                boxes.add(Box.units(0.4375, yMin, 0.5625, 0.5625, 1, 0.625).facing(side));
            }
        }
    }

    private static void wall(List<Box> boxes, Set<Direction> connections, Surroundings around) {
        post(boxes, around, 0.25);
        for (Direction side : SIDES) {
            if (connections.contains(side)) {
                double yMax = around.connectedAbove().contains(side) && around.connectsUp() ? 1 : 0.8125;
                boxes.add(Box.units(0.3125, 0, 0.5, 0.6875, yMax, 1).facing(side));
            }
        }
    }

    private static void shadowbox(List<Box> boxes, Set<Direction> connections, Surroundings around) {
        post(boxes, around, 0.0625);
        for (Direction side : SIDES) {
            if (connections.contains(side)) {
                rail(boxes, around, side, 0.0625, 0.75, 0.875, false);
                rail(boxes, around, side, 0.0625, 0.125, 0.25, false);
                boxes.add(Box.units(0.5625, 0, 0.5, 0.625, 1, 0.75).facing(side));
                boxes.add(Box.units(0.375, 0, 0.75, 0.4375, 1, 1).facing(side));
            }
        }
    }

    // Gates

    /**
     * What a gate's look depends on beyond its own block state: gates stacked above or below join into one.
     */
    public record GateSurroundings(boolean gateBelow, boolean gateAbove) {

        public static final GateSurroundings ALONE = new GateSurroundings(false, false);
    }

    /**
     * @param swing the original's rotation for the gate: north or south for a gate across the z axis, east or west
     *              for one across the x axis; when open, the gate swings to the side it turns the drawn +x side to
     */
    public static List<Box> gate(BarrierType type, Direction swing, boolean open, GateSurroundings around) {
        List<Box> boxes = new ArrayList<>();
        switch (type) {
            case PICKET -> gatePicket(boxes, open, around);
            case SHADOWBOX -> gateShadowbox(boxes, open, around);
            case WALL -> gateWall(boxes, open, around);
            default -> gateVanilla(boxes, type, open, around);
        }
        boxes.replaceAll(box -> box.facing(swing));
        return boxes;
    }

    private static void gateVanilla(List<Box> boxes, BarrierType type, boolean open, GateSurroundings around) {
        double drop = type.railDrop() / 16.0;
        boolean single = type == BarrierType.VANILLA_X3;
        double yMin = around.gateBelow() ? 0 : 0.3125 - drop;
        double yMax = around.gateAbove() ? 1 : 0.9375;

        // Hinge posts
        boxes.add(Box.units(0.4375, yMin, 0, 0.5625, 1, 0.125));
        boxes.add(Box.units(0.4375, yMin, 0.875, 0.5625, 1, 1));

        if (open) {
            if (!single) {
                yMin = around.gateBelow() ? 0 : 0.375 - drop;
                boxes.add(Box.units(0.8125, yMin, 0, 0.9375, yMax, 0.125));
                boxes.add(Box.units(0.8125, yMin, 0.875, 0.9375, yMax, 1));
                yMin = 0.375 - drop;
                boxes.add(Box.units(0.5625, yMin, 0, 0.8125, 0.5625, 0.125));
                boxes.add(Box.units(0.5625, yMin, 0.875, 0.8125, 0.5625, 1));
                yMin = 0.75 - drop;
                boxes.add(Box.units(0.5625, yMin, 0, 0.8125, 0.9375, 0.125));
                boxes.add(Box.units(0.5625, yMin, 0.875, 0.8125, 0.9375, 1));
            } else {
                yMin = around.gateBelow() ? 0 : 0.1875;
                boxes.add(Box.units(0.5625, yMin, 0, 0.9375, yMax, 0.125));
                boxes.add(Box.units(0.5625, yMin, 0.875, 0.9375, yMax, 1));
            }
        } else {
            yMin = around.gateBelow() ? 0 : 0.375 - drop;
            if (!single) {
                boxes.add(Box.units(0.4375, yMin, 0.375, 0.5625, yMax, 0.5));
                boxes.add(Box.units(0.4375, yMin, 0.5, 0.5625, yMax, 0.625));
                yMin = 0.375 - drop;
                boxes.add(Box.units(0.4375, yMin, 0.625, 0.5625, 0.5625, 0.875));
                boxes.add(Box.units(0.4375, yMin, 0.125, 0.5625, 0.5625, 0.375));
                yMin = 0.75 - drop;
                boxes.add(Box.units(0.4375, yMin, 0.625, 0.5625, 0.9375, 0.875));
                boxes.add(Box.units(0.4375, yMin, 0.125, 0.5625, 0.9375, 0.375));
            } else {
                boxes.add(Box.units(0.4375, yMin, 0.125, 0.5625, yMax, 0.875));
            }
        }
    }

    private static void gatePicket(List<Box> boxes, boolean open, GateSurroundings around) {
        double yMin = around.gateBelow() ? 0 : 0.0625;
        double yShort = around.gateAbove() ? 1 : 0.8125;
        double yTall = around.gateAbove() ? 1 : 0.875;
        if (open) {
            if (!around.gateAbove()) {
                boxes.add(Box.units(0.5, 0.625, 0.0625, 1, 0.6875, 0.1875));
                boxes.add(Box.units(0.5, 0.625, 0.8125, 1, 0.6875, 0.9375));
            }
            if (!around.gateBelow()) {
                boxes.add(Box.units(0.5, 0.1875, 0.0625, 1, 0.25, 0.1875));
                boxes.add(Box.units(0.5, 0.1875, 0.8125, 1, 0.25, 0.9375));
            }
            for (double z : new double[] { 0, 0.1875, 0.75, 0.9375 }) {
                boxes.add(Box.units(0.5, yMin, z, 0.5625, yShort, z + 0.0625));
            }
            for (double x : new double[] { 0.6875, 0.9375 }) {
                double width = x == 0.6875 ? 0.125 : 0.0625;
                for (double z : new double[] { 0, 0.1875, 0.75, 0.9375 }) {
                    boxes.add(Box.units(x, yMin, z, x + width, yTall, z + 0.0625));
                }
            }
        } else {
            if (!around.gateAbove()) {
                boxes.add(Box.units(0.4375, 0.625, 0, 0.5625, 0.6875, 1));
            }
            if (!around.gateBelow()) {
                boxes.add(Box.units(0.4375, 0.1875, 0, 0.5625, 0.25, 1));
            }
            for (double z : new double[] { 0, 0.9375 }) {
                boxes.add(Box.units(0.5625, yMin, z, 0.625, yShort, z + 0.0625));
                boxes.add(Box.units(0.375, yMin, z, 0.4375, yShort, z + 0.0625));
            }
            for (double z : new double[] { 0.1875, 0.4375, 0.6875 }) {
                boxes.add(Box.units(0.5625, yMin, z, 0.625, yTall, z + 0.125));
                boxes.add(Box.units(0.375, yMin, z, 0.4375, yTall, z + 0.125));
            }
        }
    }

    private static void gateShadowbox(List<Box> boxes, boolean open, GateSurroundings around) {
        if (open) {
            if (!around.gateAbove()) {
                boxes.add(Box.units(0.5, 0.75, 0.0625, 1, 0.875, 0.1875));
                boxes.add(Box.units(0.5, 0.75, 0.8125, 1, 0.875, 0.9375));
            }
            if (!around.gateBelow()) {
                boxes.add(Box.units(0.5, 0.125, 0.0625, 1, 0.25, 0.1875));
                boxes.add(Box.units(0.5, 0.125, 0.8125, 1, 0.25, 0.9375));
            }
            boxes.add(Box.units(0.5, 0, 0, 0.75, 1, 0.0625));
            boxes.add(Box.units(0.75, 0, 0.1875, 1, 1, 0.25));
            boxes.add(Box.units(0.5, 0, 0.75, 0.75, 1, 0.8125));
            boxes.add(Box.units(0.75, 0, 0.9375, 1, 1, 1));
        } else {
            if (!around.gateAbove()) {
                boxes.add(Box.units(0.4375, 0.75, 0, 0.5625, 0.875, 1));
            }
            if (!around.gateBelow()) {
                boxes.add(Box.units(0.4375, 0.125, 0, 0.5625, 0.25, 1));
            }
            boxes.add(Box.units(0.5625, 0, 0, 0.625, 1, 0.25));
            boxes.add(Box.units(0.375, 0, 0.25, 0.4375, 1, 0.5));
            boxes.add(Box.units(0.5625, 0, 0.5, 0.625, 1, 0.75));
            boxes.add(Box.units(0.375, 0, 0.75, 0.4375, 1, 1));
        }
    }

    private static void gateWall(List<Box> boxes, boolean open, GateSurroundings around) {
        double yMax = around.gateAbove() ? 1 : 0.8125;
        if (open) {
            boxes.add(Box.units(0.5, 0, 0, 1, yMax, 0.125));
            boxes.add(Box.units(0.5, 0, 0.875, 1, yMax, 1));
        } else {
            boxes.add(Box.units(0.4375, 0, 0, 0.5625, yMax, 1));
        }
    }
}
