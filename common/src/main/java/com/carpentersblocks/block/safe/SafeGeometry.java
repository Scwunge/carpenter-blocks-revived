package com.carpentersblocks.block.safe;

import java.util.ArrayList;
import java.util.List;

import com.carpentersblocks.block.barrier.Box;

/**
 * The safe's parts for a south-facing safe, from the original {@code BlockHandlerCarpentersSafe}: the cover-textured
 * body and sliding door, the metal panel and handle, and the lock and capacity lights.
 */
public final class SafeGeometry {

    /** Lights in the capacity strip, as the original's nine boxes. */
    public static final int CAPACITY_LIGHTS = 9;

    private static final List<Box> BODY = List.of(
            Box.units(0.0, 0.0, 0.0, 0.0625, 1.0, 1.0),
            Box.units(0.9375, 0.0, 0.0, 1.0, 1.0, 1.0),
            Box.units(0.0625, 0.0, 0.0625, 0.3125, 0.0625, 1.0),
            Box.units(0.375, 0.0, 0.0625, 0.9375, 0.0625, 1.0),
            Box.units(0.0625, 0.9375, 0.0625, 0.3125, 1.0, 1.0),
            Box.units(0.375, 0.9375, 0.0625, 0.9375, 1.0, 1.0),
            Box.units(0.0625, 0.0, 0.0, 0.3125, 1.0, 0.0625),
            Box.units(0.375, 0.0, 0.0, 0.9375, 1.0, 0.0625),
            Box.units(0.3125, 0.0, 0.0, 0.375, 1.0, 1.0),
            Box.units(0.375, 0.625, 0.0625, 0.9375, 0.6875, 0.875),
            Box.units(0.375, 0.3125, 0.0625, 0.9375, 0.375, 0.875));

    private static final List<Box> PANEL = List.of(
            Box.units(0.125, 0.875, 0.9375, 0.25, 0.9375, 1.0),
            Box.units(0.0625, 0.0625, 0.9375, 0.125, 0.9375, 1.0),
            Box.units(0.25, 0.0625, 0.9375, 0.3125, 0.9375, 1.0),
            Box.units(0.125, 0.6875, 0.9375, 0.25, 0.75, 1.0),
            Box.units(0.125, 0.0625, 0.9375, 0.25, 0.125, 1.0),
            Box.units(0.0625, 0.0625, 0.875, 0.3125, 0.9375, 0.9375));

    /** Lit while the safe is unlocked. */
    public static final Box UNLOCKED_LIGHT = Box.units(0.125, 0.8125, 0.9375, 0.25, 0.875, 1.0);
    /** Lit while the safe is locked. */
    public static final Box LOCKED_LIGHT = Box.units(0.125, 0.75, 0.9375, 0.25, 0.8125, 1.0);

    private SafeGeometry() {}

    /** The cover-textured parts: the body and the door, slid aside while the safe is open. */
    public static List<Box> body(boolean open) {
        List<Box> boxes = new ArrayList<>(BODY);
        boxes.add(Box.units(0.375, 0.0625, 0.875, open ? 0.5625 : 0.9375, 0.9375, 0.9375));
        return boxes;
    }

    /** The metal parts: the panel and the handle, which moves with the door. */
    public static List<Box> metal(boolean open) {
        List<Box> boxes = new ArrayList<>(PANEL);
        boxes.add(Box.units(open ? 0.4375 : 0.8125, 0.375, 0.9375, open ? 0.5 : 0.875, 0.625, 1.0));
        return boxes;
    }

    /** One light of the capacity strip, from the bottom (0) up. */
    public static Box capacityLight(int index) {
        double y = 0.125 + 0.0625 * index;
        return Box.units(0.125, y, 0.9375, 0.25, y + 0.0625, 1.0);
    }

    /**
     * How many capacity lights are lit, as the original: one for every ninth of the slots holding anything.
     *
     * @param filled slots holding items
     * @param size   slots the safe has (27, or 54 upgraded)
     */
    public static int capacity(int filled, int size) {
        return Math.min(CAPACITY_LIGHTS, filled / Math.max(1, 3 * size / 27));
    }
}
