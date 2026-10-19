package com.carpentersblocks.block.door;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.barrier.Box;

/**
 * Shapes and drawn parts of the Carpenter's Garage Door, from the original's {@code BlockCarpentersGarageDoor} bounds
 * and {@code BlockHandlerCarpentersGarageDoor}. Parts are drawn for a door facing south and turned to its facing. An
 * open door only shows its top piece, rolled up into the top half of the block.
 */
public final class GarageDoorGeometry {

    private static final ResourceLocation GLASS_TOP = CarpentersBlocks.id("block/garage_door/glass_top");
    private static final ResourceLocation GLASS = CarpentersBlocks.id("block/garage_door/glass");

    private GarageDoorGeometry() {}

    /** The outline and collision box of a visible piece. */
    public static Box shape(GarageDoorType type, boolean openTop, Direction facing) {
        double y1 = openTop ? 8 : 0;
        return (type == GarageDoorType.HIDDEN ? new Box(0, y1, 0, 16, 16, 2) : new Box(0, y1, 2, 16, 16, 4))
                .facing(facing);
    }

    /**
     * Parts of a visible piece: the top piece when {@code open}. The siding style's backing is iron; everything else
     * takes the cover (of the bottom piece, for an open door).
     */
    public static DoorParts parts(GarageDoorType type, boolean open, boolean host, boolean bottommost,
            Direction facing) {
        List<Box> frame = new ArrayList<>();
        List<Box> iron = new ArrayList<>();
        ResourceLocation pane = null;
        switch (type) {
            case DEFAULT -> {
                if (open) {
                    frame.add(new Box(0, 8, 2, 16, 16, 4));
                } else {
                    frame.add(new Box(0, 0, 2, 5, 16, 4));
                    frame.add(new Box(5, 12, 2, 11, 16, 4));
                    frame.add(new Box(5, 0, 2, 11, 10, 4));
                    frame.add(new Box(11, 0, 2, 16, 16, 4));
                }
            }
            case GLASS_TOP -> {
                if (open) {
                    panelsOpen(frame);
                } else if (host) {
                    pane = panelsGlassTop(frame);
                } else {
                    panels(frame);
                }
            }
            case GLASS -> {
                if (open) {
                    panelsOpen(frame);
                } else if (bottommost) {
                    pane = panelsGlassTop(frame);
                } else {
                    frame.add(new Box(0, 0, 2, 2, 16, 4));
                    frame.add(new Box(2, 14, 2, 14, 16, 4));
                    frame.add(new Box(2, 0, 2, 14, 2, 4));
                    frame.add(new Box(14, 0, 2, 16, 16, 4));
                    pane = GLASS;
                }
            }
            case SIDING -> {
                if (open) {
                    iron.add(new Box(0, 8, 3, 16, 16, 4));
                    frame.add(new Box(0, 8, 2, 16, 15, 3));
                } else {
                    iron.add(new Box(0, 0, 3, 16, 16, 4));
                    frame.add(new Box(0, 0, 2, 16, 7, 3));
                    frame.add(new Box(0, 8, 2, 16, 15, 3));
                }
            }
            case HIDDEN -> frame.add(new Box(0, open ? 8 : 0, 0, 16, 16, 2));
        }

        DoorParts parts = DoorParts.empty();
        for (Box box : frame) {
            parts.frame().add(box.facing(facing));
        }
        for (Box box : iron) {
            parts.iron().add(box.facing(facing));
        }
        if (pane != null) {
            parts.panes().add(new DoorParts.Pane(pane, new Box(0, 0, 3, 16, 16, 3).facing(facing), facing));
        }
        return parts;
    }

    private static void panelsOpen(List<Box> frame) {
        frame.add(new Box(0, 8, 3, 16, 16, 4));
        frame.add(new Box(0, 8, 2, 2, 16, 3));
        frame.add(new Box(2, 15, 2, 14, 16, 3));
        frame.add(new Box(2, 8, 2, 14, 10, 3));
        frame.add(new Box(14, 8, 2, 16, 16, 3));
    }

    /** Rails with the glass top pane; returns the pane's texture. */
    private static ResourceLocation panelsGlassTop(List<Box> frame) {
        frame.add(new Box(0, 0, 2, 2, 16, 4));
        frame.add(new Box(2, 14, 2, 14, 16, 4));
        frame.add(new Box(2, 7, 2, 14, 9, 4));
        frame.add(new Box(2, 0, 2, 14, 2, 4));
        frame.add(new Box(2, 2, 3, 14, 7, 4));
        frame.add(new Box(14, 0, 2, 16, 16, 4));
        return GLASS_TOP;
    }

    private static void panels(List<Box> frame) {
        frame.add(new Box(0, 0, 2, 2, 16, 4));
        frame.add(new Box(2, 14, 2, 14, 16, 4));
        frame.add(new Box(2, 7, 2, 14, 9, 4));
        frame.add(new Box(2, 0, 2, 14, 2, 4));
        frame.add(new Box(0, 0, 3, 16, 16, 4));
        frame.add(new Box(14, 0, 2, 16, 16, 4));
    }
}
