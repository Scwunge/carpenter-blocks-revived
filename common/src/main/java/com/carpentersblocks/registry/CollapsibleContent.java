package com.carpentersblocks.registry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.carpentersblocks.block.collapsible.CarpentersCollapsibleBlock;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.config.CBConfig;

/** Batch 9: the Carpenter's Collapsible Block. Its corner depths live in the shared block entity's data. */
public final class CollapsibleContent {

    public static CarpentersCollapsibleBlock CARPENTERS_COLLAPSIBLE_BLOCK;

    private CollapsibleContent() {}

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        if (CBConfig.enableCollapsibleBlock) {
            // The original's hardness
            CARPENTERS_COLLAPSIBLE_BLOCK = ModContent.block(registrar, "carpenters_collapsible_block",
                    new CarpentersCollapsibleBlock(CoverableBlock.baseProperties().strength(0.2F)), true);
        }
    }
}
