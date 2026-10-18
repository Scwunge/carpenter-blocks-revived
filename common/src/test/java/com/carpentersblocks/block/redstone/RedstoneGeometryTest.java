package com.carpentersblocks.block.redstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import com.carpentersblocks.block.barrier.Box;

class RedstoneGeometryTest {

    @Test
    void buttonsSitAgainstTheirSupport() {
        assertEquals(new Box(5, 6, 0, 11, 10, 2), RedstoneGeometry.button(Direction.SOUTH, false));
        assertEquals(new Box(5, 6, 14, 11, 10, 16), RedstoneGeometry.button(Direction.NORTH, false));
        assertEquals(new Box(0, 6, 5, 2, 10, 11), RedstoneGeometry.button(Direction.EAST, false));
        assertEquals(new Box(5, 0, 6, 11, 2, 10), RedstoneGeometry.button(Direction.UP, false));
        assertEquals(new Box(5, 14, 6, 11, 16, 10), RedstoneGeometry.button(Direction.DOWN, false));
        assertEquals(1, RedstoneGeometry.button(Direction.SOUTH, true).z2());
    }

    @Test
    void everyBoxStaysInsideTheBlock() {
        for (Direction facing : Direction.values()) {
            List<Box> boxes = new ArrayList<>();
            for (boolean flag : new boolean[] { false, true }) {
                boxes.add(RedstoneGeometry.button(facing, flag));
                boxes.add(RedstoneGeometry.pressurePlate(facing, flag));
                boxes.add(RedstoneGeometry.leverBase(facing, flag));
            }
            boxes.add(RedstoneGeometry.daylightSensor(facing));
            boxes.add(RedstoneGeometry.attached(RedstoneGeometry.SENSOR_GLASS, facing));
            for (Box box : boxes) {
                assertFalse(box.isEmpty(), facing + " " + box);
                assertTrue(box.x1() >= 0 && box.y1() >= 0 && box.z1() >= 0, facing + " " + box);
                assertTrue(box.x2() <= 16 && box.y2() <= 16 && box.z2() <= 16, facing + " " + box);
            }
        }
    }

    @Test
    void floorLeversLieAlongTheirAxis() {
        Box z = RedstoneGeometry.leverBase(Direction.UP, true);
        Box x = RedstoneGeometry.leverBase(Direction.UP, false);
        assertTrue(z.z2() - z.z1() > z.x2() - z.x1());
        assertTrue(x.x2() - x.x1() > x.z2() - x.z1());
        assertEquals(0, z.y1());
        assertEquals(16, RedstoneGeometry.leverBase(Direction.DOWN, true).y2());
    }

    private static Vector3f centre(Vector3f[] corners, int from) {
        Vector3f c = new Vector3f();
        for (int i = from; i < from + 4; ++i) {
            c.add(corners[i]);
        }
        return c.div(4);
    }

    @Test
    void leverHandlesStandOutOfTheBaseAndTiltWithTheirState() {
        for (Direction facing : Direction.values()) {
            for (boolean on : new boolean[] { false, true }) {
                Vector3f[] handle = RedstoneGeometry.leverHandle(facing, true, on);
                Vector3f foot = centre(handle, 0);
                Vector3f tip = centre(handle, 4);
                Vector3f out = tip.sub(foot, new Vector3f());
                // The handle points away from the support
                float along = out.dot(facing.getStepX(), facing.getStepY(), facing.getStepZ());
                assertTrue(along > 0.3F, facing + " " + on + " points " + out);
                for (Vector3f corner : handle) {
                    assertTrue(corner.x >= 0 && corner.x <= 1 && corner.y >= 0 && corner.y <= 1
                            && corner.z >= 0 && corner.z <= 1, facing + " " + corner);
                }
            }
        }
        // On a floor along z, on and off lean opposite ways
        float onLean = centre(RedstoneGeometry.leverHandle(Direction.UP, true, true), 4).z - 0.5F;
        float offLean = centre(RedstoneGeometry.leverHandle(Direction.UP, true, false), 4).z - 0.5F;
        assertTrue(onLean * offLean < 0, onLean + " " + offLean);
        // Along x, they lean on x instead
        float xLean = centre(RedstoneGeometry.leverHandle(Direction.UP, false, true), 4).x - 0.5F;
        assertTrue(Math.abs(xLean) > 0.1F, "x lean " + xLean);
    }

    @Test
    void sensitivitiesSwitchAtTheOriginalThresholds() {
        assertEquals(15, SensorSensitivity.SLEEP.output(12));
        assertEquals(0, SensorSensitivity.SLEEP.output(11));
        assertEquals(15, SensorSensitivity.MONSTERS.output(8));
        assertEquals(0, SensorSensitivity.MONSTERS.output(7));
        assertEquals(9, SensorSensitivity.DYNAMIC.output(9));
        assertEquals(SensorSensitivity.MONSTERS, SensorSensitivity.SLEEP.next());
        assertEquals(SensorSensitivity.SLEEP, SensorSensitivity.DYNAMIC.next());
    }

    @Test
    void readingsFollowTheSunAndStorms() {
        // Storms count as dark for the monster setting only
        assertEquals(7, SensorSensitivity.MONSTERS.reading(Direction.UP, 15, 0, true));
        assertEquals(15, SensorSensitivity.SLEEP.reading(Direction.UP, 15, 0, true));
        // Dynamic: full at noon facing up, dimmer on walls, nothing at night
        assertEquals(15, SensorSensitivity.DYNAMIC.reading(Direction.UP, 15, 0, false));
        assertEquals(9, SensorSensitivity.DYNAMIC.reading(Direction.NORTH, 15, 0, false));
        assertEquals(0, SensorSensitivity.DYNAMIC.reading(Direction.UP, 15, (float) Math.PI, false));
        // An east wall sees the morning sun better than the evening sun
        float morning = (float) (Math.PI * 1.75);
        float evening = (float) (Math.PI * 0.25);
        assertTrue(SensorSensitivity.DYNAMIC.reading(Direction.EAST, 15, morning, false)
                > SensorSensitivity.DYNAMIC.reading(Direction.EAST, 15, evening, false));
        assertEquals(0, SensorSensitivity.SLEEP.reading(Direction.UP, -3, 0, false));
    }

    @Test
    void platesCycleThroughTheirTriggers() {
        assertEquals(PlateTrigger.MONSTER, PlateTrigger.PLAYER.next());
        assertEquals(PlateTrigger.ANIMAL, PlateTrigger.MONSTER.next());
        assertEquals(PlateTrigger.ALL, PlateTrigger.ANIMAL.next());
        assertEquals(PlateTrigger.PLAYER, PlateTrigger.ALL.next());
    }
}
