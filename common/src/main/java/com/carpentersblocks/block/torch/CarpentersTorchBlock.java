package com.carpentersblocks.block.torch;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.joml.Vector3f;

import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.sided.SidedBlock;
import com.carpentersblocks.config.CBConfig;

/**
 * Carpenter's Torch: a torch or lantern on a floor or wall. Rain puts the torch style out a step at a time and it
 * relights when dry, or when used with another torch. The hammer switches style. Port of the original
 * {@code BlockCarpentersTorch}.
 */
public class CarpentersTorchBlock extends SidedBlock {

    public static final MapCodec<CarpentersTorchBlock> CODEC = simpleCodec(CarpentersTorchBlock::new);

    public static final EnumProperty<TorchType> TYPE = EnumProperty.create("type", TorchType.class);
    public static final EnumProperty<TorchFlame> FLAME = EnumProperty.create("flame", TorchFlame.class);

    private static final VoxelShape[][] SHAPES = new VoxelShape[TorchType.values().length][6];

    static {
        for (TorchType type : TorchType.values()) {
            for (Direction facing : Direction.values()) {
                SHAPES[type.ordinal()][facing.ordinal()] = TorchGeometry.outline(type, facing).shape();
            }
        }
    }

    public CarpentersTorchBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(TYPE, TorchType.VANILLA)
                .setValue(FLAME, TorchFlame.LIT)
                .setValue(LIGHT, 0)
                .setValue(WATERLOGGED, false));
    }

    /** Light: the flame's, or the covers' if brighter. */
    public static int lightLevel(BlockState state) {
        return Math.max(state.getValue(LIGHT), state.getValue(FLAME).light());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE, FLAME);
    }

    @Override
    protected boolean canAttach(Direction facing) {
        return facing != Direction.DOWN;
    }

    // Relighting

    /** Using a torch on an unlit or smoldering one lights it, as the original. Any torch block counts. */
    @Override
    protected void preOnBlockActivated(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, InteractionHand hand, BlockHitResult hit, ActionResult result) {
        ItemStack held = player.getItemInHand(hand);
        if (state.getValue(FLAME) != TorchFlame.LIT && held.getItem() instanceof BlockItem item
                && (item.getBlock() instanceof TorchBlock || item.getBlock() instanceof CarpentersTorchBlock)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(FLAME, TorchFlame.LIT), Block.UPDATE_ALL);
            }
            result.setAltered();
        }
    }

    // Weather

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (CBConfig.enableTorchWeatherEffects && state.getValue(TYPE) == TorchType.VANILLA) {
            TorchFlame flame = state.getValue(FLAME);
            TorchFlame next = flame.weather(level.isRainingAt(pos));
            if (next != flame) {
                setFlame(level, pos, state, next);
            }
        }
    }

    /** Sets the flame, fizzing when it dies down. */
    public static void setFlame(Level level, BlockPos pos, BlockState state, TorchFlame flame) {
        if (flame.ordinal() > state.getValue(FLAME).ordinal()) {
            Vector3f head = TorchGeometry.head(state.getValue(TYPE), state.getValue(FACING));
            level.playSound(null, pos.getX() + head.x, pos.getY() + head.y, pos.getZ() + head.z,
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                    2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        }
        level.setBlock(pos, state.setValue(FLAME, flame), Block.UPDATE_ALL);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        TorchFlame flame = state.getValue(FLAME);
        if (flame != TorchFlame.UNLIT) {
            Vector3f head = TorchGeometry.head(state.getValue(TYPE), state.getValue(FACING));
            double x = pos.getX() + head.x;
            double y = pos.getY() + head.y;
            double z = pos.getZ() + head.z;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
            if (flame == TorchFlame.LIT) {
                level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
            }
        }
    }

    // Hammer: left-click the next style, right-click the previous

    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        level.setBlock(pos, state.setValue(TYPE, state.getValue(TYPE).next()), Block.UPDATE_ALL);
        return true;
    }

    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(TYPE, state.getValue(TYPE).previous()), Block.UPDATE_ALL);
        }
        return true;
    }

    // Shapes: the lantern is solid, the torch is not

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(TYPE).ordinal()][state.getValue(FACING).ordinal()];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return state.getValue(TYPE) == TorchType.LANTERN ? getShape(state, level, pos, context) : Shapes.empty();
    }
}
