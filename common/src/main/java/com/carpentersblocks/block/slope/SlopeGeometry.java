package com.carpentersblocks.block.slope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Direction;

/**
 * Geometry of every slope, without any game classes so it can be unit tested.
 * <p>
 * Every slope except the side wedges is a height field: an upward ("positive") slope fills {@code y <= h(x, z)}, a
 * downward one fills {@code y >= 1 - h(x, z)}. {@code h} is piecewise linear over the eight triangles that the lines
 * {@code x = 1/2}, {@code z = 1/2}, {@code x = z} and {@code x = 1 - z} cut the block into, so meshing it per
 * triangle is exact. Side wedges are a right-triangle prism standing on one of the diagonals.
 */
public final class SlopeGeometry {

    /** A flat convex polygon, vertices {x, y, z} counter-clockwise seen from outside. */
    public record Polygon(List<double[]> vertices, double[] normal) {

        public boolean isAxisAligned() {
            int nonZero = 0;
            for (double component : normal) {
                if (Math.abs(component) > EPSILON) {
                    ++nonZero;
                }
            }
            return nonZero == 1;
        }
    }

    private static final double EPSILON = 1.0E-9;

    /** Boundary points of the block's top view, counter-clockwise seen from above (x east, z south). */
    private static final double[][] RING = { { 0, 0 }, { 0, 0.5 }, { 0, 1 }, { 0.5, 1 }, { 1, 1 }, { 1, 0.5 },
            { 1, 0 }, { 0.5, 0 } };
    private static final double[] CENTER = { 0.5, 0.5 };

    private SlopeGeometry() {}

    // Height field

    /** Height of a slope at a point of the top view; for side wedges 1 inside the triangle and 0 outside. */
    public static double height(Slope slope, double x, double z) {
        return switch (slope.type) {
            case WEDGE_SIDE -> sideWedgeContains(slope, x, z) ? 1 : 0;
            case WEDGE -> {
                double[] p = toNorth(horizontal(slope), x, z);
                yield p[1];
            }
            case PRISM_WEDGE -> {
                double[] p = toNorth(horizontal(slope), x, z);
                yield Math.max(p[1], Math.min(p[0], 1 - p[0]));
            }
            case WEDGE_INT, WEDGE_EXT, OBLIQUE_INT, OBLIQUE_EXT -> {
                double[] p = toNorthWest(slope, x, z);
                double u = p[0];
                double v = p[1];
                yield switch (slope.type) {
                    case WEDGE_INT -> Math.max(u, v);
                    case WEDGE_EXT -> Math.min(u, v);
                    case OBLIQUE_INT -> Math.min(1, u + v);
                    default -> Math.max(0, u + v - 1);
                };
            }
            default -> prismHeight(slope, x, z);
        };
    }

    /** Pyramid of half a block's height, raised into a ridge towards each point. */
    private static double prismHeight(Slope slope, double x, double z) {
        double toNorth = z;
        double toSouth = 1 - z;
        double toWest = x;
        double toEast = 1 - x;
        double nearest = Math.min(Math.min(toNorth, toSouth), Math.min(toWest, toEast));
        Direction region = nearest == toNorth ? Direction.NORTH
                : nearest == toSouth ? Direction.SOUTH : nearest == toWest ? Direction.WEST : Direction.EAST;
        if (!slope.facings.contains(region)) {
            return nearest;
        }
        return region.getAxis() == Direction.Axis.Z ? Math.min(toWest, toEast) : Math.min(toNorth, toSouth);
    }

    private static boolean sideWedgeContains(Slope slope, double x, double z) {
        return switch (slope) {
            case WEDGE_SE -> x + z < 1;
            case WEDGE_NW -> x + z > 1;
            case WEDGE_NE -> z > x;
            default -> x > z;
        };
    }

    /** The one horizontal facing of a wedge or prism wedge. */
    private static Direction horizontal(Slope slope) {
        for (Direction facing : slope.facings) {
            if (facing.getAxis().isHorizontal()) {
                return facing;
            }
        }
        throw new IllegalArgumentException(slope + " has no horizontal facing");
    }

    /** Coordinates in which a slope facing {@code facing} faces north (low edge at z = 0). */
    private static double[] toNorth(Direction facing, double x, double z) {
        return switch (facing) {
            case SOUTH -> new double[] { 1 - x, 1 - z };
            case WEST -> new double[] { 1 - z, x };
            case EAST -> new double[] { z, 1 - x };
            default -> new double[] { x, z };
        };
    }

    /** Coordinates in which a corner slope's low corner is at the north-west (x = z = 0). */
    private static double[] toNorthWest(Slope slope, double x, double z) {
        return new double[] { slope.facings.contains(Direction.EAST) ? 1 - x : x,
                slope.facings.contains(Direction.SOUTH) ? 1 - z : z };
    }

    /** Whether a point is inside the slope (points on the surface count as inside). */
    public static boolean contains(Slope slope, double x, double y, double z) {
        if (slope.type == Slope.Type.WEDGE_SIDE) {
            return height(slope, x, z) > 0;
        }
        double h = height(slope, x, z);
        return slope.isPositive ? y <= h + EPSILON : y >= 1 - h - EPSILON;
    }

    // Mesh

    /** The slope's surface as convex polygons, coplanar neighbors merged. */
    public static List<Polygon> polygons(Slope slope) {
        List<double[][]> triangles = new ArrayList<>();
        if (slope.type == Slope.Type.WEDGE_SIDE) {
            sideWedgeTriangles(slope, triangles);
        } else {
            heightFieldTriangles(slope, triangles);
        }
        return merge(triangles);
    }

    private static void heightFieldTriangles(Slope slope, List<double[][]> out) {
        boolean positive = slope.isPositive;
        for (int i = 0; i < RING.length; ++i) {
            double[] a = RING[i];
            double[] b = RING[(i + 1) % RING.length];
            double ha = height(slope, a[0], a[1]);
            double hb = height(slope, b[0], b[1]);
            double hc = height(slope, CENTER[0], CENTER[1]);
            if (ha < EPSILON && hb < EPSILON && hc < EPSILON) {
                continue;
            }
            // Sloped (or top) surface, base, and the side on the block edge a-b
            addOriented(out, positive, new double[][] { point(CENTER, hc), point(a, ha), point(b, hb) }, 0, 1, 0);
            addOriented(out, positive, new double[][] { point(CENTER, 0), point(a, 0), point(b, 0) }, 0, -1, 0);
            double[] outward = { (a[0] + b[0]) / 2 - 0.5, 0, (a[1] + b[1]) / 2 - 0.5 };
            if (ha > EPSILON || hb > EPSILON) {
                if (ha > EPSILON) {
                    addOriented(out, positive, new double[][] { point(a, 0), point(b, 0), point(a, ha) },
                            outward[0], 0, outward[2]);
                }
                if (hb > EPSILON) {
                    addOriented(out, positive, new double[][] { point(b, 0), point(b, hb), point(a, ha) },
                            outward[0], 0, outward[2]);
                }
            }
        }
    }

    private static void sideWedgeTriangles(Slope slope, List<double[][]> out) {
        // The triangle's corners in the top view: the right angle, then the two ends of the hypotenuse
        double[][] corners = switch (slope) {
            case WEDGE_SE -> new double[][] { { 0, 0 }, { 1, 0 }, { 0, 1 } };
            case WEDGE_NW -> new double[][] { { 1, 1 }, { 1, 0 }, { 0, 1 } };
            case WEDGE_NE -> new double[][] { { 0, 1 }, { 0, 0 }, { 1, 1 } };
            default -> new double[][] { { 1, 0 }, { 0, 0 }, { 1, 1 } };
        };
        double[] centroid = { (corners[0][0] + corners[1][0] + corners[2][0]) / 3, 0.5,
                (corners[0][1] + corners[1][1] + corners[2][1]) / 3 };
        addOriented(out, true, new double[][] { point(corners[0], 1), point(corners[1], 1), point(corners[2], 1) },
                0, 1, 0);
        addOriented(out, true, new double[][] { point(corners[0], 0), point(corners[1], 0), point(corners[2], 0) },
                0, -1, 0);
        for (int i = 0; i < 3; ++i) {
            double[] a = corners[i];
            double[] b = corners[(i + 1) % 3];
            double[] mid = { (a[0] + b[0]) / 2 - centroid[0], 0, (a[1] + b[1]) / 2 - centroid[2] };
            addOriented(out, true, new double[][] { point(a, 0), point(b, 0), point(b, 1) }, mid[0], 0, mid[2]);
            addOriented(out, true, new double[][] { point(a, 0), point(b, 1), point(a, 1) }, mid[0], 0, mid[2]);
        }
    }

    private static double[] point(double[] xz, double y) {
        return new double[] { xz[0], y, xz[1] };
    }

    /**
     * Adds a triangle wound so its normal points along {@code (dx, dy, dz)}; downward slopes are mirrored top to
     * bottom first. Degenerate triangles are dropped.
     */
    private static void addOriented(List<double[][]> out, boolean positive, double[][] triangle, double dx, double dy,
            double dz) {
        if (!positive) {
            for (int i = 0; i < 3; ++i) {
                triangle[i] = new double[] { triangle[i][0], 1 - triangle[i][1], triangle[i][2] };
            }
            dy = -dy;
        }
        double[] n = cross(sub(triangle[1], triangle[0]), sub(triangle[2], triangle[0]));
        if (dot(n, n) < EPSILON) {
            return;
        }
        if (n[0] * dx + n[1] * dy + n[2] * dz < 0) {
            double[] swap = triangle[1];
            triangle[1] = triangle[2];
            triangle[2] = swap;
        }
        out.add(triangle);
    }

    // Merging coplanar triangles into convex polygons

    private static List<Polygon> merge(List<double[][]> triangles) {
        Map<String, List<List<double[]>>> planes = new LinkedHashMap<>();
        Map<String, double[]> normals = new HashMap<>();
        for (double[][] triangle : triangles) {
            double[] n = normalize(cross(sub(triangle[1], triangle[0]), sub(triangle[2], triangle[0])));
            double d = dot(n, triangle[0]);
            String key = String.format("%.6f %.6f %.6f %.6f", n[0] + 0.0, n[1] + 0.0, n[2] + 0.0, d + 0.0);
            planes.computeIfAbsent(key, k -> new ArrayList<>()).add(new ArrayList<>(List.of(triangle)));
            normals.put(key, n);
        }

        List<Polygon> polygons = new ArrayList<>();
        planes.forEach((key, group) -> {
            double[] n = normals.get(key);
            List<List<double[]>> merged = mergeGroup(group, n);
            for (List<double[]> polygon : merged) {
                List<double[]> simple = dropCollinear(polygon);
                if (simple.size() >= 3) {
                    polygons.add(new Polygon(simple, n));
                }
            }
        });
        return polygons;
    }

    /** Merges a plane's polygons: all at once if their union is one convex polygon, otherwise pair by pair. */
    private static List<List<double[]>> mergeGroup(List<List<double[]>> group, double[] n) {
        List<double[]> all = union(group);
        if (all != null && isConvex(all, n)) {
            return List.of(all);
        }
        List<List<double[]>> polygons = new ArrayList<>(group);
        boolean changed = true;
        while (changed) {
            changed = false;
            search:
            for (int i = 0; i < polygons.size(); ++i) {
                for (int j = i + 1; j < polygons.size(); ++j) {
                    List<double[]> joined = union(List.of(polygons.get(i), polygons.get(j)));
                    if (joined != null && isConvex(joined, n)) {
                        polygons.set(i, joined);
                        polygons.remove(j);
                        changed = true;
                        break search;
                    }
                }
            }
        }
        return polygons;
    }

    /**
     * The outline of polygons that share edges, found by cancelling edges that appear in both directions; {@code null}
     * unless it is a single loop that actually joined something.
     */
    private static List<double[]> union(List<List<double[]>> polygons) {
        List<double[][]> edges = new ArrayList<>();
        for (List<double[]> polygon : polygons) {
            for (int i = 0; i < polygon.size(); ++i) {
                edges.add(new double[][] { polygon.get(i), polygon.get((i + 1) % polygon.size()) });
            }
        }
        int total = edges.size();
        List<double[][]> remaining = new ArrayList<>();
        for (double[][] edge : edges) {
            boolean cancelled = false;
            for (double[][] other : edges) {
                if (same(edge[0], other[1]) && same(edge[1], other[0])) {
                    cancelled = true;
                    break;
                }
            }
            if (!cancelled) {
                remaining.add(edge);
            }
        }
        if (polygons.size() > 1 && remaining.size() == total) {
            return null;
        }

        List<double[]> loop = new ArrayList<>();
        double[][] edge = remaining.get(0);
        while (true) {
            loop.add(edge[0]);
            double[][] next = null;
            for (double[][] candidate : remaining) {
                if (same(candidate[0], edge[1])) {
                    if (next != null) {
                        return null;
                    }
                    next = candidate;
                }
            }
            if (next == null) {
                return null;
            }
            if (next == remaining.get(0)) {
                break;
            }
            edge = next;
            if (loop.size() > remaining.size()) {
                return null;
            }
        }
        return loop.size() == remaining.size() ? loop : null;
    }

    private static boolean isConvex(List<double[]> polygon, double[] n) {
        int size = polygon.size();
        for (int i = 0; i < size; ++i) {
            double[] a = polygon.get(i);
            double[] b = polygon.get((i + 1) % size);
            double[] c = polygon.get((i + 2) % size);
            if (dot(cross(sub(b, a), sub(c, b)), n) < -EPSILON) {
                return false;
            }
        }
        return true;
    }

    private static List<double[]> dropCollinear(List<double[]> polygon) {
        List<double[]> result = new ArrayList<>(polygon);
        boolean changed = true;
        while (changed && result.size() > 3) {
            changed = false;
            for (int i = 0; i < result.size(); ++i) {
                double[] a = result.get((i + result.size() - 1) % result.size());
                double[] b = result.get(i);
                double[] c = result.get((i + 1) % result.size());
                double[] turn = cross(sub(b, a), sub(c, b));
                if (dot(turn, turn) < EPSILON) {
                    result.remove(i);
                    changed = true;
                    break;
                }
            }
        }
        return result;
    }

    // Boxes

    /**
     * Boxes {minX, minY, minZ, maxX, maxY, maxZ} on an {@code n} by {@code n} grid of columns. With {@code outer}, a
     * column reaches the highest point of the slope over it (collision, as the original's slices); otherwise the
     * lowest, so the boxes stay inside the slope (occlusion).
     */
    public static List<double[]> boxes(Slope slope, int n, boolean outer) {
        List<double[]> boxes = new ArrayList<>();
        for (int i = 0; i < n; ++i) {
            for (int k = 0; k < n; ++k) {
                double x0 = (double) i / n;
                double x1 = (double) (i + 1) / n;
                double z0 = (double) k / n;
                double z1 = (double) (k + 1) / n;
                double h = extreme(slope, x0, z0, x1, z1, outer);
                if (h < EPSILON) {
                    continue;
                }
                if (slope.type == Slope.Type.WEDGE_SIDE || slope.isPositive) {
                    boxes.add(new double[] { x0, 0, z0, x1, h, z1 });
                } else {
                    boxes.add(new double[] { x0, 1 - h, z0, x1, 1, z1 });
                }
            }
        }
        return boxes;
    }

    /**
     * Highest or lowest height over a cell. A piecewise-linear function takes its extremes at the corners of its
     * pieces, which are the cell's corners and where the crease lines cross the cell's edges or each other.
     */
    private static double extreme(Slope slope, double x0, double z0, double x1, double z1, boolean max) {
        if (slope.type == Slope.Type.WEDGE_SIDE) {
            // Inside an open triangle: some corner strictly inside (outer), or every corner inside or on it (inner)
            double[][] corners = { { x0, z0 }, { x1, z0 }, { x0, z1 }, { x1, z1 } };
            int inside = 0;
            for (double[] corner : corners) {
                if (sideWedgeCorner(slope, corner[0], corner[1])) {
                    ++inside;
                }
            }
            boolean outerHit = false;
            double cx = (x0 + x1) / 2;
            double cz = (z0 + z1) / 2;
            if (sideWedgeContains(slope, cx, cz)) {
                outerHit = true;
            }
            for (double[] corner : corners) {
                if (sideWedgeContains(slope, corner[0], corner[1])) {
                    outerHit = true;
                }
            }
            return max ? (outerHit ? 1 : 0) : (inside == 4 ? 1 : 0);
        }

        List<double[]> candidates = new ArrayList<>();
        candidates.add(new double[] { x0, z0 });
        candidates.add(new double[] { x1, z0 });
        candidates.add(new double[] { x0, z1 });
        candidates.add(new double[] { x1, z1 });
        candidates.add(new double[] { 0.5, 0.5 });
        for (double edge : new double[] { x0, x1 }) {
            candidates.add(new double[] { edge, 0.5 });
            candidates.add(new double[] { edge, edge });
            candidates.add(new double[] { edge, 1 - edge });
        }
        for (double edge : new double[] { z0, z1 }) {
            candidates.add(new double[] { 0.5, edge });
            candidates.add(new double[] { edge, edge });
            candidates.add(new double[] { 1 - edge, edge });
        }
        double result = max ? 0 : 1;
        for (double[] p : candidates) {
            if (p[0] < x0 - EPSILON || p[0] > x1 + EPSILON || p[1] < z0 - EPSILON || p[1] > z1 + EPSILON) {
                continue;
            }
            double h = height(slope, p[0], p[1]);
            result = max ? Math.max(result, h) : Math.min(result, h);
        }
        return result;
    }

    /** Side wedge membership including the hypotenuse. */
    private static boolean sideWedgeCorner(Slope slope, double x, double z) {
        return switch (slope) {
            case WEDGE_SE -> x + z <= 1 + EPSILON;
            case WEDGE_NW -> x + z >= 1 - EPSILON;
            case WEDGE_NE -> z >= x - EPSILON;
            default -> x >= z - EPSILON;
        };
    }

    // Vector helpers

    private static boolean same(double[] a, double[] b) {
        return Math.abs(a[0] - b[0]) < 1.0E-7 && Math.abs(a[1] - b[1]) < 1.0E-7 && Math.abs(a[2] - b[2]) < 1.0E-7;
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

    private static double[] normalize(double[] v) {
        double length = Math.sqrt(dot(v, v));
        return new double[] { v[0] / length, v[1] / length, v[2] / length };
    }
}
