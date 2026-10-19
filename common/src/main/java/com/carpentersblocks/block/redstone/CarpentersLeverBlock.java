package com.carpentersblocks.block.redstone;

import net.minecraft.core.BlockPos;

import net.minecraft.core.Direction;

import java.util.function.BiConsumer;

import com.mojang.serialization.MapCodec;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.carpentersblocks.block.coverable.CoverableBlockEntity;

/** Carpenter's Lever: toggles its power when used. Port of the original {@code BlockCarpentersLever}. */
public class CarpentersLeverBlock extends SidedRedstoneBlock {

    public static final MapCodec<CarpentersLeverBlock> CODEC = simpleCodec(CarpentersLeverBlock::new);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    /** Which way the lever swings on a floor or ceiling; on walls it follows the facing. */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    private static final VoxelShape[][] SHAPES = new VoxelShape[6][2];

    static {
        for (Direction facing : Direction.values()) {
            for (int z = 0; z < 2; ++z) {
                SHAPES[facing.ordinal()][z] = RedstoneGeometry.leverBase(facing, z == 1).shape();
            }
        }
    }

    public CarpentersLeverBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(INVERTED, false)
                .setValue(POWERED, false)
                .setValue(AXIS, Direction.Axis.Z)
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
        builder.add(POWERED, AXIS);
    }

    public static boolean alongZ(BlockState state) {
        return state.getValue(AXIS) == Direction.Axis.Z;
    }

    /** On floors and ceilings the lever swings the way the player looks; on walls, out from the wall. */
    @Override
    protected BlockState placementState(BlockPlaceContext context, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction.Axis axis = facing.getAxis() == Direction.Axis.Y ? context.getHorizontalDirection().getAxis()
                : facing.getAxis();
        return state.setValue(AXIS, axis);
    }

    @Override
    protected int activeOutput(BlockState state) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected void postOnBlockActivated(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, InteractionHand hand, BlockHitResult hit, ActionResult result) {
        if (!level.isClientSide) {
            boolean on = !state.getValue(POWERED);
            setPowerState(level, pos, state.setValue(POWERED, on));
            click(level, pos, player, SoundEvents.LEVER_CLICK, on);
        }
        result.setAltered().setNoSound();
    }

    /** A wind charge flips the lever, as vanilla's. */
    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion,
            BiConsumer<ItemStack, BlockPos> drops) {
        if (!level.isClientSide && explosion.canTriggerBlocks()) {
            boolean on = !state.getValue(POWERED);
            setPowerState(level, pos, state.setValue(POWERED, on));
            click(level, pos, null, SoundEvents.LEVER_CLICK, on);
        }
        super.onExplosionHit(state, level, pos, explosion, drops);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).ordinal()][alongZ(state) ? 1 : 0];
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        state = super.rotate(state, rotation);
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            state = state.setValue(AXIS, alongZ(state) ? Direction.Axis.X : Direction.Axis.Z);
        }
        return state;
    }
}
