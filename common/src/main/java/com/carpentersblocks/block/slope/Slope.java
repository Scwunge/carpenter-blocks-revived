package com.carpentersblocks.block.slope;

import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.core.Direction.WEST;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/**
 * The 65 slope shapes of the Carpenter's Slope, port of the original {@code Slope}. The ordinal is the original slope
 * ID; face shapes, face biases and facings are copied from the original tables.
 * <p>
 * Facings name where the slope faces: {@code UP} or {@code DOWN} for upward ("positive") and downward slopes, plus the
 * horizontal directions its sloped faces (or, for prisms, its points) face.
 */
public enum Slope implements StringRepresentable {

    WEDGE_SE(Type.WEDGE_SIDE, dirs(SOUTH, EAST),
            faces(Face.WEDGE, Face.WEDGE, Face.FULL, Face.NONE, Face.FULL, Face.NONE), bias(Bias.XYNN, Bias.XYNN, 0, 0, 0, 0)),
    WEDGE_NW(Type.WEDGE_SIDE, dirs(NORTH, WEST),
            faces(Face.WEDGE, Face.WEDGE, Face.NONE, Face.FULL, Face.NONE, Face.FULL), bias(Bias.XYPP, Bias.XYPP, 0, 0, 0, 0)),
    WEDGE_NE(Type.WEDGE_SIDE, dirs(NORTH, EAST),
            faces(Face.WEDGE, Face.WEDGE, Face.NONE, Face.FULL, Face.FULL, Face.NONE), bias(Bias.XYNP, Bias.XYNP, 0, 0, 0, 0)),
    WEDGE_SW(Type.WEDGE_SIDE, dirs(SOUTH, WEST),
            faces(Face.WEDGE, Face.WEDGE, Face.FULL, Face.NONE, Face.NONE, Face.FULL), bias(Bias.XYPN, Bias.XYPN, 0, 0, 0, 0)),
    WEDGE_NEG_N(Type.WEDGE, dirs(DOWN, NORTH),
            faces(Face.NONE, Face.FULL, Face.NONE, Face.FULL, Face.WEDGE, Face.WEDGE), bias(0, 0, 0, 0, Bias.XYPP, Bias.XYPP)),
    WEDGE_NEG_S(Type.WEDGE, dirs(DOWN, SOUTH),
            faces(Face.NONE, Face.FULL, Face.FULL, Face.NONE, Face.WEDGE, Face.WEDGE), bias(0, 0, 0, 0, Bias.XYNP, Bias.XYNP)),
    WEDGE_NEG_W(Type.WEDGE, dirs(DOWN, WEST),
            faces(Face.NONE, Face.FULL, Face.WEDGE, Face.WEDGE, Face.NONE, Face.FULL), bias(0, 0, Bias.XYPP, Bias.XYPP, 0, 0)),
    WEDGE_NEG_E(Type.WEDGE, dirs(DOWN, EAST),
            faces(Face.NONE, Face.FULL, Face.WEDGE, Face.WEDGE, Face.FULL, Face.NONE), bias(0, 0, Bias.XYNP, Bias.XYNP, 0, 0)),
    WEDGE_POS_N(Type.WEDGE, dirs(UP, NORTH),
            faces(Face.FULL, Face.NONE, Face.NONE, Face.FULL, Face.WEDGE, Face.WEDGE), bias(0, 0, 0, 0, Bias.XYPN, Bias.XYPN)),
    WEDGE_POS_S(Type.WEDGE, dirs(UP, SOUTH),
            faces(Face.FULL, Face.NONE, Face.FULL, Face.NONE, Face.WEDGE, Face.WEDGE), bias(0, 0, 0, 0, Bias.XYNN, Bias.XYNN)),
    WEDGE_POS_W(Type.WEDGE, dirs(UP, WEST),
            faces(Face.FULL, Face.NONE, Face.WEDGE, Face.WEDGE, Face.NONE, Face.FULL), bias(0, 0, Bias.XYPN, Bias.XYPN, 0, 0)),
    WEDGE_POS_E(Type.WEDGE, dirs(UP, EAST),
            faces(Face.FULL, Face.NONE, Face.WEDGE, Face.WEDGE, Face.FULL, Face.NONE), bias(0, 0, Bias.XYNN, Bias.XYNN, 0, 0)),
    WEDGE_INT_NEG_SE(Type.WEDGE_INT, dirs(DOWN, SOUTH, EAST),
            faces(Face.NONE, Face.FULL, Face.FULL, Face.WEDGE, Face.FULL, Face.WEDGE), bias(0, 0, 0, Bias.XYNP, 0, Bias.XYNP)),
    WEDGE_INT_NEG_NW(Type.WEDGE_INT, dirs(DOWN, NORTH, WEST),
            faces(Face.NONE, Face.FULL, Face.WEDGE, Face.FULL, Face.WEDGE, Face.FULL), bias(0, 0, Bias.XYPP, 0, Bias.XYPP, 0)),
    WEDGE_INT_NEG_NE(Type.WEDGE_INT, dirs(DOWN, NORTH, EAST),
            faces(Face.NONE, Face.FULL, Face.WEDGE, Face.FULL, Face.FULL, Face.WEDGE), bias(0, 0, Bias.XYNP, 0, 0, Bias.XYPP)),
    WEDGE_INT_NEG_SW(Type.WEDGE_INT, dirs(DOWN, SOUTH, WEST),
            faces(Face.NONE, Face.FULL, Face.FULL, Face.WEDGE, Face.WEDGE, Face.FULL), bias(0, 0, 0, Bias.XYPP, Bias.XYNP, 0)),
    WEDGE_INT_POS_SE(Type.WEDGE_INT, dirs(UP, SOUTH, EAST),
            faces(Face.FULL, Face.NONE, Face.FULL, Face.WEDGE, Face.FULL, Face.WEDGE), bias(0, 0, 0, Bias.XYNN, 0, Bias.XYNN)),
    WEDGE_INT_POS_NW(Type.WEDGE_INT, dirs(UP, NORTH, WEST),
            faces(Face.FULL, Face.NONE, Face.WEDGE, Face.FULL, Face.WEDGE, Face.FULL), bias(0, 0, Bias.XYPN, 0, Bias.XYPN, 0)),
    WEDGE_INT_POS_NE(Type.WEDGE_INT, dirs(UP, NORTH, EAST),
            faces(Face.FULL, Face.NONE, Face.WEDGE, Face.FULL, Face.FULL, Face.WEDGE), bias(0, 0, Bias.XYNN, 0, 0, Bias.XYPN)),
    WEDGE_INT_POS_SW(Type.WEDGE_INT, dirs(UP, SOUTH, WEST),
            faces(Face.FULL, Face.NONE, Face.FULL, Face.WEDGE, Face.WEDGE, Face.FULL), bias(0, 0, 0, Bias.XYPN, Bias.XYNN, 0)),
    WEDGE_EXT_NEG_SE(Type.WEDGE_EXT, dirs(DOWN, SOUTH, EAST),
            faces(Face.NONE, Face.FULL, Face.WEDGE, Face.NONE, Face.WEDGE, Face.NONE), bias(0, 0, Bias.XYNP, 0, Bias.XYNP, 0)),
    WEDGE_EXT_NEG_NW(Type.WEDGE_EXT, dirs(DOWN, NORTH, WEST),
            faces(Face.NONE, Face.FULL, Face.NONE, Face.WEDGE, Face.NONE, Face.WEDGE), bias(0, 0, 0, Bias.XYPP, 0, Bias.XYPP)),
    WEDGE_EXT_NEG_NE(Type.WEDGE_EXT, dirs(DOWN, NORTH, EAST),
            faces(Face.NONE, Face.FULL, Face.NONE, Face.WEDGE, Face.WEDGE, Face.NONE), bias(0, 0, 0, Bias.XYNP, Bias.XYPP, 0)),
    WEDGE_EXT_NEG_SW(Type.WEDGE_EXT, dirs(DOWN, SOUTH, WEST),
            faces(Face.NONE, Face.FULL, Face.WEDGE, Face.NONE, Face.NONE, Face.WEDGE), bias(0, 0, Bias.XYPP, 0, 0, Bias.XYNP)),
    WEDGE_EXT_POS_SE(Type.WEDGE_EXT, dirs(UP, SOUTH, EAST),
            faces(Face.FULL, Face.NONE, Face.WEDGE, Face.NONE, Face.WEDGE, Face.NONE), bias(0, 0, Bias.XYNN, 0, Bias.XYNN, 0)),
    WEDGE_EXT_POS_NW(Type.WEDGE_EXT, dirs(UP, NORTH, WEST),
            faces(Face.FULL, Face.NONE, Face.NONE, Face.WEDGE, Face.NONE, Face.WEDGE), bias(0, 0, 0, Bias.XYPN, 0, Bias.XYPN)),
    WEDGE_EXT_POS_NE(Type.WEDGE_EXT, dirs(UP, NORTH, EAST),
            faces(Face.FULL, Face.NONE, Face.NONE, Face.WEDGE, Face.WEDGE, Face.NONE), bias(0, 0, 0, Bias.XYNN, Bias.XYPN, 0)),
    WEDGE_EXT_POS_SW(Type.WEDGE_EXT, dirs(UP, SOUTH, WEST),
            faces(Face.FULL, Face.NONE, Face.WEDGE, Face.NONE, Face.NONE, Face.WEDGE), bias(0, 0, Bias.XYPN, 0, 0, Bias.XYNN)),
    OBL_INT_NEG_SE(Type.OBLIQUE_INT, dirs(DOWN, SOUTH, EAST),
            faces(Face.WEDGE, Face.FULL, Face.FULL, Face.WEDGE, Face.FULL, Face.WEDGE), bias(Bias.XYNN, 0, 0, Bias.XYNP, 0, Bias.XYNP)),
    OBL_INT_NEG_NW(Type.OBLIQUE_INT, dirs(DOWN, NORTH, WEST),
            faces(Face.WEDGE, Face.FULL, Face.WEDGE, Face.FULL, Face.WEDGE, Face.FULL), bias(Bias.XYPP, 0, Bias.XYPP, 0, Bias.XYPP, 0)),
    OBL_INT_NEG_NE(Type.OBLIQUE_INT, dirs(DOWN, NORTH, EAST),
            faces(Face.WEDGE, Face.FULL, Face.WEDGE, Face.FULL, Face.FULL, Face.WEDGE), bias(Bias.XYNP, 0, Bias.XYNP, 0, 0, Bias.XYPP)),
    OBL_INT_NEG_SW(Type.OBLIQUE_INT, dirs(DOWN, SOUTH, WEST),
            faces(Face.WEDGE, Face.FULL, Face.FULL, Face.WEDGE, Face.WEDGE, Face.FULL), bias(Bias.XYPN, 0, 0, Bias.XYPP, Bias.XYNP, 0)),
    OBL_INT_POS_SE(Type.OBLIQUE_INT, dirs(UP, SOUTH, EAST),
            faces(Face.FULL, Face.WEDGE, Face.FULL, Face.WEDGE, Face.FULL, Face.WEDGE), bias(0, Bias.XYNN, 0, Bias.XYNN, 0, Bias.XYNN)),
    OBL_INT_POS_NW(Type.OBLIQUE_INT, dirs(UP, NORTH, WEST),
            faces(Face.FULL, Face.WEDGE, Face.WEDGE, Face.FULL, Face.WEDGE, Face.FULL), bias(0, Bias.XYPP, Bias.XYPN, 0, Bias.XYPN, 0)),
    OBL_INT_POS_NE(Type.OBLIQUE_INT, dirs(UP, NORTH, EAST),
            faces(Face.FULL, Face.WEDGE, Face.WEDGE, Face.FULL, Face.FULL, Face.WEDGE), bias(0, Bias.XYNP, Bias.XYNN, 0, 0, Bias.XYPN)),
    OBL_INT_POS_SW(Type.OBLIQUE_INT, dirs(UP, SOUTH, WEST),
            faces(Face.FULL, Face.WEDGE, Face.FULL, Face.WEDGE, Face.WEDGE, Face.FULL), bias(0, Bias.XYPN, 0, Bias.XYPN, Bias.XYNN, 0)),
    OBL_EXT_NEG_SE(Type.OBLIQUE_EXT, dirs(DOWN, SOUTH, EAST),
            faces(Face.NONE, Face.WEDGE, Face.WEDGE, Face.NONE, Face.WEDGE, Face.NONE), bias(0, Bias.XYNN, Bias.XYNP, 0, Bias.XYNP, 0)),
    OBL_EXT_NEG_NW(Type.OBLIQUE_EXT, dirs(DOWN, NORTH, WEST),
            faces(Face.NONE, Face.WEDGE, Face.NONE, Face.WEDGE, Face.NONE, Face.WEDGE), bias(0, Bias.XYPP, 0, Bias.XYPP, 0, Bias.XYPP)),
    OBL_EXT_NEG_NE(Type.OBLIQUE_EXT, dirs(DOWN, NORTH, EAST),
            faces(Face.NONE, Face.WEDGE, Face.NONE, Face.WEDGE, Face.WEDGE, Face.NONE), bias(0, Bias.XYNP, 0, Bias.XYNP, Bias.XYPP, 0)),
    OBL_EXT_NEG_SW(Type.OBLIQUE_EXT, dirs(DOWN, SOUTH, WEST),
            faces(Face.NONE, Face.WEDGE, Face.WEDGE, Face.NONE, Face.NONE, Face.WEDGE), bias(0, Bias.XYPN, Bias.XYPP, 0, 0, Bias.XYNP)),
    OBL_EXT_POS_SE(Type.OBLIQUE_EXT, dirs(UP, SOUTH, EAST),
            faces(Face.WEDGE, Face.NONE, Face.WEDGE, Face.NONE, Face.WEDGE, Face.NONE), bias(Bias.XYNN, 0, Bias.XYNN, 0, Bias.XYNN, 0)),
    OBL_EXT_POS_NW(Type.OBLIQUE_EXT, dirs(UP, NORTH, WEST),
            faces(Face.WEDGE, Face.NONE, Face.NONE, Face.WEDGE, Face.NONE, Face.WEDGE), bias(Bias.XYPP, 0, 0, Bias.XYPN, 0, Bias.XYPN)),
    OBL_EXT_POS_NE(Type.OBLIQUE_EXT, dirs(UP, NORTH, EAST),
            faces(Face.WEDGE, Face.NONE, Face.NONE, Face.WEDGE, Face.WEDGE, Face.NONE), bias(Bias.XYNP, 0, 0, Bias.XYNN, Bias.XYPN, 0)),
    OBL_EXT_POS_SW(Type.OBLIQUE_EXT, dirs(UP, SOUTH, WEST),
            faces(Face.WEDGE, Face.NONE, Face.WEDGE, Face.NONE, Face.NONE, Face.WEDGE), bias(Bias.XYPN, 0, Bias.XYPN, 0, 0, Bias.XYNN)),
    PRISM_NEG(Type.PRISM, dirs(DOWN),
            faces(Face.NONE, Face.FULL, Face.NONE, Face.NONE, Face.NONE, Face.NONE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_POS(Type.PRISM, dirs(UP),
            faces(Face.FULL, Face.NONE, Face.NONE, Face.NONE, Face.NONE, Face.NONE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_1P_POS_N(Type.PRISM_1P, dirs(UP, NORTH),
            faces(Face.FULL, Face.NONE, Face.TRIANGLE, Face.NONE, Face.NONE, Face.NONE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_1P_POS_S(Type.PRISM_1P, dirs(UP, SOUTH),
            faces(Face.FULL, Face.NONE, Face.NONE, Face.TRIANGLE, Face.NONE, Face.NONE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_1P_POS_W(Type.PRISM_1P, dirs(UP, WEST),
            faces(Face.FULL, Face.NONE, Face.NONE, Face.NONE, Face.TRIANGLE, Face.NONE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_1P_POS_E(Type.PRISM_1P, dirs(UP, EAST),
            faces(Face.FULL, Face.NONE, Face.NONE, Face.NONE, Face.NONE, Face.TRIANGLE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_2P_POS_NS(Type.PRISM_2P, dirs(UP, NORTH, SOUTH),
            faces(Face.FULL, Face.NONE, Face.TRIANGLE, Face.TRIANGLE, Face.NONE, Face.NONE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_2P_POS_WE(Type.PRISM_2P, dirs(UP, WEST, EAST),
            faces(Face.FULL, Face.NONE, Face.NONE, Face.NONE, Face.TRIANGLE, Face.TRIANGLE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_2P_POS_SE(Type.PRISM_2P, dirs(UP, SOUTH, EAST),
            faces(Face.FULL, Face.NONE, Face.NONE, Face.TRIANGLE, Face.NONE, Face.TRIANGLE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_2P_POS_NW(Type.PRISM_2P, dirs(UP, NORTH, WEST),
            faces(Face.FULL, Face.NONE, Face.TRIANGLE, Face.NONE, Face.TRIANGLE, Face.NONE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_2P_POS_NE(Type.PRISM_2P, dirs(UP, NORTH, EAST),
            faces(Face.FULL, Face.NONE, Face.TRIANGLE, Face.NONE, Face.NONE, Face.TRIANGLE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_2P_POS_SW(Type.PRISM_2P, dirs(UP, SOUTH, WEST),
            faces(Face.FULL, Face.NONE, Face.NONE, Face.TRIANGLE, Face.TRIANGLE, Face.NONE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_3P_POS_NWE(Type.PRISM_3P, dirs(UP, NORTH, WEST, EAST),
            faces(Face.FULL, Face.NONE, Face.TRIANGLE, Face.NONE, Face.TRIANGLE, Face.TRIANGLE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_3P_POS_SWE(Type.PRISM_3P, dirs(UP, SOUTH, WEST, EAST),
            faces(Face.FULL, Face.NONE, Face.NONE, Face.TRIANGLE, Face.TRIANGLE, Face.TRIANGLE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_3P_POS_NSW(Type.PRISM_3P, dirs(UP, NORTH, SOUTH, WEST),
            faces(Face.FULL, Face.NONE, Face.TRIANGLE, Face.TRIANGLE, Face.TRIANGLE, Face.NONE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_3P_POS_NSE(Type.PRISM_3P, dirs(UP, NORTH, SOUTH, EAST),
            faces(Face.FULL, Face.NONE, Face.TRIANGLE, Face.TRIANGLE, Face.NONE, Face.TRIANGLE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_4P_POS(Type.PRISM_4P, dirs(UP, NORTH, SOUTH, WEST, EAST),
            faces(Face.FULL, Face.NONE, Face.TRIANGLE, Face.TRIANGLE, Face.TRIANGLE, Face.TRIANGLE), bias(0, 0, 0, 0, 0, 0)),
    PRISM_WEDGE_POS_N(Type.PRISM_WEDGE, dirs(UP, NORTH),
            faces(Face.FULL, Face.NONE, Face.TRIANGLE, Face.FULL, Face.WEDGE, Face.WEDGE), bias(0, 0, 0, 0, Bias.XYPN, Bias.XYPN)),
    PRISM_WEDGE_POS_S(Type.PRISM_WEDGE, dirs(UP, SOUTH),
            faces(Face.FULL, Face.NONE, Face.FULL, Face.TRIANGLE, Face.WEDGE, Face.WEDGE), bias(0, 0, 0, 0, Bias.XYNN, Bias.XYNN)),
    PRISM_WEDGE_POS_W(Type.PRISM_WEDGE, dirs(UP, WEST),
            faces(Face.FULL, Face.NONE, Face.WEDGE, Face.WEDGE, Face.TRIANGLE, Face.FULL), bias(0, 0, Bias.XYPN, Bias.XYPN, 0, 0)),
    PRISM_WEDGE_POS_E(Type.PRISM_WEDGE, dirs(UP, EAST),
            faces(Face.FULL, Face.NONE, Face.WEDGE, Face.WEDGE, Face.FULL, Face.TRIANGLE), bias(0, 0, Bias.XYNN, Bias.XYNN, 0, 0));

    /** Slope types, as the original {@code Slope.Type}. */
    public enum Type {
        WEDGE_SIDE,
        WEDGE,
        WEDGE_INT,
        WEDGE_EXT,
        OBLIQUE_INT,
        OBLIQUE_EXT,
        PRISM,
        PRISM_1P,
        PRISM_2P,
        PRISM_3P,
        PRISM_4P,
        PRISM_WEDGE
    }

    /** Shape of a block face. */
    public enum Face {
        NONE,
        FULL,
        WEDGE,
        TRIANGLE
    }

    /** The primary corner of a wedge-shaped face, stored as [X/Y], [Z/Y] or [X/Z]; 0 for other faces. */
    public static final class Bias {
        public static final int XYNN = 1;
        public static final int XYNP = 2;
        public static final int XYPN = 3;
        public static final int XYPP = 4;

        private Bias() {}
    }

    private static final Slope[] VALUES = values();

    public final Type type;
    /** Up or down, plus the horizontal directions the slope faces. */
    public final Set<Direction> facings;
    /** Faces in {@link Direction#get3DDataValue()} order. */
    private final Face[] faces;
    private final int[] faceBias;
    /** Whether the slope faces up. */
    public final boolean isPositive;

    Slope(Type type, Set<Direction> facings, Face[] faces, int[] faceBias) {
        this.type = type;
        this.facings = Collections.unmodifiableSet(facings);
        this.faces = faces;
        this.faceBias = faceBias;
        this.isPositive = facings.contains(UP);
    }

    private static Set<Direction> dirs(Direction... directions) {
        EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
        Collections.addAll(set, directions);
        return set;
    }

    private static Face[] faces(Face... faces) {
        return faces;
    }

    private static int[] bias(int... bias) {
        return bias;
    }

    /** The slope with an original slope ID; out-of-range IDs give the first slope, as the original reset them. */
    public static Slope byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : VALUES[0];
    }

    public int id() {
        return ordinal();
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The type, with every pointed prism counted as a prism. Used where slopes share common attributes. */
    public Type getPrimaryType() {
        return switch (type) {
            case PRISM, PRISM_1P, PRISM_2P, PRISM_3P, PRISM_4P -> Type.PRISM;
            default -> type;
        };
    }

    public Face getFace(Direction side) {
        return faces[side.get3DDataValue()];
    }

    public boolean isFaceFull(Direction side) {
        return getFace(side) == Face.FULL;
    }

    public boolean hasSide(Direction side) {
        return getFace(side) != Face.NONE;
    }

    public int getFaceBias(Direction side) {
        return faceBias[side.get3DDataValue()];
    }

    /** Whether this and {@code adjacent} (the slope on {@code side}) have matching faces between them. */
    public boolean sharesFaceWith(Slope adjacent, Direction side) {
        Direction opposite = side.getOpposite();
        Face face = getFace(side);
        if (face != adjacent.getFace(opposite)) {
            return false;
        }
        return face != Face.WEDGE || getFaceBias(side) == adjacent.getFaceBias(opposite);
    }

    /** The next slope type for the hammer's right-click, as the original {@code SlopeType.getNextSlopeType}. */
    public Slope nextType() {
        return switch (type) {
            case WEDGE_SIDE -> byId(id() + 8);
            case WEDGE, WEDGE_INT, WEDGE_EXT, OBLIQUE_INT -> byId(isPositive ? id() - 4 : id() + 12);
            case OBLIQUE_EXT -> isPositive ? byId(id() - 4) : PRISM_POS;
            case PRISM -> isPositive ? PRISM_NEG : PRISM_1P_POS_N;
            case PRISM_1P -> PRISM_2P_POS_NS;
            case PRISM_2P -> this == PRISM_2P_POS_NS || this == PRISM_2P_POS_WE ? PRISM_2P_POS_SE : PRISM_3P_POS_NWE;
            case PRISM_3P -> PRISM_4P_POS;
            case PRISM_4P -> PRISM_WEDGE_POS_N;
            case PRISM_WEDGE -> WEDGE_SE;
        };
    }

    /** The same slope turned around the vertical axis; the hammer's left-click turns clockwise. */
    public Slope rotate(Rotation rotation) {
        return withFacings(rotation::rotate);
    }

    public Slope mirror(Mirror mirror) {
        return withFacings(mirror::mirror);
    }

    private Slope withFacings(java.util.function.UnaryOperator<Direction> transform) {
        Set<Direction> moved = EnumSet.noneOf(Direction.class);
        for (Direction facing : facings) {
            moved.add(facing.getAxis().isVertical() ? facing : transform.apply(facing));
        }
        for (Slope slope : VALUES) {
            if (slope.type == type && slope.facings.equals(moved)) {
                return slope;
            }
        }
        throw new IllegalStateException("No slope of type " + type + " facing " + moved);
    }
}
