package com.carpentersblocks.block.door;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import com.carpentersblocks.block.barrier.Box;

/**
 * What the door family draws, in pixels and already turned into place: boxes that take the cover, iron parts (the
 * handles, and the siding garage door's backing) and glass or screen panes. Iron parts may lie in a neighboring block,
 * as the original's handles did.
 */
public record DoorParts(List<Box> frame, List<Box> iron, List<Pane> panes) {

    /** A flat pane drawn from both sides, as the original's {@code renderPane}; {@code plane} has no thickness. */
    public record Pane(ResourceLocation texture, Box plane, Direction normal) {}

    public static DoorParts empty() {
        return new DoorParts(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * A full-size pane 1.5 pixels in from the {@code side} face, as the original's hinged {@code paneBounds}.
     */
    public static Pane pane(ResourceLocation texture, Direction side) {
        double at = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 14.5 : 1.5;
        Box plane = switch (side.getAxis()) {
            case X -> new Box(at, 0, 0, at, 16, 16);
            case Y -> new Box(0, at, 0, 16, at, 16);
            case Z -> new Box(0, 0, at, 16, 16, at);
        };
        return new Pane(texture, plane, side);
    }

    /** {@code box} moved by whole blocks towards {@code dir}, for parts drawn in a neighboring block. */
    public static Box offset(Box box, Direction dir) {
        double dx = dir.getStepX() * 16, dy = dir.getStepY() * 16, dz = dir.getStepZ() * 16;
        return new Box(box.x1() + dx, box.y1() + dy, box.z1() + dz, box.x2() + dx, box.y2() + dy, box.z2() + dz);
    }
}
