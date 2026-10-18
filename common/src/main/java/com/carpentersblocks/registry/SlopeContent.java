package com.carpentersblocks.registry;

import java.util.function.BiConsumer;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.slope.CarpentersSlopeBlock;
import com.carpentersblocks.block.slope.SlopeItem;
import com.carpentersblocks.block.slope.SlopeKind;
import com.carpentersblocks.config.CBConfig;

/** Batch 2: the Carpenter's Slope and its item kinds. */
public final class SlopeContent {

    public static CarpentersSlopeBlock CARPENTERS_SLOPE;
    public static SlopeItem CARPENTERS_SLOPE_ITEM;
    public static DataComponentType<SlopeKind> SLOPE_KIND;

    private SlopeContent() {}

    public static void registerDataComponents(BiConsumer<ResourceLocation, DataComponentType<?>> registrar) {
        SLOPE_KIND = DataComponentType.<SlopeKind>builder()
                .persistent(SlopeKind.CODEC)
                .networkSynchronized(SlopeKind.STREAM_CODEC)
                .build();
        registrar.accept(CarpentersBlocks.id("slope_kind"), SLOPE_KIND);
    }

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        if (CBConfig.enableSlope) {
            CARPENTERS_SLOPE = ModContent.block(registrar, "carpenters_slope",
                    new CarpentersSlopeBlock(CoverableBlock.baseProperties()), false);
        }
    }

    public static void registerItems(BiConsumer<ResourceLocation, Item> registrar) {
        if (CARPENTERS_SLOPE != null) {
            CARPENTERS_SLOPE_ITEM = ModContent.item(registrar, "carpenters_slope",
                    new SlopeItem(CARPENTERS_SLOPE, new Item.Properties()));
            ModContent.tabVariants(CARPENTERS_SLOPE_ITEM, () -> SlopeItem.allKinds(CARPENTERS_SLOPE));
        }
    }
}
