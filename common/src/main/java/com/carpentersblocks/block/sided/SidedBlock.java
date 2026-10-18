package com.carpentersblocks.block.sided;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.CoverableBlock;

/**
 * A block attached to the face of another block, which falls when that face goes. Port of the original
 * {@code BlockSided}: the redstone family, the torch and the ladder build on it.
 */
public abstract class SidedBlock extends CoverableBlock {

    /** The side of the supporting block this is attached to, pointing out of it (the original's direction). */
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    protected SidedBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    /** Whether the block may hang on this side of another block. */
    protected boolean canAttach(Direction facing) {
        return true;
    }

    // Placement and support

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (!canAttach(facing)) {
            return null;
        }
        BlockState state = placementState(context, defaultBlockState()
                .setValue(FACING, facing)
                .setValue(WATERLOGGED, placedInWater(context)));
        return state != null && state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    /** Adds what a block sets on placement beyond facing and water; {@code null} refuses the placement. */
    @Nullable
    protected BlockState placementState(BlockPlaceContext context, BlockState state) {
        return state;
    }

    public static BlockPos supportPos(BlockState state, BlockPos pos) {
        return pos.relative(state.getValue(FACING).getOpposite());
    }

    /** As the original's {@code isSideSolid} check: the face it hangs on must be sturdy, whatever block it is. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return canAttach(facing) && level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    // Structures

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
