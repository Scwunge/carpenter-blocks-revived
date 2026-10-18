package com.carpentersblocks.block.ladder;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.sided.SidedBlock;
import com.carpentersblocks.config.CBConfig;

/**
 * Carpenter's Ladder: on a wall, or standing free on a floor or on another ladder. New ladders take the style and
 * facing of one below or above unless placed sneaking. The hammer switches style. Climbable through
 * {@code #minecraft:climbable}. Port of the original {@code BlockCarpentersLadder}.
 */
public class CarpentersLadderBlock extends SidedBlock {

    public static final MapCodec<CarpentersLadderBlock> CODEC = simpleCodec(CarpentersLadderBlock::new);

    public static final EnumProperty<LadderType> TYPE = EnumProperty.create("type", LadderType.class);

    private static final VoxelShape[] SHAPES = new VoxelShape[6];

    static {
        for (Direction facing : Direction.values()) {
            SHAPES[facing.ordinal()] = LadderGeometry.outline(facing).shape();
        }
    }

    public CarpentersLadderBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, LadderType.DEFAULT)
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
        builder.add(TYPE);
    }

    // Placement and support

    /**
     * Wall ladders need a sturdy face as usual. Clicking a floor or ceiling (or another ladder's top or bottom)
     * stands the ladder free, its face towards the player.
     */
    @Override
    @Nullable
    protected BlockState placementState(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clicked = context.getClickedFace();
        if (clicked.getAxis() == Direction.Axis.Y) {
            BlockPos support = pos.relative(clicked.getOpposite());
            BlockState below = level.getBlockState(support);
            if (!below.isFaceSturdy(level, support, clicked) && !(below.getBlock() instanceof CarpentersLadderBlock)) {
                return null;
            }
            boolean lookingAlongX = context.getHorizontalDirection().getAxis() == Direction.Axis.X;
            state = state.setValue(FACING, lookingAlongX ? Direction.UP : Direction.DOWN);
        }
        if (!context.isSecondaryUseActive()) {
            // As the original: the ladder below wins over the one above
            for (BlockPos other : new BlockPos[] { pos.above(), pos.below() }) {
                BlockState ladder = level.getBlockState(other);
                if (ladder.getBlock() instanceof CarpentersLadderBlock) {
                    BlockState copied = state.setValue(TYPE, ladder.getValue(TYPE))
                            .setValue(FACING, ladder.getValue(FACING));
                    if (copied.canSurvive(level, pos)) {
                        state = copied;
                    }
                }
            }
        }
        return state;
    }

    /** Freestanding ladders stay put; wall ladders need their wall unless freestanding ladders are enabled. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return LadderGeometry.freestanding(state.getValue(FACING)) || CBConfig.enableFreeStandingLadders
                || super.canSurvive(state, level, pos);
    }

    // Hammer: left-click the next style, right-click the previous

    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        level.setBlock(pos, state.setValue(TYPE, state.getValue(TYPE).next()), Block.UPDATE_ALL);
        return true;
    }

    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(TYPE, state.getValue(TYPE).previous()), Block.UPDATE_ALL);
        }
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).ordinal()];
    }

    // Rendering inputs

    /** Which neighbors along the row join this one: ladders with the same facing, as the original's classic style. */
    public static LadderGeometry.Joins joins(BlockGetter level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction low = Direction.get(Direction.AxisDirection.NEGATIVE, LadderGeometry.along(facing));
        return new LadderGeometry.Joins(joins(level, pos.relative(low), facing),
                joins(level, pos.relative(low.getOpposite()), facing));
    }

    private static boolean joins(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState other = level.getBlockState(pos);
        return other.getBlock() instanceof CarpentersLadderBlock && other.getValue(FACING) == facing;
    }

    // Structures: a freestanding ladder turns between its two axes

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        Direction facing = state.getValue(FACING);
        if (LadderGeometry.freestanding(facing)) {
            boolean quarter = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90;
            return quarter ? state.setValue(FACING, facing.getOpposite()) : state;
        }
        return super.rotate(state, rotation);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return LadderGeometry.freestanding(state.getValue(FACING)) ? state : super.mirror(state, mirror);
    }
}
