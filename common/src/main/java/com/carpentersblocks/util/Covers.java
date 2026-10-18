package com.carpentersblocks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.CarpentersBlockBlock;

/** Decides which items can cover a Carpenter's Block. Port of {@code BlockProperties.isCover}. */
public final class Covers {

    private Covers() {}

    /**
     * Returns the block state a held item would apply as a cover, oriented as if the player had placed it, or
     * {@code null} if the item can't be used as a cover.
     */
    @Nullable
    public static BlockState coverStateFor(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        Block block = blockItem.getBlock();
        if (block instanceof CarpentersBlockBlock || block instanceof EntityBlock) {
            return null;
        }

        BlockState defaultState = block.defaultBlockState();
        try {
            if (defaultState.getRenderShape() != RenderShape.MODEL
                    || !Block.isShapeFullBlock(defaultState.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO))) {
                return null;
            }
        } catch (RuntimeException e) {
            // Some blocks can't answer shape queries outside a real world; they aren't covers
            return null;
        }

        // Let the block orient itself (logs, pillars, pumpkins) as it would on placement
        BlockState placed = block.getStateForPlacement(new BlockPlaceContext(player, hand, stack, hit));
        return placed != null && placed.is(block) ? placed : defaultState;
    }
}
