package com.carpentersblocks.block.stairs;

import java.util.Map;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.config.CBConfig;

/**
 * Carpenter's Stairs: upright, upside-down, side, interior and exterior corner stairs that join up with each other
 * when placed. The hammer turns them (left-click) and changes their kind (right-click). Port of the original
 * {@code BlockCarpentersStairs}; the stairs ID moved from the tile entity into the block state.
 */
public class CarpentersStairsBlock extends CoverableBlock {

    public static final MapCodec<CarpentersStairsBlock> CODEC = simpleCodec(CarpentersStairsBlock::new);

    public static final EnumProperty<StairsShape> SHAPE = EnumProperty.create("shape", StairsShape.class);
    /** True when every face is covered by an opaque block, so neighbors may cull against it. */
    public static final BooleanProperty SOLID = BooleanProperty.create("solid");

    public CarpentersStairsBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(SHAPE, StairsShape.POS_N)
                .setValue(SOLID, false)
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
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return state.getValue(SOLID);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(SOLID);
    }

    // Placement

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Vec3 hit = context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos));
        StairsShape shape = StairsShape.forPlacement(context.getClickedFace(), Mth.clamp(hit.x, 0, 1),
                Mth.clamp(hit.y, 0, 1), Mth.clamp(hit.z, 0, 1), context.getHorizontalDirection());

        // Sneaking skips joining up with neighbors, as in the original
        if (!context.isSecondaryUseActive()) {
            Level level = context.getLevel();
            shape = StairsTransform.transform(shape, dir -> stairsAt(level, pos.relative(dir)));
        }
        return defaultBlockState().setValue(SHAPE, shape).setValue(WATERLOGGED, placedInWater(context));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide || placer != null && placer.isShiftKeyDown()) {
            return;
        }
        Map<Direction, StairsShape> changes = StairsTransform.transformAdjacent(state.getValue(SHAPE),
                dir -> stairsAt(level, pos.relative(dir)));
        changes.forEach((dir, shape) -> {
            BlockPos neighbor = pos.relative(dir);
            level.setBlock(neighbor, level.getBlockState(neighbor).setValue(SHAPE, shape), Block.UPDATE_ALL);
        });
    }

    /** The shape of the Carpenter's Stairs at {@code pos}, or {@code null} if there are none. */
    @Nullable
    private static StairsShape stairsAt(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof CarpentersStairsBlock ? state.getValue(SHAPE) : null;
    }

    // Hammer

    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        return setShape(level, pos, state, state.getValue(SHAPE).nextDirection());
    }

    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        return setShape(level, pos, state, state.getValue(SHAPE).nextType());
    }

    private static boolean setShape(Level level, BlockPos pos, BlockState state, StairsShape shape) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(SHAPE, shape), Block.UPDATE_ALL);
        }
        return true;
    }

    @Override
    protected double toolDamageChance() {
        return CBConfig.itemHammerDamageChanceFromStairs;
    }

    // Covers

    @Override
    protected boolean canCoverSide(CoverableBlockEntity be, int slot) {
        return true;
    }

    @Override
    public BlockState stateForCovers(BlockState state, CoverData data) {
        boolean solid = true;
        for (Direction face : Direction.values()) {
            BlockState cover = data.coverFor(face);
            if (cover == null || !cover.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
                solid = false;
                break;
            }
        }
        return super.stateForCovers(state, data).setValue(SOLID, solid);
    }

    // Structures

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(SHAPE, state.getValue(SHAPE).rotate(rotation));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(SHAPE, state.getValue(SHAPE).mirror(mirror));
    }
}
