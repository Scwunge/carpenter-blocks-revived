package com.carpentersblocks.block.coverable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.registry.ModContent;

/**
 * Holds the attributes of any coverable block: covers, dyes, overlays, chisel designs, the illuminator, the owner,
 * and a free design name and data value for blocks that need them. Port of the original {@code TEBase}.
 * <p>
 * What renders lives in an immutable {@link CoverData}; the item stacks that were applied are kept alongside so the
 * same items come back out when an attribute is removed.
 */
public class CoverableBlockEntity extends BlockEntity implements Clearable {

    private static final String TAG_COVERS = "Covers";
    private static final String TAG_SLOT = "Slot";
    private static final String TAG_STATE = "State";
    private static final String TAG_DYE = "Dye";
    private static final String TAG_DYE_ITEM = "DyeItem";
    private static final String TAG_OVERLAY = "Overlay";
    private static final String TAG_OVERLAY_ITEM = "OverlayItem";
    private static final String TAG_DESIGN = "ChiselDesign";
    private static final String TAG_ILLUMINATOR = "Illuminator";
    private static final String TAG_OWNER = "Owner";
    private static final String TAG_BLOCK_DESIGN = "Design";
    private static final String TAG_DATA = "Data";

    private volatile CoverData data = CoverData.EMPTY;
    private final ItemStack[] dyeItems = filled(CoverData.SLOTS);
    private final ItemStack[] overlayItems = filled(CoverData.SLOTS);
    private ItemStack illuminatorItem = ItemStack.EMPTY;
    @Nullable
    private UUID owner;
    private String design = "";
    private int blockData;

    /**
     * Forgets every attribute without dropping it. Vanilla calls this before commands such as /setblock and /fill
     * replace the block, as it does for containers.
     */
    @Override
    public void clearContent() {
        data = CoverData.EMPTY;
        Arrays.fill(dyeItems, ItemStack.EMPTY);
        Arrays.fill(overlayItems, ItemStack.EMPTY);
        illuminatorItem = ItemStack.EMPTY;
        setChanged();
    }

    public CoverableBlockEntity(BlockPos pos, BlockState state) {
        this(ModContent.COVERABLE_BLOCK_ENTITY, pos, state);
    }

    protected CoverableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public CoverData getCoverData() {
        return data;
    }

    // Covers

    public void setCover(int slot, @Nullable BlockState cover) {
        data = data.withCover(slot, cover);
        if (cover == null) {
            data = data.withDesign(slot, null);
        }
        onDataChanged(true);
    }

    // Dyes

    public void setDye(int slot, @Nullable DyeColor dye, ItemStack item) {
        data = data.withDye(slot, dye);
        dyeItems[slot] = dye == null ? ItemStack.EMPTY : item.copyWithCount(1);
        onDataChanged(false);
    }

    // Overlays

    public void setOverlay(int slot, @Nullable Overlay overlay, ItemStack item) {
        data = data.withOverlay(slot, overlay);
        overlayItems[slot] = overlay == null ? ItemStack.EMPTY : item.copyWithCount(1);
        onDataChanged(false);
    }

    // Chisel designs

    public void setChiselDesign(int slot, @Nullable String design) {
        data = data.withDesign(slot, design);
        onDataChanged(false);
    }

    // Illuminator

    public void setIlluminator(ItemStack item) {
        data = data.withIlluminated(!item.isEmpty());
        illuminatorItem = item.isEmpty() ? ItemStack.EMPTY : item.copyWithCount(1);
        onDataChanged(false);
    }

    // Owner, design and data

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        setChanged();
    }

    /** Name of the block-specific design (beds, flower pots), or an empty string. */
    public String getDesign() {
        return design;
    }

    public void setDesign(String design) {
        if (!this.design.equals(design)) {
            this.design = design;
            onDataChanged(false);
        }
    }

    /** Block-specific data that doesn't fit in the block state. */
    public int getBlockData() {
        return blockData;
    }

    public void setBlockData(int value) {
        if (blockData != value) {
            blockData = value;
            onDataChanged(false);
        }
    }

    /**
     * Removes the outermost attribute of a slot, in the original mod's order: illuminator, overlay, dye, then cover
     * (taking its chisel design with it).
     *
     * @return the item to drop, or {@link ItemStack#EMPTY} if nothing was removed
     */
    public ItemStack popAttribute(int slot) {
        ItemStack drop;
        if (data.isIlluminated()) {
            drop = illuminatorDrop();
            setIlluminator(ItemStack.EMPTY);
        } else if (data.overlay(slot) != null) {
            drop = overlayDrop(slot);
            setOverlay(slot, null, ItemStack.EMPTY);
        } else if (data.dye(slot) != null) {
            drop = dyeDrop(slot);
            setDye(slot, null, ItemStack.EMPTY);
        } else if (data.hasCover(slot)) {
            drop = AttributeItems.coverDrop(data.cover(slot));
            setCover(slot, null);
        } else {
            drop = ItemStack.EMPTY;
        }
        return drop;
    }

    /** Removes the cover, dye and overlay of a side (and the illuminator for the base), returning their items. */
    public List<ItemStack> removeSlot(int slot) {
        List<ItemStack> drops = new ArrayList<>();
        collectSlotDrops(slot, drops);
        data = data.withCover(slot, null).withDye(slot, null).withOverlay(slot, null).withDesign(slot, null);
        dyeItems[slot] = ItemStack.EMPTY;
        overlayItems[slot] = ItemStack.EMPTY;
        if (slot == CoverData.BASE && data.isIlluminated()) {
            drops.add(illuminatorDrop());
            data = data.withIlluminated(false);
            illuminatorItem = ItemStack.EMPTY;
        }
        onDataChanged(true);
        return drops;
    }

    /** Every attribute item, dropped when the block is broken. */
    public List<ItemStack> getAttributeDrops() {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < CoverData.SLOTS; ++slot) {
            collectSlotDrops(slot, drops);
        }
        if (data.isIlluminated()) {
            drops.add(illuminatorDrop());
        }
        return drops;
    }

    private void collectSlotDrops(int slot, List<ItemStack> drops) {
        if (data.hasCover(slot)) {
            drops.add(AttributeItems.coverDrop(data.cover(slot)));
        }
        if (data.overlay(slot) != null) {
            drops.add(overlayDrop(slot));
        }
        if (data.dye(slot) != null) {
            drops.add(dyeDrop(slot));
        }
    }

    private ItemStack dyeDrop(int slot) {
        return dyeItems[slot].isEmpty() ? AttributeItems.dyeDrop(data.dye(slot)) : dyeItems[slot].copy();
    }

    private ItemStack overlayDrop(int slot) {
        return overlayItems[slot].isEmpty() ? ItemStack.EMPTY : overlayItems[slot].copy();
    }

    private ItemStack illuminatorDrop() {
        return illuminatorItem.isEmpty() ? new ItemStack(Items.GLOWSTONE_DUST) : illuminatorItem.copy();
    }

    /** Lets the block bring its state (light, solidity) in line with the attributes. Server side only. */
    public void refreshBlockState() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        if (state.getBlock() instanceof CoverableBlock block) {
            BlockState updated = block.stateForCovers(state, data);
            if (updated != state) {
                level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
            }
        }
    }

    private void onDataChanged(boolean coversChanged) {
        setChanged();
        if (level != null && !level.isClientSide) {
            refreshBlockState();
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
            if (coversChanged) {
                // Covers decide redstone output and side solidity, which neighbors care about
                level.updateNeighborsAt(worldPosition, state.getBlock());
            }
        }
    }

    // Saving and syncing

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag slots = new ListTag();
        for (int slot = 0; slot < CoverData.SLOTS; ++slot) {
            CompoundTag entry = new CompoundTag();
            BlockState cover = data.cover(slot);
            if (cover != null) {
                entry.put(TAG_STATE, NbtUtils.writeBlockState(cover));
            }
            if (data.dye(slot) != null) {
                entry.putString(TAG_DYE, data.dye(slot).getName());
                if (!dyeItems[slot].isEmpty()) {
                    entry.put(TAG_DYE_ITEM, dyeItems[slot].save(registries));
                }
            }
            if (data.overlay(slot) != null) {
                entry.putString(TAG_OVERLAY, data.overlay(slot).getSerializedName());
                if (!overlayItems[slot].isEmpty()) {
                    entry.put(TAG_OVERLAY_ITEM, overlayItems[slot].save(registries));
                }
            }
            if (data.design(slot) != null) {
                entry.putString(TAG_DESIGN, data.design(slot));
            }
            if (!entry.isEmpty()) {
                entry.putByte(TAG_SLOT, (byte) slot);
                slots.add(entry);
            }
        }
        tag.put(TAG_COVERS, slots);
        if (data.isIlluminated()) {
            tag.put(TAG_ILLUMINATOR, illuminatorDrop().save(registries));
        }
        if (owner != null) {
            tag.putUUID(TAG_OWNER, owner);
        }
        if (!design.isEmpty()) {
            tag.putString(TAG_BLOCK_DESIGN, design);
        }
        if (blockData != 0) {
            tag.putInt(TAG_DATA, blockData);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        CoverData loaded = CoverData.EMPTY;
        for (int slot = 0; slot < CoverData.SLOTS; ++slot) {
            dyeItems[slot] = ItemStack.EMPTY;
            overlayItems[slot] = ItemStack.EMPTY;
        }

        ListTag slots = tag.getList(TAG_COVERS, Tag.TAG_COMPOUND);
        for (int idx = 0; idx < slots.size(); ++idx) {
            CompoundTag entry = slots.getCompound(idx);
            int slot = entry.getByte(TAG_SLOT);
            if (slot < 0 || slot >= CoverData.SLOTS) {
                continue;
            }
            if (entry.contains(TAG_STATE)) {
                BlockState cover = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK),
                        entry.getCompound(TAG_STATE));
                if (!cover.isAir()) {
                    loaded = loaded.withCover(slot, cover);
                }
            }
            if (entry.contains(TAG_DYE)) {
                loaded = loaded.withDye(slot, DyeColor.byName(entry.getString(TAG_DYE), null));
                dyeItems[slot] = ItemStack.parseOptional(registries, entry.getCompound(TAG_DYE_ITEM));
            }
            if (entry.contains(TAG_OVERLAY)) {
                loaded = loaded.withOverlay(slot, Overlay.byName(entry.getString(TAG_OVERLAY)));
                overlayItems[slot] = ItemStack.parseOptional(registries, entry.getCompound(TAG_OVERLAY_ITEM));
            }
            if (entry.contains(TAG_DESIGN) && ChiselDesigns.isValid(entry.getString(TAG_DESIGN))) {
                loaded = loaded.withDesign(slot, entry.getString(TAG_DESIGN));
            }
        }

        illuminatorItem = tag.contains(TAG_ILLUMINATOR)
                ? ItemStack.parseOptional(registries, tag.getCompound(TAG_ILLUMINATOR))
                : ItemStack.EMPTY;
        data = loaded.withIlluminated(!illuminatorItem.isEmpty());
        owner = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
        design = tag.getString(TAG_BLOCK_DESIGN);
        blockData = tag.getInt(TAG_DATA);

        // Client received new attributes: rebuild the chunk section so the model picks them up
        if (level != null && level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private static ItemStack[] filled(int size) {
        ItemStack[] stacks = new ItemStack[size];
        Arrays.fill(stacks, ItemStack.EMPTY);
        return stacks;
    }

    /** Convenience for callers working with faces. */
    public int slotFor(Direction face) {
        return data.slotFor(face);
    }
}
