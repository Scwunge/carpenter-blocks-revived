package com.carpentersblocks.block.door;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.barrier.Box;

/**
 * Shapes and drawn parts of the Carpenter's Door, from the original's {@code BlockHinged} bounds and
 * {@code BlockHandlerCarpentersDoor}. Parts are drawn for a door against the south face and turned to its side.
 */
public final class DoorGeometry {

    /** The 3-pixel slab of a door against the south face. */
    private static final Box SLAB = new Box(0, 0, 13, 16, 16, 16);

    private static final ResourceLocation GLASS_TOP = texture("door_glass_top");
    private static final ResourceLocation GLASS_TALL_TOP = texture("door_glass_tall_top");
    private static final ResourceLocation GLASS_TALL_BOTTOM = texture("door_glass_tall_bottom");
    private static final ResourceLocation SCREEN_TALL = texture("door_screen_tall");
    private static final ResourceLocation FRENCH_TOP = texture("door_french_glass_top");
    private static final ResourceLocation FRENCH_BOTTOM = texture("door_french_glass_bottom");

    private DoorGeometry() {}

    private static ResourceLocation texture(String name) {
        return CarpentersBlocks.id("block/door/" + name);
    }

    /** The face a door lies against: the far side when closed, the hinge side when open. */
    public static Direction side(Direction facing, boolean open, boolean hingeLeft) {
        if (!open) {
            return facing.getOpposite();
        }
        return hingeLeft ? facing.getCounterClockWise() : facing.getClockWise();
    }

    /** The door's outline and collision box. */
    public static Box shape(Direction side) {
        return SLAB.facing(side);
    }

    public static DoorParts parts(DoorType type, boolean bottom, boolean open, boolean hingeLeft, Direction side) {
        List<Box> frame = new ArrayList<>();
        ResourceLocation pane = null;
        if (type != DoorType.HIDDEN) {
            frame.add(new Box(0, 0, 13, 3, 16, 16));
            frame.add(new Box(13, 0, 13, 16, 16, 16));
        }
        switch (type) {
            case GLASS_TOP -> {
                if (bottom) {
                    frame.add(new Box(3, 3, 14, 13, 16, 15));
                    frame.add(new Box(3, 0, 13, 13, 3, 16));
                    frame.add(new Box(5, 5, 13, 11, 14, 16));
                } else {
                    frame.add(new Box(3, 13, 13, 13, 16, 16));
                    frame.add(new Box(3, 0, 13, 13, 3, 16));
                    pane = GLASS_TOP;
                }
            }
            case PANELS -> {
                if (bottom) {
                    frame.add(new Box(3, 3, 14, 13, 16, 15));
                    frame.add(new Box(3, 0, 13, 13, 3, 16));
                    frame.add(new Box(5, 5, 13, 11, 15, 16));
                } else {
                    frame.add(new Box(3, 0, 14, 13, 13, 15));
                    frame.add(new Box(3, 13, 13, 13, 16, 16));
                    frame.add(new Box(3, 1, 13, 13, 4, 16));
                    frame.add(new Box(5, 6, 13, 11, 11, 16));
                }
            }
            case GLASS_TALL, SCREEN_TALL -> {
                boolean screen = type == DoorType.SCREEN_TALL;
                if (bottom) {
                    frame.add(new Box(3, 0, 13, 13, 3, 16));
                    pane = screen ? SCREEN_TALL : GLASS_TALL_BOTTOM;
                } else {
                    frame.add(new Box(3, 13, 13, 13, 16, 16));
                    pane = screen ? SCREEN_TALL : GLASS_TALL_TOP;
                }
            }
            case FRENCH_GLASS -> {
                if (bottom) {
                    frame.add(new Box(7, 3, 14, 9, 8, 15));
                    frame.add(new Box(7, 10, 14, 9, 15, 15));
                    frame.add(new Box(3, 0, 13, 13, 3, 16));
                    frame.add(new Box(3, 8, 14, 13, 10, 15));
                    frame.add(new Box(3, 15, 14, 13, 16, 15));
                    pane = FRENCH_BOTTOM;
                } else {
                    frame.add(new Box(7, 1, 14, 9, 6, 15));
                    frame.add(new Box(7, 8, 14, 9, 13, 15));
                    frame.add(new Box(3, 13, 13, 13, 16, 16));
                    frame.add(new Box(3, 0, 14, 13, 1, 15));
                    frame.add(new Box(3, 6, 14, 13, 8, 15));
                    pane = FRENCH_TOP;
                }
            }
            case HIDDEN -> frame.add(SLAB);
        }

        DoorParts parts = DoorParts.empty();
        for (Box box : frame) {
            parts.frame().add(box.facing(side));
        }
        if (pane != null) {
            parts.panes().add(DoorParts.pane(pane, side));
        }
        // The hidden door only shows its handle on the inside
        handle(parts.iron(), bottom, open, hingeLeft, side, true, type != DoorType.HIDDEN);
        return parts;
    }

    /** The original's {@code renderPartPullHandle}: iron pulls on the latch side, the outer ones in the next block. */
    private static void handle(List<Box> iron, boolean bottom, boolean open, boolean hingeLeft, Direction side,
            boolean inner, boolean outer) {
        double x = open ? (!hingeLeft ? 14 : 1) : (!hingeLeft ? 1 : 14);
        double y1 = bottom ? 14 : 1, y2 = bottom ? 15 : 2;
        double offsetY1 = bottom ? 14 : 0, offsetY2 = bottom ? 16 : 2;
        boolean source = inner && !open || outer && open;
        boolean offset = outer && !open || inner && open;
        if (source) {
            iron.add(new Box(x, y1, 12, x + 1, y2, 13).facing(side));
            iron.add(new Box(x, offsetY1, 11, x + 1, offsetY2, 12).facing(side));
        }
        if (offset) {
            iron.add(DoorParts.offset(new Box(x, y1, 0, x + 1, y2, 1).facing(side), side));
            iron.add(DoorParts.offset(new Box(x, offsetY1, 1, x + 1, offsetY2, 2).facing(side), side));
        }
    }
}
