package com.carpentersblocks.block.slope;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import com.carpentersblocks.block.slope.Slope.Bias;

/**
 * Joins slopes up with their neighbors on placement. A literal port of the original {@code SlopeTransform}: the
 * world is replaced by a lookup of the Carpenter's Slope at an offset from the block being placed ({@code null}
 * where there is none), and changes to neighbors are returned instead of written.
 */
public final class SlopeTransform {

    private SlopeTransform() {}

    private static Function<BlockPos, Slope> offset(Function<BlockPos, Slope> at, BlockPos by) {
        return pos -> at.apply(pos.offset(by));
    }


    /**
     * Transforms wedge slope to connect with adjacent wedge slopes.
     */
    public static Slope transformWedge(Slope placed, Function<BlockPos, Slope> at) {
        Slope slope_XN = at.apply(new BlockPos(-1, 0, 0));
        Slope slope_XP = at.apply(new BlockPos(1, 0, 0));
        Slope slope_YN = at.apply(new BlockPos(0, -1, 0));
        Slope slope_YP = at.apply(new BlockPos(0, 1, 0));
        Slope slope_ZN = at.apply(new BlockPos(0, 0, -1));
        Slope slope_ZP = at.apply(new BlockPos(0, 0, 1));

        /* Transform into horizontal wedge. */

        int face_bias = slope_YP != null && slope_YP.getFace(Direction.DOWN).equals(Slope.Face.WEDGE)
                ? slope_YP.getFaceBias(Direction.DOWN)
                : slope_YN != null && slope_YN.getFace(Direction.UP).equals(Slope.Face.WEDGE)
                        ? slope_YN.getFaceBias(Direction.UP)
                        : 0;

        switch (face_bias) {
            case Bias.XYNN:
                return Slope.WEDGE_SE;
            case Bias.XYNP:
                return Slope.WEDGE_NE;
            case Bias.XYPN:
                return Slope.WEDGE_SW;
            case Bias.XYPP:
                return Slope.WEDGE_NW;
        }

        /* Transform into corner wedge. */

        Slope slope = placed;
        Slope.Type primary_type = slope.getPrimaryType();

        if (primary_type.equals(Slope.Type.WEDGE)) {

            if (slope_ZN != null) {

                if (slope_XN != null) {
                    if (slope_ZN.facings.contains(Direction.WEST)
                            && slope_XN.facings.contains(Direction.NORTH)) {
                        return slope_XN.isPositive && slope_ZN.isPositive ? Slope.WEDGE_INT_POS_NW
                                : Slope.WEDGE_INT_NEG_NW;
                    }
                    if (slope_ZN.facings.contains(Direction.EAST)
                            && slope_XN.facings.contains(Direction.SOUTH)) {
                        return slope_XN.isPositive && slope_ZN.isPositive ? Slope.WEDGE_EXT_POS_SE
                                : Slope.WEDGE_EXT_NEG_SE;
                    }
                }

                if (slope_XP != null) {
                    if (slope_ZN.facings.contains(Direction.EAST)
                            && slope_XP.facings.contains(Direction.NORTH)) {
                        return slope_XP.isPositive && slope_ZN.isPositive ? Slope.WEDGE_INT_POS_NE
                                : Slope.WEDGE_INT_NEG_NE;
                    }
                    if (slope_ZN.facings.contains(Direction.WEST)
                            && slope_XP.facings.contains(Direction.SOUTH)) {
                        return slope_XP.isPositive && slope_ZN.isPositive ? Slope.WEDGE_EXT_POS_SW
                                : Slope.WEDGE_EXT_NEG_SW;
                    }
                }
            }

            if (slope_ZP != null) {

                if (slope_XN != null) {
                    if (slope_ZP.facings.contains(Direction.WEST)
                            && slope_XN.facings.contains(Direction.SOUTH)) {
                        return slope_XN.isPositive && slope_ZP.isPositive ? Slope.WEDGE_INT_POS_SW
                                : Slope.WEDGE_INT_NEG_SW;
                    }
                    if (slope_ZP.facings.contains(Direction.EAST)
                            && slope_XN.facings.contains(Direction.NORTH)) {
                        return slope_XN.isPositive && slope_ZP.isPositive ? Slope.WEDGE_EXT_POS_NE
                                : Slope.WEDGE_EXT_NEG_NE;
                    }
                }

                if (slope_XP != null) {
                    if (slope_ZP.facings.contains(Direction.EAST)
                            && slope_XP.facings.contains(Direction.SOUTH)) {
                        return slope_XP.isPositive && slope_ZP.isPositive ? Slope.WEDGE_INT_POS_SE
                                : Slope.WEDGE_INT_NEG_SE;
                    }
                    if (slope_ZP.facings.contains(Direction.WEST)
                            && slope_XP.facings.contains(Direction.NORTH)) {
                        return slope_XP.isPositive && slope_ZP.isPositive ? Slope.WEDGE_EXT_POS_NW
                                : Slope.WEDGE_EXT_NEG_NW;
                    }
                }
            }

            if (slope_XN != null) {

                if (slope.facings.contains(Direction.WEST)) {
                    if (slope_XN.facings.contains(Direction.SOUTH)
                            && !slope_XN.facings.contains(Direction.EAST)) {
                        return slope_XN.isPositive ? Slope.WEDGE_INT_POS_SW : Slope.WEDGE_INT_NEG_SW;
                    }
                    if (slope_XN.facings.contains(Direction.NORTH)
                            && !slope_XN.facings.contains(Direction.EAST)) {
                        return slope_XN.isPositive ? Slope.WEDGE_INT_POS_NW : Slope.WEDGE_INT_NEG_NW;
                    }
                }

                if (slope.facings.contains(Direction.EAST)) {
                    if (slope_XN.facings.contains(Direction.SOUTH)
                            && !slope_XN.facings.contains(Direction.EAST)) {
                        return slope_XN.isPositive ? Slope.WEDGE_EXT_POS_SE : Slope.WEDGE_EXT_NEG_SE;
                    }
                    if (slope_XN.facings.contains(Direction.NORTH)
                            && !slope_XN.facings.contains(Direction.EAST)) {
                        return slope_XN.isPositive ? Slope.WEDGE_EXT_POS_NE : Slope.WEDGE_EXT_NEG_NE;
                    }
                }
            }

            if (slope_XP != null) {

                if (slope.facings.contains(Direction.WEST)) {
                    if (slope_XP.facings.contains(Direction.SOUTH)
                            && !slope_XP.facings.contains(Direction.WEST)) {
                        return slope_XP.isPositive ? Slope.WEDGE_EXT_POS_SW : Slope.WEDGE_EXT_NEG_SW;
                    }
                    if (slope_XP.facings.contains(Direction.NORTH)
                            && !slope_XP.facings.contains(Direction.WEST)) {
                        return slope_XP.isPositive ? Slope.WEDGE_EXT_POS_NW : Slope.WEDGE_EXT_NEG_NW;
                    }
                }

                if (slope.facings.contains(Direction.EAST)) {
                    if (slope_XP.facings.contains(Direction.SOUTH)
                            && !slope_XP.facings.contains(Direction.WEST)) {
                        return slope_XP.isPositive ? Slope.WEDGE_INT_POS_SE : Slope.WEDGE_INT_NEG_SE;
                    }
                    if (slope_XP.facings.contains(Direction.NORTH)
                            && !slope_XP.facings.contains(Direction.WEST)) {
                        return slope_XP.isPositive ? Slope.WEDGE_INT_POS_NE : Slope.WEDGE_INT_NEG_NE;
                    }
                }
            }

            if (slope_ZN != null) {

                if (slope.facings.contains(Direction.NORTH)) {
                    if (slope_ZN.facings.contains(Direction.EAST)
                            && !slope_ZN.facings.contains(Direction.SOUTH)) {
                        return slope_ZN.isPositive ? Slope.WEDGE_INT_POS_NE : Slope.WEDGE_INT_NEG_NE;
                    }
                    if (slope_ZN.facings.contains(Direction.WEST)
                            && !slope_ZN.facings.contains(Direction.SOUTH)) {
                        return slope_ZN.isPositive ? Slope.WEDGE_INT_POS_NW : Slope.WEDGE_INT_NEG_NW;
                    }
                }

                if (slope.facings.contains(Direction.SOUTH)) {
                    if (slope_ZN.facings.contains(Direction.EAST)
                            && !slope_ZN.facings.contains(Direction.SOUTH)) {
                        return slope_ZN.isPositive ? Slope.WEDGE_EXT_POS_SE : Slope.WEDGE_EXT_NEG_SE;
                    }
                    if (slope_ZN.facings.contains(Direction.WEST)
                            && !slope_ZN.facings.contains(Direction.SOUTH)) {
                        return slope_ZN.isPositive ? Slope.WEDGE_EXT_POS_SW : Slope.WEDGE_EXT_NEG_SW;
                    }
                }
            }

            if (slope_ZP != null) {

                if (slope.facings.contains(Direction.NORTH)) {
                    if (slope_ZP.facings.contains(Direction.EAST)
                            && !slope_ZP.facings.contains(Direction.NORTH)) {
                        return slope_ZP.isPositive ? Slope.WEDGE_EXT_POS_NE : Slope.WEDGE_EXT_NEG_NE;
                    }
                    if (slope_ZP.facings.contains(Direction.WEST)
                            && !slope_ZP.facings.contains(Direction.NORTH)) {
                        return slope_ZP.isPositive ? Slope.WEDGE_EXT_POS_NW : Slope.WEDGE_EXT_NEG_NW;
                    }
                }

                if (slope.facings.contains(Direction.SOUTH)) {
                    if (slope_ZP.facings.contains(Direction.EAST)
                            && !slope_ZP.facings.contains(Direction.NORTH)) {
                        return slope_ZP.isPositive ? Slope.WEDGE_INT_POS_SE : Slope.WEDGE_INT_NEG_SE;
                    }
                    if (slope_ZP.facings.contains(Direction.WEST)
                            && !slope_ZP.facings.contains(Direction.NORTH)) {
                        return slope_ZP.isPositive ? Slope.WEDGE_INT_POS_SW : Slope.WEDGE_INT_NEG_SW;
                    }
                }
            }
        }

        return placed;
    }

    /**
     * Transforms adjacent wedge slopes to connect to source wedge slopeID.
     */
    public static Map<Direction, Slope> transformAdjacentWedges(Slope placed, Function<BlockPos, Slope> at) {
        Map<Direction, Slope> changes = new EnumMap<>(Direction.class);
        Slope slope_XN = at.apply(new BlockPos(-1, 0, 0));
        Slope slope_XP = at.apply(new BlockPos(1, 0, 0));
        Slope slope_ZN = at.apply(new BlockPos(0, 0, -1));
        Slope slope_ZP = at.apply(new BlockPos(0, 0, 1));

        Slope slope = placed;
        Slope.Type primary_type = slope.getPrimaryType();

        if (primary_type.equals(Slope.Type.WEDGE)) {

            if (slope.facings.contains(Direction.WEST)) {

                if (slope_ZN != null && primary_type.equals(slope_ZN.getPrimaryType())
                        && slope.isPositive == slope_ZN.isPositive) {
                    if (slope_ZN.facings.contains(Direction.NORTH)) {
                        changes.put(Direction.NORTH, slope.isPositive ? Slope.WEDGE_EXT_POS_NW : Slope.WEDGE_EXT_NEG_NW);
                    }
                    if (slope_ZN.facings.contains(Direction.SOUTH)) {
                        changes.put(Direction.NORTH, slope.isPositive ? Slope.WEDGE_INT_POS_SW : Slope.WEDGE_INT_NEG_SW);
                    }
                }

                if (slope_ZP != null && primary_type.equals(slope_ZP.getPrimaryType())
                        && slope.isPositive == slope_ZP.isPositive) {
                    if (slope_ZP.facings.contains(Direction.SOUTH)) {
                        changes.put(Direction.SOUTH, slope.isPositive ? Slope.WEDGE_EXT_POS_SW : Slope.WEDGE_EXT_NEG_SW);
                    }
                    if (slope_ZP.facings.contains(Direction.NORTH)) {
                        changes.put(Direction.SOUTH, slope.isPositive ? Slope.WEDGE_INT_POS_NW : Slope.WEDGE_INT_NEG_NW);
                    }
                }
            }

            if (slope.facings.contains(Direction.EAST)) {

                if (slope_ZN != null && primary_type.equals(slope_ZN.getPrimaryType())
                        && slope.isPositive == slope_ZN.isPositive) {
                    if (slope_ZN.facings.contains(Direction.NORTH)) {
                        changes.put(Direction.NORTH, slope.isPositive ? Slope.WEDGE_EXT_POS_NE : Slope.WEDGE_EXT_NEG_NE);
                    }
                    if (slope_ZN.facings.contains(Direction.SOUTH)) {
                        changes.put(Direction.NORTH, slope.isPositive ? Slope.WEDGE_INT_POS_SE : Slope.WEDGE_INT_NEG_SE);
                    }
                }

                if (slope_ZP != null && primary_type.equals(slope_ZP.getPrimaryType())
                        && slope.isPositive == slope_ZP.isPositive) {
                    if (slope_ZP.facings.contains(Direction.SOUTH)) {
                        changes.put(Direction.SOUTH, slope.isPositive ? Slope.WEDGE_EXT_POS_SE : Slope.WEDGE_EXT_NEG_SE);
                    }
                    if (slope_ZP.facings.contains(Direction.NORTH)) {
                        changes.put(Direction.SOUTH, slope.isPositive ? Slope.WEDGE_INT_POS_NE : Slope.WEDGE_INT_NEG_NE);
                    }
                }
            }

            if (slope.facings.contains(Direction.NORTH)) {

                if (slope_XN != null && primary_type.equals(slope_XN.getPrimaryType())
                        && slope.isPositive == slope_XN.isPositive) {
                    if (slope_XN.facings.contains(Direction.WEST)) {
                        changes.put(Direction.WEST, slope.isPositive ? Slope.WEDGE_EXT_POS_NW : Slope.WEDGE_EXT_NEG_NW);
                    }
                    if (slope_XN.facings.contains(Direction.EAST)) {
                        changes.put(Direction.WEST, slope.isPositive ? Slope.WEDGE_INT_POS_NE : Slope.WEDGE_INT_NEG_NE);
                    }
                }

                if (slope_XP != null && primary_type.equals(slope_XP.getPrimaryType())
                        && slope.isPositive == slope_XP.isPositive) {
                    if (slope_XP.facings.contains(Direction.EAST)) {
                        changes.put(Direction.EAST, slope.isPositive ? Slope.WEDGE_EXT_POS_NE : Slope.WEDGE_EXT_NEG_NE);
                    }
                    if (slope_XP.facings.contains(Direction.WEST)) {
                        changes.put(Direction.EAST, slope.isPositive ? Slope.WEDGE_INT_POS_NW : Slope.WEDGE_INT_NEG_NW);
                    }
                }
            }

            if (slope.facings.contains(Direction.SOUTH)) {

                if (slope_XN != null && primary_type.equals(slope_XN.getPrimaryType())
                        && slope.isPositive == slope_XN.isPositive) {
                    if (slope_XN.facings.contains(Direction.WEST)) {
                        changes.put(Direction.WEST, slope.isPositive ? Slope.WEDGE_EXT_POS_SW : Slope.WEDGE_EXT_NEG_SW);
                    }
                    if (slope_XN.facings.contains(Direction.EAST)) {
                        changes.put(Direction.WEST, slope.isPositive ? Slope.WEDGE_INT_POS_SE : Slope.WEDGE_INT_NEG_SE);
                    }
                }

                if (slope_XP != null && primary_type.equals(slope_XP.getPrimaryType())
                        && slope.isPositive == slope_XP.isPositive) {
                    if (slope_XP.facings.contains(Direction.EAST)) {
                        changes.put(Direction.EAST, slope.isPositive ? Slope.WEDGE_EXT_POS_SE : Slope.WEDGE_EXT_NEG_SE);
                    }
                    if (slope_XP.facings.contains(Direction.WEST)) {
                        changes.put(Direction.EAST, slope.isPositive ? Slope.WEDGE_INT_POS_SW : Slope.WEDGE_INT_NEG_SW);
                    }
                }
            }
        }

        return changes;
    }

    /**
     * Transforms adjacent prism slopes to connect to source prism slope.
     */
    public static Map<Direction, Slope> transformAdjacentPrisms(Function<BlockPos, Slope> at) {
        Map<Direction, Slope> changes = new EnumMap<>(Direction.class);
        Slope slope_XN = at.apply(new BlockPos(-1, 0, 0));
        Slope slope_XP = at.apply(new BlockPos(1, 0, 0));
        Slope slope_ZN = at.apply(new BlockPos(0, 0, -1));
        Slope slope_ZP = at.apply(new BlockPos(0, 0, 1));

        if (slope_XN != null && slope_XN.getPrimaryType().equals(Slope.Type.PRISM)) {
            changes.put(Direction.WEST, transformPrism(slope_XN, offset(at, new BlockPos(-1, 0, 0))));
        }
        if (slope_XP != null && slope_XP.getPrimaryType().equals(Slope.Type.PRISM)) {
            changes.put(Direction.EAST, transformPrism(slope_XP, offset(at, new BlockPos(1, 0, 0))));
        }
        if (slope_ZN != null && slope_ZN.getPrimaryType().equals(Slope.Type.PRISM)) {
            changes.put(Direction.NORTH, transformPrism(slope_ZN, offset(at, new BlockPos(0, 0, -1))));
        }
        if (slope_ZP != null && slope_ZP.getPrimaryType().equals(Slope.Type.PRISM)) {
            changes.put(Direction.SOUTH, transformPrism(slope_ZP, offset(at, new BlockPos(0, 0, 1))));
        }

        return changes;
    }

    /**
     * Transforms prism slope to connect with adjacent prism slopes.
     */
    public static Slope transformPrism(Slope placed, Function<BlockPos, Slope> at) {
        Slope slope_XN = at.apply(new BlockPos(-1, 0, 0));
        Slope slope_XP = at.apply(new BlockPos(1, 0, 0));
        Slope slope_ZN = at.apply(new BlockPos(0, 0, -1));
        Slope slope_ZP = at.apply(new BlockPos(0, 0, 1));

        boolean prism_XN = slope_XN != null
                && (slope_XN.getPrimaryType().equals(Slope.Type.PRISM) || slope_XN.getPrimaryType().equals(Slope.Type.PRISM_WEDGE)
                        && slope_XN.facings.contains(Direction.EAST));
        boolean prism_XP = slope_XP != null
                && (slope_XP.getPrimaryType().equals(Slope.Type.PRISM) || slope_XP.getPrimaryType().equals(Slope.Type.PRISM_WEDGE)
                        && slope_XP.facings.contains(Direction.WEST));
        boolean prism_ZN = slope_ZN != null
                && (slope_ZN.getPrimaryType().equals(Slope.Type.PRISM) || slope_ZN.getPrimaryType().equals(Slope.Type.PRISM_WEDGE)
                        && slope_ZN.facings.contains(Direction.SOUTH));
        boolean prism_ZP = slope_ZP != null
                && (slope_ZP.getPrimaryType().equals(Slope.Type.PRISM) || slope_ZP.getPrimaryType().equals(Slope.Type.PRISM_WEDGE)
                        && slope_ZP.facings.contains(Direction.NORTH));

        int prism_set = 0x0000;

        if (prism_XN) {
            prism_set |= 0x0100;
        }
        if (prism_XP) {
            prism_set |= 0x1000;
        }
        if (prism_ZN) {
            prism_set |= 0x0001;
        }
        if (prism_ZP) {
            prism_set |= 0x0010;
        }

        switch (prism_set) {
            case 0x0001:
                return Slope.PRISM_1P_POS_N;
            case 0x0010:
                return Slope.PRISM_1P_POS_S;
            case 0x0100:
                return Slope.PRISM_1P_POS_W;
            case 0x1000:
                return Slope.PRISM_1P_POS_E;
            case 0x0011:
                return Slope.PRISM_2P_POS_NS;
            case 0x0101:
                return Slope.PRISM_2P_POS_NW;
            case 0x0110:
                return Slope.PRISM_2P_POS_SW;
            case 0x1001:
                return Slope.PRISM_2P_POS_NE;
            case 0x1010:
                return Slope.PRISM_2P_POS_SE;
            case 0x1100:
                return Slope.PRISM_2P_POS_WE;
            case 0x0111:
                return Slope.PRISM_3P_POS_NSW;
            case 0x1011:
                return Slope.PRISM_3P_POS_NSE;
            case 0x1101:
                return Slope.PRISM_3P_POS_NWE;
            case 0x1110:
                return Slope.PRISM_3P_POS_SWE;
            case 0x1111:
                return Slope.PRISM_4P_POS;
        }

        return placed;
    }
}
