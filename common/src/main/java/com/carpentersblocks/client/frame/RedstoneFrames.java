package com.carpentersblocks.client.frame;


import net.minecraft.client.renderer.block.model.BakedQuad;
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

    private static final int WHITE = FixedQuads.WHITE;
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
            // The stick's end (u 7-9, v 6-8) on its foot and tip, its length (v 6-16) on the sides
            for (BakedQuad quad : FixedQuads.stick(corners, LEVER, new float[] { 7, 6, 9, 8 },
                    new float[] { 7, 6, 9, 16 }, FixedQuads.WHITE)) {
                mesh.fixed(quad);
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
            FixedQuads.box(mesh, RedstoneGeometry.attached(RedstoneGeometry.SENSOR_GLASS, facing), GLASS, WHITE);
            FixedQuads.box(mesh, RedstoneGeometry.attached(RedstoneGeometry.SENSOR_LAPIS, facing), LAPIS, WHITE);
            boolean active = state.getBlock() instanceof CarpentersDaylightSensorBlock sensor && sensor.isActive(state);
            for (Box part : RedstoneGeometry.SENSOR_REDSTONE) {
                FixedQuads.box(mesh, RedstoneGeometry.attached(part, facing), REDSTONE, active ? WHITE : DIM);
            }
        }
    };

    private static void box(FrameMesh mesh, Box box, ResourceLocation sprite) {
        mesh.box((float) box.x1() / 16, (float) box.y1() / 16, (float) box.z1() / 16, (float) box.x2() / 16,
                (float) box.y2() / 16, (float) box.z2() / 16, sprite);
    }
}
