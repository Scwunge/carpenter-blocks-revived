package com.carpentersblocks.block.stairs;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.core.Direction.WEST;

/**
 * The 28 shapes of the Carpenter's Stairs, in the order of the original's stairs IDs (the ordinal is the ID).
 * Port of {@code data.Stairs} with the boxes of {@code StairsUtil.genBounds}, in pixels.
 * <p>
 * The facings are the directions the stairs open towards: a {@link Type#NORMAL} stair facing up and north climbs
 * towards the south. {@code NEG} stairs are upside down; {@code SIDE} stairs stand on end, open on two sides.
 */
public enum StairsShape implements StringRepresentable {

    SIDE_SE(Type.SIDE, set(SOUTH, EAST), box(0, 0, 0, 8, 16, 16), box(8, 0, 0, 16, 16, 8)),
    SIDE_NW(Type.SIDE, set(NORTH, WEST), box(8, 0, 0, 16, 16, 16), box(0, 0, 8, 8, 16, 16)),
    SIDE_NE(Type.SIDE, set(NORTH, EAST), box(0, 0, 0, 8, 16, 16), box(8, 0, 8, 16, 16, 16)),
    SIDE_SW(Type.SIDE, set(SOUTH, WEST), box(8, 0, 0, 16, 16, 16), box(0, 0, 0, 8, 16, 8)),
    NEG_N(Type.NORMAL, set(DOWN, NORTH), box(0, 0, 8, 16, 16, 16), box(0, 8, 0, 16, 16, 8)),
    NEG_S(Type.NORMAL, set(DOWN, SOUTH), box(0, 0, 0, 16, 16, 8), box(0, 8, 8, 16, 16, 16)),
    NEG_W(Type.NORMAL, set(DOWN, WEST), box(8, 0, 0, 16, 16, 16), box(0, 8, 0, 8, 16, 16)),
    NEG_E(Type.NORMAL, set(DOWN, EAST), box(0, 0, 0, 8, 16, 16), box(8, 8, 0, 16, 16, 16)),
    POS_N(Type.NORMAL, set(UP, NORTH), box(0, 0, 8, 16, 16, 16), box(0, 0, 0, 16, 8, 8)),
    POS_S(Type.NORMAL, set(UP, SOUTH), box(0, 0, 0, 16, 16, 8), box(0, 0, 8, 16, 8, 16)),
    POS_W(Type.NORMAL, set(UP, WEST), box(8, 0, 0, 16, 16, 16), box(0, 0, 0, 8, 8, 16)),
    POS_E(Type.NORMAL, set(UP, EAST), box(0, 0, 0, 8, 16, 16), box(8, 0, 0, 16, 8, 16)),
    INT_NEG_SE(Type.INTERIOR, set(DOWN, SOUTH, EAST),
            box(0, 0, 0, 8, 16, 16), box(8, 0, 0, 16, 16, 8), box(8, 8, 8, 16, 16, 16)),
    INT_NEG_NW(Type.INTERIOR, set(DOWN, NORTH, WEST),
            box(8, 0, 0, 16, 16, 16), box(0, 0, 8, 8, 16, 16), box(0, 8, 0, 8, 16, 8)),
    INT_NEG_NE(Type.INTERIOR, set(DOWN, NORTH, EAST),
            box(0, 0, 0, 8, 16, 16), box(8, 0, 8, 16, 16, 16), box(8, 8, 0, 16, 16, 8)),
    INT_NEG_SW(Type.INTERIOR, set(DOWN, SOUTH, WEST),
            box(8, 0, 0, 16, 16, 16), box(0, 0, 0, 8, 16, 8), box(0, 8, 8, 8, 16, 16)),
    INT_POS_SE(Type.INTERIOR, set(UP, SOUTH, EAST),
            box(0, 0, 0, 8, 16, 16), box(8, 0, 0, 16, 16, 8), box(8, 0, 8, 16, 8, 16)),
    INT_POS_NW(Type.INTERIOR, set(UP, NORTH, WEST),
            box(8, 0, 0, 16, 16, 16), box(0, 0, 8, 8, 16, 16), box(0, 0, 0, 8, 8, 8)),
    INT_POS_NE(Type.INTERIOR, set(UP, NORTH, EAST),
            box(0, 0, 0, 8, 16, 16), box(8, 0, 8, 16, 16, 16), box(8, 0, 0, 16, 8, 8)),
    INT_POS_SW(Type.INTERIOR, set(UP, SOUTH, WEST),
            box(8, 0, 0, 16, 16, 16), box(0, 0, 0, 8, 16, 8), box(0, 0, 8, 8, 8, 16)),
    EXT_NEG_SE(Type.EXTERIOR, set(DOWN, SOUTH, EAST),
            box(0, 0, 0, 8, 16, 8), box(8, 8, 0, 16, 16, 16), box(0, 8, 8, 8, 16, 16)),
    EXT_NEG_NW(Type.EXTERIOR, set(DOWN, NORTH, WEST),
            box(8, 0, 8, 16, 16, 16), box(0, 8, 0, 8, 16, 16), box(8, 8, 0, 16, 16, 8)),
    EXT_NEG_NE(Type.EXTERIOR, set(DOWN, NORTH, EAST),
            box(0, 0, 8, 8, 16, 16), box(8, 8, 0, 16, 16, 16), box(0, 8, 0, 8, 16, 8)),
    EXT_NEG_SW(Type.EXTERIOR, set(DOWN, SOUTH, WEST),
            box(8, 0, 0, 16, 16, 8), box(0, 8, 0, 8, 16, 16), box(8, 8, 8, 16, 16, 16)),
    EXT_POS_SE(Type.EXTERIOR, set(UP, SOUTH, EAST),
            box(0, 0, 0, 8, 16, 8), box(8, 0, 0, 16, 8, 16), box(0, 0, 8, 8, 8, 16)),
    EXT_POS_NW(Type.EXTERIOR, set(UP, NORTH, WEST),
            box(8, 0, 8, 16, 16, 16), box(0, 0, 0, 8, 8, 16), box(8, 0, 0, 16, 8, 8)),
    EXT_POS_NE(Type.EXTERIOR, set(UP, NORTH, EAST),
            box(0, 0, 8, 8, 16, 16), box(8, 0, 0, 16, 8, 16), box(0, 0, 0, 8, 8, 8)),
    EXT_POS_SW(Type.EXTERIOR, set(UP, SOUTH, WEST),
            box(8, 0, 0, 16, 16, 8), box(0, 0, 0, 8, 8, 16), box(8, 0, 8, 16, 8, 16));

    /** Kinds of stairs; the original's {@code NORMAL_SIDE}, {@code NORMAL}, {@code NORMAL_INT} and {@code NORMAL_EXT}. */
    public enum Type { SIDE, NORMAL, INTERIOR, EXTERIOR }

    /** Within a group of four, the order of the directions of {@code NORMAL} stairs. */
    private static final List<Direction> NORMAL_ORDER = List.of(NORTH, SOUTH, WEST, EAST);

    private final Type type;
    private final Set<Direction> facings;
    private final List<double[]> boxes;
    private final VoxelShape shape;

    StairsShape(Type type, Set<Direction> facings, double[]... boxes) {
        this.type = type;
        this.facings = facings;
        this.boxes = List.of(boxes);
        VoxelShape union = Shapes.empty();
        for (double[] b : boxes) {
            union = Shapes.or(union, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
        }
        this.shape = union.optimize();
    }

    private static Set<Direction> set(Direction... directions) {
        return EnumSet.of(directions[0], directions);
    }

    private static double[] box(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new double[] { x1, y1, z1, x2, y2, z2 };
    }

    public Type type() {
        return type;
    }

    /** Directions the stairs open towards. */
    public Set<Direction> facings() {
        return EnumSet.copyOf(facings);
    }

    public boolean faces(Direction direction) {
        return facings.contains(direction);
    }

    /** Whether the stairs climb upwards (the solid half is at the bottom). Side stairs are not positive. */
    public boolean isPositive() {
        return facings.contains(UP);
    }

    /** The boxes making up the stairs, as {x1, y1, z1, x2, y2, z2} in pixels. */
    public List<double[]> boxes() {
        return boxes.stream().map(double[]::clone).toList();
    }

    public VoxelShape shape() {
        return shape;
    }

    /** The original's stairs ID. */
    public int id() {
        return ordinal();
    }

    public static StairsShape byId(int id) {
        return values()[id];
    }

    /** Hammer left-click: the next direction within the same kind of stairs. */
    public StairsShape nextDirection() {
        int group = id() / 4 * 4;
        return byId(group + (id() - group + 1) % 4);
    }

    /**
     * Hammer right-click: side, then upright, upside-down, interior upright, interior upside-down, exterior upright,
     * exterior upside-down, and back to side, as the original's ID arithmetic.
     */
    public StairsShape nextType() {
        return switch (type) {
            case SIDE -> byId(id() + 8);
            case NORMAL, INTERIOR -> isPositive() ? byId(id() - 4) : byId(id() + 12);
            case EXTERIOR -> isPositive() ? byId(id() - 4) : SIDE_SE;
        };
    }

    public StairsShape rotate(Rotation rotation) {
        Set<Direction> rotated = EnumSet.noneOf(Direction.class);
        facings.forEach(facing -> rotated.add(rotation.rotate(facing)));
        return withFacings(rotated);
    }

    public StairsShape mirror(Mirror mirror) {
        Set<Direction> mirrored = EnumSet.noneOf(Direction.class);
        facings.forEach(facing -> mirrored.add(mirror.mirror(facing)));
        return withFacings(mirrored);
    }

    private StairsShape withFacings(Set<Direction> wanted) {
        for (StairsShape candidate : values()) {
            if (candidate.type == type && candidate.facings.equals(wanted)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No " + type + " stairs facing " + wanted);
    }

    /** Upright or upside-down stairs open towards the given horizontal direction. */
    public static StairsShape normal(boolean positive, Direction opening) {
        return byId((positive ? POS_N : NEG_N).id() + NORMAL_ORDER.indexOf(opening));
    }

    /**
     * The shape for stairs placed against {@code face}, as the original's {@code onBlockPlaced}: on a side face, the
     * upper or lower triangle of the face gives upside-down or upright stairs and the left or right edge gives side
     * stairs; on the top or bottom face, the stairs climb the way the player looks.
     *
     * @param hitX where the face was hit, from 0 to 1 within the block space
     * @param lookDirection the horizontal direction the player looks in
     */
    public static StairsShape forPlacement(Direction face, double hitX, double hitY, double hitZ,
            Direction lookDirection) {
        if (face.getAxis() == Direction.Axis.Y) {
            return normal(face == UP, lookDirection.getOpposite());
        }
        // Distance along the face from its left edge, seen from outside
        double across = switch (face) {
            case NORTH -> 1.0 - hitX;
            case WEST -> hitZ;
            case EAST -> 1.0 - hitZ;
            default -> hitX;
        };
        boolean upperTriangle = hitY > 0.5 && across > 1.0 - hitY && across < hitY;
        boolean lowerTriangle = hitY < 0.5 && across < 1.0 - hitY && across > hitY;
        if (!upperTriangle && !lowerTriangle) {
            if (across < 0.2) {
                return switch (face) {
                    case NORTH -> SIDE_NW;
                    case SOUTH -> SIDE_SE;
                    case WEST -> SIDE_SW;
                    default -> SIDE_NE;
                };
            }
            if (across > 0.8) {
                return switch (face) {
                    case NORTH -> SIDE_NE;
                    case SOUTH -> SIDE_SW;
                    case WEST -> SIDE_NW;
                    default -> SIDE_SE;
                };
            }
        }
        boolean upsideDown = upperTriangle || !lowerTriangle && hitY > 0.5;
        return normal(!upsideDown, face);
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
