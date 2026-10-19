package com.carpentersblocks.block.flowerpot;

import java.util.List;

import net.minecraft.util.Mth;

import com.carpentersblocks.block.barrier.Box;

/** The boxes of the Carpenter's Flower Pot, in pixels, from the original's {@code BlockHandlerCarpentersFlowerPot}. */
public final class PotGeometry {

    /** The pot: a floor and four walls, 6 by 6 and 6 tall. */
    public static final List<Box> POT = List.of(
            new Box(6, 0, 6, 10, 1, 10),
            new Box(6, 0, 5, 10, 6, 6),
            new Box(6, 0, 10, 10, 6, 11),
            new Box(5, 0, 5, 6, 6, 11),
            new Box(10, 0, 5, 11, 6, 11));

    public static final Box SOIL = new Box(6, 1, 6, 10, 4, 10);

    /** Plants stand on the soil, a quarter block up. */
    public static final double PLANT_BASE = 4;

    /** Leaves and cactus are drawn as three stacked blocks on the soil, as the original's {@code drawStackedBlocks}. */
    public static final List<Box> STACKED = List.of(
            new Box(6, 4, 6, 10, 8, 10),
            new Box(6, 8, 6, 10, 12, 10),
            new Box(6, 12, 6, 10, 16, 10));

    /** The outline for each kind of contents; only the pot collides, unless the plant is solid. */
    public static final Box EMPTY_OUTLINE = new Box(5, 0, 5, 11, 6, 11);
    public static final Box PLANT_OUTLINE = new Box(5, 0, 5, 11, 12, 11);
    public static final Box SOLID_OUTLINE = new Box(5, 0, 5, 11, 15.84, 11);

    private PotGeometry() {}

    /**
     * Whether a click at {@code x}, {@code z} (block units) on the top face lands on the soil rather than the rim, as
     * the original's soil area.
     */
    public static boolean onSoil(double x, double z) {
        return x > 0.375 && x < 0.625 && z > 0.375 && z < 0.625;
    }

    /** Clicks above this height (block units) reach the plant rather than the soil. */
    public static final double PLANT_CLICK_HEIGHT = 0.375;

    /** Which of 16 turns a plant takes from the yaw of the player placing it, as the original. */
    public static int angle(float yaw) {
        return Mth.floor((yaw + 180.0F) * 16.0F / 360.0F + 0.5) & 15;
    }

    /** The turn in radians for a plant angle. */
    public static float radians(int angle) {
        return (float) (angle * Math.PI / 8);
    }
}
