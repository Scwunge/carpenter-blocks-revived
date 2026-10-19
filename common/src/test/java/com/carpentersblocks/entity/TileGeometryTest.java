package com.carpentersblocks.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.junit.jupiter.api.Test;

class TileGeometryTest {

    private static final double D = TileGeometry.DEPTH;

    @Test
    void tilesSitAgainstTheBlockBehind() {
        BlockPos pos = new BlockPos(2, 5, -3);
        assertEquals(new AABB(2, 6 - D, -3, 3, 6, -2), TileGeometry.box(pos, Direction.DOWN), "under a ceiling");
        assertEquals(new AABB(2, 5, -3, 3, 5 + D, -2), TileGeometry.box(pos, Direction.UP), "on a floor");
        assertEquals(new AABB(2, 5, -2 - D, 3, 6, -2), TileGeometry.box(pos, Direction.NORTH), "on a south wall");
        assertEquals(new AABB(2, 5, -3, 3, 6, -3 + D), TileGeometry.box(pos, Direction.SOUTH), "on a north wall");
        assertEquals(new AABB(3 - D, 5, -3, 3, 6, -2), TileGeometry.box(pos, Direction.WEST), "on an east wall");
        assertEquals(new AABB(2, 5, -3, 2 + D, 6, -2), TileGeometry.box(pos, Direction.EAST), "on a west wall");
    }

    @Test
    void clicksNearAnEdgePickThatEdge() {
        assertNull(TileGeometry.edge(Direction.UP, new Vec3(0.5, 1, 0.5)), "middle");
        assertNull(TileGeometry.edge(Direction.UP, new Vec3(0.25, 1, 0.75)), "still the middle");
        assertEquals(Direction.NORTH, TileGeometry.edge(Direction.UP, new Vec3(0.5, 1, 0.1)));
        assertEquals(Direction.EAST, TileGeometry.edge(Direction.UP, new Vec3(0.9, 1, 0.15)), "the nearer edge");
        assertEquals(Direction.DOWN, TileGeometry.edge(Direction.SOUTH, new Vec3(0.5, 0.05, 1)));
        assertEquals(Direction.SOUTH, TileGeometry.edge(Direction.WEST, new Vec3(0, 0.5, 0.95)));
    }
}
