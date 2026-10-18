package com.carpentersblocks.client.frame;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.redstone.CarpentersButtonBlock;
import com.carpentersblocks.block.redstone.CarpentersDaylightSensorBlock;
import com.carpentersblocks.block.redstone.CarpentersLeverBlock;
import com.carpentersblocks.block.redstone.CarpentersPressurePlateBlock;
import com.carpentersblocks.block.redstone.RedstoneGeometry;
import com.carpentersblocks.block.redstone.SidedRedstoneBlock;

/**
 * Frame meshes of the redstone family, from {@link RedstoneGeometry}. The lever's handle and the daylight sensor's
 * glass, lapis and redstone are fixed quads that keep their own textures under covers, as the original drew them.
 */
public final class RedstoneFrames {

    private static final ResourceLocation FRAME = CarpentersBlocks.id("block/frame");
    private static final ResourceLocation SOLID = CarpentersBlocks.id("block/general/solid");
    private static final ResourceLocation FULL_FRAME = CarpentersBlocks.id("block/general/full_frame");
    private static final ResourceLocation LEVER = CarpentersBlocks.id("block/lever/lever");
    private static final ResourceLocation GLASS = CarpentersBlocks.id("block/daylight_sensor/daylight_sensor_glass_top");
    private static final ResourceLocation LAPIS = ResourceLocation.withDefaultNamespace("block/lapis_block");
    private static final ResourceLocation REDSTONE = ResourceLocation.withDefaultNamespace("block/redstone_block");

    private static final int WHITE = -1;
    /** The original dimmed an idle sensor's redstone to half brightness. */
    private static final int DIM = 0xFF808080;

    private RedstoneFrames() {}

    public static final FrameBuilder BUTTON = (state, key, mesh) -> box(mesh, RedstoneGeometry.button(
            state.getValue(SidedRedstoneBlock.FACING), state.getValue(CarpentersButtonBlock.POWERED)), FRAME);

    public static final FrameBuilder PRESSURE_PLATE = (state, key, mesh) -> box(mesh, RedstoneGeometry.pressurePlate(
            state.getValue(SidedRedstoneBlock.FACING), state.getValue(CarpentersPressurePlateBlock.POWERED)),
            FULL_FRAME);

    public static final FrameBuilder LEVER_FRAME = new FrameBuilder() {
        @Override
        public void build(BlockState state, @Nullable Object key, FrameMesh mesh) {
            Direction facing = state.getValue(SidedRedstoneBlock.FACING);
            boolean alongZ = CarpentersLeverBlock.alongZ(state);
            box(mesh, RedstoneGeometry.leverBase(facing, alongZ), SOLID);
            Vector3f[] corners = RedstoneGeometry.leverHandle(facing, alongZ,
                    state.getValue(CarpentersLeverBlock.POWERED));
            TextureAtlasSprite sprite = sprite(LEVER);
            for (int face = 0; face < 6; ++face) {
                int[] index = RedstoneGeometry.HANDLE_FACES[face];
                // The stick's end (u 7-9, v 6-8) on its foot and tip, its length (v 6-16) on the sides
                float vMax = face < 2 ? 8 : 16;
                float[][] uv = { { 7, vMax }, { 9, vMax }, { 9, 6 }, { 7, 6 } };
                Vector3f[] quad = new Vector3f[4];
                for (int i = 0; i < 4; ++i) {
                    quad[i] = corners[index[i]];
                    uv[i][0] /= 16;
                    uv[i][1] /= 16;
                }
                mesh.fixed(bake(quad, uv, sprite, WHITE));
            }
        }
    };

    public static final FrameBuilder DAYLIGHT_SENSOR = new FrameBuilder() {
        @Override
        public void build(BlockState state, @Nullable Object key, FrameMesh mesh) {
            Direction facing = state.getValue(SidedRedstoneBlock.FACING);
            for (Box part : RedstoneGeometry.SENSOR_FRAME) {
                box(mesh, RedstoneGeometry.attached(part, facing), FRAME);
            }
            fixedBox(mesh, RedstoneGeometry.attached(RedstoneGeometry.SENSOR_GLASS, facing), GLASS, WHITE);
            fixedBox(mesh, RedstoneGeometry.attached(RedstoneGeometry.SENSOR_LAPIS, facing), LAPIS, WHITE);
            boolean active = state.getBlock() instanceof CarpentersDaylightSensorBlock sensor && sensor.isActive(state);
            for (Box part : RedstoneGeometry.SENSOR_REDSTONE) {
                fixedBox(mesh, RedstoneGeometry.attached(part, facing), REDSTONE, active ? WHITE : DIM);
            }
        }
    };

    private static void box(FrameMesh mesh, Box box, ResourceLocation sprite) {
        mesh.box((float) box.x1() / 16, (float) box.y1() / 16, (float) box.z1() / 16, (float) box.x2() / 16,
                (float) box.y2() / 16, (float) box.z2() / 16, sprite);
    }

    /** A box of fixed quads, UVs projected as {@link FrameMesh} does. */
    private static void fixedBox(FrameMesh mesh, Box box, ResourceLocation texture, int color) {
        float x1 = (float) box.x1() / 16, y1 = (float) box.y1() / 16, z1 = (float) box.z1() / 16;
        float x2 = (float) box.x2() / 16, y2 = (float) box.y2() / 16, z2 = (float) box.z2() / 16;
        Vector3f[] c = new Vector3f[8];
        for (int i = 0; i < 8; ++i) {
            c[i] = new Vector3f((i & 1) == 0 ? x1 : x2, (i & 2) == 0 ? y1 : y2, (i & 4) == 0 ? z1 : z2);
        }
        // Corner index bits: 1 = max x, 2 = max y, 4 = max z; faces counter-clockwise from outside
        List<int[]> faces = List.of(new int[] { 0, 1, 5, 4 }, new int[] { 2, 6, 7, 3 }, new int[] { 0, 2, 3, 1 },
                new int[] { 4, 5, 7, 6 }, new int[] { 0, 4, 6, 2 }, new int[] { 1, 3, 7, 5 });
        List<Direction> directions = List.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH,
                Direction.WEST, Direction.EAST);
        TextureAtlasSprite sprite = sprite(texture);
        for (int f = 0; f < 6; ++f) {
            Vector3f[] quad = new Vector3f[4];
            float[][] uv = new float[4][];
            for (int i = 0; i < 4; ++i) {
                quad[i] = c[faces.get(f)[i]];
                uv[i] = FrameMesh.project(quad[i], directions.get(f));
            }
            mesh.fixed(bake(quad, uv, sprite, color));
        }
    }

    private static TextureAtlasSprite sprite(ResourceLocation texture) {
        return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(texture);
    }

    /** A quad from four corners (block units) and their UVs (0 to 1 across the sprite). */
    private static BakedQuad bake(Vector3f[] corners, float[][] uv, TextureAtlasSprite sprite, int color) {
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
