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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.ChiselDesigns;
import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.Overlay;

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
        List<BakedQuad> frameQuads = frame.getQuads(state, cullFace, random);
        if (data == null || data.isEmpty()) {
            List<Part> parts = new ArrayList<>(frameQuads.size());
            for (BakedQuad frameQuad : frameQuads) {
                parts.add(new Part(frameQuad, cullFace, FRAME_LAYER));
            }
            return parts;
        }

        List<Part> parts = new ArrayList<>(frameQuads.size() * 2);
        for (BakedQuad frameQuad : frameQuads) {
            Direction face = frameQuad.getDirection();
            int slot = data.slotFor(face);
            BlockState cover = data.cover(slot);
            boolean dyed = data.dye(slot) != null;

            // Cover (or bare frame)
            if (cover == null) {
                parts.add(new Part(dyed ? retexture(frameQuad, frameQuad.getSprite(), CoverTint.dye(slot)) : frameQuad,
                        cullFace, FRAME_LAYER));
            } else {
                addCover(parts, frameQuad, cullFace, cover, slot, dyed, random, layerOf.apply(cover));
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

    private static void addCover(List<Part> parts, BakedQuad frameQuad, @Nullable Direction cullFace,
            BlockState cover, int slot, boolean dyed, RandomSource random, RenderType layer) {
        BakedModel coverModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(cover);
        Direction face = frameQuad.getDirection();

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
