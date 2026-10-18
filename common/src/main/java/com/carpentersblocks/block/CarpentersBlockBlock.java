package com.carpentersblocks.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.blockentity.CarpentersBlockEntity;
import com.carpentersblocks.blockentity.CoverData;
import com.carpentersblocks.item.CarpentersHammerItem;
import com.carpentersblocks.util.Covers;

/**
 * The Carpenter's Block: a frame that takes on the look of blocks applied to it, and can be hammered into slabs.
 * Port of the original {@code BlockCarpentersBlock} and the cover handling in {@code BlockCoverable}.
 */
public class CarpentersBlockBlock extends BaseEntityBlock {

    public static final MapCodec<CarpentersBlockBlock> CODEC = simpleCodec(CarpentersBlockBlock::new);

    public static final EnumProperty<FrameShape> SHAPE = EnumProperty.create("shape", FrameShape.class);
    /** True when every exposed face is covered by an opaque block, so neighbors may cull against it. */
    public static final BooleanProperty SOLID = BooleanProperty.create("solid");
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

    public CarpentersBlockBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any().setValue(SHAPE, FrameShape.FULL).setValue(SOLID, false).setValue(LIGHT, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, SOLID, LIGHT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CarpentersBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(SOLID);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();

        // Match the shape of the Carpenter's Block placed against, unless sneaking (as in the original)
        if (!context.isSecondaryUseActive()) {
            BlockPos against = context.getClickedPos().relative(context.getClickedFace().getOpposite());
            BlockState againstState = context.getLevel().getBlockState(against);
            if (againstState.is(this)) {
                state = state.setValue(SHAPE, againstState.getValue(SHAPE));
            }
        }

        return state;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof CarpentersHammerItem) {
            // The hammer handles itself in useOn, which also runs while sneaking
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(level.getBlockEntity(pos) instanceof CarpentersBlockEntity blockEntity)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        CoverData data = blockEntity.getCoverData();

        if (stack.is(Items.GLOWSTONE_DUST)) {
            if (data.isIlluminated()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                blockEntity.setIlluminated(true);
                stack.consume(1, player);
                level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        BlockState cover = Covers.coverStateFor(stack, player, hand, hit);
        if (cover == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // The base cover goes on first; after that, covers go on the clicked side
        int slot = data.hasCover(CoverData.BASE) ? hit.getDirection().get3DDataValue() : CoverData.BASE;
        if (data.hasCover(slot)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide) {
            blockEntity.setCover(slot, cover);
            stack.consume(1, player);
            SoundType sound = cover.getSoundType();
            level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F,
                    sound.getPitch() * 0.8F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Hammer action: a full block becomes a slab on the side away from the clicked face; a slab becomes full. */
    public void cycleShape(Level level, BlockPos pos, BlockState state, Direction clickedFace) {
        FrameShape next = state.getValue(SHAPE) == FrameShape.FULL ? FrameShape.slabAgainst(clickedFace.getOpposite())
                : FrameShape.FULL;
        level.setBlock(pos, state.setValue(SHAPE, next), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof CarpentersBlockEntity blockEntity) {
            blockEntity.refreshBlockState();
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CarpentersBlockEntity blockEntity) {
            for (ItemStack drop : blockEntity.getAttributeDrops()) {
                popResource(level, pos, drop);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
