package com.carpentersblocks.block.slope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import org.junit.jupiter.api.Test;

class SlopeGeometryTest {

    /** The original's ROTATION_Y table for the hammer's left-click (rotation about UP), by slope ID. */
    private static final String[] CLOCKWISE = {
            "WEDGE_SW", "WEDGE_NE", "WEDGE_SE", "WEDGE_NW", "WEDGE_NEG_E", "WEDGE_NEG_W", "WEDGE_NEG_N",
            "WEDGE_NEG_S", "WEDGE_POS_E", "WEDGE_POS_W", "WEDGE_POS_N", "WEDGE_POS_S", "WEDGE_INT_NEG_SW",
            "WEDGE_INT_NEG_NE", "WEDGE_INT_NEG_SE", "WEDGE_INT_NEG_NW", "WEDGE_INT_POS_SW", "WEDGE_INT_POS_NE",
            "WEDGE_INT_POS_SE", "WEDGE_INT_POS_NW", "WEDGE_EXT_NEG_SW", "WEDGE_EXT_NEG_NE", "WEDGE_EXT_NEG_SE",
            "WEDGE_EXT_NEG_NW", "WEDGE_EXT_POS_SW", "WEDGE_EXT_POS_NE", "WEDGE_EXT_POS_SE", "WEDGE_EXT_POS_NW",
            "OBL_INT_NEG_SW", "OBL_INT_NEG_NE", "OBL_INT_NEG_SE", "OBL_INT_NEG_NW", "OBL_INT_POS_SW",
            "OBL_INT_POS_NE", "OBL_INT_POS_SE", "OBL_INT_POS_NW", "OBL_EXT_NEG_SW", "OBL_EXT_NEG_NE",
            "OBL_EXT_NEG_SE", "OBL_EXT_NEG_NW", "OBL_EXT_POS_SW", "OBL_EXT_POS_NE", "OBL_EXT_POS_SE",
            "OBL_EXT_POS_NW", "PRISM_NEG", "PRISM_POS", "PRISM_1P_POS_E", "PRISM_1P_POS_W", "PRISM_1P_POS_N",
            "PRISM_1P_POS_S", "PRISM_2P_POS_WE", "PRISM_2P_POS_NS", "PRISM_2P_POS_SW", "PRISM_2P_POS_NE",
            "PRISM_2P_POS_SE", "PRISM_2P_POS_NW", "PRISM_3P_POS_NSE", "PRISM_3P_POS_NSW", "PRISM_3P_POS_NWE",
            "PRISM_3P_POS_SWE", "PRISM_4P_POS", "PRISM_WEDGE_POS_E", "PRISM_WEDGE_POS_W", "PRISM_WEDGE_POS_N",
            "PRISM_WEDGE_POS_S"
    };

    private static final double EPSILON = 1.0E-6;

    @Test
    void sixtyFiveSlopesInOriginalOrder() {
        assertEquals(65, Slope.values().length);
        assertEquals(Slope.WEDGE_SE, Slope.byId(0));
        assertEquals(Slope.WEDGE_POS_N, Slope.byId(8));
        assertEquals(Slope.PRISM_NEG, Slope.byId(44));
        assertEquals(Slope.PRISM_4P_POS, Slope.byId(60));
        assertEquals(Slope.PRISM_WEDGE_POS_E, Slope.byId(64));
        assertEquals(Slope.WEDGE_SE, Slope.byId(99), "abnormal IDs reset to the first slope");
    }

    @Test
    void clockwiseRotationMatchesOriginalTable() {
        for (Slope slope : Slope.values()) {
            assertEquals(CLOCKWISE[slope.id()], slope.rotate(Rotation.CLOCKWISE_90).name(), slope.name());
        }
    }

    @Test
    void rotationsAndMirrorsRoundTrip() {
        for (Slope slope : Slope.values()) {
            Slope turned = slope;
            for (int i = 0; i < 4; ++i) {
                turned = turned.rotate(Rotation.CLOCKWISE_90);
            }
            assertEquals(slope, turned);
            assertEquals(slope, slope.rotate(Rotation.CLOCKWISE_90).rotate(Rotation.COUNTERCLOCKWISE_90));
            assertEquals(slope, slope.mirror(Mirror.FRONT_BACK).mirror(Mirror.FRONT_BACK));
            assertEquals(slope, slope.mirror(Mirror.LEFT_RIGHT).mirror(Mirror.LEFT_RIGHT));
        }
    }

    @Test
    void hammerTypeCycleFollowsOriginal() {
        assertEquals(Slope.WEDGE_POS_N, Slope.WEDGE_SE.nextType());
        assertEquals(Slope.WEDGE_NEG_N, Slope.WEDGE_POS_N.nextType());
        assertEquals(Slope.WEDGE_INT_POS_SE, Slope.WEDGE_NEG_N.nextType());
        assertEquals(Slope.PRISM_POS, Slope.OBL_EXT_NEG_SE.nextType());
        assertEquals(Slope.PRISM_NEG, Slope.PRISM_POS.nextType());
        assertEquals(Slope.PRISM_1P_POS_N, Slope.PRISM_NEG.nextType());
        assertEquals(Slope.PRISM_2P_POS_NS, Slope.PRISM_1P_POS_E.nextType());
        assertEquals(Slope.PRISM_2P_POS_SE, Slope.PRISM_2P_POS_NS.nextType());
        assertEquals(Slope.PRISM_3P_POS_NWE, Slope.PRISM_2P_POS_SE.nextType());
        assertEquals(Slope.PRISM_4P_POS, Slope.PRISM_3P_POS_NSE.nextType());
        assertEquals(Slope.PRISM_WEDGE_POS_N, Slope.PRISM_4P_POS.nextType());
        assertEquals(Slope.WEDGE_SE, Slope.PRISM_WEDGE_POS_W.nextType());

        // The cycle never gets stuck in a loop that skips the wedges
        for (Slope slope : Slope.values()) {
            Set<Slope> seen = EnumSet.noneOf(Slope.class);
            Slope next = slope;
            while (seen.add(next)) {
                next = next.nextType();
            }
            assertTrue(seen.contains(Slope.WEDGE_SE), "cycle from " + slope);
        }
    }

    /** The original's face table (NONE / FULL / WEDGE / TRIANGLE) agrees with the area each face has in the mesh. */
    @Test
    void faceTablesMatchGeometry() {
        for (Slope slope : Slope.values()) {
            List<SlopeGeometry.Polygon> polygons = SlopeGeometry.polygons(slope);
            for (Direction side : Direction.values()) {
                double area = 0;
                for (SlopeGeometry.Polygon polygon : polygons) {
                    if (onSide(polygon, side)) {
                        area += area(polygon);
                    }
                }
                double expected = switch (slope.getFace(side)) {
                    case NONE -> 0;
                    case FULL -> 1;
                    case WEDGE -> 0.5;
                    case TRIANGLE -> 0.25;
                };
                assertEquals(expected, area, EPSILON, slope + " " + side.getName() + " face");
            }
        }
    }

    @Test
    void meshesAreClosedConvexAndEncloseTheHeightField() {
        for (Slope slope : Slope.values()) {
            List<SlopeGeometry.Polygon> polygons = SlopeGeometry.polygons(slope);
            double[] normalSum = new double[3];
            double volume = 0;
            for (SlopeGeometry.Polygon polygon : polygons) {
                double area = area(polygon);
                assertTrue(area > EPSILON, slope + " has an empty polygon");
                for (double[] v : polygon.vertices()) {
                    for (double c : v) {
                        assertTrue(c > -EPSILON && c < 1 + EPSILON, slope + " vertex outside the block");
                    }
                    assertEquals(dot(polygon.normal(), polygon.vertices().get(0)), dot(polygon.normal(), v), EPSILON,
                            slope + " polygon is not flat");
                }
                for (int i = 0; i < 3; ++i) {
                    normalSum[i] += polygon.normal()[i] * area;
                }
                volume += dot(polygon.normal(), polygon.vertices().get(0)) * area / 3;

                // Just inside a face is inside the slope; just outside is not
                double[] c = centroid(polygon);
                double[] n = polygon.normal();
                double[] in = { c[0] - n[0] * 1e-3, c[1] - n[1] * 1e-3, c[2] - n[2] * 1e-3 };
                double[] out = { c[0] + n[0] * 1e-3, c[1] + n[1] * 1e-3, c[2] + n[2] * 1e-3 };
                assertTrue(SlopeGeometry.contains(slope, in[0], in[1], in[2]), slope + " inside of a face");
                if (insideBlock(out)) {
                    assertFalse(SlopeGeometry.contains(slope, out[0], out[1], out[2]), slope + " outside of a face");
                }
            }
            for (double component : normalSum) {
                assertEquals(0, component, EPSILON, slope + " mesh is not closed");
            }
            assertEquals(integratedVolume(slope), volume, 3e-3, slope + " volume");
        }
    }

    @Test
    void outerBoxesCoverTheSlopeAndInnerBoxesStayInside() {
        for (int n : new int[] { 1, 2, 3, 4, 8 }) {
            for (Slope slope : Slope.values()) {
                List<double[]> outer = SlopeGeometry.boxes(slope, n, true);
                List<double[]> inner = SlopeGeometry.boxes(slope, n, false);
                int steps = 24;
                for (int i = 0; i < steps; ++i) {
                    for (int j = 0; j < steps; ++j) {
                        for (int k = 0; k < steps; ++k) {
                            double x = (i + 0.5) / steps;
                            double y = (j + 0.5) / steps;
                            double z = (k + 0.5) / steps;
                            boolean contained = SlopeGeometry.contains(slope, x, y, z);
                            if (contained) {
                                assertTrue(inAny(outer, x, y, z), slope + " n=" + n + " outer misses a point");
                            }
                            if (inAny(inner, x, y, z)) {
                                assertTrue(contained, slope + " n=" + n + " inner box leaves the slope");
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void defaultSmoothnessGivesTwoStepWedge() {
        List<double[]> boxes = SlopeGeometry.boxes(Slope.WEDGE_POS_N, 2, true);
        assertEquals(4, boxes.size());
        for (double[] box : boxes) {
            assertEquals(box[2] < 0.5 ? 0.5 : 1.0, box[4], EPSILON);
        }
    }

    // Helpers

    private static boolean onSide(SlopeGeometry.Polygon polygon, Direction side) {
        double[] n = polygon.normal();
        if (Math.abs(n[0] - side.getStepX()) > EPSILON || Math.abs(n[1] - side.getStepY()) > EPSILON
                || Math.abs(n[2] - side.getStepZ()) > EPSILON) {
            return false;
        }
        double bound = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0;
        int axis = side.getAxis().ordinal();
        for (double[] v : polygon.vertices()) {
            if (Math.abs(v[axis] - bound) > EPSILON) {
                return false;
            }
        }
        return true;
    }

    private static double area(SlopeGeometry.Polygon polygon) {
        double[] sum = new double[3];
        List<double[]> v = polygon.vertices();
        for (int i = 1; i < v.size() - 1; ++i) {
            double[] c = cross(sub(v.get(i), v.get(0)), sub(v.get(i + 1), v.get(0)));
            for (int k = 0; k < 3; ++k) {
                sum[k] += c[k];
            }
        }
        return dot(sum, polygon.normal()) / 2;
    }

    private static double[] centroid(SlopeGeometry.Polygon polygon) {
        double[] c = new double[3];
        for (double[] v : polygon.vertices()) {
            for (int k = 0; k < 3; ++k) {
                c[k] += v[k] / polygon.vertices().size();
            }
        }
        return c;
    }

    private static double integratedVolume(Slope slope) {
        int steps = 400;
        double sum = 0;
        for (int i = 0; i < steps; ++i) {
            for (int k = 0; k < steps; ++k) {
                sum += SlopeGeometry.height(slope, (i + 0.5) / steps, (k + 0.5) / steps);
            }
        }
        return sum / steps / steps;
    }

    private static boolean insideBlock(double[] p) {
        return p[0] > 0 && p[0] < 1 && p[1] > 0 && p[1] < 1 && p[2] > 0 && p[2] < 1;
    }

    private static boolean inAny(List<double[]> boxes, double x, double y, double z) {
        for (double[] b : boxes) {
            if (x >= b[0] && x <= b[3] && y >= b[1] && y <= b[4] && z >= b[2] && z <= b[5]) {
                return true;
            }
        }
        return false;
    }

    private static double[] sub(double[] a, double[] b) {
        return new double[] { a[0] - b[0], a[1] - b[1], a[2] - b[2] };
    }

    private static double[] cross(double[] a, double[] b) {
        return new double[] { a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0] };
    }

    private static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }
}
