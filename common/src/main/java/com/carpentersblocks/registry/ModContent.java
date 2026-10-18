package com.carpentersblocks.registry;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.CarpentersBlockBlock;
import com.carpentersblocks.blockentity.CarpentersBlockEntity;
import com.carpentersblocks.item.CarpentersHammerItem;

/**
 * All blocks, items and block entity types. Each loader calls the {@code register*} methods from its own
 * registration hook, since objects must be constructed while the registries are open.
 */
public final class ModContent {

    public static CarpentersBlockBlock CARPENTERS_BLOCK;
    public static Item CARPENTERS_BLOCK_ITEM;
    public static Item CARPENTERS_HAMMER;
    public static BlockEntityType<CarpentersBlockEntity> CARPENTERS_BLOCK_ENTITY;

    public static final ResourceLocation TAB_ID = CarpentersBlocks.id("main");

    private ModContent() {}

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        CARPENTERS_BLOCK = new CarpentersBlockBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .instrument(NoteBlockInstrument.BASS)
                .strength(1.5F)
                .sound(SoundType.WOOD)
                .ignitedByLava()
                .lightLevel(state -> state.getValue(CarpentersBlockBlock.LIGHT)));
        registrar.accept(CarpentersBlocks.id("carpenters_block"), CARPENTERS_BLOCK);
    }

    public static void registerItems(BiConsumer<ResourceLocation, Item> registrar) {
        CARPENTERS_BLOCK_ITEM = new BlockItem(CARPENTERS_BLOCK, new Item.Properties());
        registrar.accept(CarpentersBlocks.id("carpenters_block"), CARPENTERS_BLOCK_ITEM);

        CARPENTERS_HAMMER = new CarpentersHammerItem(new Item.Properties().stacksTo(1));
        registrar.accept(CarpentersBlocks.id("carpenters_hammer"), CARPENTERS_HAMMER);
    }

    public static void registerBlockEntities(BiConsumer<ResourceLocation, BlockEntityType<?>> registrar,
            BlockEntityTypeFactory factory) {
        CARPENTERS_BLOCK_ENTITY = factory.create(CarpentersBlockEntity::new, CARPENTERS_BLOCK);
        registrar.accept(CarpentersBlocks.id("carpenters_block"), CARPENTERS_BLOCK_ENTITY);
    }

    /** Builds a block entity type; vanilla's builder is only accessible through each loader. */
    @FunctionalInterface
    public interface BlockEntityTypeFactory {
        <T extends BlockEntity> BlockEntityType<T> create(BiFunction<BlockPos, BlockState, T> factory, Block... blocks);
    }

    /** Fills in a loader-provided creative tab builder. */
    public static CreativeModeTab buildTab(CreativeModeTab.Builder builder) {
        return builder.title(Component.translatable("itemGroup.carpentersblocks"))
                .icon(() -> new ItemStack(CARPENTERS_BLOCK_ITEM))
                .displayItems((parameters, output) -> {
                    output.accept(CARPENTERS_BLOCK_ITEM);
                    output.accept(CARPENTERS_HAMMER);
                })
                .build();
    }
}
