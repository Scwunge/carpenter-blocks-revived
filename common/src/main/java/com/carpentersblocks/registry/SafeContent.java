package com.carpentersblocks.registry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.safe.CarpentersSafeBlock;
import com.carpentersblocks.block.safe.SafeBlockEntity;
import com.carpentersblocks.config.CBConfig;

/** Batch 9: the Carpenter's Safe, with its own block entity type for the inventory. */
public final class SafeContent {

    public static CarpentersSafeBlock CARPENTERS_SAFE;
    public static BlockEntityType<SafeBlockEntity> SAFE_BLOCK_ENTITY;

    private SafeContent() {}

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        if (CBConfig.enableSafe) {
            CARPENTERS_SAFE = ModContent.block(registrar, "carpenters_safe",
                    new CarpentersSafeBlock(CoverableBlock.baseProperties().strength(2.5F)), true);
        }
    }

    public static void registerBlockEntities(BiConsumer<ResourceLocation, BlockEntityType<?>> registrar,
            ModContent.BlockEntityTypeFactory factory) {
        if (CARPENTERS_SAFE != null) {
            SAFE_BLOCK_ENTITY = factory.create(SafeBlockEntity::new, CARPENTERS_SAFE);
            registrar.accept(CarpentersBlocks.id("safe"), SAFE_BLOCK_ENTITY);
        }
    }
}
