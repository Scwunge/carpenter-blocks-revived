package com.carpentersblocks.client.frame;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.barrier.BarrierGeometry;
import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.barrier.CarpentersBarrierBlock;
import com.carpentersblocks.block.barrier.CarpentersGateBlock;

/** Frame meshes of the Carpenter's Barrier and Gate, from {@link BarrierGeometry}. */
public final class BarrierFrames {

    private static final ResourceLocation FRAME = CarpentersBlocks.id("block/frame");

    private BarrierFrames() {}

    public static final FrameBuilder BARRIER = new FrameBuilder() {
        @Override
        public Object renderKey(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            return CarpentersBarrierBlock.surroundings(level, pos);
        }

        @Override
        public void build(BlockState state, @Nullable Object key, FrameMesh mesh) {
            BarrierGeometry.Surroundings around = key instanceof BarrierGeometry.Surroundings s ? s
                    : BarrierGeometry.Surroundings.ALONE;
            add(mesh, BarrierGeometry.barrier(state.getValue(CarpentersBarrierBlock.TYPE),
                    CarpentersBarrierBlock.connections(state), around));
        }
    };

    public static final FrameBuilder GATE = new FrameBuilder() {
        @Override
        public Object renderKey(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            return CarpentersGateBlock.surroundings(level, pos);
        }

        @Override
        public void build(BlockState state, @Nullable Object key, FrameMesh mesh) {
            BarrierGeometry.GateSurroundings around = key instanceof BarrierGeometry.GateSurroundings s ? s
                    : BarrierGeometry.GateSurroundings.ALONE;
            add(mesh, BarrierGeometry.gate(state.getValue(CarpentersGateBlock.TYPE),
                    state.getValue(CarpentersGateBlock.FACING), state.getValue(CarpentersGateBlock.OPEN), around));
        }
    };

    private static void add(FrameMesh mesh, List<Box> boxes) {
        for (Box box : boxes) {
            if (!box.isEmpty()) {
                mesh.box((float) box.x1() / 16, (float) box.y1() / 16, (float) box.z1() / 16, (float) box.x2() / 16,
                        (float) box.y2() / 16, (float) box.z2() / 16, FRAME);
            }
        }
    }
}
