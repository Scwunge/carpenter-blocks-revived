package com.carpentersblocks.client.frame;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.safe.CarpentersSafeBlock;
import com.carpentersblocks.block.safe.SafeBlockEntity;
import com.carpentersblocks.block.safe.SafeGeometry;
import com.carpentersblocks.block.safe.SafePanel;

/**
 * The Carpenter's Safe's frame mesh: the body and door take the cover, the panel and handle are the upgrade's metal,
 * and the lights glow in the original's colors when lit.
 */
public final class SafeFrames {

    private static final ResourceLocation SOLID = CarpentersBlocks.id("block/general/solid");
    private static final ResourceLocation LIGHT = CarpentersBlocks.id("block/safe/safe_light");

    private static final int LOCKED_ACTIVE = 0xff0000;
    private static final int LOCKED_INACTIVE = 0x7e3636;
    private static final int UNLOCKED_ACTIVE = 0x00ff00;
    private static final int UNLOCKED_INACTIVE = 0x367e36;
    private static final int CAPACITY_ACTIVE = 0x0000ff;
    private static final int CAPACITY_INACTIVE = 0x383884;

    private SafeFrames() {}

    /** The panel's metal and how many capacity lights are lit. */
    private record Key(SafePanel panel, int capacity) {}

    public static final FrameBuilder SAFE = new FrameBuilder() {
        @Override
        @Nullable
        public Object renderKey(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            return level.getBlockEntity(pos) instanceof SafeBlockEntity safe
                    ? new Key(SafePanel.of(safe.getUpgrade()), safe.getCapacity())
                    : null;
        }

        @Override
        public void build(BlockState state, @Nullable Object key, FrameMesh mesh) {
            Direction facing = state.getValue(CarpentersSafeBlock.FACING);
            boolean open = state.getValue(CarpentersSafeBlock.OPEN);
            boolean locked = state.getValue(CarpentersSafeBlock.LOCKED);
            Key safe = key instanceof Key k ? k : new Key(SafePanel.IRON, 0);

            for (Box box : SafeGeometry.body(open)) {
                Box b = box.facing(facing);
                mesh.box((float) b.x1() / 16, (float) b.y1() / 16, (float) b.z1() / 16, (float) b.x2() / 16,
                        (float) b.y2() / 16, (float) b.z2() / 16, SOLID);
            }
            for (Box box : SafeGeometry.metal(open)) {
                FixedQuads.box(mesh, box.facing(facing), safe.panel().texture(), FixedQuads.WHITE);
            }

            TextureAtlasSprite light = FixedQuads.sprite(LIGHT);
            light(mesh, SafeGeometry.UNLOCKED_LIGHT.facing(facing), light, !locked, UNLOCKED_ACTIVE,
                    UNLOCKED_INACTIVE);
            light(mesh, SafeGeometry.LOCKED_LIGHT.facing(facing), light, locked, LOCKED_ACTIVE, LOCKED_INACTIVE);
            for (int i = 0; i < SafeGeometry.CAPACITY_LIGHTS; ++i) {
                light(mesh, SafeGeometry.capacityLight(i).facing(facing), light, i < safe.capacity(),
                        CAPACITY_ACTIVE, CAPACITY_INACTIVE);
            }
        }
    };

    private static void light(FrameMesh mesh, Box box, TextureAtlasSprite sprite, boolean lit, int active,
            int inactive) {
        int color = abgr(lit ? active : inactive);
        for (Direction face : Direction.values()) {
            var quad = FixedQuads.face(box, face, sprite, color);
            if (lit) {
                mesh.fixedEmissive(quad);
            } else {
                mesh.fixed(quad);
            }
        }
    }

    /** An opaque RGB color as a vertex's packed ABGR color. */
    private static int abgr(int rgb) {
        return 0xFF000000 | (rgb & 0xFF) << 16 | (rgb & 0xFF00) | (rgb >> 16 & 0xFF);
    }
}
