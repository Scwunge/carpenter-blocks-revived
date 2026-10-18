package com.carpentersblocks.block.sided;

import net.minecraft.core.Direction;

import org.joml.Vector3f;

/**
 * The original's {@code Vec3.rotateAround*} from 1.7, which turn the opposite way to JOML's, for porting its hand-built
 * handles (lever, torch) vertex for vertex.
 */
public final class LegacyVectors {

    /**
     * Corner indices of a cuboid's six faces, counter-clockwise from outside, for corners 0-3 at its foot and 4-7 at
     * its end in the original's order: down, up, north, east, south, west as first built.
     */
    public static final int[][] CUBOID_FACES = { { 0, 1, 2, 3 }, { 7, 6, 5, 4 }, { 1, 0, 4, 5 }, { 2, 1, 5, 6 },
            { 3, 2, 6, 7 }, { 0, 3, 7, 4 } };

    private LegacyVectors() {}

    /** The eight corners of an upright square stick {@code 2 * half} wide, from {@code y1} to {@code y2}. */
    public static Vector3f[] stick(float half, float y1, float y2) {
        return new Vector3f[] { new Vector3f(-half, y1, -half), new Vector3f(half, y1, -half),
                new Vector3f(half, y1, half), new Vector3f(-half, y1, half), new Vector3f(-half, y2, -half),
                new Vector3f(half, y2, -half), new Vector3f(half, y2, half), new Vector3f(-half, y2, half) };
    }

    public static void rotateX(Vector3f v, float angle) {
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float y = v.y * c + v.z * s;
        float z = v.z * c - v.y * s;
        v.set(v.x, y, z);
    }

    public static void rotateY(Vector3f v, float angle) {
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float x = v.x * c + v.z * s;
        float z = v.z * c - v.x * s;
        v.set(x, v.y, z);
    }

    public static void rotateZ(Vector3f v, float angle) {
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float x = v.x * c + v.y * s;
        float y = v.y * c - v.x * s;
        v.set(x, y, v.z);
    }

    /** The original's turn of a wall-mounted handle from facing north to {@code facing}. */
    public static float wallTurn(Direction facing) {
        return switch (facing) {
            case SOUTH -> (float) Math.PI;
            case WEST -> (float) Math.PI / 2F;
            case EAST -> -(float) Math.PI / 2F;
            default -> 0;
        };
    }
}
