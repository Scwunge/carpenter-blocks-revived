package com.carpentersblocks.registry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.flowerpot.CarpentersFlowerPotBlock;
import com.carpentersblocks.block.flowerpot.FlowerPotBlockEntity;
import com.carpentersblocks.config.CBConfig;

/** Batch 8: the Carpenter's Flower Pot, with its own block entity type for the soil and plant. */
public final class FlowerPotContent {

    public static CarpentersFlowerPotBlock CARPENTERS_FLOWER_POT;
    public static BlockEntityType<FlowerPotBlockEntity> FLOWER_POT_BLOCK_ENTITY;

    private FlowerPotContent() {}

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        if (CBConfig.enableFlowerPot) {
            CARPENTERS_FLOWER_POT = ModContent.block(registrar, "carpenters_flower_pot",
                    new CarpentersFlowerPotBlock(CoverableBlock.baseProperties().strength(0.5F)), true);
        }
    }

    public static void registerBlockEntities(BiConsumer<ResourceLocation, BlockEntityType<?>> registrar,
            ModContent.BlockEntityTypeFactory factory) {
        if (CARPENTERS_FLOWER_POT != null) {
            FLOWER_POT_BLOCK_ENTITY = factory.create(FlowerPotBlockEntity::new, CARPENTERS_FLOWER_POT);
            registrar.accept(CarpentersBlocks.id("flower_pot"), FLOWER_POT_BLOCK_ENTITY);
        }
    }
}
