package com.carpentersblocks.registry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.redstone.CarpentersButtonBlock;
import com.carpentersblocks.block.redstone.CarpentersDaylightSensorBlock;
import com.carpentersblocks.block.redstone.CarpentersLeverBlock;
import com.carpentersblocks.block.redstone.CarpentersPressurePlateBlock;
import com.carpentersblocks.config.CBConfig;

/** Batch 5: the Carpenter's Button, Lever, Pressure Plate and Daylight Sensor. */
public final class RedstoneContent {

    public static CarpentersButtonBlock CARPENTERS_BUTTON;
    public static CarpentersLeverBlock CARPENTERS_LEVER;
    public static CarpentersPressurePlateBlock CARPENTERS_PRESSURE_PLATE;
    public static CarpentersDaylightSensorBlock CARPENTERS_DAYLIGHT_SENSOR;

    private RedstoneContent() {}

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        // Hardness as their vanilla counterparts; the switches have nothing to stand on, as vanilla's
        if (CBConfig.enableButton) {
            CARPENTERS_BUTTON = ModContent.block(registrar, "carpenters_button",
                    new CarpentersButtonBlock(CoverableBlock.baseProperties().noCollission().strength(0.5F)), true);
        }
        if (CBConfig.enableLever) {
            CARPENTERS_LEVER = ModContent.block(registrar, "carpenters_lever",
                    new CarpentersLeverBlock(CoverableBlock.baseProperties().noCollission().strength(0.5F)), true);
        }
        if (CBConfig.enablePressurePlate) {
            CARPENTERS_PRESSURE_PLATE = ModContent.block(registrar, "carpenters_pressure_plate",
                    new CarpentersPressurePlateBlock(CoverableBlock.baseProperties().noCollission().strength(0.5F)),
                    true);
        }
        if (CBConfig.enableDaylightSensor) {
            CARPENTERS_DAYLIGHT_SENSOR = ModContent.block(registrar, "carpenters_daylight_sensor",
                    new CarpentersDaylightSensorBlock(CoverableBlock.baseProperties().strength(0.2F)), true);
        }
    }
}
