package com.carpentersblocks.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.blockentity.CoverData;

/**
 * Builds the quads of a covered Carpenter's Block, shared by both loaders' model wrappers.
 * <p>
 * The frame model supplies the geometry (full cube or slab). Each frame quad is re-textured with the sprites the
 * cover block shows on that face, so grass sides, logs and the like keep their per-face textures and tinting.
 */
public final class CoverQuads {

    /** A quad to render, the face it is culled against, and the chunk layer it belongs to. */
    public record Part(BakedQuad quad, @Nullable Direction cullFace, RenderType layer) {}

    /** Layer of the bare frame, whose texture has transparent gaps. */
    public static final RenderType FRAME_LAYER = RenderType.cutout();

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
        List<Part> parts = new ArrayList<>(frameQuads.size());

        for (BakedQuad frameQuad : frameQuads) {
            Direction face = frameQuad.getDirection();
            BlockState cover = data == null ? null : data.coverFor(face);
            if (cover == null) {
                parts.add(new Part(frameQuad, cullFace, FRAME_LAYER));
                continue;
            }

            int slot = data.slotFor(face);
            RenderType layer = layerOf.apply(cover);
            BakedModel coverModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(cover);
            List<BakedQuad> coverQuads = faceQuads(coverModel, cover, face, random);

            if (coverQuads.isEmpty()) {
                parts.add(new Part(retexture(frameQuad, coverModel.getParticleIcon(), -1), cullFace, layer));
            } else {
                for (BakedQuad coverQuad : coverQuads) {
                    int tint = coverQuad.isTinted() ? CoverTint.encode(slot, coverQuad.getTintIndex()) : -1;
                    parts.add(new Part(retexture(frameQuad, coverQuad.getSprite(), tint), cullFace, layer));
                }
            }
        }

        return parts;
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
