package com.carpentersblocks.block.redstone;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.CoverableBlockEntity;

/**
 * Carpenter's Daylight Sensor: power from the sky, read once a second. It sits on floors and walls, not ceilings.
 * The hammer's right-click picks the sensitivity. Port of the original {@code BlockCarpentersDaylightSensor}.
 */
public class CarpentersDaylightSensorBlock extends SidedRedstoneBlock {

    public static final MapCodec<CarpentersDaylightSensorBlock> CODEC =
            simpleCodec(CarpentersDaylightSensorBlock::new);

    public static final EnumProperty<SensorSensitivity> SENSITIVITY =
            EnumProperty.create("sensitivity", SensorSensitivity.class);
    /** The last sky reading, 0 to 15. */
    public static final IntegerProperty READING = IntegerProperty.create("reading", 0, 15);
    /** The original's update interval. */
    public static final int UPDATE_TICKS = 20;

    private static final VoxelShape[] SHAPES = new VoxelShape[6];

    static {
        for (Direction facing : Direction.values()) {
            SHAPES[facing.ordinal()] = RedstoneGeometry.daylightSensor(facing).shape();
        }
    }

    public CarpentersDaylightSensorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(INVERTED, false)
                .setValue(SENSITIVITY, SensorSensitivity.SLEEP)
                .setValue(READING, 0)
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
        builder.add(SENSITIVITY, READING);
    }

    @Override
    protected boolean canAttach(Direction facing) {
        return facing != Direction.DOWN;
    }

    @Override
    protected int activeOutput(BlockState state) {
        return state.getValue(SENSITIVITY).output(state.getValue(READING));
    }

    /** Whether the redstone inlay glows: any output at all, as the original's {@code isActive}. */
    public boolean isActive(BlockState state) {
        return output(state) > 0;
    }

    // Reading the sky

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null : (tickLevel, pos, tickState, be) -> {
            if (tickLevel.getGameTime() % UPDATE_TICKS == 0L) {
                updateReading(tickLevel, pos, tickState);
            }
        };
    }

    public void updateReading(Level level, BlockPos pos, BlockState state) {
        if (!level.dimensionType().hasSkyLight() || !state.is(this)) {
            return;
        }
        int skylight = level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken();
        int reading = state.getValue(SENSITIVITY).reading(state.getValue(FACING), skylight, level.getSunAngle(1.0F),
                level.isThundering());
        if (reading != state.getValue(READING)) {
            setPowerState(level, pos, state.setValue(READING, reading));
        }
    }

    // Hammer

    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        if (!level.isClientSide) {
            SensorSensitivity next = state.getValue(SENSITIVITY).next();
            setPowerState(level, pos, state.setValue(SENSITIVITY, next));
            message(player, "sensitivity_" + next.getSerializedName());
        }
        return true;
    }

    // Shapes: the sensor is solid, unlike the rest of the family

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).ordinal()];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return getShape(state, level, pos, context);
    }
}
