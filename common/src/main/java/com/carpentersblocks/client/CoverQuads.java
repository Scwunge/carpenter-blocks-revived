package com.carpentersblocks.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.coverable.ChiselDesigns;
import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.Overlay;
import com.carpentersblocks.block.slope.SlopedCoverFace;
import com.carpentersblocks.client.frame.FixedQuads;
import com.carpentersblocks.client.frame.FrameMesh;

/**
 * Builds the quads of a coverable block, shared by both loaders' model wrappers.
 * <p>
 * The frame model supplies the geometry. Each frame quad is re-textured with the sprites the cover block shows on
 * that face, so grass sides, logs and the like keep their per-face textures and tinting. On top of that come the
 * overlay (grass, snow, ...) and the chisel design of the face's slot, as in the original's
 * {@code renderMultiTexturedSide}.
 */
public final class CoverQuads {

    /** A quad to render, the face it is culled against, the chunk layer it belongs to, and whether it glows. */
    public record Part(BakedQuad quad, @Nullable Direction cullFace, RenderType layer, boolean emissive) {
        public Part(BakedQuad quad, @Nullable Direction cullFace, RenderType layer) {
            this(quad, cullFace, layer, false);
        }
    }

    /** Layer of the bare frame, whose texture has transparent gaps. */
    public static final RenderType FRAME_LAYER = RenderType.cutout();
    /** Overlays have transparent fringes. */
    public static final RenderType OVERLAY_LAYER = RenderType.cutoutMipped();
    /** Chisel designs are drawn see-through over the cover, as on the original's alpha pass. */
    public static final RenderType DESIGN_LAYER = RenderType.translucent();
    /** See-through panes ({@code enableAlphaPanes}), as on the original's alpha pass. */
    public static final RenderType PANE_LAYER = RenderType.translucent();

    /** Covers that show their top on upward slopes; see {@link SlopedCoverFace}. */
    public static final TagKey<Block> SLOPE_TOP_COVERS = SlopedCoverFace.SLOPE_TOP_COVERS;

    private static final long SEED = 42L;

    private CoverQuads() {}

    /**
     * Quads for one cull face ({@code null} for unculled quads).
     *
     * @param layerOf resolves the chunk layer a cover block renders in; this differs between loaders
     */
    public static List<Part> build(BakedModel frame, BlockState state, @Nullable Direction cullFace,
            @Nullable CoverData data, RandomSource random, Function<BlockState, RenderType> layerOf) {
        random.setSeed(SEED);
        List<FrameMesh.Quad> frameQuads = new ArrayList<>();
        for (BakedQuad quad : frame.getQuads(state, cullFace, random)) {
            frameQuads.add(new FrameMesh.Quad(quad, null));
        }
        return build(frameQuads, cullFace, data, random, layerOf);
    }

    /** Quads for one cull face of a code-built frame (see {@link FrameMesh}). */
    public static List<Part> build(List<FrameMesh.Quad> frameQuads, @Nullable Direction cullFace,
            @Nullable CoverData data, RandomSource random, Function<BlockState, RenderType> layerOf) {
        return build(frameQuads, cullFace, data, random, layerOf, true);
    }

    /** Quads of the rail slope fill: the base cover only, without side covers, as the original drew it. */
    public static List<Part> buildFill(List<FrameMesh.Quad> frameQuads, @Nullable CoverData data,
            RandomSource random, Function<BlockState, RenderType> layerOf) {
        return build(frameQuads, null, data, random, layerOf, false);
    }

    private static List<Part> build(List<FrameMesh.Quad> frameQuads, @Nullable Direction cullFace,
            @Nullable CoverData data, RandomSource random, Function<BlockState, RenderType> layerOf,
            boolean sideCovers) {
        if (data == null || data.isEmpty()) {
            List<Part> parts = new ArrayList<>(frameQuads.size());
            for (FrameMesh.Quad frameQuad : frameQuads) {
                parts.add(new Part(frameQuad.quad(), cullFace, frameQuad.translucent() ? PANE_LAYER : FRAME_LAYER,
                        frameQuad.emissive()));
            }
            return parts;
        }

        List<Part> parts = new ArrayList<>(frameQuads.size() * 2);
        for (FrameMesh.Quad meshQuad : frameQuads) {
            if (meshQuad.fixed()) {
                parts.add(new Part(meshQuad.quad(), cullFace, meshQuad.translucent() ? PANE_LAYER : FRAME_LAYER,
                        meshQuad.emissive()));
                continue;
            }
            BakedQuad frameQuad = meshQuad.quad();
            addSlot(parts, frameQuad, meshQuad.sloped(), cullFace, data, CoverData.BASE, random, layerOf);

            int side = frameQuad.getDirection().get3DDataValue();
            if (sideCovers && meshQuad.sloped() == null && data.hasCover(side)) {
                addSideCover(parts, frameQuad, cullFace, data, side, random, layerOf);
            }
        }

        return parts;
    }

    /** One frame quad drawn with the cover, overlay and chisel design of a slot, as the original's {@code renderSide}. */
    private static void addSlot(List<Part> parts, BakedQuad frameQuad, @Nullable FrameMesh.Sloped sloped,
            @Nullable Direction cullFace, CoverData data, int slot, RandomSource random,
            Function<BlockState, RenderType> layerOf) {
        Direction face = frameQuad.getDirection();
        BlockState cover = data.cover(slot);
        boolean dyed = data.dye(slot) != null;

        // Cover (or bare frame)
        if (cover == null) {
            parts.add(new Part(dyed ? retexture(frameQuad, frameQuad.getSprite(), CoverTint.dye(slot)) : frameQuad,
                    cullFace, FRAME_LAYER));
        } else {
            Direction spriteFace = sloped == null ? face : slopedFace(cover, sloped);
            addCover(parts, frameQuad, spriteFace, cullFace, cover, slot, dyed, random, layerOf.apply(cover));
        }

        // Overlay
        Overlay overlay = data.overlay(slot);
        ResourceLocation overlayTexture = overlay == null ? null : overlay.texture(face);
        if (overlayTexture != null) {
            parts.add(new Part(retexture(frameQuad, sprite(overlayTexture),
                    overlay.tinted() ? CoverTint.overlay(slot) : -1), cullFace, OVERLAY_LAYER));
        }

        // Chisel design
        String design = data.design(slot);
        if (design != null && cover != null) {
            parts.add(new Part(retexture(frameQuad, sprite(ChiselDesigns.texture(design)), -1), cullFace,
                    DESIGN_LAYER));
        }
    }

    /** How far a side cover stands out from its face, as the original's {@code getSideCoverRenderBounds}. */
    private static final float SIDE_COVER_DEPTH = 1.0F / 16.0F;
    /** Snow on top stands as deep as a vanilla snow layer. */
    private static final float SNOW_SIDE_COVER_DEPTH = 2.0F / 16.0F;

    /**
     * A side cover: a plate standing out from {@code frameQuad} by a pixel, drawn with the side's slot, as the
     * original's {@code renderSideBlocks}. The face under it keeps the base cover, so see-through side covers show
     * the base behind them. A plate on the block's edge reaches into the neighbor's space and is culled with the
     * face; its UVs come from that space, as the original drew it there.
     */
    private static void addSideCover(List<Part> parts, BakedQuad frameQuad, @Nullable Direction cullFace,
            CoverData data, int slot, RandomSource random, Function<BlockState, RenderType> layerOf) {
        Direction side = frameQuad.getDirection();
        BlockState cover = data.cover(slot);
        float depth = side == Direction.UP && (cover.is(Blocks.SNOW_BLOCK) || cover.is(Blocks.SNOW))
                ? SNOW_SIDE_COVER_DEPTH : SIDE_COVER_DEPTH;

        float[] min = { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE };
        float[] max = { -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE };
        int[] vertices = frameQuad.getVertices();
        int stride = vertices.length / 4;
        for (int vertex = 0; vertex < 4; ++vertex) {
            for (int axis = 0; axis < 3; ++axis) {
                float p = Float.intBitsToFloat(vertices[vertex * stride + axis]);
                min[axis] = Math.min(min[axis], p);
                max[axis] = Math.max(max[axis], p);
            }
        }
        int axis = side.getAxis().ordinal();
        if (side.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            max[axis] = min[axis] + depth;
        } else {
            min[axis] = max[axis] - depth;
        }
        Box plate = new Box(min[0] * 16, min[1] * 16, min[2] * 16, max[0] * 16, max[1] * 16, max[2] * 16);

        Vector3f uvShift = cullFace == null ? new Vector3f()
                : new Vector3f(side.getStepX(), side.getStepY(), side.getStepZ());
        for (Direction face : Direction.values()) {
            if (face == side.getOpposite()) {
                continue; // Against the face, hidden
            }
            Vector3f[] corners = FixedQuads.faceCorners(plate, face);
            float[][] uv = new float[4][];
            for (int i = 0; i < 4; ++i) {
                uv[i] = FrameMesh.project(new Vector3f(corners[i]).sub(uvShift), face);
            }
            BakedQuad quad = FixedQuads.bake(corners, uv, frameQuad.getSprite(), FixedQuads.WHITE);
            addSlot(parts, quad, null, cullFace, data, slot, random, layerOf);
        }
    }

    /**
     * The cover face whose sprite a sloped quad shows, as the original's {@code getUniqueIcon}: the side texture by
     * default; pillar ends when the pillar stands upright (bark when it lies down); the top for directional blocks
     * on anything but plain wedges; and the top of grass-like blocks on upward slopes.
     */
    public static Direction slopedFace(BlockState cover, FrameMesh.Sloped sloped) {
        return SlopedCoverFace.of(cover, sloped.positive(), sloped.side(), sloped.wedge());
    }

    private static void addCover(List<Part> parts, BakedQuad frameQuad, Direction face, @Nullable Direction cullFace,
            BlockState cover, int slot, boolean dyed, RandomSource random, RenderType layer) {
        BakedModel coverModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(cover);

        // Panes and bars have no full faces; show their main texture, as the original did
        List<BakedQuad> coverQuads = cover.getBlock() instanceof IronBarsBlock ? List.of()
                : faceQuads(coverModel, cover, face, random);

        if (coverQuads.isEmpty()) {
            parts.add(new Part(retexture(frameQuad, coverModel.getParticleIcon(), dyed ? CoverTint.dye(slot) : -1),
                    cullFace, layer));
            return;
        }
        for (BakedQuad coverQuad : coverQuads) {
            int tint = dyed ? CoverTint.dye(slot)
                    : coverQuad.isTinted() ? CoverTint.cover(slot, coverQuad.getTintIndex()) : -1;
            float[] bounds = faceBounds(coverQuad, face);
            if (bounds[0] <= EPSILON && bounds[1] <= EPSILON && bounds[2] >= 1 - EPSILON && bounds[3] >= 1 - EPSILON) {
                parts.add(new Part(retexture(frameQuad, coverQuad.getSprite(), tint), cullFace, layer));
            } else {
                addPartial(parts, frameQuad, coverQuad, face, bounds, tint, cullFace, layer);
            }
        }
    }

    private static final float EPSILON = 1.0E-4F;

    /** Where a cover quad lies on its face, as {@code {minU, minV, maxU, maxV}} of {@link FrameMesh#project}. */
    private static float[] faceBounds(BakedQuad quad, Direction face) {
        float[] bounds = { Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE };
        for (float[] p : faceCoords(quad, face)) {
            bounds[0] = Math.min(bounds[0], p[0]);
            bounds[1] = Math.min(bounds[1], p[1]);
            bounds[2] = Math.max(bounds[2], p[0]);
            bounds[3] = Math.max(bounds[3], p[1]);
        }
        return bounds;
    }

    private static float[][] faceCoords(BakedQuad quad, Direction face) {
        int[] vertices = quad.getVertices();
        int stride = vertices.length / 4;
        float[][] coords = new float[4][];
        for (int vertex = 0; vertex < 4; ++vertex) {
            int offset = vertex * stride;
            coords[vertex] = FrameMesh.project(new Vector3f(Float.intBitsToFloat(vertices[offset]),
                    Float.intBitsToFloat(vertices[offset + 1]), Float.intBitsToFloat(vertices[offset + 2])), face);
        }
        return coords;
    }

    /**
     * A cover quad that fills only part of its face (connected-texture models split faces into quarters, for one):
     * the frame quad is clipped to that part and takes the cover quad's own UVs there, instead of stretching each
     * piece over the whole face.
     */
    private static void addPartial(List<Part> parts, BakedQuad frameQuad, BakedQuad coverQuad, Direction face,
            float[] bounds, int tint, @Nullable Direction cullFace, RenderType layer) {
        int[] frameVertices = frameQuad.getVertices();
        int stride = frameVertices.length / 4;
        TextureAtlasSprite from = frameQuad.getSprite();

        // Where on the face each frame vertex is, read from its UVs as retexture does
        float[][] frameCoords = new float[4][];
        float[][] framePositions = new float[4][];
        for (int vertex = 0; vertex < 4; ++vertex) {
            int offset = vertex * stride;
            frameCoords[vertex] = new float[] {
                    (Float.intBitsToFloat(frameVertices[offset + 4]) - from.getU0()) / (from.getU1() - from.getU0()),
                    (Float.intBitsToFloat(frameVertices[offset + 5]) - from.getV0()) / (from.getV1() - from.getV0()) };
            framePositions[vertex] = new float[] { Float.intBitsToFloat(frameVertices[offset]),
                    Float.intBitsToFloat(frameVertices[offset + 1]), Float.intBitsToFloat(frameVertices[offset + 2]) };
        }
        Affine toPosition = Affine.fit(frameCoords, framePositions);

        int[] coverVertices = coverQuad.getVertices();
        int coverStride = coverVertices.length / 4;
        float[][] coverUvs = new float[4][];
        for (int vertex = 0; vertex < 4; ++vertex) {
            coverUvs[vertex] = new float[] { Float.intBitsToFloat(coverVertices[vertex * coverStride + 4]),
                    Float.intBitsToFloat(coverVertices[vertex * coverStride + 5]) };
        }
        Affine toUv = Affine.fit(faceCoords(coverQuad, face), coverUvs);
        if (toPosition == null || toUv == null) {
            return;
        }

        List<float[]> polygon = clip(List.of(frameCoords), bounds);
        if (polygon.size() < 3) {
            return;
        }
        // Fan the clipped polygon into quads; a triangle repeats its last corner
        for (int start = 1; start < polygon.size() - 1; start += 2) {
            int[] vertices = frameVertices.clone();
            for (int vertex = 0; vertex < 4; ++vertex) {
                float[] p = polygon.get(vertex == 0 ? 0 : Math.min(start + vertex - 1, polygon.size() - 1));
                float[] position = toPosition.apply(p);
                float[] uv = toUv.apply(p);
                int offset = vertex * stride;
                vertices[offset] = Float.floatToRawIntBits(position[0]);
                vertices[offset + 1] = Float.floatToRawIntBits(position[1]);
                vertices[offset + 2] = Float.floatToRawIntBits(position[2]);
                vertices[offset + 4] = Float.floatToRawIntBits(uv[0]);
                vertices[offset + 5] = Float.floatToRawIntBits(uv[1]);
            }
            parts.add(new Part(new BakedQuad(vertices, tint, frameQuad.getDirection(), coverQuad.getSprite(),
                    frameQuad.isShade()), cullFace, layer));
        }
    }

    /** Clips a polygon to the rectangle {@code {minU, minV, maxU, maxV}}, dropping repeated corners. */
    private static List<float[]> clip(List<float[]> polygon, float[] bounds) {
        for (int edge = 0; edge < 4; ++edge) {
            int axis = edge & 1;
            boolean upper = edge >= 2;
            float limit = bounds[edge];
            List<float[]> out = new ArrayList<>();
            for (int i = 0; i < polygon.size(); ++i) {
                float[] a = polygon.get(i);
                float[] b = polygon.get((i + 1) % polygon.size());
                boolean aIn = upper ? a[axis] <= limit : a[axis] >= limit;
                boolean bIn = upper ? b[axis] <= limit : b[axis] >= limit;
                if (aIn) {
                    out.add(a);
                }
                if (aIn != bIn) {
                    float t = (limit - a[axis]) / (b[axis] - a[axis]);
                    out.add(new float[] { a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t });
                }
            }
            polygon = out;
        }
        List<float[]> distinct = new ArrayList<>();
        for (float[] p : polygon) {
            float[] last = distinct.isEmpty() ? null : distinct.get(distinct.size() - 1);
            if (last == null || Math.abs(last[0] - p[0]) > EPSILON || Math.abs(last[1] - p[1]) > EPSILON) {
                distinct.add(p);
            }
        }
        if (distinct.size() > 1) {
            float[] first = distinct.get(0), last = distinct.get(distinct.size() - 1);
            if (Math.abs(last[0] - first[0]) <= EPSILON && Math.abs(last[1] - first[1]) <= EPSILON) {
                distinct.remove(distinct.size() - 1);
            }
        }
        return distinct;
    }

    /** An affine map from a point on a face to values (a position, a UV), fitted to a quad's corners. */
    private record Affine(float[] origin, float[] alongU, float[] alongV, float[] p0, float[] e1, float[] e2,
            float det) {

        /** Fits the map through the three corners that span the most area, or {@code null} for a degenerate quad. */
        @Nullable
        static Affine fit(float[][] points, float[][] values) {
            int[][] triples = { { 0, 1, 2 }, { 0, 1, 3 }, { 0, 2, 3 }, { 1, 2, 3 } };
            Affine best = null;
            for (int[] t : triples) {
                float[] p0 = points[t[0]];
                float[] e1 = { points[t[1]][0] - p0[0], points[t[1]][1] - p0[1] };
                float[] e2 = { points[t[2]][0] - p0[0], points[t[2]][1] - p0[1] };
                float det = e1[0] * e2[1] - e1[1] * e2[0];
                if (Math.abs(det) > 1.0E-8F && (best == null || Math.abs(det) > Math.abs(best.det))) {
                    int n = values[0].length;
                    float[] origin = new float[n], alongU = new float[n], alongV = new float[n];
                    for (int k = 0; k < n; ++k) {
                        origin[k] = values[t[0]][k];
                        alongU[k] = values[t[1]][k] - origin[k];
                        alongV[k] = values[t[2]][k] - origin[k];
                    }
                    best = new Affine(origin, alongU, alongV, p0, e1, e2, det);
                }
            }
            return best;
        }

        float[] apply(float[] p) {
            float dx = p[0] - p0[0], dy = p[1] - p0[1];
            float s = (dx * e2[1] - dy * e2[0]) / det;
            float t = (e1[0] * dy - e1[1] * dx) / det;
            float[] out = new float[origin.length];
            for (int k = 0; k < out.length; ++k) {
                out[k] = origin[k] + alongU[k] * s + alongV[k] * t;
            }
            return out;
        }
    }

    /** The cover model's quads facing {@code face}, whether or not the model marks them as culled. */
    private static List<BakedQuad> faceQuads(BakedModel model, BlockState state, Direction face, RandomSource random) {
        random.setSeed(SEED);
        List<BakedQuad> culled = model.getQuads(state, face, random);
        if (!culled.isEmpty()) {
            return culled;
        }

        random.setSeed(SEED);
        List<BakedQuad> facing = new ArrayList<>();
        for (BakedQuad quad : model.getQuads(state, null, random)) {
            if (quad.getDirection() == face) {
                facing.add(quad);
            }
        }
        return facing;
    }

    private static TextureAtlasSprite sprite(ResourceLocation texture) {
        return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(texture);
    }

    /** Copies a quad, moving its UVs from its own sprite onto {@code sprite}. */
    private static BakedQuad retexture(BakedQuad quad, TextureAtlasSprite sprite, int tintIndex) {
        TextureAtlasSprite from = quad.getSprite();
        int[] vertices = quad.getVertices().clone();
        int stride = vertices.length / 4;

        for (int vertex = 0; vertex < 4; ++vertex) {
            int offset = vertex * stride;
            float u = Float.intBitsToFloat(vertices[offset + 4]);
            float v = Float.intBitsToFloat(vertices[offset + 5]);
            float relU = (u - from.getU0()) / (from.getU1() - from.getU0());
            float relV = (v - from.getV0()) / (from.getV1() - from.getV0());
            vertices[offset + 4] = Float.floatToRawIntBits(sprite.getU0() + (sprite.getU1() - sprite.getU0()) * relU);
            vertices[offset + 5] = Float.floatToRawIntBits(sprite.getV0() + (sprite.getV1() - sprite.getV0()) * relV);
        }

        return new BakedQuad(vertices, tintIndex, quad.getDirection(), sprite, quad.isShade());
    }
}
