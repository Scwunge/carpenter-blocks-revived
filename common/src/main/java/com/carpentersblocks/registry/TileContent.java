package com.carpentersblocks.registry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.config.CBConfig;
import com.carpentersblocks.entity.CarpentersTileEntity;
import com.carpentersblocks.item.CarpentersTileItem;

/** Batch 10: the Carpenter's Tile, an entity placed by its item. */
public final class TileContent {

    public static EntityType<CarpentersTileEntity> CARPENTERS_TILE;
    public static Item CARPENTERS_TILE_ITEM;

    private TileContent() {}

    public static void registerEntityTypes(BiConsumer<ResourceLocation, EntityType<?>> registrar) {
        if (CBConfig.enableTile) {
            ResourceLocation id = CarpentersBlocks.id("carpenters_tile");
            // Tracked and updated like an item frame
            CARPENTERS_TILE = EntityType.Builder.<CarpentersTileEntity>of(CarpentersTileEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).eyeHeight(0.0F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE)
                    .build(id.toString());
            registrar.accept(id, CARPENTERS_TILE);
        }
    }

    public static void registerItems(BiConsumer<ResourceLocation, Item> registrar) {
        if (CBConfig.enableTile) {
            CARPENTERS_TILE_ITEM = ModContent.item(registrar, "carpenters_tile",
                    new CarpentersTileItem(new Item.Properties()));
        }
    }
}
