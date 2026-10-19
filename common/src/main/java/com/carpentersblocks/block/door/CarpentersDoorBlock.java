package com.carpentersblocks.block.door;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;

/**
 * Carpenter's Door: a two-block door in six styles that opens by hand (or only by redstone when rigid) and opens
 * together with a matching door beside it. The hammer swaps the hinge (left-click), changes the style (right-click)
 * or the rigidity (sneak right-click). Port of the original {@code BlockHinged} and {@code BlockCarpentersDoor}, with
 * {@code ItemCarpentersDoor}'s placement.
 * <p>
 * The state uses vanilla's door property names. {@link #FACING} is the way the placer looked, so a closed door lies
 * against the face away from them, as the original's; the hinge side is seen from the placer.
 */
public class CarpentersDoorBlock extends CoverableBlock {

    public static final MapCodec<CarpentersDoorBlock> CODEC = simpleCodec(CarpentersDoorBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    /** Modern addition: like vanilla's doors, redstone toggles the door only when its signal changes. */
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<DoorType> TYPE = EnumProperty.create("type", DoorType.class);
    public static final BooleanProperty RIGID = Openables.RIGID;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            SHAPES.put(side, DoorGeometry.shape(side).shape());
        }
    }

    public CarpentersDoorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(HINGE, DoorHingeSide.LEFT)
                .setValue(OPEN, false)
                .setValue(POWERED, false)
                .setValue(TYPE, DoorType.GLASS_TOP)
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
        builder.add(FACING, HALF, HINGE, OPEN, POWERED, TYPE, RIGID);
    }

    public static boolean isLower(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    public static boolean hingeLeft(BlockState state) {
        return state.getValue(HINGE) == DoorHingeSide.LEFT;
    }

    /** The face the door lies against now. */
    public static Direction side(BlockState state) {
        return DoorGeometry.side(state.getValue(FACING), state.getValue(OPEN), hingeLeft(state));
    }

    private static Direction toOtherHalf(BlockState state) {
        return isLower(state) ? Direction.UP : Direction.DOWN;
    }

    private static boolean isDoor(BlockState state) {
        return state.getBlock() instanceof CarpentersDoorBlock;
    }

    // Placement, as ItemCarpentersDoor: on top of a block with a solid top, with room for both halves

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos below = pos.below();
        if (!context.replacingClickedOnBlock() && context.getClickedFace() != Direction.UP
                || pos.getY() >= level.getMaxBuildHeight() - 1
                || !level.getBlockState(pos.above()).canBeReplaced(context)
                || !level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return null;
        }
        Direction facing = context.getHorizontalDirection();
        boolean powered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
        BlockState state = defaultBlockState()
                .setValue(FACING, facing)
                .setValue(HINGE, hingeFor(level, pos, facing))
                .setValue(OPEN, powered)
                .setValue(POWERED, powered)
                .setValue(WATERLOGGED, placedInWater(context));
        // Style and rigidity follow the first neighboring door, as the original
        for (Direction dir : new Direction[] { Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH }) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (isDoor(neighbor)) {
                state = state.setValue(TYPE, neighbor.getValue(TYPE)).setValue(RIGID, neighbor.getValue(RIGID));
                break;
            }
        }
        return state;
    }

    /** The hinge goes on the right when that makes a double door with a left-hinged door beside it. */
    private static DoorHingeSide hingeFor(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState neighbor = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        return isDoor(neighbor) && isLower(neighbor) && neighbor.getValue(FACING) == facing && hingeLeft(neighbor)
                ? DoorHingeSide.RIGHT
                : DoorHingeSide.LEFT;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) {
            return;
        }
        BlockPos upper = pos.above();
        boolean water = level.getFluidState(upper).getType() == Fluids.WATER;
        level.setBlock(upper, state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(WATERLOGGED, water),
                Block.UPDATE_ALL);
        CoverableBlockEntity be = blockEntity(level, upper);
        if (be != null && placer instanceof Player player) {
            be.setOwner(player.getUUID());
        }
    }

    /** A half without its other half goes, without dropping anything, as the original's orphan check. */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        state = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (direction == toOtherHalf(state)
                && !(isDoor(neighborState) && neighborState.getValue(HALF) != state.getValue(HALF))) {
            // Destroying keeps the water of a waterlogged half
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    /** In creative, breaking the upper half takes the lower one without dropping it, as vanilla's doors. */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && !isLower(state)) {
            BlockPos lower = pos.below();
            BlockState lowerState = level.getBlockState(lower);
            if (isDoor(lowerState) && isLower(lowerState)) {
                BlockState replacement = lowerState.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState()
                        : Blocks.AIR.defaultBlockState();
                level.setBlock(lower, replacement, Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, lower, Block.getId(lowerState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    // Opening

    /**
     * The pieces that open together, as the original's {@code getHingePieces}: this half, the other half, and both
     * halves of a door beside it facing the same way with the opposite hinge.
     */
    public static List<BlockPos> hingePieces(BlockGetter level, BlockPos pos, BlockState state) {
        List<BlockPos> pieces = new ArrayList<>();
        pieces.add(pos);
        Direction toOther = toOtherHalf(state);
        if (!isDoor(level.getBlockState(pos.relative(toOther)))) {
            return pieces;
        }
        pieces.add(pos.relative(toOther));
        Direction hinge = hingeLeft(state) ? state.getValue(FACING).getCounterClockWise()
                : state.getValue(FACING).getClockWise();
        BlockPos partner = pos.relative(hinge.getOpposite());
        BlockState partnerState = level.getBlockState(partner);
        if (isDoor(partnerState) && partnerState.getValue(HALF) == state.getValue(HALF)
                && partnerState.getValue(FACING) == state.getValue(FACING)
                && partnerState.getValue(HINGE) != state.getValue(HINGE)) {
            pieces.add(partner);
            if (isDoor(level.getBlockState(partner.relative(toOther)))) {
                pieces.add(partner.relative(toOther));
            }
        }
        return pieces;
    }

    /** Sets a property on each piece that is still a door. */
    private static <T extends Comparable<T>> void setOnPieces(Level level, List<BlockPos> pieces,
            Property<T> property, T value) {
        for (BlockPos piece : pieces) {
            BlockState state = level.getBlockState(piece);
            if (isDoor(state) && !state.getValue(property).equals(value)) {
                level.setBlock(piece, state.setValue(property, value), Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
            }
        }
    }

    private static void setOpen(Level level, BlockPos pos, BlockState state, @Nullable Player player, boolean open) {
        setOnPieces(level, hingePieces(level, pos, state), OPEN, open);
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

    /**
     * Redstone on any of the pieces opens them all, as the original; the original's foundation check drops a lower
     * half left without a solid top under it.
     */
    /** A wind charge swings the door, as vanilla's wooden doors; a rigid door stays put, as an iron one. */
    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion,
            BiConsumer<ItemStack, BlockPos> drops) {
        if (!level.isClientSide && !state.getValue(RIGID) && !state.getValue(POWERED)
                && Openables.windToggles(explosion, hingePieces(level, pos, state))) {
            setOpen(level, pos, state, null, !state.getValue(OPEN));
        }
        super.onExplosionHit(state, level, pos, explosion, drops);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide) {
            return;
        }
        state = level.getBlockState(pos);
        if (!isDoor(state)) {
            return;
        }
        BlockPos below = pos.below();
        if (isLower(state) && !level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            level.destroyBlock(pos, true);
            return;
        }
        List<BlockPos> pieces = hingePieces(level, pos, state);
        boolean powered = false;
        for (BlockPos piece : pieces) {
            powered |= level.hasNeighborSignal(piece);
        }
        if (powered != state.getValue(POWERED)) {
            setOnPieces(level, pieces, POWERED, powered);
            if (powered != state.getValue(OPEN)) {
                setOpen(level, pos, state, null, powered);
            }
        }
    }

    // Hammer

    /** Swaps the hinge side of both halves. */
    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        DoorHingeSide hinge = hingeLeft(state) ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
        setOnPieces(level, List.of(pos, pos.relative(toOtherHalf(state))), HINGE, hinge);
        return true;
    }

    /** Cycles the style of both halves, or (sneaking) the rigidity of every piece. */
    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        if (level.isClientSide) {
            return true;
        }
        if (player.isSecondaryUseActive()) {
            boolean rigid = !state.getValue(RIGID);
            setOnPieces(level, hingePieces(level, pos, state), RIGID, rigid);
            Openables.rigidityMessage(player, rigid);
        } else {
            setOnPieces(level, List.of(pos, pos.relative(toOtherHalf(state))), TYPE, state.getValue(TYPE).next());
        }
        return true;
    }

    // Shapes

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(side(state));
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
            case WATER -> false;
        };
    }

    // Structures

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return mirror == Mirror.NONE ? state
                : state.rotate(mirror.getRotation(state.getValue(FACING))).cycle(HINGE);
    }
}
