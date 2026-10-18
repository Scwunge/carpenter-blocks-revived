package com.carpentersblocks.client.frame;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.ladder.CarpentersLadderBlock;
import com.carpentersblocks.block.ladder.LadderGeometry;
import com.carpentersblocks.block.ladder.LadderType;
import com.carpentersblocks.block.sided.SidedBlock;

/**
 * Frame mesh of the Carpenter's Ladder, from {@link LadderGeometry}. The default style joins its neighbors in a row;
 * the rail style's iron keeps its own texture.
 */
public final class LadderFrames {

    private static final ResourceLocation FRAME = CarpentersBlocks.id("block/frame");
    private static final ResourceLocation IRON = ResourceLocation.withDefaultNamespace("block/iron_block");

    private LadderFrames() {}

    public static final FrameBuilder LADDER = new FrameBuilder() {
        @Override
        @Nullable
        public Object renderKey(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            return state.getValue(CarpentersLadderBlock.TYPE) == LadderType.DEFAULT
                    ? CarpentersLadderBlock.joins(level, pos, state) : null;
        }

        @Override
        public void build(BlockState state, @Nullable Object key, FrameMesh mesh) {
            LadderGeometry.Joins joins = key instanceof LadderGeometry.Joins j ? j : LadderGeometry.Joins.NONE;
            LadderGeometry.Parts parts = LadderGeometry.parts(state.getValue(CarpentersLadderBlock.TYPE),
                    state.getValue(SidedBlock.FACING), joins);
            for (Box box : parts.frame()) {
                mesh.box((float) box.x1() / 16, (float) box.y1() / 16, (float) box.z1() / 16, (float) box.x2() / 16,
                        (float) box.y2() / 16, (float) box.z2() / 16, FRAME);
            }
            for (Box box : parts.iron()) {
                FixedQuads.box(mesh, box, IRON, FixedQuads.WHITE);
            }
        }
    };
}
