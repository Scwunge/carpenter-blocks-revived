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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.coverable.ChiselDesigns;
import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.Overlay;
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

    /** A quad to render, the face it is culled against, and the chunk layer it belongs to. */
    public record Part(BakedQuad quad, @Nullable Direction cullFace, RenderType layer) {}

    /** Layer of the bare frame, whose texture has transparent gaps. */
    public static final RenderType FRAME_LAYER = RenderType.cutout();
    /** Overlays have transparent fringes. */
    public static final RenderType OVERLAY_LAYER = RenderType.cutoutMipped();
    /** Chisel designs are drawn see-through over the cover, as on the original's alpha pass. */
    public static final RenderType DESIGN_LAYER = RenderType.translucent();
    /** See-through panes ({@code enableAlphaPanes}), as on the original's alpha pass. */
    public static final RenderType PANE_LAYER = RenderType.translucent();

    /** Covers that show their top on upward slopes, like grass; anything extending SnowyDirtBlock counts too. */
    public static final TagKey<Block> SLOPE_TOP_COVERS = TagKey.create(Registries.BLOCK,
            CarpentersBlocks.id("slope_top_covers"));

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
        if (data == null || data.isEmpty()) {
            List<Part> parts = new ArrayList<>(frameQuads.size());
            for (FrameMesh.Quad frameQuad : frameQuads) {
                parts.add(new Part(frameQuad.quad(), cullFace, frameQuad.translucent() ? PANE_LAYER : FRAME_LAYER));
            }
            return parts;
        }

        List<Part> parts = new ArrayList<>(frameQuads.size() * 2);
        for (FrameMesh.Quad meshQuad : frameQuads) {
            if (meshQuad.fixed()) {
                parts.add(new Part(meshQuad.quad(), cullFace, meshQuad.translucent() ? PANE_LAYER : FRAME_LAYER));
                continue;
            }
            BakedQuad frameQuad = meshQuad.quad();
            Direction face = frameQuad.getDirection();
            int slot = data.slotFor(face);
            BlockState cover = data.cover(slot);
            boolean dyed = data.dye(slot) != null;

            // Cover (or bare frame)
            if (cover == null) {
                parts.add(new Part(dyed ? retexture(frameQuad, frameQuad.getSprite(), CoverTint.dye(slot)) : frameQuad,
                        cullFace, FRAME_LAYER));
            } else {
                Direction spriteFace = meshQuad.sloped() == null ? face : slopedFace(cover, meshQuad.sloped());
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

        return parts;
    }

    /**
     * The cover face whose sprite a sloped quad shows, as the original's {@code getUniqueIcon}: the side texture by
     * default; pillar ends when the pillar stands upright (bark when it lies down); the top for directional blocks
     * on anything but plain wedges; and the top of grass-like blocks on upward slopes.
     */
    public static Direction slopedFace(BlockState cover, FrameMesh.Sloped sloped) {
        Direction face = sloped.side();
        if (cover.hasProperty(BlockStateProperties.AXIS)) {
            Direction.Axis axis = cover.getValue(BlockStateProperties.AXIS);
            if (axis == Direction.Axis.Y) {
                face = sloped.positive() ? Direction.UP : Direction.DOWN;
            } else if (sloped.side().getAxis() == axis) {
                face = Direction.UP;
            }
        } else if (!sloped.wedge() && cover.getBlock() instanceof HorizontalDirectionalBlock) {
            face = Direction.UP;
        }
        if (sloped.positive() && (cover.is(SLOPE_TOP_COVERS) || cover.getBlock() instanceof SnowyDirtBlock)) {
            face = Direction.UP;
        }
        return face;
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
            parts.add(new Part(retexture(frameQuad, coverQuad.getSprite(), tint), cullFace, layer));
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
