package com.carpentersblocks.block.stairs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;

import org.junit.jupiter.api.Test;

class StairsShapeTest {

    /** Faces the original's {@code faceShape} marks FULL, by stairs ID, as [D, U, N, S, W, E]. */
    private static final String[] ORIGINAL_FULL_FACES = {
        "N W", "S E", "S W", "N E", // side
        "U S", "U N", "U E", "U W", // upside down
        "D S", "D N", "D E", "D W", // upright
        "U N W", "U S E", "U S W", "U N E", // interior upside down
        "D N W", "D S E", "D S W", "D N E", // interior upright
        "U", "U", "U", "U", // exterior upside down
        "D", "D", "D", "D", // exterior upright
    };

    private static final double EPSILON = 1e-9;

    @Test
    void ordinalsAreTheOriginalIds() {
        assertEquals(28, StairsShape.values().length);
        assertEquals(StairsShape.SIDE_SE, StairsShape.byId(0));
        assertEquals(StairsShape.POS_N, StairsShape.byId(8));
        assertEquals(StairsShape.INT_NEG_SE, StairsShape.byId(12));
        assertEquals(StairsShape.EXT_POS_SW, StairsShape.byId(27));
    }

    @Test
    void fullFacesMatchTheOriginal() {
        for (StairsShape stairs : StairsShape.values()) {
            Set<Direction> expected = EnumSet.noneOf(Direction.class);
            for (String face : ORIGINAL_FULL_FACES[stairs.id()].split(" ")) {
                expected.add(Direction.byName(switch (face) {
                    case "D" -> "down";
                    case "U" -> "up";
                    case "N" -> "north";
                    case "S" -> "south";
                    case "W" -> "west";
                    default -> "east";
                }));
            }
            Set<Direction> actual = EnumSet.noneOf(Direction.class);
            for (Direction face : Direction.values()) {
                if (coversFace(stairs, face)) {
                    actual.add(face);
                }
            }
            assertEquals(expected, actual, stairs.getSerializedName());
        }
    }

    /** Whether the boxes cover the whole face of the block space, checked on a grid of half-pixel samples. */
    private static boolean coversFace(StairsShape stairs, Direction face) {
        int axis = face.getAxis().ordinal();
        double plane = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 16 : 0;
        int[] others = axis == 0 ? new int[] { 1, 2 } : axis == 1 ? new int[] { 0, 2 } : new int[] { 0, 1 };
        for (double a = 0.25; a < 16; a += 0.5) {
            for (double b = 0.25; b < 16; b += 0.5) {
                boolean covered = false;
                for (double[] box : stairs.boxes()) {
                    covered |= (box[axis] == plane || box[axis + 3] == plane)
                            && box[others[0]] < a && a < box[others[0] + 3]
                            && box[others[1]] < b && b < box[others[1] + 3];
                }
                if (!covered) {
                    return false;
                }
            }
        }
        return true;
    }

    @Test
    void boxesDoNotOverlapAndFillTheRightVolume() {
        for (StairsShape stairs : StairsShape.values()) {
            List<double[]> boxes = stairs.boxes();
            double volume = 0;
            for (int i = 0; i < boxes.size(); ++i) {
                AABB a = aabb(boxes.get(i));
                volume += a.getXsize() * a.getYsize() * a.getZsize();
                for (int j = i + 1; j < boxes.size(); ++j) {
                    assertFalse(a.intersects(aabb(boxes.get(j))), stairs.getSerializedName() + " boxes overlap");
                }
            }
            double expected = switch (stairs.type()) {
                case SIDE, NORMAL -> 0.75;
                case INTERIOR -> 0.875;
                case EXTERIOR -> 0.625;
            };
            assertEquals(expected * 4096, volume, EPSILON, stairs.getSerializedName());
            assertEquals(volume / 4096, stairs.shape().toAabbs().stream()
                    .mapToDouble(box -> box.getXsize() * box.getYsize() * box.getZsize()).sum(), EPSILON);
        }
    }

    private static AABB aabb(double[] box) {
        return new AABB(box[0], box[1], box[2], box[3], box[4], box[5]);
    }

    @Test
    void hammerLeftClickTurnsThroughFourDirections() {
        for (StairsShape stairs : StairsShape.values()) {
            Set<StairsShape> seen = new HashSet<>();
            StairsShape next = stairs;
            for (int i = 0; i < 4; ++i) {
                assertEquals(stairs.type(), next.type());
                assertEquals(stairs.isPositive(), next.isPositive());
                seen.add(next);
                next = next.nextDirection();
            }
            assertEquals(stairs, next);
            assertEquals(4, seen.size());
        }
    }

    @Test
    void hammerRightClickVisitsEveryKindInTheOriginalOrder() {
        List<String> kinds = new ArrayList<>();
        StairsShape stairs = StairsShape.SIDE_SE;
        for (int i = 0; i < 7; ++i) {
            kinds.add(stairs.type() + (stairs.isPositive() ? "+" : "-"));
            stairs = stairs.nextType();
        }
        assertEquals(StairsShape.SIDE_SE, stairs);
        assertEquals(List.of("SIDE-", "NORMAL+", "NORMAL-", "INTERIOR+", "INTERIOR-", "EXTERIOR+", "EXTERIOR-"),
                kinds);
    }

    @Test
    void rotatingAndMirroringKeepsTheKind() {
        for (StairsShape stairs : StairsShape.values()) {
            StairsShape turned = stairs;
            for (int i = 0; i < 4; ++i) {
                turned = turned.rotate(Rotation.CLOCKWISE_90);
                assertEquals(stairs.type(), turned.type());
                assertEquals(stairs.isPositive(), turned.isPositive());
            }
            assertEquals(stairs, turned);
            assertEquals(stairs, stairs.mirror(Mirror.FRONT_BACK).mirror(Mirror.FRONT_BACK));
            assertEquals(stairs, stairs.rotate(Rotation.CLOCKWISE_180).rotate(Rotation.CLOCKWISE_180));
        }
        assertEquals(StairsShape.POS_E, StairsShape.POS_N.rotate(Rotation.CLOCKWISE_90));
        assertEquals(StairsShape.INT_NEG_NE, StairsShape.INT_NEG_NW.mirror(Mirror.FRONT_BACK));
    }

    @Test
    void placementFollowsTheOriginal() {
        // Top face: climbs the way the player looks
        assertEquals(StairsShape.POS_N, StairsShape.forPlacement(Direction.UP, 0.5, 1, 0.5, Direction.SOUTH));
        assertEquals(StairsShape.NEG_E, StairsShape.forPlacement(Direction.DOWN, 0.5, 0, 0.5, Direction.WEST));
        // Side face: middle of the upper half is upside down, lower half upright, edges stand on end
        assertEquals(StairsShape.NEG_S, StairsShape.forPlacement(Direction.SOUTH, 0.5, 0.9, 1, Direction.NORTH));
        assertEquals(StairsShape.POS_S, StairsShape.forPlacement(Direction.SOUTH, 0.5, 0.1, 1, Direction.NORTH));
        assertEquals(StairsShape.SIDE_SE, StairsShape.forPlacement(Direction.SOUTH, 0.1, 0.5, 1, Direction.NORTH));
        assertEquals(StairsShape.SIDE_SW, StairsShape.forPlacement(Direction.SOUTH, 0.9, 0.5, 1, Direction.NORTH));
        assertEquals(StairsShape.SIDE_NW, StairsShape.forPlacement(Direction.NORTH, 0.9, 0.5, 0, Direction.SOUTH));
        assertEquals(StairsShape.SIDE_NE, StairsShape.forPlacement(Direction.EAST, 1, 0.5, 0.9, Direction.WEST));
    }

    @Test
    void stairsMeetingAtRightAnglesFormCorners() {
        // An upright stair climbing south (open north) beside one climbing east (open west) forms a corner
        Map<Direction, StairsShape> around = new EnumMap<>(Direction.class);
        around.put(Direction.NORTH, StairsShape.POS_W);
        around.put(Direction.WEST, StairsShape.POS_N);
        assertEquals(StairsShape.INT_POS_NW, StairsTransform.transform(StairsShape.POS_N, around::get));

        // Alone or with no matching neighbors, stairs are left as placed
        assertEquals(StairsShape.POS_N, StairsTransform.transform(StairsShape.POS_N, dir -> null));

        // Stairs stacked on side stairs copy them
        assertEquals(StairsShape.SIDE_NE, StairsTransform.transform(StairsShape.POS_N,
                dir -> dir == Direction.DOWN ? StairsShape.SIDE_NE : null));
    }

    @Test
    void placedStairsTurnTheirNeighborsIntoCorners() {
        Map<Direction, StairsShape> changes = StairsTransform.transformAdjacent(StairsShape.POS_W,
                dir -> dir == Direction.NORTH ? StairsShape.POS_N : null);
        assertEquals(Map.of(Direction.NORTH, StairsShape.EXT_POS_NW), changes);

        // Neighbors the other way up are left alone
        assertTrue(StairsTransform.transformAdjacent(StairsShape.POS_W,
                dir -> dir == Direction.NORTH ? StairsShape.NEG_N : null).isEmpty());
    }

    @Test
    void generatedModelsMatchTheBoxes() throws Exception {
        String root = "/assets/carpentersblocks/";
        JsonObject blockstate = read(root + "blockstates/carpenters_stairs.json").getAsJsonObject("variants");
        assertEquals(28, blockstate.size());
        for (StairsShape stairs : StairsShape.values()) {
            JsonObject variant = blockstate.getAsJsonObject("shape=" + stairs.getSerializedName());
            assertNotNull(variant, stairs.getSerializedName());
            String model = variant.get("model").getAsString().replace("carpentersblocks:", "");
            JsonArray elements = read(root + "models/" + model + ".json").getAsJsonArray("elements");
            List<double[]> boxes = stairs.boxes();
            List<double[]> actual = new ArrayList<>();
            for (JsonElement element : elements) {
                double[] box = new double[6];
                for (int k = 0; k < 3; ++k) {
                    box[k] = element.getAsJsonObject().getAsJsonArray("from").get(k).getAsDouble();
                    box[k + 3] = element.getAsJsonObject().getAsJsonArray("to").get(k).getAsDouble();
                }
                actual.add(box);
            }
            // Each box is an element; the rest only carry the visible part of a face partly buried in another box
            for (double[] box : boxes) {
                assertTrue(actual.stream().anyMatch(e -> same(e, box)), model + " has no element " + Arrays.toString(box));
            }
            for (double[] e : actual) {
                assertTrue(boxes.stream().anyMatch(box -> inside(e, box)), model + " element " + Arrays.toString(e)
                        + " lies outside the boxes");
            }
        }
        assertNull(blockstate.get("shape=nonexistent"));
        assertNotEquals(0, read(root + "models/item/carpenters_stairs.json").size());
    }

    private static boolean same(double[] a, double[] b) {
        for (int k = 0; k < 6; ++k) {
            if (Math.abs(a[k] - b[k]) > EPSILON) {
                return false;
            }
        }
        return true;
    }

    private static boolean inside(double[] a, double[] box) {
        for (int k = 0; k < 3; ++k) {
            if (a[k] < box[k] - EPSILON || a[k + 3] > box[k + 3] + EPSILON) {
                return false;
            }
        }
        return true;
    }

    private static JsonObject read(String path) throws Exception {
        try (InputStream in = StairsShapeTest.class.getResourceAsStream(path)) {
            assertNotNull(in, path);
            JsonElement json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            return json.getAsJsonObject();
        }
    }
}
