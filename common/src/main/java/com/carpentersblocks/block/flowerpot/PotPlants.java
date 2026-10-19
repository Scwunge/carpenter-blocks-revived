package com.carpentersblocks.block.flowerpot;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;

/**
 * Which items a Carpenter's Flower Pot takes as soil and plants, and how a plant is drawn. The original went by block
 * material and Forge's plant interfaces, with a list of names for the drawing; here plain bushes (any mod's flowers,
 * saplings, mushrooms and crops) count as plants, and tags add the rest. Port of the original
 * {@code FlowerPotProperties} and {@code FlowerPotHandler}.
 */
public final class PotPlants {

    /** Extra plants beyond bushes, cactus and sugar cane: leaves, vines and the like. */
    public static final TagKey<Block> PLANTS = TagKey.create(Registries.BLOCK,
            CarpentersBlocks.id("flower_pot_plants"));
    /** Blocks the pot takes as soil. */
    public static final TagKey<Block> SOILS = TagKey.create(Registries.BLOCK, CarpentersBlocks.id("flower_pot_soils"));
    /** Plants drawn at full size rather than three quarters, as the original's {@code TRUE_SCALE}. */
    public static final TagKey<Block> FULL_SIZE = TagKey.create(Registries.BLOCK,
            CarpentersBlocks.id("flower_pot_full_size"));
    /** Items that fertilize a tintable plant, as bone meal did. */
    public static final TagKey<Item> FERTILIZERS = TagKey.create(Registries.ITEM, CarpentersBlocks.id("fertilizers"));

    /** How a plant is drawn and how solid it makes the pot. */
    public enum Profile {
        /** Three quarters of its size. */
        REDUCED,
        /** Full size. */
        FULL,
        /** Both halves of a tall plant, each three quarters of its size. */
        DOUBLE,
        /** Three small blocks stacked on the soil (leaves and cactus), which you can't walk through. */
        STACKED
    }

    private PotPlants() {}

    @Nullable
    private static Block blockOf(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item ? item.getBlock() : null;
    }

    public static boolean isSoil(ItemStack stack) {
        Block block = blockOf(stack);
        return block != null && !(block instanceof EntityBlock) && block.defaultBlockState().is(SOILS);
    }

    public static boolean isPlant(ItemStack stack) {
        Block block = blockOf(stack);
        return block != null && isPlant(block);
    }

    private static boolean isPlant(Block block) {
        if (block instanceof EntityBlock) {
            return false;
        }
        return block instanceof BushBlock || block instanceof CactusBlock || block instanceof SugarCaneBlock
                || block.defaultBlockState().is(PLANTS) || block.defaultBlockState().is(BlockTags.LEAVES);
    }

    /**
     * The state a plant item is drawn as: grown, as the original drew crops fully matured, and the lower half of tall
     * plants. {@code null} if the item isn't a plant.
     */
    @Nullable
    public static BlockState plantState(ItemStack stack) {
        Block block = blockOf(stack);
        if (block == null || !isPlant(block)) {
            return null;
        }
        BlockState state = block.defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty age && property.getName().equals("age")) {
                state = state.setValue(age, age.getPossibleValues().stream().max(Integer::compare).orElse(0));
            }
        }
        return state;
    }

    /** The soil's block state, or {@code null} if the item isn't a block. */
    @Nullable
    public static BlockState soilState(ItemStack stack) {
        Block block = blockOf(stack);
        return block == null ? null : block.defaultBlockState();
    }

    public static Profile profile(BlockState plant) {
        if (plant.is(BlockTags.LEAVES) || plant.getBlock() instanceof CactusBlock) {
            return Profile.STACKED;
        }
        if (plant.getBlock() instanceof DoublePlantBlock) {
            return Profile.DOUBLE;
        }
        return plant.is(FULL_SIZE) ? Profile.FULL : Profile.REDUCED;
    }

    /** What the pot's shape follows, for a plant item (empty for none). */
    public static PotContents contents(ItemStack plant) {
        BlockState state = plantState(plant);
        if (state == null) {
            return PotContents.EMPTY;
        }
        return profile(state) == Profile.STACKED ? PotContents.SOLID : PotContents.PLANT;
    }
}
