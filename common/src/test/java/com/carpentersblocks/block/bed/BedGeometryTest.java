package com.carpentersblocks.block.bed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.minecraft.core.Direction;

import org.junit.jupiter.api.Test;

import com.carpentersblocks.block.barrier.Box;

class BedGeometryTest {

    private static boolean inBlock(Box b) {
        return b.x1() >= 0 && b.y1() >= 0 && b.z1() >= 0 && b.x2() <= 16 && b.y2() <= 16 && b.z2() <= 16
                && !b.isEmpty();
    }

    @Test
    void everyBoxFitsTheBlock() {
        for (boolean head : new boolean[] { true, false }) {
            for (BedGeometry.Joins joins : List.of(BedGeometry.Joins.NONE, new BedGeometry.Joins(true, true))) {
                BedGeometry.frame(head, joins).forEach(b -> assertTrue(inBlock(b), b.toString()));
                for (boolean occupied : new boolean[] { true, false }) {
                    BedGeometry.fabric(head, joins, occupied).forEach(f -> assertTrue(inBlock(f.box()), f.toString()));
                }
            }
        }
    }

    @Test
    void joinedFeetLoseTheirLegsAndTheMattressRunsOut() {
        assertEquals(3, BedGeometry.frame(false, BedGeometry.Joins.NONE).size());
        assertEquals(1, BedGeometry.frame(false, new BedGeometry.Joins(true, true)).size());
        Box mattress = BedGeometry.fabric(false, new BedGeometry.Joins(true, true), false).get(0).box();
        assertEquals(0, mattress.x1());
        assertEquals(16, mattress.x2());
    }

    @Test
    void theBlanketRisesWhenOccupied() {
        double free = BedGeometry.fabric(true, BedGeometry.Joins.NONE, false).stream()
                .filter(f -> f.fabric() == BedGeometry.Fabric.BLANKET_TOP).findFirst().orElseThrow().box().y2();
        double used = BedGeometry.fabric(true, BedGeometry.Joins.NONE, true).stream()
                .filter(f -> f.fabric() == BedGeometry.Fabric.BLANKET_TOP).findFirst().orElseThrow().box().y2();
        assertEquals(BedGeometry.BLANKET_TOP, free);
        assertEquals(BedGeometry.BLANKET_TOP_OCCUPIED, used);
    }

    @Test
    void templateUvsStayOnTheTemplate() {
        for (boolean head : new boolean[] { true, false }) {
            for (boolean occupied : new boolean[] { true, false }) {
                for (BedGeometry.FabricBox f : BedGeometry.fabric(head, BedGeometry.Joins.NONE, occupied)) {
                    Box b = f.box();
                    for (Direction face : Direction.values()) {
                        for (double x : new double[] { b.x1(), b.x2() }) {
                            for (double y : new double[] { b.y1(), b.y2() }) {
                                for (double z : new double[] { b.z1(), b.z2() }) {
                                    double[] uv = BedGeometry.templateUv(f.fabric(), head, occupied, x, y, z, face);
                                    assertTrue(uv[0] >= 0 && uv[0] <= 48 && uv[1] >= 0 && uv[1] <= 64,
                                            f + " " + face + " " + uv[0] + "," + uv[1]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void theLowSideIsClockwiseOfFacing() {
        assertEquals(Direction.WEST, BedGeometry.lowSide(Direction.SOUTH));
        assertEquals(Direction.EAST, BedGeometry.lowSide(Direction.NORTH));
    }
}
