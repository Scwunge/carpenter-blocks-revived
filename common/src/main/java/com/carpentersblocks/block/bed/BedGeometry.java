package com.carpentersblocks.block.bed;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;

import com.carpentersblocks.block.barrier.Box;

/**
 * The boxes of a Carpenter's Bed half, in pixels, from the original's {@code BlockHandlerCarpentersBed}. Boxes are
 * drawn for a bed facing south (head to the south of the foot, as vanilla's {@code FACING}) and turned with
 * {@link Box#facing}; low x is the side towards {@code facing.getClockWise()}.
 * <p>
 * The frame (legs, headboard, support board) takes the cover. The fabric keeps its own textures: a white mattress, the
 * pillow, and the blanket in its dye color or the bed's design.
 */
public final class BedGeometry {

    /** What a fabric box is, which decides its texture. */
    public enum Fabric { MATTRESS, PILLOW, BLANKET_TOP, BLANKET_X_LOW, BLANKET_X_HIGH, BLANKET_END }

    public record FabricBox(Box box, Fabric fabric) {}

    /**
     * Which side neighbors are the same half of a bed facing the same way, so the two beds read as one wide bed: the
     * mattress runs into the neighbor and the legs between them go, as the original's {@code bedParallelPos/Neg}.
     */
    public record Joins(boolean low, boolean high) {
        public static final Joins NONE = new Joins(false, false);
    }

    /** The outline and collision: a 10 pixel slab, as the original's block bounds. */
    public static final Box OUTLINE = new Box(0, 0, 0, 16, 10, 16);

    /** Mattress height range. */
    public static final double MATTRESS_Y1 = 5, MATTRESS_Y2 = 9;
    /** Top of the blanket, raised while someone sleeps in the bed. */
    public static final double BLANKET_TOP = 10, BLANKET_TOP_OCCUPIED = 14;
    /** How far the blanket hangs down the sides. */
    public static final double BLANKET_DEPTH = 5;

    private BedGeometry() {}

    /** The covered frame of one half, drawn facing south. */
    public static List<Box> frame(boolean head, Joins joins) {
        List<Box> boxes = new ArrayList<>();
        if (head) {
            // Corner posts run up beside the headboard; beside a joined bed they shorten to the headboard
            boxes.add(joins.low() ? new Box(0, 3, 14, 2, 14, 16) : new Box(0, 0, 14, 2, 16, 16));
            boxes.add(joins.high() ? new Box(14, 3, 14, 16, 14, 16) : new Box(14, 0, 14, 16, 16, 16));
            boxes.add(new Box(2, 3, 14, 14, 14, 16));
            boxes.add(new Box(0, 3, 0, 16, 5, 14));
        } else {
            if (!joins.low()) {
                boxes.add(new Box(0, 0, 0, 2, 3, 2));
            }
            if (!joins.high()) {
                boxes.add(new Box(14, 0, 0, 16, 3, 2));
            }
            boxes.add(new Box(0, 3, 0, 16, 5, 16));
        }
        return boxes;
    }

    /** The mattress, pillow and blanket of one half, drawn facing south. */
    public static List<FabricBox> fabric(boolean head, Joins joins, boolean occupied) {
        List<FabricBox> boxes = new ArrayList<>();
        double x1 = joins.low() ? 0 : 1;
        double x2 = joins.high() ? 16 : 15;
        boxes.add(new FabricBox(head ? new Box(x1, MATTRESS_Y1, 0, x2, MATTRESS_Y2, 14)
                : new Box(x1, MATTRESS_Y1, 1, x2, MATTRESS_Y2, 16), Fabric.MATTRESS));

        double top = occupied ? BLANKET_TOP_OCCUPIED : BLANKET_TOP;
        double low = top - BLANKET_DEPTH;
        if (head) {
            boxes.add(new FabricBox(new Box(2, 9, 7, 14, 11, 13), Fabric.PILLOW));
            boxes.add(new FabricBox(new Box(1, top - 1, 0, 15, top, 8), Fabric.BLANKET_TOP));
            boxes.add(new FabricBox(new Box(0, low, 0, 1, top, 8), Fabric.BLANKET_X_LOW));
            boxes.add(new FabricBox(new Box(15, low, 0, 16, top, 8), Fabric.BLANKET_X_HIGH));
        } else {
            boxes.add(new FabricBox(new Box(1, top - 1, 1, 15, top, 16), Fabric.BLANKET_TOP));
            boxes.add(new FabricBox(new Box(0, low, 0, 1, top, 16), Fabric.BLANKET_X_LOW));
            boxes.add(new FabricBox(new Box(15, low, 0, 16, top, 16), Fabric.BLANKET_X_HIGH));
            boxes.add(new FabricBox(new Box(1, low, 0, 15, top, 1), Fabric.BLANKET_END));
        }
        return boxes;
    }

    /** The side the low-x edge of a south-facing drawing turns to for a bed facing {@code facing}. */
    public static Direction lowSide(Direction facing) {
        return facing.getClockWise();
    }

    /**
     * Where a point of a design's fabric falls on its 48 by 64 template, in template pixels. The template is the
     * original's: a 3 by 4 grid of 16 pixel cells seen from above with the pillow (top middle) towards the head, the
     * head half's blanket on the second row, the foot half's on the third and the foot end below. The side cells fold
     * down over the blanket's sides, and the foot end folds down from the foot half's top.
     *
     * @param x,y,z the point in the south-facing drawing, in pixels
     * @param face  the face of the box the point lies on
     * @return {u, v} in template pixels
     */
    public static double[] templateUv(Fabric fabric, boolean head, boolean occupied, double x, double y, double z,
            Direction face) {
        double top = occupied ? BLANKET_TOP_OCCUPIED : BLANKET_TOP;
        // Seen from above with the head at the top of the template: south is up and east is to the left
        double u = 16 + (16 - x);
        double rowTop = head ? 16 : 32;
        double v = rowTop + (16 - z);
        switch (fabric) {
            // The pillow is 6 pixels deep; it shows the middle of its cell
            case PILLOW -> v = 18 - z;
            case BLANKET_X_HIGH -> {
                if (face.getAxis() != Direction.Axis.Y) {
                    u = 16 - (top - y);
                }
            }
            case BLANKET_X_LOW -> {
                if (face.getAxis() != Direction.Axis.Y) {
                    u = 32 + (top - y);
                }
            }
            case BLANKET_END -> {
                if (face.getAxis() != Direction.Axis.Y) {
                    v = 48 + (top - y);
                }
            }
            default -> {}
        }
        return new double[] { u, v };
    }
}
