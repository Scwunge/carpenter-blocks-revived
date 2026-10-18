package com.carpentersblocks.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;

/**
 * The Carpenter's Block: a frame that takes on the look of blocks applied to it, and can be hammered into slabs.
 * Port of the original {@code BlockCarpentersBlock}.
 */
public class CarpentersBlockBlock extends CoverableBlock {

    public static final MapCodec<CarpentersBlockBlock> CODEC = simpleCodec(CarpentersBlockBlock::new);

    public static final EnumProperty<FrameShape> SHAPE = EnumProperty.create("shape", FrameShape.class);
    /** True when every exposed face is covered by an opaque block, so neighbors may cull against it. */
    public static final BooleanProperty SOLID = BooleanProperty.create("solid");

    public CarpentersBlockBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(SHAPE, FrameShape.FULL)
                .setValue(SOLID, false)
                .setValue(LIGHT, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SHAPE, SOLID);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(SHAPE).shape();
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(SOLID) ? state.getValue(SHAPE).shape() : Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(SOLID);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();

        // Match the shape of an adjacent Carpenter's Block, unless sneaking (as in the original)
        if (!context.isSecondaryUseActive()) {
            for (Direction dir : Direction.values()) {
                BlockState adjacent = context.getLevel().getBlockState(context.getClickedPos().relative(dir));
                if (adjacent.is(this)) {
                    state = state.setValue(SHAPE, adjacent.getValue(SHAPE));
                }
            }
        }

        return state;
    }

    @Override
    protected boolean canCoverSide(CoverableBlockEntity be, int slot) {
        return true;
    }

    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        return cycleShape(level, pos, state, face);
    }

    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        return cycleShape(level, pos, state, face);
    }

    /** A full block becomes a slab on the side away from the clicked face; a slab becomes full. */
    private boolean cycleShape(Level level, BlockPos pos, BlockState state, Direction clickedFace) {
        if (!level.isClientSide) {
            FrameShape next = state.getValue(SHAPE) == FrameShape.FULL
                    ? FrameShape.slabAgainst(clickedFace.getOpposite())
                    : FrameShape.FULL;
            CoverableBlockEntity be = blockEntity(level, pos);
            BlockState updated = state.setValue(SHAPE, next);
            if (be != null) {
                updated = stateForCovers(updated, be.getCoverData());
            }
            level.setBlock(pos, updated, Block.UPDATE_ALL);
        }
        return true;
    }

    @Override
    public BlockState stateForCovers(BlockState state, CoverData data) {
        FrameShape shape = state.getValue(SHAPE);
        boolean solid = true;
        for (Direction face : Direction.values()) {
            BlockState cover = data.coverFor(face);
            if (cover == null
                    || shape.coversFace(face) && !cover.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
                solid = false;
                break;
            }
        }
        return super.stateForCovers(state, data).setValue(SOLID, solid);
    }
}
