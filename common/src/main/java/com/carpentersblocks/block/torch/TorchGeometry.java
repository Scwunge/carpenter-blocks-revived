package com.carpentersblocks.block.torch;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;

import org.joml.Vector3f;

import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.sided.LegacyVectors;

/**
 * Geometry of the Carpenter's Torch, from the original's {@code setBlockBoundsBasedOnState} and renderer. Boxes are
 * in pixels; the vanilla-style torch is a stick in block units, tilted on walls as the original's.
 */
public final class TorchGeometry {

    private TorchGeometry() {}

    public static Box outline(TorchType type, Direction facing) {
        if (type == TorchType.VANILLA) {
            return facing == Direction.UP ? new Box(6.4, 0, 6.4, 9.6, 9.6, 9.6)
                    : new Box(5.6, 3.2, 0, 10.4, 12.8, 4.8).attached(facing);
        }
        return facing == Direction.UP ? new Box(3, 0, 3, 13, 15, 13) : new Box(3, 4, 0, 13, 15, 13).attached(facing);
    }

    /** Where flames and smoke rise, as the original's {@code getHeadCoordinates} (block units). */
    public static Vector3f head(TorchType type, Direction facing) {
        if (type == TorchType.LANTERN) {
            return new Vector3f(0.5F, 0.5625F, 0.5F);
        }
        float up = 0.22F;
        float out = 0.27F;
        return switch (facing) {
            case NORTH -> new Vector3f(0.5F, 0.7F + up, 0.5F + out);
            case SOUTH -> new Vector3f(0.5F, 0.7F + up, 0.5F - out);
            case WEST -> new Vector3f(0.5F + out, 0.7F + up, 0.5F);
            case EAST -> new Vector3f(0.5F - out, 0.7F + up, 0.5F);
            default -> new Vector3f(0.5F, 0.7F, 0.5F);
        };
    }

    /** The vanilla-style torch's handle, which takes the cover: a 2-pixel stick half a block long. */
    public static Vector3f[] handle(Direction facing) {
        return place(LegacyVectors.stick(0.0625F, 0, 0.5F), facing);
    }

    /** The vanilla-style torch's burning head on top of the handle. */
    public static Vector3f[] flame(Direction facing) {
        return place(LegacyVectors.stick(0.0625F, 0.5F, 0.625F), facing);
    }

    /** Stands a stick up from the floor, or leans it out from a wall as the original's {@code setVanillaRotations}. */
    private static Vector3f[] place(Vector3f[] stick, Direction facing) {
        for (Vector3f v : stick) {
            if (facing == Direction.UP) {
                v.add(0.5F, 0, 0.5F);
            } else {
                v.z += 0.0625F;
                LegacyVectors.rotateX(v, -((float) Math.PI * 3.4F / 9F));
                v.y -= 0.4375F;
                LegacyVectors.rotateX(v, (float) Math.PI / 2F);
                LegacyVectors.rotateY(v, LegacyVectors.wallTurn(facing));
                v.add(0.5F, 0.1875F, 0.5F);
            }
        }
        return stick;
    }

    /** The lantern's body, which takes the cover. */
    public static List<Box> lanternFrame(Direction facing) {
        List<Box> boxes = new ArrayList<>();
        if (facing == Direction.UP) {
            boxes.add(new Box(6, 0, 6, 10, 6, 10));
        } else {
            // An arm out from the wall and a cup under the flame
            boxes.add(new Box(7, 6, 0, 9, 12, 4).attached(facing));
            boxes.add(new Box(6, 6, 4, 10, 12, 6).attached(facing));
            boxes.add(new Box(6, 4, 6, 10, 6, 10));
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            // Corner posts and the bottom rim
            boxes.add(new Box(4, 4, 4, 6, 12, 6).facing(side));
            boxes.add(new Box(4, 4, 6, 6, 6, 10).facing(side));
        }
        boxes.add(new Box(3, 12, 3, 13, 14, 13));
        boxes.add(new Box(5, 14, 5, 11, 15, 11));
        return boxes;
    }

    /**
     * The lantern's glass panes as flat boxes, each with the side it faces, leaving out the one where the arm meets
     * the wall.
     */
    public static List<Pane> lanternGlass(Direction facing) {
        List<Pane> panes = new ArrayList<>();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (side != facing) {
                panes.add(new Pane(new Box(6, 6, 5, 10, 12, 5).facing(side), side.getOpposite()));
            }
        }
        return panes;
    }

    public record Pane(Box box, Direction faces) {}

    public static final Box LANTERN_FLAME = new Box(7, 6, 7, 9, 8, 9);
}
