package com.carpentersblocks.block.redstone;

import java.util.List;

import net.minecraft.core.Direction;

import org.joml.Vector3f;

import com.carpentersblocks.block.barrier.Box;

/**
 * Boxes (pixels) of the redstone family, from the original's {@code setBlockBoundsBasedOnState} and renderers. Each
 * is drawn for a block on the south face of its support (pressed against z = 0) and turned to its facing.
 */
public final class RedstoneGeometry {

    private RedstoneGeometry() {}

    /** A south-facing box turned to {@code facing}, as the original's direction-aware {@code setBlockBounds}. */
    public static Box attached(Box south, Direction facing) {
        return switch (facing) {
            case UP -> new Box(south.x1(), south.z1(), south.y1(), south.x2(), south.z2(), south.y2());
            case DOWN -> new Box(south.x1(), 16 - south.z2(), south.y1(), south.x2(), 16 - south.z1(), south.y2());
            default -> south.facing(facing);
        };
    }

    public static Box button(Direction facing, boolean pressed) {
        return attached(new Box(5, 6, 0, 11, 10, pressed ? 1 : 2), facing);
    }

    /** The plate. Its idle box also bounds what presses it, as the original's {@code fullBounds}. */
    public static Box pressurePlate(Direction facing, boolean pressed) {
        return attached(new Box(1, 1, 0, 15, 15, pressed ? 0.5 : 1), facing);
    }

    /** The lever's base. {@code alongZ} only matters on floors and ceilings. */
    public static Box leverBase(Direction facing, boolean alongZ) {
        double o = 3; // 0.1875 of a block
        double lo = 3.2;
        double hi = 12.8;
        return switch (facing) {
            case UP -> alongZ ? new Box(8 - o, 0, lo, 8 + o, o, hi) : new Box(lo, 0, 8 - o, hi, o, 8 + o);
            case DOWN -> alongZ ? new Box(8 - o, 16 - o, lo, 8 + o, 16, hi) : new Box(lo, 16 - o, 8 - o, hi, 16, 8 + o);
            default -> attached(new Box(8 - o, lo, 0, 8 + o, hi, o), facing);
        };
    }

    /** The daylight sensor's outline: a quarter-block slab against its support. */
    public static Box daylightSensor(Direction facing) {
        return attached(new Box(0, 0, 0, 16, 16, 4), facing);
    }

    /** The parts of the daylight sensor that take the cover: its back and rim. */
    public static final List<Box> SENSOR_FRAME = List.of(
            new Box(1, 1, 0, 15, 15, 1),
            new Box(0, 0, 0, 1, 16, 4),
            new Box(1, 0, 0, 15, 1, 4),
            new Box(1, 15, 0, 15, 16, 4),
            new Box(15, 0, 0, 16, 16, 4));
    public static final Box SENSOR_GLASS = new Box(1, 1, 3, 15, 15, 4);
    public static final Box SENSOR_LAPIS = new Box(2, 2, 1, 14, 14, 3);
    public static final List<Box> SENSOR_REDSTONE = List.of(
            new Box(1, 1, 1, 2, 15, 3),
            new Box(2, 1, 1, 14, 2, 3),
            new Box(2, 14, 1, 14, 15, 3),
            new Box(14, 1, 1, 15, 15, 3));

    /**
     * The eight corners (block units) of the lever's handle, as the original's renderer: corners 0-3 are its foot and
     * 4-7 its tip. It tilts 40 degrees one way when on and the other when off.
     */
    public static Vector3f[] leverHandle(Direction facing, boolean alongZ, boolean on) {
        float w = 0.0625F;
        float[][] corners = { { -w, 0, -w }, { w, 0, -w }, { w, 0, w }, { -w, 0, w }, { -w, 0.625F, -w },
                { w, 0.625F, -w }, { w, 0.625F, w }, { -w, 0.625F, w } };
        float tilt = (float) Math.PI * 2F / 9F;
        Vector3f[] out = new Vector3f[8];
        for (int i = 0; i < 8; ++i) {
            Vector3f v = new Vector3f(corners[i][0], corners[i][1], corners[i][2]);
            if (on) {
                v.z -= 0.0625F;
                rotateX(v, tilt);
            } else {
                v.z += 0.0625F;
                rotateX(v, -tilt);
            }
            if (facing.getAxis() == Direction.Axis.Y) {
                if (facing == Direction.DOWN) {
                    rotateZ(v, (float) Math.PI);
                }
                if (!alongZ) {
                    rotateY(v, (float) Math.PI / 2F);
                }
                v.add(0.5F, facing == Direction.UP ? 0.125F : 0.875F, 0.5F);
            } else {
                v.y -= 0.375F;
                rotateX(v, (float) Math.PI / 2F);
                rotateY(v, switch (facing) {
                    case SOUTH -> (float) Math.PI;
                    case WEST -> (float) Math.PI / 2F;
                    case EAST -> -(float) Math.PI / 2F;
                    default -> 0;
                });
                v.add(0.5F, 0.5F, 0.5F);
            }
            out[i] = v;
        }
        return out;
    }

    /** Corner indices of the handle's six faces, counter-clockwise from outside: foot, tip, then the four sides. */
    public static final int[][] HANDLE_FACES = { { 0, 1, 2, 3 }, { 7, 6, 5, 4 }, { 1, 0, 4, 5 }, { 2, 1, 5, 6 },
            { 3, 2, 6, 7 }, { 0, 3, 7, 4 } };

    // Vec3.rotateAround* from 1.7, which turn the opposite way to JOML's

    private static void rotateX(Vector3f v, float angle) {
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float y = v.y * c + v.z * s;
        float z = v.z * c - v.y * s;
        v.set(v.x, y, z);
    }

    private static void rotateY(Vector3f v, float angle) {
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float x = v.x * c + v.z * s;
        float z = v.z * c - v.x * s;
        v.set(x, v.y, z);
    }

    private static void rotateZ(Vector3f v, float angle) {
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float x = v.x * c + v.y * s;
        float y = v.y * c - v.x * s;
        v.set(x, y, v.z);
    }
}
