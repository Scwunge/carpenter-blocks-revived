package com.carpentersblocks.block.ladder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.minecraft.core.Direction;

import org.junit.jupiter.api.Test;

import com.carpentersblocks.block.barrier.Box;

class LadderGeometryTest {

    @Test
    void outlinesFollowTheOriginal() {
        assertEquals(new Box(0, 0, 13, 16, 16, 16), LadderGeometry.outline(Direction.NORTH));
        assertEquals(new Box(0, 0, 0, 3, 16, 16), LadderGeometry.outline(Direction.EAST));
        assertEquals(new Box(0, 0, 6, 16, 16, 10), LadderGeometry.outline(Direction.DOWN));
        assertEquals(new Box(6, 0, 0, 10, 16, 16), LadderGeometry.outline(Direction.UP));
        assertEquals(Direction.Axis.X, LadderGeometry.along(Direction.DOWN));
        assertEquals(Direction.Axis.Z, LadderGeometry.along(Direction.UP));
    }

    @Test
    void defaultLaddersInARowDropTheStilesBetweenThem() {
        List<Box> alone = LadderGeometry.parts(LadderType.DEFAULT, Direction.NORTH, LadderGeometry.Joins.NONE).frame();
        assertEquals(6, alone.size());
        List<Box> low = LadderGeometry.parts(LadderType.DEFAULT, Direction.NORTH,
                new LadderGeometry.Joins(true, false)).frame();
        assertEquals(5, low.size());
        // Rungs run out to the joined side, here west (low x), and no stile stands there
        assertTrue(low.stream().anyMatch(b -> b.x1() == 0 && b.y2() - b.y1() == 1));
        assertTrue(low.stream().noneMatch(b -> b.x1() == 0 && b.y2() - b.y1() == 16));

        List<Box> east = LadderGeometry.parts(LadderType.DEFAULT, Direction.EAST,
                new LadderGeometry.Joins(false, true)).frame();
        assertTrue(east.stream().anyMatch(b -> b.z2() == 16 && b.y2() - b.y1() == 1), "east wall runs along z");
        assertTrue(east.stream().allMatch(b -> b.x1() == 0 && b.x2() <= 3), "against the west side");
    }

    @Test
    void onlyRailsHaveIron() {
        for (Direction facing : Direction.values()) {
            assertFalse(LadderGeometry.parts(LadderType.RAIL, facing, LadderGeometry.Joins.NONE).iron().isEmpty());
            assertTrue(LadderGeometry.parts(LadderType.POLE, facing, LadderGeometry.Joins.NONE).iron().isEmpty());
        }
    }

    /** Parts stay inside the outline, except freestanding rails, which the original drew 1 pixel wider. */
    @Test
    void everyPartStaysInsideTheOutline() {
        for (Direction facing : Direction.values()) {
            Box outline = LadderGeometry.outline(facing);
            if (LadderGeometry.freestanding(facing)) {
                outline = facing == Direction.UP ? new Box(5, 0, 0, 11, 16, 16) : new Box(0, 0, 5, 16, 16, 11);
            }
            for (LadderType type : LadderType.values()) {
                for (boolean low : new boolean[] { false, true }) {
                    for (boolean high : new boolean[] { false, true }) {
                        var parts = LadderGeometry.parts(type, facing, new LadderGeometry.Joins(low, high));
                        for (List<Box> list : List.of(parts.frame(), parts.iron())) {
                            for (Box b : list) {
                                String what = type + " " + facing + " " + b;
                                assertFalse(b.isEmpty(), what);
                                assertTrue(b.x1() >= outline.x1() && b.y1() >= outline.y1() && b.z1() >= outline.z1()
                                        && b.x2() <= outline.x2() && b.y2() <= outline.y2() && b.z2() <= outline.z2(),
                                        what);
                            }
                        }
                    }
                }
            }
        }
    }
}
