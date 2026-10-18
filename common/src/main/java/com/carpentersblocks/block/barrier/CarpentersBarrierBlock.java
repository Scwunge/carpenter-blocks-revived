package com.carpentersblocks.block.barrier;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;

/**
 * Carpenter's Barrier: a fence in vanilla, picket, shadowbox or wall style that joins neighboring barriers, gates,
 * fences, walls and solid faces. The hammer toggles a forced post (left-click) and changes the style (right-click;
 * sneak for the vanilla sub-types). Port of the original {@code BlockCarpentersBarrier}.
 */
public class CarpentersBarrierBlock extends CoverableBlock {

    public static final MapCodec<CarpentersBarrierBlock> CODEC = simpleCodec(CarpentersBarrierBlock::new);

    public static final EnumProperty<BarrierType> TYPE = EnumProperty.create("type", BarrierType.class);
    /** A post forced by the hammer, which also stops the barrier from joining solid faces. */
    public static final BooleanProperty POST = BooleanProperty.create("post");
    public static final Map<Direction, BooleanProperty> CONNECTIONS = Map.of(
            Direction.NORTH, BlockStateProperties.NORTH,
            Direction.SOUTH, BlockStateProperties.SOUTH,
            Direction.WEST, BlockStateProperties.WEST,
            Direction.EAST, BlockStateProperties.EAST);

    /** Blocks barriers join besides other Carpenter's barriers and gates, fence gates and solid faces. */
    public static final TagKey<Block> CONNECTS_TO = TagKey.create(Registries.BLOCK,
            CarpentersBlocks.id("barrier_connects_to"));

    /** Shapes by connection mask (north 1, south 2, west 4, east 8). */
    private static final VoxelShape[] COLLISION = new VoxelShape[16];
    private static final VoxelShape[] OUTLINE_VANILLA = new VoxelShape[16];
    private static final VoxelShape[] OUTLINE_WIDE = new VoxelShape[16];

    static {
        for (int mask = 0; mask < 16; ++mask) {
            boolean n = (mask & 1) != 0;
            boolean s = (mask & 2) != 0;
            boolean w = (mask & 4) != 0;
            boolean e = (mask & 8) != 0;

            // As the original: up to two bars, 1.5 blocks tall so nothing jumps over
            VoxelShape collision = Shapes.empty();
            if (n || s) {
                collision = Block.box(6, 0, n ? 0 : 6, 10, 24, s ? 16 : 10);
            }
            if (w || e || !n && !s) {
                collision = Shapes.or(collision, Block.box(w ? 0 : 6, 0, 6, e ? 16 : 10, 24, 10));
            }
            COLLISION[mask] = collision.optimize();

            // As the original: one box around the post and every connected side
            OUTLINE_VANILLA[mask] = Block.box(w ? 0 : 6, 0, n ? 0 : 6, e ? 16 : 10, 16, s ? 16 : 10);
            double x1 = w ? 0 : 4;
            double x2 = e ? 16 : 12;
            double z1 = n ? 0 : 4;
            double z2 = s ? 16 : 12;
            if (n && s && !w && !e) {
                x1 = 5;
                x2 = 11;
            } else if (!n && !s && w && e) {
                z1 = 5;
                z2 = 11;
            }
            OUTLINE_WIDE[mask] = Block.box(x1, 0, z1, x2, 16, z2);
        }
    }

    public CarpentersBarrierBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any()
                .setValue(TYPE, BarrierType.VANILLA)
                .setValue(POST, false)
                .setValue(LIGHT, 0)
                .setValue(WATERLOGGED, false);
        for (BooleanProperty connection : CONNECTIONS.values()) {
            state = state.setValue(connection, false);
        }
        registerDefaultState(state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE, POST);
        CONNECTIONS.values().forEach(builder::add);
    }

    // Connections

    /** The horizontal sides this barrier is joined on. */
    public static Set<Direction> connections(BlockState state) {
        Set<Direction> sides = EnumSet.noneOf(Direction.class);
        CONNECTIONS.forEach((side, property) -> {
            if (state.getValue(property)) {
                sides.add(side);
            }
        });
        return sides;
    }

    private static int mask(BlockState state) {
        int mask = 0;
        int bit = 1;
        for (Direction side : BarrierGeometry.SIDES) {
            if (state.getValue(CONNECTIONS.get(side))) {
                mask |= bit;
            }
            bit <<= 1;
        }
        return mask;
    }

    /**
     * Whether a barrier joins the block at {@code neighborPos}, which lies towards {@code side}: Carpenter's
     * barriers and gates always, fences and walls by tag, fence gates facing the right way, and otherwise any solid
     * face unless a post is forced.
     */
    public static boolean connectsTo(BlockGetter level, BlockPos neighborPos, Direction side, boolean forcedPost) {
        BlockState neighbor = level.getBlockState(neighborPos);
        Block block = neighbor.getBlock();
        if (block instanceof CarpentersBarrierBlock || block instanceof CarpentersGateBlock
                || neighbor.is(CONNECTS_TO)) {
            return true;
        }
        if (block instanceof FenceGateBlock && side.getAxis().isHorizontal()) {
            return FenceGateBlock.connectsToDirection(neighbor, side);
        }
        return !forcedPost && !isExceptionForConnection(neighbor)
                && neighbor.isFaceSturdy(level, neighborPos, side.getOpposite());
    }

    private static BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
        for (Map.Entry<Direction, BooleanProperty> entry : CONNECTIONS.entrySet()) {
            Direction side = entry.getKey();
            state = state.setValue(entry.getValue(),
                    connectsTo(level, pos.relative(side), side, state.getValue(POST)));
        }
        return state;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = defaultBlockState().setValue(WATERLOGGED, placedInWater(context))
                .setValue(TYPE, adjacentType(level, pos, BarrierType.VANILLA));
        return withConnections(state, level, pos);
    }

    /** The style of an adjacent barrier or gate, so new ones match; the last found wins, as in the original. */
    static BarrierType adjacentType(BlockGetter level, BlockPos pos, BarrierType fallback) {
        BarrierType type = fallback;
        for (Direction dir : Direction.values()) {
            BlockState adjacent = level.getBlockState(pos.relative(dir));
            if (adjacent.getBlock() instanceof CarpentersBarrierBlock) {
                type = adjacent.getValue(TYPE);
            } else if (adjacent.getBlock() instanceof CarpentersGateBlock) {
                type = adjacent.getValue(CarpentersGateBlock.TYPE);
            }
        }
        return type;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        state = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        BooleanProperty connection = CONNECTIONS.get(direction);
        return connection == null ? state
                : state.setValue(connection, connectsTo(level, neighborPos, direction, state.getValue(POST)));
    }

    // Shapes

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int mask = mask(state);
        return state.getValue(TYPE).isVanilla() ? OUTLINE_VANILLA[mask] : OUTLINE_WIDE[mask];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return COLLISION[mask(state)];
    }

    /** Torches and the like stand on the top, as the original's solid top face allowed. */
    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
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
        return false;
    }

    // Hammer

    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        if (!level.isClientSide) {
            BlockState toggled = state.setValue(POST, !state.getValue(POST));
            level.setBlock(pos, withConnections(toggled, level, pos), Block.UPDATE_ALL);
        }
        return true;
    }

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

    /** What the barrier's look depends on in the world around it; see {@link BarrierGeometry.Surroundings}. */
    public static BarrierGeometry.Surroundings surroundings(BlockGetter level, BlockPos pos) {
        BlockPos above = pos.above();
        Set<Direction> connectedAbove = EnumSet.noneOf(Direction.class);
        for (Direction side : BarrierGeometry.SIDES) {
            if (connectsTo(level, above.relative(side), side, false)) {
                connectedAbove.add(side);
            }
        }
        return new BarrierGeometry.Surroundings(connectedAbove, connectsTo(level, above, Direction.UP, false),
                isBarrier(level.getBlockState(pos.below())), isBarrier(level.getBlockState(above)),
                postInColumn(level, pos));
    }

    private static boolean isBarrier(BlockState state) {
        return state.getBlock() instanceof CarpentersBarrierBlock;
    }

    /** Whether any barrier in the unbroken column through {@code pos} has a post, as the original's isPostAt. */
    static boolean postInColumn(BlockGetter level, BlockPos pos) {
        for (int step : new int[] { 1, -1 }) {
            BlockPos.MutableBlockPos cursor = pos.mutable();
            while (!level.isOutsideBuildHeight(cursor) && isBarrier(level.getBlockState(cursor))) {
                if (hasPost(level, cursor)) {
                    return true;
                }
                cursor.move(0, step, 0);
            }
        }
        return false;
    }

    /**
     * Whether the barrier at {@code pos} needs a post of its own: when forced, under a torch or flower pot, beside a
     * gate, at an end, crossing or corner, as the original's hasPost.
     */
    static boolean hasPost(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getValue(POST)) {
            return true;
        }
        BlockPos abovePos = pos.above();
        BlockState above = level.getBlockState(abovePos);
        if (!above.isAir() && above.getFluidState().isEmpty()
                && (above.is(BlockTags.FLOWER_POTS) || above.getCollisionShape(level, abovePos).isEmpty())) {
            return true;
        }
        for (Direction side : BarrierGeometry.SIDES) {
            if (level.getBlockState(pos.relative(side)).getBlock() instanceof CarpentersGateBlock) {
                return true;
            }
        }
        boolean n = state.getValue(BlockStateProperties.NORTH);
        boolean s = state.getValue(BlockStateProperties.SOUTH);
        boolean w = state.getValue(BlockStateProperties.WEST);
        boolean e = state.getValue(BlockStateProperties.EAST);
        return (w && e) == (n && s) || (w || e) && (n || s);
    }

    // Structures

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState rotated = state;
        for (Map.Entry<Direction, BooleanProperty> entry : CONNECTIONS.entrySet()) {
            rotated = rotated.setValue(CONNECTIONS.get(rotation.rotate(entry.getKey())), state.getValue(entry.getValue()));
        }
        return rotated;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = state;
        for (Map.Entry<Direction, BooleanProperty> entry : CONNECTIONS.entrySet()) {
            mirrored = mirrored.setValue(CONNECTIONS.get(mirror.mirror(entry.getKey())), state.getValue(entry.getValue()));
        }
        return mirrored;
    }
}
