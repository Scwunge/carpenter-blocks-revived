package com.carpentersblocks.block.bed;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.AttributeItems;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.design.DesignList;
import com.carpentersblocks.config.CBConfig;

/**
 * Carpenter's Bed: two halves, each with its own covers, sharing a design and a blanket color. Sleeping, respawning,
 * occupancy and exploding outside the overworld work as vanilla beds. Port of the original
 * {@code BlockCarpentersBed}.
 * <p>
 * The state uses vanilla's bed properties ({@link BedBlock#FACING}, {@link BedBlock#PART}, {@link BedBlock#OCCUPIED})
 * so the game's sleeping code reads it as a bed. NeoForge asks the block through {@link #isBed}; on Fabric the sleep
 * events and a respawn mixin do.
 * <p>
 * The hammer cycles the quilt design (left-click back, right-click forward, sneak right-click removes it). A dye
 * applied while sneaking colors the blanket, as a dye on the original's foot half did; sneak left-click with the
 * hammer takes it back off.
 */
public class CarpentersBedBlock extends CoverableBlock {

    public static final MapCodec<CarpentersBedBlock> CODEC = simpleCodec(CarpentersBedBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    private static final VoxelShape SHAPE = BedGeometry.OUTLINE.shape();

    public CarpentersBedBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, BedPart.FOOT)
                .setValue(OCCUPIED, false)
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
        builder.add(FACING, PART, OCCUPIED);
    }

    public static boolean isHead(BlockState state) {
        return state.getValue(PART) == BedPart.HEAD;
    }

    /** The direction from this half to the other. */
    public static Direction toOtherHalf(BlockState state) {
        Direction facing = state.getValue(FACING);
        return isHead(state) ? facing.getOpposite() : facing;
    }

    // Placement, as vanilla's bed: the foot where the player clicks, the head beyond it

    /** Both halves need room, and (as the original) a solid top under each. */
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos foot = context.getClickedPos();
        BlockPos head = foot.relative(facing);
        Level level = context.getLevel();
        if (!level.getBlockState(head).canBeReplaced(context) || !level.getWorldBorder().isWithinBounds(head)
                || !level.getBlockState(foot.below()).isFaceSturdy(level, foot.below(), Direction.UP)
                || !level.getBlockState(head.below()).isFaceSturdy(level, head.below(), Direction.UP)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, facing).setValue(WATERLOGGED, placedInWater(context));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) {
            return;
        }
        BlockPos head = pos.relative(state.getValue(FACING));
        boolean water = level.getFluidState(head).getType() == Fluids.WATER;
        level.setBlock(head, state.setValue(PART, BedPart.HEAD).setValue(WATERLOGGED, water), Block.UPDATE_ALL);
        level.blockUpdated(pos, Blocks.AIR);
        state.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
        CoverableBlockEntity be = blockEntity(level, head);
        if (be != null && placer instanceof Player player) {
            be.setOwner(player.getUUID());
        }
    }

    /** A half without its other half goes, as vanilla's; the foot follows the head's occupancy. */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        state = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (direction == toOtherHalf(state)) {
            return neighborState.is(this) && neighborState.getValue(PART) != state.getValue(PART)
                    ? state.setValue(OCCUPIED, neighborState.getValue(OCCUPIED))
                    : Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    /** In creative, breaking the foot takes the head without dropping it, as vanilla's. */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && !isHead(state)) {
            BlockPos other = pos.relative(toOtherHalf(state));
            BlockState otherState = level.getBlockState(other);
            if (otherState.is(this) && isHead(otherState)) {
                level.setBlock(other, Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, other, Block.getId(otherState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** The bed stands on legs, so it hides none of the faces around it (the floor under it least of all). */
    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    // Sleeping

    /** NeoForge's bed hook; Fabric's sleep events ask the same. */
    public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, @Nullable LivingEntity sleeper) {
        return true;
    }

    /** Right-click without anything to apply: sleep, as vanilla's bed. */
    @Override
    protected void postOnBlockActivated(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, InteractionHand hand, BlockHitResult hit, ActionResult result) {
        result.setAltered().setNoSound();
        if (level.isClientSide) {
            return;
        }
        if (!isHead(state)) {
            pos = pos.relative(state.getValue(FACING));
            state = level.getBlockState(pos);
            if (!state.is(this)) {
                return;
            }
        }
        if (!BedBlock.canSetSpawn(level)) {
            level.removeBlock(pos, false);
            BlockPos foot = pos.relative(toOtherHalf(state));
            if (level.getBlockState(foot).is(this)) {
                level.removeBlock(foot, false);
            }
            Vec3 center = pos.getCenter();
            level.explode(null, level.damageSources().badRespawnPointExplosion(center), null, center, 5.0F, true,
                    Level.ExplosionInteraction.BLOCK);
        } else if (state.getValue(OCCUPIED)) {
            if (!kickVillagerOutOfBed(level, pos)) {
                player.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
            }
        } else {
            player.startSleepInBed(pos).ifLeft(problem -> {
                if (problem.getMessage() != null) {
                    player.displayClientMessage(problem.getMessage(), true);
                }
            });
        }
    }

    private static boolean kickVillagerOutOfBed(Level level, BlockPos pos) {
        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, new AABB(pos), LivingEntity::isSleeping);
        if (villagers.isEmpty()) {
            return false;
        }
        villagers.get(0).stopSleeping();
        return true;
    }

    // Design and blanket, kept the same on both halves

    /** Applies {@code change} to this half's block entity and the other half's. */
    private static void bothHalves(Level level, BlockPos pos, BlockState state,
            Consumer<CoverableBlockEntity> change) {
        CoverableBlockEntity be = blockEntity(level, pos);
        if (be != null) {
            change.accept(be);
        }
        BlockPos other = pos.relative(toOtherHalf(state));
        if (level.getBlockState(other).getBlock() instanceof CarpentersBedBlock) {
            CoverableBlockEntity otherBe = blockEntity(level, other);
            if (otherBe != null) {
                change.accept(otherBe);
            }
        }
    }

    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        String design = DesignList.BED.previous(be.getDesign());
        bothHalves(level, pos, state, half -> half.setDesign(design));
        return true;
    }

    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        if (!level.isClientSide) {
            String design = player.isSecondaryUseActive() ? "" : DesignList.BED.next(be.getDesign());
            bothHalves(level, pos, state, half -> half.setDesign(design));
        }
        return true;
    }

    /** The blanket's dye, or {@code null} for white. Stored in the block data of both halves. */
    @Nullable
    public static DyeColor blanket(CoverableBlockEntity be) {
        int value = be.getBlockData();
        return value == 0 ? null : DyeColor.byId(value - 1);
    }

    private static void setBlanket(Level level, BlockPos pos, BlockState state, @Nullable DyeColor dye) {
        int value = dye == null ? 0 : dye.getId() + 1;
        bothHalves(level, pos, state, half -> half.setBlockData(value));
    }

    /** Sneaking with a dye colors the blanket instead of the frame. */
    @Override
    protected void preOnBlockActivated(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, InteractionHand hand, BlockHitResult hit, ActionResult result) {
        ItemStack stack = player.getItemInHand(hand);
        DyeColor dye;
        if (!CBConfig.enableDyeColors || !player.isSecondaryUseActive() || (dye = AttributeItems.dyeFor(stack)) == null) {
            return;
        }
        if (blanket(be) == null) {
            if (!level.isClientSide) {
                setBlanket(level, pos, state, dye);
            }
            result.setAltered().decInventory().setSoundSource(Blocks.SAND.defaultBlockState());
        } else {
            result.setAltered().setNoSound();
        }
    }

    /** Sneak left-click with the hammer takes the blanket's dye off first. */
    @Override
    protected void preOnBlockClicked(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face, ActionResult result) {
        DyeColor dye = blanket(be);
        if (player.isSecondaryUseActive() && dye != null) {
            setBlanket(level, pos, state, null);
            popResourceFromFace(level, pos, face, AttributeItems.dyeDrop(dye));
            result.setAltered();
        }
    }

    /** The foot drops the blanket's dye, so a broken bed gives it back once. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !isHead(state)) {
            CoverableBlockEntity be = blockEntity(level, pos);
            DyeColor dye = be == null ? null : blanket(be);
            if (dye != null) {
                popResource(level, pos, AttributeItems.dyeDrop(dye));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // Rendering inputs

    /** What a half's frame depends on beyond its state. */
    public record Look(BedGeometry.Joins joins, String design, @Nullable DyeColor blanket) {}

    public static Look look(BlockGetter level, BlockPos pos, BlockState state) {
        CoverableBlockEntity be = blockEntity(level, pos);
        String design = be == null ? "" : be.getDesign();
        DyeColor blanket = be == null ? null : blanket(be);
        Direction low = BedGeometry.lowSide(state.getValue(FACING));
        return new Look(new BedGeometry.Joins(joins(level, pos.relative(low), state),
                joins(level, pos.relative(low.getOpposite()), state)), DesignList.BED.isValid(design) ? design : "",
                blanket);
    }

    /** A neighbor joins when it is the same half of a bed facing the same way, as the original. */
    private static boolean joins(BlockGetter level, BlockPos pos, BlockState state) {
        BlockState other = level.getBlockState(pos);
        return other.getBlock() instanceof CarpentersBedBlock && other.getValue(PART) == state.getValue(PART)
                && other.getValue(FACING) == state.getValue(FACING);
    }

    // Structures

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
