package com.carpentersblocks.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.block.CarpentersBlockBlock;
import com.carpentersblocks.blockentity.CarpentersBlockEntity;

/**
 * Right-click a Carpenter's Block to switch between full block and slab. Sneak + right-click to take off the last
 * thing applied to the clicked face.
 * <p>
 * The original used left-click for removal; right-click works the same on both loaders without extra events.
 */
public class CarpentersHammerItem extends Item {

    public CarpentersHammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof CarpentersBlockBlock block)
                || !(level.getBlockEntity(pos) instanceof CarpentersBlockEntity blockEntity)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player != null && player.isSecondaryUseActive()) {
            ItemStack removed = blockEntity.popAttribute(context.getClickedFace());
            if (removed.isEmpty()) {
                return InteractionResult.PASS;
            }
            Block.popResourceFromFace(level, pos, context.getClickedFace(), removed);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            block.cycleShape(level, pos, state, context.getClickedFace());
            level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.CONSUME;
    }
}
