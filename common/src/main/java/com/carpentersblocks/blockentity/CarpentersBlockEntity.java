package com.carpentersblocks.blockentity;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.CarpentersBlockBlock;
import com.carpentersblocks.block.FrameShape;
import com.carpentersblocks.registry.ModContent;

/**
 * Stores the covers applied to a Carpenter's Block. Port of the original {@code TEBase}, limited for now to covers
 * and the glowstone illuminator.
 */
public class CarpentersBlockEntity extends BlockEntity {

    private static final String TAG_COVERS = "Covers";
    private static final String TAG_SLOT = "Slot";
    private static final String TAG_STATE = "State";
    private static final String TAG_ILLUMINATED = "Illuminated";

    private volatile CoverData data = CoverData.EMPTY;

    public CarpentersBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.CARPENTERS_BLOCK_ENTITY, pos, state);
    }

    public CoverData getCoverData() {
        return data;
    }

    public void setCover(int slot, @Nullable BlockState cover) {
        data = data.withCover(slot, cover);
        onDataChanged();
    }

    public void setIlluminated(boolean illuminated) {
        data = data.withIlluminated(illuminated);
        onDataChanged();
    }

    /**
     * Removes the outermost attribute on the given face, in the original mod's order: illuminator first, then the
     * side cover, then the base cover.
     *
     * @return the item to drop, or {@link ItemStack#EMPTY} if nothing was removed
     */
    public ItemStack popAttribute(Direction face) {
        if (data.isIlluminated()) {
            setIlluminated(false);
            return new ItemStack(Items.GLOWSTONE_DUST);
        }
        int slot = data.slotFor(face);
        BlockState cover = data.cover(slot);
        if (cover == null) {
            return ItemStack.EMPTY;
        }
        setCover(slot, null);
        return new ItemStack(cover.getBlock());
    }

    /** Items to drop when the block is broken. */
    public List<ItemStack> getAttributeDrops() {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < CoverData.SLOTS; ++slot) {
            BlockState cover = data.cover(slot);
            if (cover != null) {
                drops.add(new ItemStack(cover.getBlock()));
            }
        }
        if (data.isIlluminated()) {
            drops.add(new ItemStack(Items.GLOWSTONE_DUST));
        }
        return drops;
    }

    /**
     * Brings the block state's light and occlusion properties in line with the covers. These live in the block state
     * because vanilla caches lighting and face culling per state.
     */
    public void refreshBlockState() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof CarpentersBlockBlock)) {
            return;
        }
        FrameShape shape = state.getValue(CarpentersBlockBlock.SHAPE);

        int light = data.isIlluminated() ? 15 : 0;
        boolean solid = true;
        for (Direction face : Direction.values()) {
            BlockState cover = data.coverFor(face);
            if (cover == null) {
                solid = false;
            } else {
                light = Math.max(light, cover.getLightEmission());
                if (shape.coversFace(face) && !cover.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
                    solid = false;
                }
            }
        }

        BlockState updated = state.setValue(CarpentersBlockBlock.LIGHT, light).setValue(CarpentersBlockBlock.SOLID, solid);
        if (updated != state) {
            level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        }
    }

    private void onDataChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            refreshBlockState();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag covers = new ListTag();
        for (int slot = 0; slot < CoverData.SLOTS; ++slot) {
            BlockState cover = data.cover(slot);
            if (cover != null) {
                CompoundTag entry = new CompoundTag();
                entry.putByte(TAG_SLOT, (byte) slot);
                entry.put(TAG_STATE, NbtUtils.writeBlockState(cover));
                covers.add(entry);
            }
        }
        tag.put(TAG_COVERS, covers);
        tag.putBoolean(TAG_ILLUMINATED, data.isIlluminated());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        CoverData loaded = CoverData.EMPTY;
        ListTag covers = tag.getList(TAG_COVERS, Tag.TAG_COMPOUND);
        for (int idx = 0; idx < covers.size(); ++idx) {
            CompoundTag entry = covers.getCompound(idx);
            int slot = entry.getByte(TAG_SLOT);
            if (slot >= 0 && slot < CoverData.SLOTS) {
                BlockState cover = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), entry.getCompound(TAG_STATE));
                if (!cover.isAir()) {
                    loaded = loaded.withCover(slot, cover);
                }
            }
        }
        data = loaded.withIlluminated(tag.getBoolean(TAG_ILLUMINATED));

        // Client received new covers: rebuild the chunk section so the model picks them up
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
}
