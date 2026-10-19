package com.carpentersblocks.block.coverable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;

/**
 * Decides what a held item applies to a coverable block: a cover, dye, overlay or illuminator. Port of the item
 * checks in {@code BlockProperties}, {@code DyeHandler} and {@code OverlayHandler}, using tags where the original
 * used the ore dictionary or display names.
 */
public final class AttributeItems {

    /** Blocks that may never be used as covers. */
    public static final TagKey<Block> COVER_DENYLIST = TagKey.create(Registries.BLOCK,
            CarpentersBlocks.id("cover_denylist"));
    /** Blocks that may be used as covers even though they fail the usual checks (the original's cover exceptions). */
    public static final TagKey<Block> COVER_ALLOWLIST = TagKey.create(Registries.BLOCK,
            CarpentersBlocks.id("cover_allowlist"));
    /** Items that make a block give off full light (glowstone dust in the original). */
    public static final TagKey<Item> ILLUMINATORS = TagKey.create(Registries.ITEM,
            CarpentersBlocks.id("illuminators"));

    private AttributeItems() {}

    /**
     * Returns the block state a held item would apply as a cover, oriented as if the player had placed it, or
     * {@code null} if the item can't be used as a cover.
     * <p>
     * As in the original, full blocks, slabs, panes and glass-like blocks qualify. Slabs cover as their double slab
     * so the faces show the slab's texture.
     */
    @Nullable
    public static BlockState coverStateFor(ItemStack stack, @Nullable Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!(stack.getItem() instanceof BlockItem blockItem) || Overlay.forItem(stack) != null) {
            return null;
        }
        Block block = blockItem.getBlock();
        BlockState defaultState = block.defaultBlockState();
        if (block instanceof CoverableBlock || defaultState.is(COVER_DENYLIST)) {
            return null;
        }

        if (!defaultState.is(COVER_ALLOWLIST)) {
            if (block instanceof EntityBlock) {
                return null;
            }
            if (block instanceof SlabBlock) {
                return defaultState.setValue(SlabBlock.TYPE, SlabType.DOUBLE);
            }
            if (block instanceof IronBarsBlock) {
                return defaultState;
            }
            try {
                if (defaultState.getRenderShape() != RenderShape.MODEL
                        || !Block.isShapeFullBlock(defaultState.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO))) {
                    return null;
                }
            } catch (RuntimeException e) {
                // Some blocks can't answer shape queries outside a real world; they aren't covers
                return null;
            }
        }

        // Let the block orient itself (logs, pillars, pumpkins) as it would on placement
        BlockState placed = player == null ? null
                : block.getStateForPlacement(new BlockPlaceContext(player, hand, stack, hit));
        return placed != null && placed.is(block) ? placed : defaultState;
    }

    /**
     * Dye color the item applies, or {@code null}. White dye doesn't count, as in the original, since an undyed
     * face already looks white-tinted.
     */
    @Nullable
    public static DyeColor dyeFor(ItemStack stack) {
        DyeColor color = anyDyeFor(stack);
        return color == DyeColor.WHITE ? null : color;
    }

    /** Dye color the item applies, white included (tiles can be dyed back to white), or {@code null}. */
    @Nullable
    public static DyeColor anyDyeFor(ItemStack stack) {
        DyeColor color = null;
        if (stack.getItem() instanceof DyeItem dyeItem) {
            color = dyeItem.getDyeColor();
        } else {
            for (DyeColor candidate : DyeColor.values()) {
                if (stack.is(dyeTag(candidate))) {
                    color = candidate;
                    break;
                }
            }
        }
        return color;
    }

    public static boolean isIlluminator(ItemStack stack) {
        return stack.is(ILLUMINATORS);
    }

    /** The common {@code c:dyes/<color>} tag shared by both loaders. */
    private static TagKey<Item> dyeTag(DyeColor color) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "dyes/" + color.getName()));
    }

    /** Item to drop when a cover is removed. */
    public static ItemStack coverDrop(BlockState cover) {
        return new ItemStack(cover.getBlock());
    }

    /** Item to drop when a dye is removed. */
    public static ItemStack dyeDrop(DyeColor dye) {
        return new ItemStack(DyeItem.byColor(dye));
    }
}
