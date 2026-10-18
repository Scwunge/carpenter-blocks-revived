package com.carpentersblocks.block.torch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.Direction;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import com.carpentersblocks.block.barrier.Box;

class TorchGeometryTest {

    @Test
    void rainPutsTorchesOutAStepAtATimeAndDryWeatherRelightsThem() {
        assertEquals(TorchFlame.SMOLDERING, TorchFlame.LIT.weather(true));
        assertEquals(TorchFlame.UNLIT, TorchFlame.SMOLDERING.weather(true));
        assertEquals(TorchFlame.UNLIT, TorchFlame.UNLIT.weather(true));
        assertEquals(TorchFlame.SMOLDERING, TorchFlame.UNLIT.weather(false));
        assertEquals(TorchFlame.LIT, TorchFlame.SMOLDERING.weather(false));
        assertEquals(TorchFlame.LIT, TorchFlame.LIT.weather(false));
        assertTrue(TorchFlame.LIT.light() > TorchFlame.SMOLDERING.light());
        assertEquals(0, TorchFlame.UNLIT.light());
    }

    @Test
    void hammerCyclesStyles() {
        assertEquals(TorchType.LANTERN, TorchType.VANILLA.next());
        assertEquals(TorchType.VANILLA, TorchType.LANTERN.next());
        assertEquals(TorchType.LANTERN, TorchType.VANILLA.previous());
    }

    private static Vector3f centre(Vector3f[] corners, int from) {
        Vector3f c = new Vector3f();
        for (int i = from; i < from + 4; ++i) {
            c.add(corners[i]);
        }
        return c.div(4);
    }

    @Test
    void wallTorchesLeanOutFromTheWallWithTheFlameOnTop() {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            Vector3f[] handle = TorchGeometry.handle(facing);
            Vector3f foot = centre(handle, 0);
            Vector3f tip = centre(handle, 4);
            Vector3f lean = tip.sub(foot, new Vector3f());
            assertTrue(lean.y > 0.4F, facing + " stands up " + lean);
            float out = lean.dot(facing.getStepX(), 0, facing.getStepZ());
            assertTrue(out > 0.1F && out < lean.y, facing + " leans out a little " + lean);
            // The foot is at the wall it hangs on
            Vector3f wall = new Vector3f(0.5F, 0, 0.5F).sub(facing.getStepX() * 0.5F, 0, facing.getStepZ() * 0.5F);
            float fromWall = foot.sub(wall, new Vector3f()).dot(facing.getStepX(), 0, facing.getStepZ());
            assertTrue(Math.abs(fromWall) < 0.1F, facing + " foot " + foot);
            Vector3f flameFoot = centre(TorchGeometry.flame(facing), 0);
            assertTrue(flameFoot.distance(tip) < 0.01F, "flame sits on the handle");
            Vector3f flameTip = centre(TorchGeometry.flame(facing), 4);
            assertTrue(TorchGeometry.head(TorchType.VANILLA, facing).distance(flameTip) < 0.2F,
                    "smoke rises from the flame");
        }
        Vector3f floor = centre(TorchGeometry.handle(Direction.UP), 0);
        assertEquals(new Vector3f(0.5F, 0, 0.5F), floor);
    }

    @Test
    void lanternsHaveGlassOnEverySideButTheWall() {
        assertEquals(4, TorchGeometry.lanternGlass(Direction.UP).size());
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            assertEquals(3, TorchGeometry.lanternGlass(facing).size());
            assertTrue(TorchGeometry.lanternGlass(facing).stream().noneMatch(p -> p.faces() == facing.getOpposite()));
        }
    }

    @Test
    void everyBoxStaysInsideTheBlock() {
        for (Direction facing : new Direction[] { Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST,
                Direction.EAST }) {
            for (TorchType type : TorchType.values()) {
                check(TorchGeometry.outline(type, facing));
            }
            TorchGeometry.lanternFrame(facing).forEach(TorchGeometryTest::check);
            for (Vector3f corner : TorchGeometry.flame(facing)) {
                assertTrue(corner.x >= 0 && corner.x <= 1 && corner.y >= 0 && corner.y <= 1 && corner.z >= 0
                        && corner.z <= 1, facing + " " + corner);
            }
        }
    }

    private static void check(Box box) {
        assertTrue(box.x1() >= 0 && box.y1() >= 0 && box.z1() >= 0 && box.x2() <= 16 && box.y2() <= 16
                && box.z2() <= 16 && !box.isEmpty(), box.toString());
    }
}
