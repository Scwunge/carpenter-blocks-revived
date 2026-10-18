package com.carpentersblocks.block.barrier;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;

/**
 * Carpenter's Gate: a gate in the barrier styles that opens away from the player, joins with a gate stacked above
 * or below, and follows redstone. The hammer changes the style (right-click; sneak for the vanilla sub-types).
 * Port of the original {@code BlockCarpentersGate}.
 */
public class CarpentersGateBlock extends CoverableBlock {

    public static final MapCodec<CarpentersGateBlock> CODEC = simpleCodec(CarpentersGateBlock::new);

    public static final EnumProperty<BarrierType> TYPE = CarpentersBarrierBlock.TYPE;
    /**
     * The original's facing and opening direction in one: north or south for a gate across the z axis (a path on
     * x), east or west for one across the x axis. An open gate swings towards the side this turns +x to.
     */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public CarpentersGateBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(TYPE, BarrierType.VANILLA)
                .setValue(FACING, Direction.WEST)
                .setValue(OPEN, false)
                .setValue(POWERED, false)
                .setValue(LIGHT, 0)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE, FACING, OPEN, POWERED);
    }

    /** Whether the gate runs along the z axis, so the path through it is on x. */
    public static boolean acrossZ(BlockState state) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z;
    }

    // Placement

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        // As the original, gates need something solid underneath
        if (!level.getBlockState(pos.below()).blocksMotion()) {
            return null;
        }
        Direction look = context.getHorizontalDirection();
        boolean powered = level.hasNeighborSignal(pos);
        return defaultBlockState()
                .setValue(FACING, look.getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.WEST)
                .setValue(TYPE, CarpentersBarrierBlock.adjacentType(level, pos, BarrierType.VANILLA))
                .setValue(OPEN, powered)
                .setValue(POWERED, powered)
                .setValue(WATERLOGGED, placedInWater(context));
    }

    // Opening

    @Override
    protected void postOnBlockActivated(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, InteractionHand hand, BlockHitResult hit, ActionResult result) {
        if (!level.isClientSide) {
            BlockState next = state.getValue(OPEN) ? state.setValue(OPEN, false)
                    : openAwayFrom(state, player.getDirection());
            setGate(level, pos, next, player, true);
        }
        result.setAltered().setNoSound();
    }

    /** Opens the gate so it swings away from a player looking towards {@code look}, as the original. */
    static BlockState openAwayFrom(BlockState state, Direction look) {
        Direction facing = acrossZ(state)
                ? look == Direction.EAST ? Direction.SOUTH : Direction.NORTH
                : look == Direction.SOUTH ? Direction.WEST : Direction.EAST;
        return state.setValue(FACING, facing).setValue(OPEN, true);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide) {
            return;
        }
        state = level.getBlockState(pos);
        if (!state.is(this)) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            BlockState next = state.setValue(POWERED, powered);
            if (powered != state.getValue(OPEN)) {
                setGate(level, pos, next.setValue(OPEN, powered), null, true);
            } else {
                level.setBlock(pos, next, Block.UPDATE_CLIENTS);
            }
        }
    }

    /** Sets the gate, and opens or closes one gate stacked directly below, or else above, along with it. */
    private void setGate(Level level, BlockPos pos, BlockState state, @Nullable Player player, boolean sound) {
        level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
        boolean open = state.getValue(OPEN);
        if (sound) {
            level.playSound(null, pos, open ? SoundEvents.FENCE_GATE_OPEN : SoundEvents.FENCE_GATE_CLOSE,
                    SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
        }

        // Only one stacked gate follows, as the original intended
        for (BlockPos other : new BlockPos[] { pos.below(), pos.above() }) {
            BlockState stacked = level.getBlockState(other);
            if (stacked.is(this)) {
                if (acrossZ(stacked) == acrossZ(state)) {
                    level.setBlock(other, stacked.setValue(FACING, state.getValue(FACING)).setValue(OPEN, open),
                            Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
                }
                break;
            }
        }
    }

    // Shapes

    private static boolean thin(BlockState state) {
        BarrierType type = state.getValue(TYPE);
        return type == BarrierType.VANILLA || type == BarrierType.WALL;
    }

    private static VoxelShape panel(BlockState state, double height) {
        double lo = thin(state) ? 7 : 6;
        double hi = 16 - lo;
        return acrossZ(state) ? Block.box(lo, 0, 0, hi, height, 16) : Block.box(0, 0, lo, 16, height, hi);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return panel(state, 16);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return state.getValue(OPEN) ? Shapes.empty() : panel(state, 24);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(WATERLOGGED);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return type == PathComputationType.LAND && state.getValue(OPEN);
    }

    // Hammer

    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        if (!level.isClientSide) {
            BarrierType type = state.getValue(TYPE);
            BarrierType next = player.isSecondaryUseActive() ? type.nextSubType() : type.nextType();
            level.setBlock(pos, state.setValue(TYPE, next), Block.UPDATE_ALL);
        }
        return true;
    }

    @Override
    protected boolean canCoverSide(CoverableBlockEntity be, int slot) {
        return true;
    }

    // Rendering inputs

    public static BarrierGeometry.GateSurroundings surroundings(BlockGetter level, BlockPos pos) {
        return new BarrierGeometry.GateSurroundings(
                level.getBlockState(pos.below()).getBlock() instanceof CarpentersGateBlock,
                level.getBlockState(pos.above()).getBlock() instanceof CarpentersGateBlock);
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
