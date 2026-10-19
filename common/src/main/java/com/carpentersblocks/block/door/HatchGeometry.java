package com.carpentersblocks.block.door;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.barrier.Box;

/**
 * Shapes and drawn parts of the Carpenter's Hatch, from the original's {@code BlockCarpentersHatch} bounds and
 * {@code BlockHandlerCarpentersHatch}. Parts are drawn for a hatch against the south face and laid against its side:
 * the top or bottom when closed, the supporting block when open.
 */
public final class HatchGeometry {

    private static final ResourceLocation GLASS = texture("hatch_glass");
    private static final ResourceLocation SCREEN = texture("hatch_screen");
    private static final ResourceLocation FRENCH_GLASS = texture("hatch_french_glass");

    private HatchGeometry() {}

    private static ResourceLocation texture(String name) {
        return CarpentersBlocks.id("block/hatch/" + name);
    }

    /** The hidden hatch is thinner, which helps climbing it. */
    private static double thickness(HatchType type) {
        return type == HatchType.HIDDEN ? 2 : 3;
    }

    /** The face the hatch lies against. */
    public static Direction side(boolean high, boolean open, Direction support) {
        return open ? support : high ? Direction.UP : Direction.DOWN;
    }

    /** The hatch's outline and collision box. */
    public static Box shape(HatchType type, boolean high, boolean open, Direction support) {
        double t = thickness(type);
        if (open) {
            return new Box(0, 0, 16 - t, 16, 16, 16).facing(support);
        }
        return high ? new Box(0, 16 - t, 0, 16, 16, 16) : new Box(0, 0, 0, 16, t, 16);
    }

    public static DoorParts parts(HatchType type, boolean high, boolean open, Direction support) {
        Direction side = side(high, open, support);
        List<Box> frame = new ArrayList<>();
        ResourceLocation pane = null;
        switch (type) {
            case HIDDEN -> frame.add(new Box(0, 0, 14, 16, 16, 16));
            case WINDOW, SCREEN -> {
                addFrame(frame);
                pane = type == HatchType.SCREEN ? SCREEN : GLASS;
            }
            case FRENCH_WINDOW -> {
                frame.add(new Box(3, 7, 14, 13, 9, 15));
                frame.add(new Box(7, 3, 14, 9, 7, 15));
                frame.add(new Box(7, 9, 14, 9, 13, 15));
                addFrame(frame);
                pane = FRENCH_GLASS;
            }
            case PANEL -> {
                // 0.8215 is the original's
                frame.add(Box.units(0.1875, 0.1875, 0.875, 0.8215, 0.8215, 0.9375));
                frame.add(new Box(5, 5, 13, 11, 11, 16));
                addFrame(frame);
            }
        }

        DoorParts parts = DoorParts.empty();
        for (Box box : frame) {
            parts.frame().add(box.attached(side));
        }
        if (pane != null) {
            parts.panes().add(DoorParts.pane(pane, side));
        }
        handle(parts.iron(), type, high, open, support, side, true, type != HatchType.HIDDEN);
        return parts;
    }

    private static void addFrame(List<Box> frame) {
        frame.add(new Box(0, 0, 13, 3, 16, 16));
        frame.add(new Box(3, 13, 13, 13, 16, 16));
        frame.add(new Box(3, 0, 13, 13, 3, 16));
        frame.add(new Box(13, 0, 13, 16, 16, 16));
    }

    /** The three iron pieces of a handle, {@code back} pixels in from the south face. */
    private static List<Box> handlePieces(double y1, double y2, double back) {
        return List.of(new Box(6, y1, 15 - back, 7, y2, 16 - back), new Box(9, y1, 15 - back, 10, y2, 16 - back),
                new Box(6, y1, 14 - back, 10, y2, 15 - back));
    }

    /**
     * The original's {@code renderPartHandle}: a handle on each face of the hatch, the one past the hatch's side
     * drawn in the neighboring block.
     */
    private static void handle(List<Box> iron, HatchType type, boolean high, boolean open, Direction support,
            Direction side, boolean interior, boolean exterior) {
        double back = thickness(type);
        double y1 = high ? 1 : 14, y2 = high ? 2 : 15;
        Direction turn = high ? support : support.getOpposite();
        if (interior) {
            if (open) {
                for (Box box : handlePieces(y1, y2, 0)) {
                    iron.add(DoorParts.offset(box.attached(side.getOpposite()), side));
                }
            } else {
                for (Box box : handlePieces(y1, y2, back)) {
                    iron.add(box.attached(side).attached(turn));
                }
            }
        }
        if (exterior) {
            if (open) {
                for (Box box : handlePieces(y1, y2, back)) {
                    iron.add(box.attached(support));
                }
            } else {
                for (Box box : handlePieces(y1, y2, 0)) {
                    iron.add(DoorParts.offset(box.attached(side.getOpposite()).attached(turn), side));
                }
            }
        }
    }
}
