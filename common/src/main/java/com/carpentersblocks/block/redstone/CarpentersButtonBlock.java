package com.carpentersblocks.block.redstone;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.carpentersblocks.block.coverable.CoverableBlockEntity;

/** Carpenter's Button: one second of power when pressed. Port of the original {@code BlockCarpentersButton}. */
public class CarpentersButtonBlock extends SidedRedstoneBlock {

    public static final MapCodec<CarpentersButtonBlock> CODEC = simpleCodec(CarpentersButtonBlock::new);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    /** The original's tick rate: how long a press lasts. */
    public static final int PRESS_TICKS = 20;

    private static final VoxelShape[][] SHAPES = new VoxelShape[6][2];

    static {
        for (Direction facing : Direction.values()) {
            for (int pressed = 0; pressed < 2; ++pressed) {
                SHAPES[facing.ordinal()][pressed] = RedstoneGeometry.button(facing, pressed == 1).shape();
            }
        }
    }

    public CarpentersButtonBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(INVERTED, false)
                .setValue(POWERED, false)
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
        builder.add(POWERED);
    }

    @Override
    protected int activeOutput(BlockState state) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected void postOnBlockActivated(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, InteractionHand hand, BlockHitResult hit, ActionResult result) {
        if (!state.getValue(POWERED)) {
            if (!level.isClientSide) {
                setPowerState(level, pos, state.setValue(POWERED, true));
                click(level, pos, player, SoundEvents.STONE_BUTTON_CLICK_ON, true);
                level.scheduleTick(pos, this, PRESS_TICKS);
            }
            result.setAltered().setNoSound();
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            setPowerState(level, pos, state.setValue(POWERED, false));
            click(level, pos, null, SoundEvents.STONE_BUTTON_CLICK_OFF, false);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).ordinal()][state.getValue(POWERED) ? 1 : 0];
    }
}
