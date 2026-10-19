package com.carpentersblocks.block.collapsible;

import static com.carpentersblocks.block.collapsible.CollapsibleGeometry.QUAD_XZNN;
import static com.carpentersblocks.block.collapsible.CollapsibleGeometry.QUAD_XZNP;
import static com.carpentersblocks.block.collapsible.CollapsibleGeometry.QUAD_XZPN;
import static com.carpentersblocks.block.collapsible.CollapsibleGeometry.QUAD_XZPP;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.Direction;

import org.junit.jupiter.api.Test;

import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.collapsible.CollapsibleGeometry.Depths;

class CollapsibleGeometryTest {

    @Test
    void depthsPackAsTheOriginal() {
        // XZNN at bit 18, XZNP at 13, XZPN at 8, XZPP at 3; the low three bits are left alone
        Depths depths = new Depths(16, 1, 8, 3);
        int data = depths.pack(0b101);
        assertEquals(16 << 18 | 1 << 13 | 8 << 8 | 3 << 3 | 0b101, data);
        assertEquals(depths, Depths.of(data));
        assertEquals(Depths.FULL, Depths.of(0x1F << 18 | 0x1F << 13 | 0x1F << 8 | 0x1F << 3), "capped at 16");
    }

    @Test
    void depthsStayWithinABlock() {
        Depths depths = new Depths(0, 16, 5, 5);
        assertEquals(depths, depths.with(QUAD_XZNN, -1));
        assertEquals(depths, depths.with(QUAD_XZNP, 17));
        assertEquals(new Depths(0, 16, 6, 5), depths.with(QUAD_XZPN, 6));
        assertTrue(new Depths(0, 0, 0, 0).isMin());
        assertTrue(Depths.FULL.isMax());
        assertEquals(16, depths.max());
    }

    @Test
    void hitsPickTheNearestCorner() {
        assertEquals(QUAD_XZNN, CollapsibleGeometry.quad(0.2, 0.4));
        assertEquals(QUAD_XZNP, CollapsibleGeometry.quad(0.2, 0.6));
        assertEquals(QUAD_XZPN, CollapsibleGeometry.quad(0.8, 0.1));
        assertEquals(QUAD_XZPP, CollapsibleGeometry.quad(0.5, 0.5), "the middle rounds up, as Math.round");
    }

    @Test
    void theMiddleIsTheSteeperDiagonalsAverage() {
        // NN-PP differ by 0, PN-NP by 8: the middle is the PN-NP average
        assertArrayEquals(new double[] { 0.5, 0, 0.5, 0.5, 0.25 },
                CollapsibleGeometry.heights(new Depths(8, 0, 8, 8)), 1e-9);
        // NN-PP differ by 16, PN-NP by 0
        assertArrayEquals(new double[] { 1, 0.25, 0.25, 0, 0.5 },
                CollapsibleGeometry.heights(new Depths(16, 4, 4, 0)), 1e-9);
        // A tie goes to NN-PP
        assertEquals(0.75, CollapsibleGeometry.heights(new Depths(16, 0, 8, 8))[4], 1e-9);
        double[] flat = CollapsibleGeometry.heights(new Depths(0, 0, 0, 0));
        assertEquals(1.0 / 1024, flat[0], 1e-12, "a flat block's top is lifted off the face below");
        assertEquals(1.0 / 1024, flat[4], 1e-12);
    }

    @Test
    void collisionBoxesStaggerAtMostHalfABlock() {
        Depths depths = new Depths(16, 2, 12, 0);
        assertEquals(new Box(0, 0, 0, 8, 16, 8), CollapsibleGeometry.bounds(depths, QUAD_XZNN, true));
        assertEquals(new Box(0, 0, 8, 8, 8, 16), CollapsibleGeometry.bounds(depths, QUAD_XZNP, true));
        assertEquals(new Box(8, 0, 0, 16, 12, 8), CollapsibleGeometry.bounds(depths, QUAD_XZPN, true));
        assertEquals(new Box(8, 0, 8, 16, 8, 16), CollapsibleGeometry.bounds(depths, QUAD_XZPP, true));
        // Hanging blocks don't stagger
        assertEquals(new Box(0, 14, 8, 8, 16, 16), CollapsibleGeometry.bounds(depths, QUAD_XZNP, false));
        assertTrue(CollapsibleGeometry.bounds(depths, QUAD_XZPP, false).isEmpty());
    }

    @Test
    void solidSidesNeedBothCornersFull() {
        Depths depths = new Depths(16, 4, 16, 16);
        assertTrue(CollapsibleGeometry.isSideSolid(depths, true, Direction.NORTH));
        assertTrue(CollapsibleGeometry.isSideSolid(depths, true, Direction.EAST));
        assertFalse(CollapsibleGeometry.isSideSolid(depths, true, Direction.SOUTH));
        assertFalse(CollapsibleGeometry.isSideSolid(depths, true, Direction.WEST));
        assertTrue(CollapsibleGeometry.isSideSolid(depths, true, Direction.DOWN), "the flat side");
        assertFalse(CollapsibleGeometry.isSideSolid(depths, true, Direction.UP));
        assertTrue(CollapsibleGeometry.isSideSolid(depths, false, Direction.UP), "hanging: the flat side is up");
        assertFalse(CollapsibleGeometry.isSideSolid(depths, false, Direction.DOWN));
        assertTrue(CollapsibleGeometry.isSideSolid(Depths.FULL, true, Direction.UP));
    }

    @Test
    void sidesListTheirCorners() {
        assertArrayEquals(new int[] { QUAD_XZPN, QUAD_XZPP }, CollapsibleGeometry.cornersOn(Direction.EAST));
        assertArrayEquals(new int[] { QUAD_XZNN, QUAD_XZPN }, CollapsibleGeometry.cornersOn(Direction.NORTH));
        assertEquals(0, CollapsibleGeometry.cornersOn(Direction.UP).length);
    }
}
