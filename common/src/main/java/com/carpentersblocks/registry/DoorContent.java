package com.carpentersblocks.registry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.door.CarpentersDoorBlock;
import com.carpentersblocks.block.door.CarpentersGarageDoorBlock;
import com.carpentersblocks.block.door.CarpentersHatchBlock;
import com.carpentersblocks.config.CBConfig;

/** Batch 7: the Carpenter's Door, Hatch and Garage Door. */
public final class DoorContent {

    public static CarpentersDoorBlock CARPENTERS_DOOR;
    public static CarpentersHatchBlock CARPENTERS_HATCH;
    public static CarpentersGarageDoorBlock CARPENTERS_GARAGE_DOOR;

    private DoorContent() {}

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        // All three are as soft as the original's (hardness 0.2)
        if (CBConfig.enableDoor) {
            CARPENTERS_DOOR = ModContent.block(registrar, "carpenters_door",
                    new CarpentersDoorBlock(CoverableBlock.baseProperties().strength(0.2F)), true);
        }
        if (CBConfig.enableHatch) {
            CARPENTERS_HATCH = ModContent.block(registrar, "carpenters_hatch",
                    new CarpentersHatchBlock(CoverableBlock.baseProperties().strength(0.2F)), true);
        }
        if (CBConfig.enableGarageDoor) {
            CARPENTERS_GARAGE_DOOR = ModContent.block(registrar, "carpenters_garage_door",
                    new CarpentersGarageDoorBlock(CoverableBlock.baseProperties().strength(0.2F)), true);
        }
    }
}
