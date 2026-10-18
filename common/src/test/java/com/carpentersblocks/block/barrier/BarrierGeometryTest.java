package com.carpentersblocks.block.barrier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.Direction;

import org.junit.jupiter.api.Test;

class BarrierGeometryTest {

    private static final BarrierGeometry.Surroundings NO_POST =
            new BarrierGeometry.Surroundings(Set.of(), false, false, false, false);

    @Test
    void boxesTurnFromSouthToEachSide() {
        // A rail from the middle to the south edge
        Box south = new Box(7, 0, 8, 9, 16, 16);
        assertEquals(south, south.facing(Direction.SOUTH));
        assertEquals(new Box(7, 0, 0, 9, 16, 8), south.facing(Direction.NORTH));
        assertEquals(new Box(8, 0, 7, 16, 16, 9), south.facing(Direction.EAST));
        assertEquals(new Box(0, 0, 7, 8, 16, 9), south.facing(Direction.WEST));
    }

    @Test
    void hammerCyclesStylesAndVanillaSubTypes() {
        assertEquals(BarrierType.PICKET, BarrierType.VANILLA_X2.nextType());
        assertEquals(BarrierType.SHADOWBOX, BarrierType.PICKET.nextType());
        assertEquals(BarrierType.WALL, BarrierType.SHADOWBOX.nextType());
        assertEquals(BarrierType.VANILLA, BarrierType.WALL.nextType());
        assertEquals(BarrierType.VANILLA_X1, BarrierType.VANILLA.nextSubType());
        assertEquals(BarrierType.VANILLA, BarrierType.VANILLA_X3.nextSubType());
        assertEquals(BarrierType.PICKET, BarrierType.PICKET.nextSubType());
    }

    @Test
    void loneBarrierIsJustAPost() {
        List<Box> boxes = BarrierGeometry.barrier(BarrierType.VANILLA, Set.of(), BarrierGeometry.Surroundings.ALONE);
        assertEquals(List.of(new Box(6, 0, 6, 10, 16, 10)), boxes);
    }

    @Test
    void vanillaRailsReachTheConnectedSideAndDropWithTheSubType() {
        List<Box> two = BarrierGeometry.barrier(BarrierType.VANILLA, Set.of(Direction.NORTH), NO_POST);
        assertEquals(2, two.size());
        two.forEach(box -> assertEquals(0, box.z1()));
        assertEquals(12, two.get(0).y1());

        List<Box> lowered = BarrierGeometry.barrier(BarrierType.VANILLA_X2, Set.of(Direction.NORTH), NO_POST);
        assertEquals(10, lowered.get(0).y1());

        assertEquals(1, BarrierGeometry.barrier(BarrierType.VANILLA_X3, Set.of(Direction.EAST), NO_POST).size());
    }

    @Test
    void stackedBarriersDropTheRailsBetweenThem() {
        Set<Direction> north = Set.of(Direction.NORTH);
        int alone = BarrierGeometry.barrier(BarrierType.SHADOWBOX, north, NO_POST).size();
        int stacked = BarrierGeometry.barrier(BarrierType.SHADOWBOX, north,
                new BarrierGeometry.Surroundings(north, true, true, true, false)).size();
        assertEquals(alone - 2, stacked);
    }

    @Test
    void everyShapeStaysInsideTheBlock() {
        for (BarrierType type : BarrierType.values()) {
            for (int mask = 0; mask < 16; ++mask) {
                Set<Direction> sides = EnumSet.noneOf(Direction.class);
                for (int i = 0; i < 4; ++i) {
                    if ((mask & 1 << i) != 0) {
                        sides.add(BarrierGeometry.SIDES.get(i));
                    }
                }
                for (boolean flag : new boolean[] { false, true }) {
                    var around = new BarrierGeometry.Surroundings(sides, flag, flag, flag, flag);
                    checkInside(BarrierGeometry.barrier(type, sides, around), type + " " + sides);
                }
            }
            for (Direction swing : BarrierGeometry.SIDES) {
                for (boolean open : new boolean[] { false, true }) {
                    for (boolean stacked : new boolean[] { false, true }) {
                        var around = new BarrierGeometry.GateSurroundings(stacked, stacked);
                        List<Box> boxes = BarrierGeometry.gate(type, swing, open, around);
                        assertFalse(boxes.isEmpty());
                        checkInside(boxes, type + " gate " + swing);
                    }
                }
            }
        }
    }

    private static void checkInside(List<Box> boxes, String what) {
        for (Box box : boxes) {
            assertFalse(box.isEmpty(), what + " " + box);
            assertTrue(box.x1() >= 0 && box.y1() >= 0 && box.z1() >= 0, what + " " + box);
            assertTrue(box.x2() <= 16 && box.y2() <= 16 && box.z2() <= 16, what + " " + box);
        }
    }

    @Test
    void openGatesClearTheWay() {
        for (BarrierType type : BarrierType.values()) {
            // A gate across the x axis (the path runs north-south) opening to the south
            for (Box box : BarrierGeometry.gate(type, Direction.WEST, true, BarrierGeometry.GateSurroundings.ALONE)) {
                assertTrue(box.x2() <= 4 || box.x1() >= 12, type + " leaves " + box + " in the way");
                assertTrue(box.z1() >= 7, type + " swings the wrong way: " + box);
            }
        }
    }
}
