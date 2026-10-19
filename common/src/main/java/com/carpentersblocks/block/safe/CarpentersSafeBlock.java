package com.carpentersblocks.block.safe;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.coverable.Permissions;

/**
 * Carpenter's Safe: a lockable chest of 27 slots, 54 with a gold, diamond or emerald upgrade (sneak right-click), that
 * one player at a time can open. A locked safe opens only for its owner (and operators, and in singleplayer), can't be
 * broken by anyone else or while open, and resists explosions as bedrock. The hammer turns it to face the player
 * (left-click), cycles what hoppers may do (right-click) or locks it (sneak right-click). Lights on the front show
 * the lock and how full it is. Port of the original {@code BlockCarpentersSafe}.
 */
public class CarpentersSafeBlock extends CoverableBlock {

    public static final MapCodec<CarpentersSafeBlock> CODEC = simpleCodec(CarpentersSafeBlock::new);

    /** The side the door is on. */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** A player has the safe open; the door slides aside. */
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

    public CarpentersSafeBlock(Properties properties) {
        super(properties);
        // New safes are locked with automation off, as the original's empty data
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(LOCKED, true)
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
        builder.add(FACING, OPEN, LOCKED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SafeBlockEntity(pos, state);
    }

    @Nullable
    private static SafeBlockEntity safe(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SafeBlockEntity safe ? safe : null;
    }

    /** Facing the player who placed it. */
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, placedInWater(context));
    }

    // Opening

    /** The owner may always use the safe; others only while it is unlocked. */
    @Override
    protected boolean canPlayerActivate(CoverableBlockEntity be, Player player) {
        return Permissions.hasElevatedPermission(be, player, true) || !be.getBlockState().getValue(LOCKED);
    }

    @Override
    protected void postOnBlockActivated(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, InteractionHand hand, BlockHitResult hit, ActionResult result) {
        result.setAltered();
        if (!(be instanceof SafeBlockEntity safe)) {
            return;
        }
        if (state.getValue(OPEN) || !canPlayerActivate(be, player)) {
            if (!level.isClientSide) {
                message(player, "block_lock");
            }
            result.setNoSound();
            return;
        }

        ItemStack held = player.getItemInHand(hand);
        SafePanel panel = SafePanel.forUpgrade(held);
        if (player.isSecondaryUseActive() && panel != null && !safe.hasUpgrade()) {
            if (!level.isClientSide) {
                safe.setUpgrade(held);
            }
            result.decInventory().setSoundSource(panel.block().defaultBlockState());
            return;
        }

        result.setNoSound();
        if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider((id, inventory, p) -> safe.hasUpgrade()
                    ? ChestMenu.sixRows(id, inventory, safe)
                    : ChestMenu.threeRows(id, inventory, safe),
                    Component.translatable("container." + CarpentersBlocks.MOD_ID + ".carpenters_safe")));
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        SafeBlockEntity safe = safe(level, pos);
        if (safe != null) {
            safe.recheckOpen();
        }
    }

    private static void message(Player player, String key) {
        player.displayClientMessage(Component.translatable("message." + CarpentersBlocks.MOD_ID + "." + key), false);
    }

    // Hammer: only the owner may turn, lock or change automation

    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        if (!Permissions.hasElevatedPermission(be, player, true)) {
            return false;
        }
        Direction toward = player.getDirection().getOpposite();
        if (toward == state.getValue(FACING)) {
            return false;
        }
        level.setBlock(pos, state.setValue(FACING, toward), Block.UPDATE_ALL);
        return true;
    }

    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        if (level.isClientSide || !(be instanceof SafeBlockEntity safe)) {
            return true;
        }
        if (!Permissions.hasElevatedPermission(be, player, true)) {
            message(player, "block_lock");
            return true;
        }
        if (player.isSecondaryUseActive()) {
            boolean locked = !state.getValue(LOCKED);
            level.setBlock(pos, state.setValue(LOCKED, locked), Block.UPDATE_ALL);
            safe.setAutomation(locked ? SafeAutomation.DISABLED : SafeAutomation.ALL);
            message(player, locked ? "safe_lock" : "safe_unlock");
        } else {
            SafeAutomation automation = safe.getAutomation().next();
            safe.setAutomation(automation);
            message(player, automation.message());
        }
        return true;
    }

    // Protection

    /** Only the owner can break a safe, and not while it is open. */
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        CoverableBlockEntity be = blockEntity(level, pos);
        if (state.getValue(OPEN) || be != null && !Permissions.hasElevatedPermission(be, player, true)) {
            return 0.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return Blocks.BEDROCK.getExplosionResistance();
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean removed = !state.is(newState.getBlock());
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (removed) {
            level.updateNeighbourForOutputSignal(pos, this);
        }
    }

    // Comparators read how full it is, as a chest

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
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
