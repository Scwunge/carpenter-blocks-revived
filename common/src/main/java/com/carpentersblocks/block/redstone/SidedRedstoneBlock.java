package com.carpentersblocks.block.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.sided.SidedBlock;

/**
 * A redstone block attached to the face of another block: the Button, Lever, Pressure Plate and Daylight Sensor. It
 * gives its output as weak power all round and strong power into the block it hangs on, and the hammer's left-click
 * inverts that output, as the polarity the four shared in the original.
 */
public abstract class SidedRedstoneBlock extends SidedBlock {

    /** The original's negative polarity: full power when idle, none when active. */
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

    protected SidedRedstoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(INVERTED);
    }

    /** Output before polarity, 0 to 15. */
    protected abstract int activeOutput(BlockState state);

    /** Power given off, 0 to 15. */
    public int output(BlockState state) {
        int active = activeOutput(state);
        return state.getValue(INVERTED) ? 15 - active : active;
    }

    // Power

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return Math.max(super.getSignal(state, level, pos, direction), output(state));
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        int power = super.getDirectSignal(state, level, pos, direction);
        return direction == state.getValue(FACING) ? Math.max(power, output(state)) : power;
    }

    /** Sets a state with a new output and tells the neighbors, and the neighbors of the supporting block. */
    protected void setPowerState(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
        updatePower(level, pos, state);
    }

    protected void updatePower(Level level, BlockPos pos, BlockState state) {
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(supportPos(state, pos), this);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!movedByPiston && !state.is(newState.getBlock()) && output(state) > 0) {
            updatePower(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * The click the original played on switching, unless the base cover is cloth. Also sends the vibration a vanilla
     * switch does.
     */
    protected void click(Level level, BlockPos pos, @Nullable Player player, SoundEvent sound, boolean on) {
        BlockState base = baseCover(level, pos);
        if (base == null || !base.is(BlockTags.WOOL) && !base.is(BlockTags.WOOL_CARPETS)) {
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.3F, on ? 0.6F : 0.5F);
        }
        level.gameEvent(player, on ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
    }

    protected static void message(Player player, String key) {
        player.displayClientMessage(Component.translatable("message." + CarpentersBlocks.MOD_ID + "." + key), false);
    }

    // Hammer

    /** Inverts the output, as the original's polarity. */
    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        boolean inverted = !state.getValue(INVERTED);
        setPowerState(level, pos, state.setValue(INVERTED, inverted));
        message(player, inverted ? "polarity_neg" : "polarity_pos");
        return true;
    }

    // Shapes

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.empty();
    }
}
