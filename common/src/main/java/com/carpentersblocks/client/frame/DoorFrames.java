package com.carpentersblocks.client.frame;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.door.CarpentersDoorBlock;
import com.carpentersblocks.block.door.CarpentersGarageDoorBlock;
import com.carpentersblocks.block.door.CarpentersHatchBlock;
import com.carpentersblocks.block.door.DoorGeometry;
import com.carpentersblocks.block.door.DoorParts;
import com.carpentersblocks.block.door.GarageDoorGeometry;
import com.carpentersblocks.block.door.HatchGeometry;
import com.carpentersblocks.config.CBConfig;

/**
 * Frame meshes of the Carpenter's Door, Hatch and Garage Door, from their geometry classes. The frames take the cover;
 * handles and the siding garage door's backing are iron, and panes keep their own glass or screen texture.
 */
public final class DoorFrames {

    private static final ResourceLocation SOLID = CarpentersBlocks.id("block/general/solid");
    private static final ResourceLocation IRON = ResourceLocation.withDefaultNamespace("block/iron_block");
    /** How far down an open garage door's top piece looks for its bottom piece while chunks build. */
    private static final int GARAGE_DOOR_REACH = 16;

    private DoorFrames() {}

    public static final FrameBuilder DOOR = (state, key, mesh) -> add(mesh,
            DoorGeometry.parts(state.getValue(CarpentersDoorBlock.TYPE), CarpentersDoorBlock.isLower(state),
                    state.getValue(CarpentersDoorBlock.OPEN), CarpentersDoorBlock.hingeLeft(state),
                    CarpentersDoorBlock.side(state)));

    public static final FrameBuilder HATCH = (state, key, mesh) -> add(mesh,
            HatchGeometry.parts(state.getValue(CarpentersHatchBlock.TYPE), CarpentersHatchBlock.isHigh(state),
                    state.getValue(CarpentersHatchBlock.OPEN), state.getValue(CarpentersHatchBlock.FACING)));

    public static final FrameBuilder GARAGE_DOOR = new FrameBuilder() {
        /** Whether the piece is the bottom one, which the glass style draws differently. */
        @Override
        @Nullable
        public Object renderKey(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            return CarpentersGarageDoorBlock.isBottommost(level, pos);
        }

        /** An open door is drawn on its top piece with the bottom piece's attributes, as the original. */
        @Override
        public BlockPos coverSource(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            if (!state.getValue(CarpentersGarageDoorBlock.OPEN)) {
                return pos;
            }
            BlockPos bottom = pos;
            for (int i = 0; i < GARAGE_DOOR_REACH
                    && level.getBlockState(bottom.below()).getBlock() instanceof CarpentersGarageDoorBlock; ++i) {
                bottom = bottom.below();
            }
            return bottom;
        }

        @Override
        public void build(BlockState state, @Nullable Object key, FrameMesh mesh) {
            if (!CarpentersGarageDoorBlock.isVisible(state)) {
                return;
            }
            boolean bottommost = !(key instanceof Boolean b) || b;
            add(mesh, GarageDoorGeometry.parts(state.getValue(CarpentersGarageDoorBlock.TYPE),
                    state.getValue(CarpentersGarageDoorBlock.OPEN), state.getValue(CarpentersGarageDoorBlock.HOST),
                    bottommost, state.getValue(CarpentersGarageDoorBlock.FACING)));
        }
    };

    private static void add(FrameMesh mesh, DoorParts parts) {
        for (Box box : parts.frame()) {
            mesh.box((float) box.x1() / 16, (float) box.y1() / 16, (float) box.z1() / 16, (float) box.x2() / 16,
                    (float) box.y2() / 16, (float) box.z2() / 16, SOLID);
        }
        if (!parts.iron().isEmpty()) {
            TextureAtlasSprite iron = FixedQuads.sprite(IRON);
            for (Box box : parts.iron()) {
                ironBox(mesh, box, iron);
            }
        }
        for (DoorParts.Pane pane : parts.panes()) {
            TextureAtlasSprite sprite = FixedQuads.sprite(pane.texture());
            for (Direction face : new Direction[] { pane.normal(), pane.normal().getOpposite() }) {
                var quad = FixedQuads.face(pane.plane(), face, sprite, FixedQuads.WHITE);
                if (CBConfig.enableAlphaPanes) {
                    mesh.fixedTranslucent(quad);
                } else {
                    mesh.fixed(quad);
                }
            }
        }
    }

    /**
     * An iron box, which may lie in a neighboring block: its UVs are projected from where it sits in that block, so
     * they stay on the sprite.
     */
    private static void ironBox(FrameMesh mesh, Box box, TextureAtlasSprite sprite) {
        // The block the box lies in, by its middle
        float ox = (float) Math.floor((box.x1() + box.x2()) / 32);
        float oy = (float) Math.floor((box.y1() + box.y2()) / 32);
        float oz = (float) Math.floor((box.z1() + box.z2()) / 32);
        for (Direction face : Direction.values()) {
            Vector3f[] corners = FixedQuads.faceCorners(box, face);
            float[][] uv = new float[4][];
            for (int i = 0; i < 4; ++i) {
                uv[i] = FrameMesh.project(new Vector3f(corners[i]).sub(ox, oy, oz), face);
            }
            mesh.fixed(FixedQuads.bake(corners, uv, sprite, FixedQuads.WHITE));
        }
    }
}
