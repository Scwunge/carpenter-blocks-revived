package com.carpentersblocks.client.frame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Frame quads built in code, grouped by cull face. Positions are in block space (0 to 1). UVs are projected from
 * positions the way vanilla does for a full-block element, so neighbouring pieces line up and covers tile seamlessly.
 */
public final class FrameMesh {

    /**
     * A sloped quad: covers pick the sprite for it through {@code CoverQuads} rather than from the quad's direction.
     *
     * @param positive the slope faces up
     * @param side     the horizontal direction the slope faces, whose side texture covers show by default
     * @param wedge    the quad belongs to a plain wedge, where directional covers keep their side texture
     */
    public record Sloped(boolean positive, Direction side, boolean wedge) {}

    /**
     * A frame quad and, for sloped quads, how covers texture it. Fixed quads keep their own texture under covers;
     * translucent ones (panes, with {@code enableAlphaPanes}) draw in the translucent layer; emissive ones (the
     * safe's lit lights) draw at full brightness, unshaded.
     */
    public record Quad(BakedQuad quad, @Nullable Sloped sloped, boolean fixed, boolean translucent, boolean emissive) {
        public Quad(BakedQuad quad, @Nullable Sloped sloped, boolean fixed, boolean translucent) {
            this(quad, sloped, fixed, translucent, false);
        }

        public Quad(BakedQuad quad, @Nullable Sloped sloped, boolean fixed) {
            this(quad, sloped, fixed, false);
        }

        public Quad(BakedQuad quad, @Nullable Sloped sloped) {
            this(quad, sloped, false);
        }
    }

    private static final float EPSILON = 1.0E-5F;

    private final List<Quad> unculled = new ArrayList<>();
    private final Map<Direction, List<Quad>> culled = new EnumMap<>(Direction.class);
    private final float raise;
    private boolean hasFixed;
    private boolean hasTranslucent;

    public FrameMesh() {
        this(0);
    }

    private FrameMesh(float raise) {
        this.raise = raise;
    }

    /**
     * A mesh drawn one block above its own position, as the rail slope fill is: nothing is culled, faces on the bottom
     * (resting on the block itself) are dropped, and UVs come from the unraised positions.
     */
    public static FrameMesh raised() {
        return new FrameMesh(1);
    }

    /** The quads culled against {@code cullFace}, or the unculled ones for {@code null}. */
    public List<Quad> quads(@Nullable Direction cullFace) {
        return cullFace == null ? unculled : culled.getOrDefault(cullFace, Collections.emptyList());
    }

    /** All quads, unculled ones first. */
    public List<Quad> all() {
        List<Quad> all = new ArrayList<>(unculled);
        for (Direction direction : Direction.values()) {
            all.addAll(quads(direction));
        }
        return all;
    }

    /** Adds a ready-made, unculled quad that covers never re-texture, such as a lever handle or an inlay. */
    public FrameMesh fixed(BakedQuad quad) {
        unculled.add(new Quad(quad, null, true));
        hasFixed = true;
        return this;
    }

    /** Adds a fixed quad drawn in the translucent layer, such as a pane when panes are see-through. */
    public FrameMesh fixedTranslucent(BakedQuad quad) {
        unculled.add(new Quad(quad, null, true, true));
        hasTranslucent = true;
        return this;
    }

    /** Adds a fixed quad that glows: full brightness, no shading, as the original's lit safe lights. */
    public FrameMesh fixedEmissive(BakedQuad quad) {
        unculled.add(new Quad(FixedQuads.emissive(quad), null, true, false, true));
        hasFixed = true;
        return this;
    }

    /** True when the mesh has fixed quads, which draw in the frame's layer whatever the covers are. */
    public boolean hasFixed() {
        return hasFixed;
    }

    /** True when the mesh has fixed quads in the translucent layer. */
    public boolean hasTranslucent() {
        return hasTranslucent;
    }

    /** A cuboid, coordinates 0 to 1. Faces on the block boundary are culled against it; zero-area faces are skipped. */
    public FrameMesh box(float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
            ResourceLocation sprite) {
        Vector3f[] c = new Vector3f[8];
        for (int i = 0; i < 8; ++i) {
            c[i] = new Vector3f((i & 1) == 0 ? minX : maxX, (i & 2) == 0 ? minY : maxY, (i & 4) == 0 ? minZ : maxZ);
        }
        // Corner index bits: 1 = max x, 2 = max y, 4 = max z. Each face is listed counter-clockwise from outside.
        if (maxX > minX && maxZ > minZ) {
            polygon(List.of(c[0], c[1], c[5], c[4]), sprite, null);
            polygon(List.of(c[2], c[6], c[7], c[3]), sprite, null);
        }
        if (maxX > minX && maxY > minY) {
            polygon(List.of(c[0], c[2], c[3], c[1]), sprite, null);
            polygon(List.of(c[4], c[5], c[7], c[6]), sprite, null);
        }
        if (maxY > minY && maxZ > minZ) {
            polygon(List.of(c[0], c[4], c[6], c[2]), sprite, null);
            polygon(List.of(c[1], c[3], c[7], c[5]), sprite, null);
        }
        return this;
    }

    /**
     * A flat convex polygon, vertices counter-clockwise seen from outside. It is split into quads (a triangle becomes
     * a quad with a repeated vertex). Axis-aligned polygons get that direction, and are culled when they lie on the
     * block boundary; sloped ones get {@code UP} or {@code DOWN} by their normal, or the side for vertical ones.
     */
    public FrameMesh polygon(List<Vector3f> vertices, ResourceLocation sprite, @Nullable Sloped sloped) {
        if (vertices.size() < 3) {
            return this;
        }
        Vector3f normal = normal(vertices);
        if (normal.lengthSquared() < EPSILON * EPSILON) {
            return this;
        }
        normal.normalize();
        Direction axis = axisDirection(normal);
        Direction face;
        Direction cullFace = null;
        if (axis != null) {
            face = axis;
            sloped = null;
            vertices = vanillaOrder(vertices, axis);
            if (onBoundary(vertices, axis)) {
                if (raise != 0 && axis == Direction.DOWN) {
                    return this;
                }
                cullFace = raise == 0 ? axis : null;
            }
        } else if (Math.abs(normal.y) > EPSILON) {
            face = normal.y > 0 ? Direction.UP : Direction.DOWN;
        } else {
            face = sloped != null ? sloped.side() : Direction.getNearest(normal.x, 0, normal.z);
        }
        Direction projection = axis != null ? axis : Direction.getNearest(normal.x, 0, normal.z);
        if (axis == null && Math.abs(normal.x) == Math.abs(normal.z)) {
            projection = normal.z < 0 ? Direction.NORTH : Direction.SOUTH;
        }
        if (axis == null && Math.abs(normal.x) < EPSILON && Math.abs(normal.z) < EPSILON) {
            projection = face;
        }

        TextureAtlasSprite atlasSprite = Minecraft.getInstance().getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(sprite);
        List<Quad> target = cullFace == null ? unculled : culled.computeIfAbsent(cullFace, d -> new ArrayList<>());
        for (int start = 1; start < vertices.size() - 1; start += 2) {
            Vector3f a = vertices.get(0);
            Vector3f b = vertices.get(start);
            Vector3f c = vertices.get(start + 1);
            Vector3f d = start + 2 < vertices.size() ? vertices.get(start + 2) : c;
            target.add(new Quad(bake(new Vector3f[] { a, b, c, d }, normal, face, projection, atlasSprite, raise),
                    sloped));
        }
        return this;
    }

    private static BakedQuad bake(Vector3f[] corners, Vector3f normal, Direction face, Direction projection,
            TextureAtlasSprite sprite, float raise) {
        int[] data = new int[32];
        int packedNormal = (((byte) Math.round(normal.x * 127)) & 0xFF)
                | ((((byte) Math.round(normal.y * 127)) & 0xFF) << 8)
                | ((((byte) Math.round(normal.z * 127)) & 0xFF) << 16);
        for (int i = 0; i < 4; ++i) {
            Vector3f p = corners[i];
            float[] uv = project(p, projection);
            int offset = i * 8;
            data[offset] = Float.floatToRawIntBits(p.x);
            data[offset + 1] = Float.floatToRawIntBits(p.y + raise);
            data[offset + 2] = Float.floatToRawIntBits(p.z);
            data[offset + 3] = -1;
            data[offset + 4] = Float.floatToRawIntBits(sprite.getU0() + (sprite.getU1() - sprite.getU0()) * uv[0]);
            data[offset + 5] = Float.floatToRawIntBits(sprite.getV0() + (sprite.getV1() - sprite.getV0()) * uv[1]);
            data[offset + 7] = packedNormal;
        }
        return new BakedQuad(data, -1, face, sprite, true);
    }

    /** UV (0 to 1) of a point projected onto a face, matching vanilla's automatic UVs for a full-block element. */
    static float[] project(Vector3f p, Direction face) {
        return switch (face) {
            case DOWN -> new float[] { p.x, 1 - p.z };
            case UP -> new float[] { p.x, p.z };
            case NORTH -> new float[] { 1 - p.x, 1 - p.y };
            case SOUTH -> new float[] { p.x, 1 - p.y };
            case WEST -> new float[] { p.z, 1 - p.y };
            case EAST -> new float[] { 1 - p.z, 1 - p.y };
        };
    }

    /**
     * Rotates an axis-aligned polygon to start at the corner vanilla's FaceBakery starts that face with. Smooth lighting
     * reads the corners of full faces by vertex index, so the order has to match.
     */
    private static List<Vector3f> vanillaOrder(List<Vector3f> vertices, Direction face) {
        int start = 0;
        float best = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < vertices.size(); ++i) {
            Vector3f v = vertices.get(i);
            float score = switch (face) {
                case DOWN -> -v.x + v.z;
                case UP -> -v.x - v.z;
                case NORTH -> v.x + v.y;
                case SOUTH -> -v.x + v.y;
                case WEST -> -v.z + v.y;
                case EAST -> v.z + v.y;
            };
            if (score > best + EPSILON) {
                best = score;
                start = i;
            }
        }
        List<Vector3f> ordered = new ArrayList<>(vertices.size());
        for (int i = 0; i < vertices.size(); ++i) {
            ordered.add(vertices.get((start + i) % vertices.size()));
        }
        return ordered;
    }

    /** Newell's method: robust for any planar polygon, length proportional to its area. */
    private static Vector3f normal(List<Vector3f> vertices) {
        Vector3f n = new Vector3f();
        for (int i = 0; i < vertices.size(); ++i) {
            Vector3f a = vertices.get(i);
            Vector3f b = vertices.get((i + 1) % vertices.size());
            n.x += (a.y - b.y) * (a.z + b.z);
            n.y += (a.z - b.z) * (a.x + b.x);
            n.z += (a.x - b.x) * (a.y + b.y);
        }
        return n;
    }

    @Nullable
    private static Direction axisDirection(Vector3f n) {
        for (Direction direction : Direction.values()) {
            if (n.dot(direction.getStepX(), direction.getStepY(), direction.getStepZ()) > 1 - EPSILON) {
                return direction;
            }
        }
        return null;
    }

    private static boolean onBoundary(List<Vector3f> vertices, Direction face) {
        float bound = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0;
        for (Vector3f v : vertices) {
            float coordinate = switch (face.getAxis()) {
                case X -> v.x;
                case Y -> v.y;
                case Z -> v.z;
            };
            if (Math.abs(coordinate - bound) > EPSILON) {
                return false;
            }
        }
        return true;
    }
}
