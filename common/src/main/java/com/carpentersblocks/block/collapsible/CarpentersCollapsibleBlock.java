package com.carpentersblocks.block.collapsible;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.collapsible.CollapsibleGeometry.Depths;
import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.config.CBConfig;

/**
 * The Carpenter's Collapsible Block: a block whose four corners the hammer raises and lowers a pixel at a time, for
 * smooth terrain. It stands on the floor ({@link #FACING} up) or hangs from the ceiling (down); the corner depths are
 * the block data ({@link CollapsibleGeometry}). Port of the original {@code BlockCarpentersCollapsibleBlock}.
 */
public class CarpentersCollapsibleBlock extends CoverableBlock {

    public static final MapCodec<CarpentersCollapsibleBlock> CODEC = simpleCodec(CarpentersCollapsibleBlock::new);

    /** Up: rests on the floor and rises to the depths. Down: hangs from the ceiling. */
    public static final DirectionProperty FACING = BlockStateProperties.VERTICAL_DIRECTION;

    /** How far placement looks for the ends of a slope, as the original's {@code MAX_HEIGHT}. */
    private static final int SCAN_DISTANCE = 16;
    /** The thinnest outline, so a flat block can still be aimed at. */
    private static final double MIN_OUTLINE = 1;
    /** Thickness of the slabs that make up the support shape. */
    private static final double SUPPORT_THICKNESS = 1;

    /** The shapes depend on the block entity, so the block state can't cache them. */
    public CarpentersCollapsibleBlock(Properties properties) {
        super(properties.dynamicShape());
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
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
        builder.add(FACING);
    }

    public static boolean isPositive(BlockState state) {
        return state.getValue(FACING) == Direction.UP;
    }

    /** The corner depths, or {@code null} without a block entity. */
    @Nullable
    public static Depths depths(BlockGetter level, BlockPos pos) {
        CoverableBlockEntity be = blockEntity(level, pos);
        return be == null ? null : Depths.of(be.getBlockData());
    }

    public static void setDepths(CoverableBlockEntity be, Depths depths) {
        be.setBlockData(depths.pack(be.getBlockData()));
    }

    // Placement

    /** The clicked face decides, as the original's {@code onBlockPlaced}; on a side, the half that was hit does. */
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face.getAxis() != Direction.Axis.Y) {
            double hitY = context.getClickLocation().y - context.getClickedPos().getY();
            face = hitY > 0.5 ? Direction.DOWN : Direction.UP;
        }
        return defaultBlockState().setValue(FACING, face).setValue(WATERLOGGED, placedInWater(context));
    }

    /** Sneaking places a full block; otherwise the corners slope between the ground around it. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CoverableBlockEntity be = blockEntity(level, pos);
        if (level.isClientSide || be == null) {
            return;
        }
        if (placer != null && placer.isShiftKeyDown()) {
            setDepths(be, Depths.FULL);
        } else {
            setDepths(be, slopeFromSurroundings(level, pos, state));
            for (int quad = 0; quad < 4; ++quad) {
                smoothAdjacent(level, pos, state, quad);
            }
        }
    }

    /**
     * Corner depths that run in a straight line between the ground found along each axis, as the original's
     * {@code onBlockPlacedBy}: matching collapsible blocks give their facing corners, other blocks their height.
     */
    private Depths slopeFromSurroundings(Level level, BlockPos pos, BlockState state) {
        int xn = scan(level, pos, Direction.WEST);
        int xp = scan(level, pos, Direction.EAST);
        int zn = scan(level, pos, Direction.NORTH);
        int zp = scan(level, pos, Direction.SOUTH);

        // Heights at the near corners of the block where each scan ended, low then high along the other axis
        int[] hxn = edgeHeights(level, pos.relative(Direction.WEST, xn), state, Direction.EAST);
        int[] hxp = edgeHeights(level, pos.relative(Direction.EAST, xp), state, Direction.WEST);
        int[] hzn = edgeHeights(level, pos.relative(Direction.NORTH, zn), state, Direction.SOUTH);
        int[] hzp = edgeHeights(level, pos.relative(Direction.SOUTH, zp), state, Direction.NORTH);

        double dx1 = (double) (hxp[0] - hxn[0]) / (xp + xn - 1);
        double dx2 = (double) (hxp[1] - hxn[1]) / (xp + xn - 1);
        int nn = Math.max(0, (int) (hxn[0] + dx1 * (xn - 1)));
        int np = Math.max(0, (int) (hxn[1] + dx2 * (xn - 1)));
        int pn = Math.max(0, (int) (hxn[0] + dx1 * xn));
        int pp = Math.max(0, (int) (hxn[1] + dx2 * xn));

        double dz1 = (double) (hzp[0] - hzn[0]) / (zp + zn - 1);
        double dz2 = (double) (hzp[1] - hzn[1]) / (zp + zn - 1);
        nn = Math.max(nn, (int) (hzn[0] + dz1 * (zn - 1)));
        np = Math.max(np, (int) (hzn[0] + dz1 * zn));
        pn = Math.max(pn, (int) (hzn[1] + dz2 * (zn - 1)));
        pp = Math.max(pp, (int) (hzn[1] + dz2 * zn));

        return new Depths(0, 0, 0, 0).with(CollapsibleGeometry.QUAD_XZNN, nn).with(CollapsibleGeometry.QUAD_XZNP, np)
                .with(CollapsibleGeometry.QUAD_XZPN, pn).with(CollapsibleGeometry.QUAD_XZPP, pp);
    }

    /** Blocks to the first slope boundary along {@code dir}, 1 to 15; 1 when there is none. */
    private static int scan(Level level, BlockPos pos, Direction dir) {
        for (int distance = 1; distance < SCAN_DISTANCE; ++distance) {
            if (isSlopeBoundary(level, pos.relative(dir, distance))) {
                return distance;
            }
        }
        return 1;
    }

    /** A Carpenter's block, something solid, or a drop: where a slope ends. */
    @SuppressWarnings("deprecation")
    private static boolean isSlopeBoundary(Level level, BlockPos pos) {
        return blockEntity(level, pos) != null || level.getBlockState(pos).blocksMotion()
                || !level.getBlockState(pos.below()).blocksMotion();
    }

    /**
     * The heights (pixels) of the two corners of the block at {@code pos} on its {@code side} facing us, low then high
     * along the other axis: a matching collapsible block's depths there, otherwise the block's height.
     */
    @SuppressWarnings("deprecation")
    private int[] edgeHeights(Level level, BlockPos pos, BlockState state, Direction side) {
        BlockState other = level.getBlockState(pos);
        Depths depths = matches(state, other) ? depths(level, pos) : null;
        if (depths != null) {
            int[] corners = CollapsibleGeometry.cornersOn(side);
            return new int[] { depths.get(corners[0]), depths.get(corners[1]) };
        }
        int height = 0;
        if (other.blocksMotion()) {
            VoxelShape shape = other.getCollisionShape(level, pos);
            height = shape.isEmpty() ? 0 : (int) (shape.max(Direction.Axis.Y) * 16);
        }
        return new int[] { height, height };
    }

    /** Collapsible blocks smooth into each other when they face the same way, as the original's {@code match}. */
    private boolean matches(BlockState state, BlockState other) {
        return other.is(this) && other.getValue(FACING) == state.getValue(FACING);
    }

    /**
     * Gives the three matching neighbors that meet a corner the same depth, as the original's
     * {@code smoothAdjacentCollapsibles}.
     */
    private void smoothAdjacent(Level level, BlockPos pos, BlockState state, int quad) {
        Depths own = depths(level, pos);
        if (own == null) {
            return;
        }
        int depth = own.get(quad);
        boolean xp = quad == CollapsibleGeometry.QUAD_XZPN || quad == CollapsibleGeometry.QUAD_XZPP;
        boolean zp = quad == CollapsibleGeometry.QUAD_XZNP || quad == CollapsibleGeometry.QUAD_XZPP;
        int dx = xp ? 1 : -1;
        int dz = zp ? 1 : -1;
        int[][] neighbors = { { 0, dz, quad ^ 1 }, { dx, dz, quad ^ 3 }, { dx, 0, quad ^ 2 } };
        for (int[] neighbor : neighbors) {
            BlockPos other = pos.offset(neighbor[0], 0, neighbor[1]);
            CoverableBlockEntity be = blockEntity(level, other);
            if (be != null && matches(state, level.getBlockState(other))) {
                setDepths(be, Depths.of(be.getBlockData()).with(neighbor[2], depth));
            }
        }
    }

    // Hammer

    /** Left-click lowers the corner under the cursor a pixel (raises it, hanging), as the original. */
    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        return adjust(state, level, pos, be, player, isPositive(state) ? -1 : 1);
    }

    /** Right-click raises the corner under the cursor a pixel (lowers it, hanging). */
    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        return adjust(state, level, pos, be, player, isPositive(state) ? 1 : -1);
    }

    private boolean adjust(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be, Player player,
            int step) {
        Vec3 hit = aimedAt(player, pos);
        if (hit == null) {
            return false;
        }
        if (!level.isClientSide) {
            int quad = CollapsibleGeometry.quad(hit.x, hit.z);
            Depths depths = Depths.of(be.getBlockData());
            setDepths(be, depths.with(quad, depths.get(quad) + step));
            smoothAdjacent(level, pos, state, quad);
        }
        return true;
    }

    /**
     * Where the player's view meets the block, relative to it, or {@code null} if they aren't looking at it. Clicks
     * don't carry the hit position to the server, so this traces it, as the original's event handler did.
     */
    @Nullable
    private static Vec3 aimedAt(Player player, BlockPos pos) {
        HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK && ((BlockHitResult) hit).getBlockPos().equals(pos)) {
            return hit.getLocation().subtract(Vec3.atLowerCornerOf(pos));
        }
        return null;
    }

    /** The hammer wears less here, as the original's {@code itemHammerDamageChanceFromCollapsible}. */
    @Override
    protected double toolDamageChance() {
        return CBConfig.itemHammerDamageChanceFromCollapsible;
    }

    // Shapes

    /** The original's selection box: the whole block up to the highest corner. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Depths depths = depths(level, pos);
        if (depths == null) {
            return Shapes.block();
        }
        double height = Math.max(MIN_OUTLINE, depths.max());
        return isPositive(state) ? Block.box(0, 0, 0, 16, height, 16) : Block.box(0, 16 - height, 0, 16, 16, 16);
    }

    /** A box under each corner, as the original's {@code addCollisionBoxesToList}. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        Depths depths = depths(level, pos);
        if (depths == null) {
            return Shapes.block();
        }
        VoxelShape shape = Shapes.empty();
        for (int quad = 0; quad < 4; ++quad) {
            Box box = CollapsibleGeometry.bounds(depths, quad, isPositive(state));
            if (!box.isEmpty()) {
                shape = Shapes.or(shape, box.shape());
            }
        }
        return shape.optimize();
    }

    /**
     * Whole faces for {@link CollapsibleGeometry#isSideSolid}, when the cover is opaque or there is none, as the
     * original's {@code isSideSolid}.
     */
    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        Depths depths = depths(level, pos);
        BlockState cover = baseCover(level, pos);
        if (depths == null || cover != null && !cover.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
            return Shapes.empty();
        }
        VoxelShape shape = Shapes.empty();
        double t = SUPPORT_THICKNESS;
        for (Direction side : Direction.values()) {
            if (CollapsibleGeometry.isSideSolid(depths, isPositive(state), side)) {
                VoxelShape slab = switch (side) {
                    case DOWN -> Block.box(0, 0, 0, 16, t, 16);
                    case UP -> Block.box(0, 16 - t, 0, 16, 16, 16);
                    case NORTH -> Block.box(0, 0, 0, 16, 16, t);
                    case SOUTH -> Block.box(0, 0, 16 - t, 16, 16, 16);
                    case WEST -> Block.box(0, 0, 0, t, 16, 16);
                    case EAST -> Block.box(16 - t, 0, 0, 16, 16, 16);
                };
                shape = Shapes.or(shape, slab);
            }
        }
        return shape.optimize();
    }

    /**
     * Nothing culls against it: the block state doesn't know the depths, and vanilla caches face culling by state.
     */
    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    /** Light passes, as it can't depend on the depths either (the light engine reads it off the main thread). */
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    @Override
    protected boolean canCoverSide(CoverableBlockEntity be, int slot) {
        return slot == CoverData.BASE;
    }
}
