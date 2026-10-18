package com.carpentersblocks.block.redstone;

import java.util.List;

import net.minecraft.core.Direction;

import org.joml.Vector3f;

import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.sided.LegacyVectors;

/**
 * Boxes (pixels) of the redstone family, from the original's {@code setBlockBoundsBasedOnState} and renderers. Each
 * is drawn for a block on the south face of its support (pressed against z = 0) and turned to its facing.
 */
public final class RedstoneGeometry {

    private RedstoneGeometry() {}

    /** A south-facing box turned to {@code facing}; see {@link Box#attached}. */
    public static Box attached(Box south, Direction facing) {
        return south.attached(facing);
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
        Vector3f[] out = LegacyVectors.stick(0.0625F, 0, 0.625F);
        float tilt = (float) Math.PI * 2F / 9F;
        for (Vector3f v : out) {
            if (on) {
                v.z -= 0.0625F;
                LegacyVectors.rotateX(v, tilt);
            } else {
                v.z += 0.0625F;
                LegacyVectors.rotateX(v, -tilt);
            }
            if (facing.getAxis() == Direction.Axis.Y) {
                if (facing == Direction.DOWN) {
                    LegacyVectors.rotateZ(v, (float) Math.PI);
                }
                if (!alongZ) {
                    LegacyVectors.rotateY(v, (float) Math.PI / 2F);
                }
                v.add(0.5F, facing == Direction.UP ? 0.125F : 0.875F, 0.5F);
            } else {
                v.y -= 0.375F;
                LegacyVectors.rotateX(v, (float) Math.PI / 2F);
                LegacyVectors.rotateY(v, LegacyVectors.wallTurn(facing));
                v.add(0.5F, 0.5F, 0.5F);
            }
        }
        return out;
    }
}
