package com.carpentersblocks.block.door;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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
import com.carpentersblocks.config.CBConfig;

/**
 * Carpenter's Garage Door: hangs from a block with a solid bottom and fills the space below it down to the ground.
 * Every piece of a door, and the doors beside it facing the same way, open and close together; an open door rolls
 * up into its top piece (the host). The hammer cycles the style (left-click forwards, right-click backwards) or the
 * rigidity (sneak right-click). Port of the original {@code BlockCarpentersGarageDoor} and {@code GarageDoor}.
 * <p>
 * {@link #FACING} is the way the placer looked; the door hangs on the side of its space nearest them.
 */
public class CarpentersGarageDoorBlock extends CoverableBlock {

    public static final MapCodec<CarpentersGarageDoorBlock> CODEC = simpleCodec(CarpentersGarageDoorBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<GarageDoorType> TYPE = EnumProperty.create("type", GarageDoorType.class);
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    /** Modern addition: whether this piece had redstone at its last check, so only a change of signal acts. */
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty RIGID = Openables.RIGID;
    /** The topmost piece, which drops the door and holds the rolled-up door when open. */
    public static final BooleanProperty HOST = BooleanProperty.create("host");

    private static final Map<BlockState, VoxelShape> SHAPES = new ConcurrentHashMap<>();

    public CarpentersGarageDoorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, GarageDoorType.DEFAULT)
                .setValue(OPEN, false)
                .setValue(POWERED, false)
                .setValue(RIGID, false)
                .setValue(HOST, false)
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
        builder.add(FACING, TYPE, OPEN, POWERED, RIGID, HOST);
    }

    private static boolean isGarageDoor(BlockState state) {
        return state.getBlock() instanceof CarpentersGarageDoorBlock;
    }

    /** An open door only shows (and can only be touched at) its top piece. */
    public static boolean isVisible(BlockState state) {
        return !state.getValue(OPEN) || state.getValue(HOST);
    }

    // The door as a whole

    /**
     * The pieces that act as one, as the original's {@code getBlocks}: pieces above, below and beside each other in
     * the door's plane whose facing matches, up to the configured size limit. Facing is matched as the original
     * matched its data bits, so a door also joins a door beside it facing the opposite way in some directions.
     */
    public static Set<BlockPos> pieces(BlockGetter level, BlockPos pos, BlockState state) {
        int match = state.getValue(FACING).get3DDataValue();
        Direction plane = state.getValue(FACING).getClockWise();
        Direction[] dirs = { Direction.UP, Direction.DOWN, plane, plane.getOpposite() };
        Set<BlockPos> pieces = new LinkedHashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(pos);
        while (!queue.isEmpty() && pieces.size() <= CBConfig.multiBlockSizeLimit) {
            BlockPos piece = queue.poll();
            if (!pieces.add(piece)) {
                continue;
            }
            for (Direction dir : dirs) {
                BlockPos next = piece.relative(dir);
                BlockState other = level.getBlockState(next);
                if (!pieces.contains(next) && isGarageDoor(other)
                        && (other.getValue(FACING).get3DDataValue() & match) == match) {
                    queue.add(next);
                }
            }
        }
        return pieces;
    }

    public static BlockPos topmost(BlockGetter level, BlockPos pos) {
        while (isGarageDoor(level.getBlockState(pos.above()))) {
            pos = pos.above();
        }
        return pos;
    }

    public static BlockPos bottommost(BlockGetter level, BlockPos pos) {
        while (isGarageDoor(level.getBlockState(pos.below()))) {
            pos = pos.below();
        }
        return pos;
    }

    public static boolean isBottommost(BlockGetter level, BlockPos pos) {
        return !isGarageDoor(level.getBlockState(pos.below()));
    }

    private static <T extends Comparable<T>> void setOnPieces(Level level, Set<BlockPos> pieces,
            Property<T> property, T value) {
        for (BlockPos piece : pieces) {
            BlockState state = level.getBlockState(piece);
            if (isGarageDoor(state) && !state.getValue(property).equals(value)) {
                level.setBlock(piece, state.setValue(property, value), Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
            }
        }
    }

    private static void setOpen(Level level, BlockPos pos, BlockState state, @Nullable Player player, boolean open) {
        setOnPieces(level, pieces(level, pos, state), OPEN, open);
        Openables.playToggle(level, pos, player, open);
    }

    // Placement

    /** As the original's {@code canPlaceBlockAt}: under a solid bottom face, or under another garage door piece. */
    private static boolean canHangAt(BlockGetter level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        return isGarageDoor(aboveState) || aboveState.isFaceSturdy(level, above, Direction.DOWN);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!canHangAt(level, pos)) {
            return null;
        }
        Direction facing = context.getHorizontalDirection();
        BlockState state = defaultBlockState()
                .setValue(FACING, facing)
                .setValue(HOST, true)
                .setValue(POWERED, level.hasNeighborSignal(pos))
                .setValue(WATERLOGGED, placedInWater(context));
        // A door beside this one, at this height or further down, lends its style, rigidity and state
        BlockState reference = findReferencePiece(level, pos, facing);
        if (reference != null) {
            state = state.setValue(TYPE, reference.getValue(TYPE)).setValue(RIGID, reference.getValue(RIGID))
                    .setValue(OPEN, reference.getValue(OPEN));
        }
        return state;
    }

    /** The original's {@code findReferencePiece}: a piece beside the column, searching down through air. */
    @Nullable
    private static BlockState findReferencePiece(Level level, BlockPos pos, Direction facing) {
        Direction dir = facing.getClockWise();
        BlockPos at = pos;
        while (true) {
            for (BlockPos side : new BlockPos[] { at.relative(dir), at.relative(dir.getOpposite()) }) {
                BlockState other = level.getBlockState(side);
                if (isGarageDoor(other) && other.getValue(FACING) == facing) {
                    return other;
                }
            }
            if (at.getY() <= level.getMinBuildHeight() + 1 || !level.getBlockState(at.below()).isAir()) {
                return null;
            }
            at = at.below();
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            fillBelow(level, pos, state);
        }
    }

    /** The original's {@code create}: pieces like this one down to the first block that isn't replaceable. */
    private static void fillBelow(Level level, BlockPos pos, BlockState state) {
        CoverableBlockEntity source = blockEntity(level, pos);
        BlockState piece = state.setValue(HOST, false);
        for (BlockPos at = pos.below(); at.getY() >= level.getMinBuildHeight() && level.getBlockState(at).canBeReplaced()
                && canHangAt(level, at); at = at.below()) {
            boolean water = level.getFluidState(at).getType() == Fluids.WATER;
            level.setBlock(at, piece.setValue(WATERLOGGED, water), Block.UPDATE_ALL);
            CoverableBlockEntity be = blockEntity(level, at);
            if (be != null && source != null) {
                be.setOwner(source.getOwner());
            }
        }
    }

    // Breaking: the whole column goes, and only the top piece drops the door

    /**
     * Destroys the column through {@code pos} (but not {@code pos} itself), passing through air as the original's
     * {@code destroyInDirection}. Pieces above may drop the door when {@code drop} is set; pieces below never do.
     */
    private void destroyColumn(Level level, BlockPos pos, boolean drop) {
        for (BlockPos at = pos.above(); at.getY() < level.getMaxBuildHeight(); at = at.above()) {
            BlockState state = level.getBlockState(at);
            if (state.is(this)) {
                level.destroyBlock(at, drop);
            } else if (!state.isAir()) {
                break;
            }
        }
        for (BlockPos at = pos.below(); at.getY() >= level.getMinBuildHeight(); at = at.below()) {
            BlockState state = level.getBlockState(at);
            if (state.is(this)) {
                level.destroyBlock(at, false);
            } else if (!state.isAir()) {
                break;
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            destroyColumn(level, pos, !player.isCreative());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** An explosion takes the entire column, as the original. */
    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        super.wasExploded(level, pos, explosion);
        if (!level.isClientSide) {
            destroyColumn(level, pos, true);
        }
    }

    /** Rolled-up pieces can't be blown apart, as the original gave them bedrock's resistance. */
    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        if (state.getValue(OPEN) && !state.getValue(HOST)) {
            return Blocks.BEDROCK.getExplosionResistance();
        }
        return super.getExplosionResistance(state, level, pos, explosion);
    }

    // Opening

    /** An open door shows the bottom piece's attributes on its top piece, so clicks change those. */
    @Override
    protected CoverableBlockEntity getBlockEntityForActivation(CoverableBlockEntity be) {
        Level level = be.getLevel();
        if (level == null || !be.getBlockState().getValue(OPEN)) {
            return be;
        }
        CoverableBlockEntity bottom = blockEntity(level, bottommost(level, be.getBlockPos()));
        return bottom == null ? be : bottom;
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
    public void onClientAttributesChanged(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(OPEN)) {
            BlockPos top = topmost(level, pos);
            BlockState topState = level.getBlockState(top);
            level.sendBlockUpdated(top, topState, topState, Block.UPDATE_IMMEDIATE);
        }
    }

    /** A wind charge rolls the whole door up or down, once however many pieces it reaches; a rigid door stays put. */
    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion,
            BiConsumer<ItemStack, BlockPos> drops) {
        if (!level.isClientSide && !state.getValue(RIGID) && !state.getValue(POWERED)) {
            Set<BlockPos> pieces = pieces(level, pos, state);
            if (Openables.windToggles(explosion, pieces)) {
                setOpen(level, pos, state, null, !state.getValue(OPEN));
            }
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
        if (!isGarageDoor(state)) {
            return;
        }
        if (state.getValue(HOST) && !canHangAt(level, pos)) {
            destroyColumn(level, pos, true);
            level.destroyBlock(pos, true);
            return;
        }

        // Redstone on any piece opens the door; it closes once no piece has any
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            state = state.setValue(POWERED, powered);
            level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
            if (powered && !state.getValue(OPEN)) {
                setOpen(level, pos, state, null, true);
            } else if (!powered && state.getValue(OPEN)) {
                boolean anyPowered = false;
                for (BlockPos piece : pieces(level, pos, state)) {
                    anyPowered |= level.hasNeighborSignal(piece);
                }
                if (!anyPowered) {
                    setOpen(level, pos, state, null, false);
                }
            }
        }

        // Fill the gap when a block under the door goes
        if (CBConfig.enableGarageDoorVoidFill && !(neighborBlock instanceof CarpentersGarageDoorBlock)
                && level.getBlockState(pos.below()).isAir()) {
            fillBelow(level, pos, level.getBlockState(pos));
        }
    }

    // Hammer

    private void setTypeOnPieces(Level level, BlockPos pos, BlockState state, GarageDoorType type) {
        setOnPieces(level, pieces(level, pos, state), TYPE, type);
    }

    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        setTypeOnPieces(level, pos, state, state.getValue(TYPE).next());
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
            Openables.rigidityMessage(player, rigid);
            setOnPieces(level, pieces(level, pos, state), RIGID, rigid);
        } else {
            setTypeOnPieces(level, pos, state, state.getValue(TYPE).previous());
        }
        return true;
    }

    // Shapes: pieces rolled up into the top piece can't be touched or bumped into

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!isVisible(state)) {
            return Shapes.empty();
        }
        return SHAPES.computeIfAbsent(state, s -> GarageDoorGeometry.shape(s.getValue(TYPE),
                s.getValue(OPEN) && s.getValue(HOST), s.getValue(FACING)).shape());
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
            case LAND, AIR -> !isVisible(state);
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
