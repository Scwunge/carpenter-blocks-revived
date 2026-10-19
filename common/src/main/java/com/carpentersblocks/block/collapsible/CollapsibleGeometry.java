package com.carpentersblocks.block.collapsible;

import net.minecraft.core.Direction;

import com.carpentersblocks.block.barrier.Box;

/**
 * The Collapsible Block's shape: four corner depths, 0 to 16 pixels, kept in the block data as the original's
 * {@code Collapsible} packed them, and the boxes and heights {@code CollapsibleUtil} worked out from them. A positive
 * block stands on the floor and rises to the depths; a negative one hangs from the ceiling and reaches down to them.
 */
public final class CollapsibleGeometry {

    /** The corners, by the side of the block they lean to on x then z. */
    public static final int QUAD_XZNN = 0, QUAD_XZNP = 1, QUAD_XZPN = 2, QUAD_XZPP = 3;
    public static final int MAX_DEPTH = 16;

    // [000000000] [00000] [00000] [00000] [00000] [000]: unused, XZNN, XZNP, XZPN, XZPP, direction
    private static final int[] SHIFT = { 18, 13, 8, 3 };
    private static final int DEPTH_MASK = 0x1F;

    /** Height of a flat block's top, keeping it off the face below, as the original's Z-fighting bias. */
    private static final double FLAT_BIAS = 1.0 / 1024;

    private CollapsibleGeometry() {}

    /** The corner a hit falls closest to, from its position across the block (0 to 1). */
    public static int quad(double hitX, double hitZ) {
        boolean x = Math.round(hitX) != 0;
        boolean z = Math.round(hitZ) != 0;
        return x ? (z ? QUAD_XZPP : QUAD_XZPN) : (z ? QUAD_XZNP : QUAD_XZNN);
    }

    /** The four depths, in quad order. */
    public record Depths(int nn, int np, int pn, int pp) {

        public static final Depths FULL = new Depths(MAX_DEPTH, MAX_DEPTH, MAX_DEPTH, MAX_DEPTH);

        public static Depths of(int data) {
            int[] d = new int[4];
            for (int quad = 0; quad < 4; ++quad) {
                d[quad] = Math.min(MAX_DEPTH, (data >> SHIFT[quad]) & DEPTH_MASK);
            }
            return new Depths(d[0], d[1], d[2], d[3]);
        }

        public int get(int quad) {
            return switch (quad) {
                case QUAD_XZNN -> nn;
                case QUAD_XZNP -> np;
                case QUAD_XZPN -> pn;
                default -> pp;
            };
        }

        /** The data with these depths, leaving the other bits of {@code data} alone. */
        public int pack(int data) {
            for (int quad = 0; quad < 4; ++quad) {
                data = (data & ~(DEPTH_MASK << SHIFT[quad])) | get(quad) << SHIFT[quad];
            }
            return data;
        }

        /** These depths with one corner changed; depths outside 0 to 16 change nothing, as in the original. */
        public Depths with(int quad, int depth) {
            if (depth < 0 || depth > MAX_DEPTH) {
                return this;
            }
            return new Depths(quad == QUAD_XZNN ? depth : nn, quad == QUAD_XZNP ? depth : np,
                    quad == QUAD_XZPN ? depth : pn, quad == QUAD_XZPP ? depth : pp);
        }

        /** Fully collapsed. */
        public boolean isMin() {
            return nn == 0 && np == 0 && pn == 0 && pp == 0;
        }

        /** A full cube. */
        public boolean isMax() {
            return equals(FULL);
        }

        public int max() {
            return Math.max(Math.max(nn, np), Math.max(pn, pp));
        }
    }

    /**
     * Heights (0 to 1) of the corners and the middle, measured from the flat side: {nn, np, pn, pp, center}. The
     * middle is the average of the pair of opposite corners that differ most, as the original's
     * {@code computeOffsets} (ties go to the north-west and south-east pair).
     */
    public static double[] heights(Depths depths) {
        double bias = depths.isMin() ? FLAT_BIAS : 0;
        double nn = depths.nn() / 16.0 + bias;
        double np = depths.np() / 16.0 + bias;
        double pn = depths.pn() / 16.0 + bias;
        double pp = depths.pp() / 16.0 + bias;
        double center = Math.abs(nn - pp) < Math.abs(pn - np) ? (pn + np) / 2 : (nn + pp) / 2;
        return new double[] { nn, np, pn, pp, center };
    }

    /**
     * The collision box (pixels) under one corner. On a positive block no box sits more than half a block below the
     * highest, so players can always walk across, as the original's {@code genBounds}.
     */
    public static Box bounds(Depths depths, int quad, boolean positive) {
        double x1 = quad == QUAD_XZPN || quad == QUAD_XZPP ? 8 : 0;
        double z1 = quad == QUAD_XZNP || quad == QUAD_XZPP ? 8 : 0;
        double depth = depths.get(quad);
        if (positive) {
            depth = Math.max(depth, depths.max() - 8);
            return new Box(x1, 0, z1, x1 + 8, depth, z1 + 8);
        }
        return new Box(x1, 16 - depth, z1, x1 + 8, 16, z1 + 8);
    }

    /**
     * Whether a face is whole, so things can rest against it: the flat side always, the far side when full, and an
     * upright side when both its corners are full height.
     */
    public static boolean isSideSolid(Depths depths, boolean positive, Direction side) {
        return switch (side) {
            case DOWN -> positive || depths.isMax();
            case UP -> !positive || depths.isMax();
            case NORTH -> depths.nn() + depths.pn() == 2 * MAX_DEPTH;
            case SOUTH -> depths.np() + depths.pp() == 2 * MAX_DEPTH;
            case WEST -> depths.nn() + depths.np() == 2 * MAX_DEPTH;
            case EAST -> depths.pn() + depths.pp() == 2 * MAX_DEPTH;
        };
    }

    /** The corners lying on a side of the block, west to east or north to south; none for up and down. */
    public static int[] cornersOn(Direction side) {
        return switch (side) {
            case NORTH -> new int[] { QUAD_XZNN, QUAD_XZPN };
            case SOUTH -> new int[] { QUAD_XZNP, QUAD_XZPP };
            case WEST -> new int[] { QUAD_XZNN, QUAD_XZNP };
            case EAST -> new int[] { QUAD_XZPN, QUAD_XZPP };
            default -> new int[0];
        };
    }
}
