package com.carpentersblocks.block.flowerpot;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.registry.FlowerPotContent;

/**
 * A Carpenter's Flower Pot's block entity: the covers, plus the soil, plant and fertilizer put in it. The plant's
 * turn (0 to 15) is the block data, as the original's angle bits. Port of the original {@code TECarpentersFlowerPot}
 * with the pot's attributes from {@code TEBase}.
 */
public class FlowerPotBlockEntity extends CoverableBlockEntity {

    private static final String TAG_SOIL = "Soil";
    private static final String TAG_PLANT = "Plant";
    private static final String TAG_FERTILIZER = "Fertilizer";

    private ItemStack soil = ItemStack.EMPTY;
    private ItemStack plant = ItemStack.EMPTY;
    private ItemStack fertilizer = ItemStack.EMPTY;

    public FlowerPotBlockEntity(BlockPos pos, BlockState state) {
        super(FlowerPotContent.FLOWER_POT_BLOCK_ENTITY, pos, state);
    }

    public ItemStack getSoil() {
        return soil;
    }

    public ItemStack getPlant() {
        return plant;
    }

    public ItemStack getFertilizer() {
        return fertilizer;
    }

    public boolean hasSoil() {
        return !soil.isEmpty();
    }

    public boolean hasPlant() {
        return !plant.isEmpty();
    }

    public boolean isFertilized() {
        return !fertilizer.isEmpty();
    }

    public void setSoil(ItemStack stack) {
        soil = stack.copyWithCount(stack.isEmpty() ? 0 : 1);
        onContentsChanged();
    }

    public void setPlant(ItemStack stack) {
        plant = stack.copyWithCount(stack.isEmpty() ? 0 : 1);
        if (plant.isEmpty()) {
            fertilizer = ItemStack.EMPTY;
        }
        onContentsChanged();
    }

    public void setFertilizer(ItemStack stack) {
        fertilizer = stack.copyWithCount(stack.isEmpty() ? 0 : 1);
        onContentsChanged();
    }

    /** The plant's turn, 0 to 15. */
    public int getAngle() {
        return getBlockData() & 15;
    }

    public void setAngle(int angle) {
        setBlockData(angle & 15);
    }

    /** Saves, sends the contents to clients and brings the pot's shape in line with the plant. */
    private void onContentsChanged() {
        setChanged();
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(CarpentersFlowerPotBlock.CONTENTS)) {
            BlockState updated = state.setValue(CarpentersFlowerPotBlock.CONTENTS, PotPlants.contents(plant));
            if (updated != state) {
                level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
                state = updated;
            }
        }
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    /** The covers' items, then fertilizer, plant and soil, as the original's drops. */
    @Override
    public List<ItemStack> getAttributeDrops() {
        List<ItemStack> drops = super.getAttributeDrops();
        for (ItemStack stack : new ItemStack[] { fertilizer, plant, soil }) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        return drops;
    }

    @Override
    public void clearContent() {
        soil = ItemStack.EMPTY;
        plant = ItemStack.EMPTY;
        fertilizer = ItemStack.EMPTY;
        super.clearContent();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!soil.isEmpty()) {
            tag.put(TAG_SOIL, soil.save(registries));
        }
        if (!plant.isEmpty()) {
            tag.put(TAG_PLANT, plant.save(registries));
        }
        if (!fertilizer.isEmpty()) {
            tag.put(TAG_FERTILIZER, fertilizer.save(registries));
        }
    }

    /** Reads the pot's contents before the covers, whose loading asks the client to redraw the pot. */
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        soil = ItemStack.parseOptional(registries, tag.getCompound(TAG_SOIL));
        plant = ItemStack.parseOptional(registries, tag.getCompound(TAG_PLANT));
        fertilizer = ItemStack.parseOptional(registries, tag.getCompound(TAG_FERTILIZER));
        super.loadAdditional(tag, registries);
    }
}
