package com.carpentersblocks.client.frame;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import org.joml.Vector3f;

import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.sided.LegacyVectors;

/**
 * Builds the quads code-built frames add with {@link FrameMesh#fixed}: parts the original drew with their own
 * texture whatever the cover, such as handles, inlays, glass and torch flames.
 */
public final class FixedQuads {

    public static final int WHITE = -1;

    private static final List<Direction> BOX_FACES = List.of(Direction.DOWN, Direction.UP, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST);
    // Corner index bits: 1 = max x, 2 = max y, 4 = max z; faces counter-clockwise from outside
    private static final int[][] BOX_CORNERS = { { 0, 1, 5, 4 }, { 2, 6, 7, 3 }, { 0, 2, 3, 1 }, { 4, 5, 7, 6 },
            { 0, 4, 6, 2 }, { 1, 3, 7, 5 } };

    private FixedQuads() {}

    public static TextureAtlasSprite sprite(ResourceLocation texture) {
        return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(texture);
    }

    /** The corners (block units) of a box in pixels, indexed as {@link FrameMesh#box}. */
    public static Vector3f[] corners(Box box) {
        Vector3f[] c = new Vector3f[8];
        for (int i = 0; i < 8; ++i) {
            c[i] = new Vector3f((float) ((i & 1) == 0 ? box.x1() : box.x2()) / 16,
                    (float) ((i & 2) == 0 ? box.y1() : box.y2()) / 16, (float) ((i & 4) == 0 ? box.z1() : box.z2()) / 16);
        }
        return c;
    }

    /** A box (pixels), UVs projected as {@link FrameMesh} does. */
    public static void box(FrameMesh mesh, Box box, ResourceLocation texture, int color) {
        Vector3f[] c = corners(box);
        TextureAtlasSprite sprite = sprite(texture);
        for (int f = 0; f < 6; ++f) {
            Vector3f[] quad = new Vector3f[4];
            float[][] uv = new float[4][];
            for (int i = 0; i < 4; ++i) {
                quad[i] = c[BOX_CORNERS[f][i]];
                uv[i] = FrameMesh.project(quad[i], BOX_FACES.get(f));
            }
            mesh.fixed(bake(quad, uv, sprite, color));
        }
    }

    /** One face of a box (pixels), UVs projected. */
    public static void face(FrameMesh mesh, Box box, Direction face, ResourceLocation texture, int color) {
        Vector3f[] c = corners(box);
        int f = BOX_FACES.indexOf(face);
        Vector3f[] quad = new Vector3f[4];
        float[][] uv = new float[4][];
        for (int i = 0; i < 4; ++i) {
            quad[i] = c[BOX_CORNERS[f][i]];
            uv[i] = FrameMesh.project(quad[i], face);
        }
        mesh.fixed(bake(quad, uv, sprite(texture), color));
    }

    /**
     * The six faces of a stick built as the original's handles ({@link LegacyVectors#CUBOID_FACES} order), each face
     * mapped onto a rectangle of the sprite in pixels: {@code ends} for its foot and tip, {@code sides} for the rest,
     * each as {u1, v1, u2, v2}.
     */
    public static List<BakedQuad> stick(Vector3f[] corners, ResourceLocation texture, float[] ends, float[] sides,
            int color) {
        TextureAtlasSprite sprite = sprite(texture);
        List<BakedQuad> quads = new ArrayList<>(6);
        for (int face = 0; face < 6; ++face) {
            float[] r = face < 2 ? ends : sides;
            // As the original's drawVertex order: (u1, v2), (u2, v2), (u2, v1), (u1, v1)
            float[][] uv = { { r[0] / 16, r[3] / 16 }, { r[2] / 16, r[3] / 16 }, { r[2] / 16, r[1] / 16 },
                    { r[0] / 16, r[1] / 16 } };
            Vector3f[] quad = new Vector3f[4];
            for (int i = 0; i < 4; ++i) {
                quad[i] = corners[LegacyVectors.CUBOID_FACES[face][i]];
            }
            quads.add(bake(quad, uv, sprite, color));
        }
        return quads;
    }

    /** A quad from four corners (block units) and their UVs (0 to 1 across the sprite). */
    public static BakedQuad bake(Vector3f[] corners, float[][] uv, TextureAtlasSprite sprite, int color) {
        Vector3f normal = new Vector3f(corners[1]).sub(corners[0]).cross(new Vector3f(corners[2]).sub(corners[1]));
        if (normal.lengthSquared() > 0) {
            normal.normalize();
        }
        int packedNormal = (((byte) Math.round(normal.x * 127)) & 0xFF)
                | ((((byte) Math.round(normal.y * 127)) & 0xFF) << 8)
                | ((((byte) Math.round(normal.z * 127)) & 0xFF) << 16);
        int[] data = new int[32];
        for (int i = 0; i < 4; ++i) {
            int offset = i * 8;
            data[offset] = Float.floatToRawIntBits(corners[i].x);
            data[offset + 1] = Float.floatToRawIntBits(corners[i].y);
            data[offset + 2] = Float.floatToRawIntBits(corners[i].z);
            data[offset + 3] = color;
            data[offset + 4] = Float.floatToRawIntBits(sprite.getU0() + (sprite.getU1() - sprite.getU0()) * uv[i][0]);
            data[offset + 5] = Float.floatToRawIntBits(sprite.getV0() + (sprite.getV1() - sprite.getV0()) * uv[i][1]);
            data[offset + 7] = packedNormal;
        }
        Direction face = Direction.getNearest(normal.x, normal.y, normal.z);
        return new BakedQuad(data, -1, face, sprite, true);
    }
}
