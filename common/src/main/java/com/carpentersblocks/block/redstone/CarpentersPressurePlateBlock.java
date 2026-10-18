package com.carpentersblocks.block.redstone;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.carpentersblocks.block.coverable.CoverableBlockEntity;

/**
 * Carpenter's Pressure Plate: powered while something it is set to notice touches it, checked once a second. It
 * hangs on any face, as the original. The hammer's right-click picks what triggers it. Port of the original
 * {@code BlockCarpentersPressurePlate}.
 */
public class CarpentersPressurePlateBlock extends SidedRedstoneBlock {

    public static final MapCodec<CarpentersPressurePlateBlock> CODEC = simpleCodec(CarpentersPressurePlateBlock::new);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<PlateTrigger> TRIGGER = EnumProperty.create("trigger", PlateTrigger.class);
    /** The original's tick rate: how often a pressed plate looks for what holds it down. */
    public static final int CHECK_TICKS = 20;

    private static final VoxelShape[][] SHAPES = new VoxelShape[6][2];

    static {
        for (Direction facing : Direction.values()) {
            for (int pressed = 0; pressed < 2; ++pressed) {
                SHAPES[facing.ordinal()][pressed] = RedstoneGeometry.pressurePlate(facing, pressed == 1).shape();
            }
        }
    }

    public CarpentersPressurePlateBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(INVERTED, false)
                .setValue(POWERED, false)
                .setValue(TRIGGER, PlateTrigger.PLAYER)
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
        builder.add(POWERED, TRIGGER);
    }

    @Override
    protected int activeOutput(BlockState state) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    // Pressing

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && !state.getValue(POWERED) && pressed(state, level, pos)) {
            setPowerState(level, pos, state.setValue(POWERED, true));
            click(level, pos, entity instanceof Player player ? player : null,
                    SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, true);
            level.scheduleTick(pos, this, CHECK_TICKS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) {
            return;
        }
        if (pressed(state, level, pos)) {
            level.scheduleTick(pos, this, CHECK_TICKS);
        } else {
            setPowerState(level, pos, state.setValue(POWERED, false));
            click(level, pos, null, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, false);
        }
    }

    /** Whether a triggering entity touches the plate's idle box. Spectators and the like never do. */
    public static boolean pressed(BlockState state, Level level, BlockPos pos) {
        AABB bounds = RedstoneGeometry.pressurePlate(state.getValue(FACING), false).shape().bounds().move(pos);
        PlateTrigger trigger = state.getValue(TRIGGER);
        return !level.getEntities((Entity) null, bounds, entity -> EntitySelector.NO_SPECTATORS.test(entity)
                && !entity.isIgnoringBlockTriggers() && trigger.triggeredBy(entity)).isEmpty();
    }

    // Hammer

    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        if (!level.isClientSide) {
            PlateTrigger next = state.getValue(TRIGGER).next();
            setPowerState(level, pos, state.setValue(TRIGGER, next));
            message(player, "trigger_" + next.getSerializedName());
        }
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).ordinal()][state.getValue(POWERED) ? 1 : 0];
    }
}
