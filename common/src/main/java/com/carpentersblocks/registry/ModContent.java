package com.carpentersblocks.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.CarpentersBlockBlock;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.config.CBConfig;
import com.carpentersblocks.item.CarpentersChiselItem;
import com.carpentersblocks.item.CarpentersHammerItem;
import com.carpentersblocks.recipe.ConfiguredShapedRecipe;

/**
 * All blocks, items and block entity types. Each loader calls the {@code register*} methods from its own
 * registration hook, since objects must be constructed while the registries are open.
 * <p>
 * Each block family registers through its own {@code <Family>Content} class, called from here, using
 * {@link #block} and {@link #item} so its blocks join the shared block entity type and the creative tab.
 */
public final class ModContent {

    public static CarpentersBlockBlock CARPENTERS_BLOCK;
    public static Item CARPENTERS_HAMMER;
    public static Item CARPENTERS_CHISEL;
    public static BlockEntityType<CoverableBlockEntity> COVERABLE_BLOCK_ENTITY;

    public static final ResourceLocation TAB_ID = CarpentersBlocks.id("main");

    /** Every coverable block, for the shared block entity type. */
    private static final List<Block> COVERABLE_BLOCKS = new ArrayList<>();
    /** Blocks that get a plain block item, in registration order. */
    private static final List<Map.Entry<String, Block>> BLOCKS_WITH_ITEMS = new ArrayList<>();
    /** Creative tab contents, in order. */
    private static final List<Item> TAB_ITEMS = new ArrayList<>();
    /** Items shown in the creative tab as several stacks (such as the slope kinds) instead of one. */
    private static final Map<Item, Supplier<List<ItemStack>>> TAB_VARIANTS = new HashMap<>();

    private ModContent() {}

    /** Every coverable block registered so far; the loaders wrap their models and colors. */
    public static List<Block> coverableBlocks() {
        return List.copyOf(COVERABLE_BLOCKS);
    }

    /** Registers a block, giving it a block item unless {@code withItem} is false. */
    public static <B extends Block> B block(BiConsumer<ResourceLocation, Block> registrar, String name, B block,
            boolean withItem) {
        registrar.accept(CarpentersBlocks.id(name), block);
        if (block instanceof CoverableBlock) {
            COVERABLE_BLOCKS.add(block);
        }
        if (withItem) {
            BLOCKS_WITH_ITEMS.add(Map.entry(name, block));
        }
        return block;
    }

    /** Registers an item and adds it to the creative tab. */
    public static <I extends Item> I item(BiConsumer<ResourceLocation, Item> registrar, String name, I item) {
        registrar.accept(CarpentersBlocks.id(name), item);
        TAB_ITEMS.add(item);
        return item;
    }

    /** Shows {@code item} in the creative tab as these stacks. */
    public static void tabVariants(Item item, Supplier<List<ItemStack>> stacks) {
        TAB_VARIANTS.put(item, stacks);
    }

    public static void registerDataComponents(BiConsumer<ResourceLocation, DataComponentType<?>> registrar) {
        SlopeContent.registerDataComponents(registrar);
    }

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        COVERABLE_BLOCKS.clear();
        BLOCKS_WITH_ITEMS.clear();
        if (CBConfig.enableBlock) {
            CARPENTERS_BLOCK = block(registrar, "carpenters_block",
                    new CarpentersBlockBlock(CoverableBlock.baseProperties()), true);
        }
        SlopeContent.registerBlocks(registrar);
        StairsContent.registerBlocks(registrar);
        BarrierContent.registerBlocks(registrar);
        RedstoneContent.registerBlocks(registrar);
        TorchContent.registerBlocks(registrar);
        LadderContent.registerBlocks(registrar);
        DoorContent.registerBlocks(registrar);
        BedContent.registerBlocks(registrar);
        FlowerPotContent.registerBlocks(registrar);
        CollapsibleContent.registerBlocks(registrar);
    }

    public static void registerItems(BiConsumer<ResourceLocation, Item> registrar) {
        TAB_ITEMS.clear();
        TAB_VARIANTS.clear();
        for (Map.Entry<String, Block> entry : BLOCKS_WITH_ITEMS) {
            item(registrar, entry.getKey(), new BlockItem(entry.getValue(), new Item.Properties()));
            if (entry.getValue() == CARPENTERS_BLOCK) {
                SlopeContent.registerItems(registrar);
            }
        }
        if (CARPENTERS_BLOCK == null) {
            SlopeContent.registerItems(registrar);
        }
        if (CBConfig.enableHammer) {
            CARPENTERS_HAMMER = item(registrar, "carpenters_hammer", new CarpentersHammerItem(toolProperties()));
        }
        if (CBConfig.enableChisel) {
            CARPENTERS_CHISEL = item(registrar, "carpenters_chisel", new CarpentersChiselItem(toolProperties()));
        }
        TileContent.registerItems(registrar);
    }

    /** {@code carpentersblocks:configured_shaped}: shaped recipes whose count is a config option. */
    public static void registerRecipeSerializers(BiConsumer<ResourceLocation, RecipeSerializer<?>> registrar) {
        registrar.accept(CarpentersBlocks.id("configured_shaped"), ConfiguredShapedRecipe.SERIALIZER);
    }

    public static void registerEntityTypes(BiConsumer<ResourceLocation, EntityType<?>> registrar) {
        TileContent.registerEntityTypes(registrar);
    }

    private static Item.Properties toolProperties() {
        Item.Properties properties = new Item.Properties().stacksTo(1);
        return CBConfig.toolsDamageable ? properties.durability(CBConfig.toolUses) : properties;
    }

    public static void registerBlockEntities(BiConsumer<ResourceLocation, BlockEntityType<?>> registrar,
            BlockEntityTypeFactory factory) {
        COVERABLE_BLOCK_ENTITY = factory.create(CoverableBlockEntity::new, COVERABLE_BLOCKS.toArray(Block[]::new));
        registrar.accept(CarpentersBlocks.id("coverable"), COVERABLE_BLOCK_ENTITY);
        FlowerPotContent.registerBlockEntities(registrar, factory);
    }

    /** Builds a block entity type; vanilla's builder is only accessible through each loader. */
    @FunctionalInterface
    public interface BlockEntityTypeFactory {
        <T extends BlockEntity> BlockEntityType<T> create(BiFunction<BlockPos, BlockState, T> factory, Block... blocks);
    }

    /** Fills in a loader-provided creative tab builder. */
    public static CreativeModeTab buildTab(CreativeModeTab.Builder builder) {
        return builder.title(Component.translatable("itemGroup.carpentersblocks"))
                .icon(() -> TAB_ITEMS.isEmpty() ? ItemStack.EMPTY : new ItemStack(TAB_ITEMS.get(0)))
                .displayItems((parameters, output) -> {
                    for (Item item : TAB_ITEMS) {
                        Supplier<List<ItemStack>> variants = TAB_VARIANTS.get(item);
                        if (variants == null) {
                            output.accept(item);
                        } else {
                            variants.get().forEach(output::accept);
                        }
                    }
                })
                .build();
    }
}
