package com.carpentersblocks.block.door;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.CarpentersBlockBlock;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.stairs.CarpentersStairsBlock;

/**
 * Carpenter's Hatch: a trapdoor in five styles hung on the side of a block, at the top or bottom of its space. It
 * opens by hand (or only by redstone when rigid) and swings up against its support. The hammer moves it to the next
 * supporting side (left-click), changes the style (right-click) or the rigidity (sneak right-click). An open hidden
 * hatch at the top can be climbed. Port of the original {@code BlockCarpentersHatch}.
 */
public class CarpentersHatchBlock extends CoverableBlock {

    public static final MapCodec<CarpentersHatchBlock> CODEC = simpleCodec(CarpentersHatchBlock::new);

    /** The side the supporting block is on (the original's direction: Z_NEG is south, Z_POS north and so on). */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** Top or bottom of the block space (the original's position, high or low). */
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    /** Modern addition: like vanilla's trapdoors, redstone toggles the hatch only when its signal changes. */
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<HatchType> TYPE = EnumProperty.create("type", HatchType.class);
    public static final BooleanProperty RIGID = Openables.RIGID;

    /** The order the hammer tries supporting sides in, as the original's directions 0 to 3. */
    private static final Direction[] SUPPORT_ORDER = { Direction.SOUTH, Direction.NORTH, Direction.EAST,
            Direction.WEST };

    private static final Map<BlockState, VoxelShape> SHAPES = new ConcurrentHashMap<>();

    public CarpentersHatchBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.SOUTH)
                .setValue(HALF, Half.BOTTOM)
                .setValue(OPEN, false)
                .setValue(POWERED, false)
                .setValue(TYPE, HatchType.HIDDEN)
                .setValue(RIGID, false)
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
        builder.add(FACING, HALF, OPEN, POWERED, TYPE, RIGID);
    }

    public static boolean isHigh(BlockState state) {
        return state.getValue(HALF) == Half.TOP;
    }

    // Placement: on the side of a supporting block, high when the upper half of the face was clicked

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face.getAxis() == Direction.Axis.Y) {
            return null;
        }
        Direction support = face.getOpposite();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!canHangOn(level, pos, support)) {
            return null;
        }
        boolean high = context.getClickLocation().y - pos.getY() > 0.5;
        boolean powered = level.hasNeighborSignal(pos);
        return defaultBlockState()
                .setValue(FACING, support)
                .setValue(HALF, high ? Half.TOP : Half.BOTTOM)
                .setValue(OPEN, powered)
                .setValue(POWERED, powered)
                .setValue(WATERLOGGED, placedInWater(context));
    }

    /**
     * Whether the block on the {@code support} side holds a hatch: a solid face, or as the original glowstone,
     * Carpenter's blocks and stairs, and any slab or stairs.
     */
    public static boolean canHangOn(BlockGetter level, BlockPos pos, Direction support) {
        BlockPos supportPos = pos.relative(support);
        BlockState state = level.getBlockState(supportPos);
        Block block = state.getBlock();
        return block == Blocks.GLOWSTONE || block instanceof CarpentersStairsBlock
                || block instanceof CarpentersBlockBlock || block instanceof SlabBlock || block instanceof StairBlock
                || state.isFaceSturdy(level, supportPos, support.getOpposite());
    }

    /**
     * Turns the hatch to the next side that can hold it, as the original's {@code findNextSideSupportBlock}; it
     * drops when no side can.
     */
    private static void moveToNextSupport(Level level, BlockPos pos, BlockState state) {
        int current = 0;
        for (int i = 0; i < SUPPORT_ORDER.length; ++i) {
            if (SUPPORT_ORDER[i] == state.getValue(FACING)) {
                current = i;
            }
        }
        for (int step = 1; step <= SUPPORT_ORDER.length; ++step) {
            Direction support = SUPPORT_ORDER[(current + step) % SUPPORT_ORDER.length];
            if (canHangOn(level, pos, support)) {
                if (support != state.getValue(FACING)) {
                    level.setBlock(pos, state.setValue(FACING, support), Block.UPDATE_ALL);
                }
                return;
            }
        }
        level.destroyBlock(pos, true);
    }

    // Opening

    private static void setOpen(Level level, BlockPos pos, BlockState state, @Nullable Player player, boolean open) {
        level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        Openables.playToggle(level, pos, player, open);
    }

    @Override
    protected void postOnBlockActivated(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, InteractionHand hand, BlockHitResult hit, ActionResult result) {
        if (state.getValue(RIGID)) {
            return;
        }
        if (!level.isClientSide) {
            setOpen(level, pos, state, player, !state.getValue(OPEN));
        }
        result.setAltered().setNoSound();
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
        if (!canHangOn(level, pos, state.getValue(FACING))) {
            moveToNextSupport(level, pos, state);
            state = level.getBlockState(pos);
            if (!state.is(this)) {
                return;
            }
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            state = state.setValue(POWERED, powered);
            if (powered != state.getValue(OPEN)) {
                setOpen(level, pos, state, null, powered);
            } else {
                level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            }
        }
    }

    // Hammer

    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        moveToNextSupport(level, pos, state);
        return true;
    }

    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        if (level.isClientSide) {
            return true;
        }
        if (player.isSecondaryUseActive()) {
            boolean rigid = !state.getValue(RIGID);
            level.setBlock(pos, state.setValue(RIGID, rigid), Block.UPDATE_ALL);
            Openables.rigidityMessage(player, rigid);
        } else {
            level.setBlock(pos, state.setValue(TYPE, state.getValue(TYPE).next()), Block.UPDATE_ALL);
        }
        return true;
    }

    // Climbing

    /**
     * An open hidden hatch at the top of its space is a ladder, as the original. Overrides NeoForge's block
     * extension; Fabric calls it from a mixin.
     */
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return state.getValue(TYPE) == HatchType.HIDDEN && isHigh(state) && state.getValue(OPEN);
    }

    // Shapes

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.computeIfAbsent(state, s -> HatchGeometry.shape(s.getValue(TYPE), isHigh(s),
                s.getValue(OPEN), s.getValue(FACING)).shape());
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
        return switch (type) {
            case LAND, AIR -> state.getValue(OPEN);
            case WATER -> state.getValue(WATERLOGGED);
        };
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
