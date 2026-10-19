package com.carpentersblocks.block.safe;

import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.registry.SafeContent;

/**
 * A Carpenter's Safe's block entity: the covers, plus 27 slots (54 with an upgrade), the upgrade item and the
 * automation setting. Hoppers and pipes reach the slots through every side but the front, as the setting allows.
 * Clients get only how full the safe is, for the capacity lights. Port of the original {@code TECarpentersSafe}.
 */
public class SafeBlockEntity extends CoverableBlockEntity implements WorldlyContainer {

    public static final int SLOTS = 27;
    public static final int UPGRADED_SLOTS = 54;

    private static final String TAG_UPGRADE = "Upgrade";
    private static final String TAG_AUTOMATION = "Automation";
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_CAPACITY = "Capacity";

    private static final int[] SLOTS_SMALL = IntStream.range(0, SLOTS).toArray();
    private static final int[] SLOTS_LARGE = IntStream.range(0, UPGRADED_SLOTS).toArray();

    private final NonNullList<ItemStack> items = NonNullList.withSize(UPGRADED_SLOTS, ItemStack.EMPTY);
    private ItemStack upgrade = ItemStack.EMPTY;
    private SafeAutomation automation = SafeAutomation.DISABLED;
    /** Lit capacity lights; computed from the items on the server, sent to clients. */
    private int capacity;

    private final ContainerOpenersCounter openers = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            playSound(level, pos, true);
            setOpen(level, pos, state, true);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            playSound(level, pos, false);
            setOpen(level, pos, state, false);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int count, int openCount) {}

        @Override
        protected boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof ChestMenu menu && menu.getContainer() == SafeBlockEntity.this;
        }
    };

    public SafeBlockEntity(BlockPos pos, BlockState state) {
        super(SafeContent.SAFE_BLOCK_ENTITY, pos, state);
    }

    private static void playSound(Level level, BlockPos pos, boolean open) {
        level.playSound(null, pos, open ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE,
                SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        level.gameEvent(null, open ? GameEvent.CONTAINER_OPEN : GameEvent.CONTAINER_CLOSE, pos);
    }

    private static void setOpen(Level level, BlockPos pos, BlockState state, boolean open) {
        if (state.hasProperty(CarpentersSafeBlock.OPEN)) {
            level.setBlock(pos, state.setValue(CarpentersSafeBlock.OPEN, open), Block.UPDATE_ALL);
        }
    }

    /** Called from the block's scheduled tick, as the barrel's, to notice players who left without closing. */
    public void recheckOpen() {
        if (!remove && level != null) {
            openers.recheckOpeners(level, worldPosition, getBlockState());
        }
    }

    // Upgrade and automation

    public boolean hasUpgrade() {
        return !upgrade.isEmpty();
    }

    public ItemStack getUpgrade() {
        return upgrade;
    }

    public void setUpgrade(ItemStack stack) {
        upgrade = stack.copyWithCount(1);
        setChanged();
        sendUpdate();
    }

    public SafeAutomation getAutomation() {
        return automation;
    }

    public void setAutomation(SafeAutomation automation) {
        this.automation = automation;
        setChanged();
    }

    /** Lit capacity lights (0 to 9). */
    public int getCapacity() {
        return capacity;
    }

    private int computeCapacity() {
        int filled = 0;
        int size = getContainerSize();
        for (int slot = 0; slot < size; ++slot) {
            if (!items.get(slot).isEmpty()) {
                ++filled;
            }
        }
        return SafeGeometry.capacity(filled, size);
    }

    private void sendUpdate() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** Saves, and redraws the safe for clients when the capacity lights change. */
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            int updated = computeCapacity();
            if (updated != capacity) {
                capacity = updated;
                sendUpdate();
            }
        }
    }

    // Container

    @Override
    public int getContainerSize() {
        return hasUpgrade() ? UPGRADED_SLOTS : SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void startOpen(Player player) {
        if (!remove && !player.isSpectator() && level != null) {
            openers.incrementOpeners(player, level, worldPosition, getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!remove && !player.isSpectator() && level != null) {
            openers.decrementOpeners(player, level, worldPosition, getBlockState());
        }
    }

    @Override
    public void clearContent() {
        items.clear();
        upgrade = ItemStack.EMPTY;
        super.clearContent();
    }

    // Automation, as the original's ISidedInventory: every slot from every side but the front

    @Override
    public int[] getSlotsForFace(Direction side) {
        return hasUpgrade() ? SLOTS_LARGE : SLOTS_SMALL;
    }

    private boolean isFront(@Nullable Direction side) {
        BlockState state = getBlockState();
        return side != null && state.hasProperty(CarpentersSafeBlock.FACING)
                && state.getValue(CarpentersSafeBlock.FACING) == side;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return automation.allowsInsertion() && !isFront(side);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return automation.allowsExtraction() && !isFront(side);
    }

    // Drops and saving

    /** The covers' items, then the contents and the upgrade, as the original's drops. */
    @Override
    public List<ItemStack> getAttributeDrops() {
        List<ItemStack> drops = super.getAttributeDrops();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        if (hasUpgrade()) {
            drops.add(upgrade.copy());
        }
        return drops;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        if (hasUpgrade()) {
            tag.put(TAG_UPGRADE, upgrade.save(registries));
        }
        tag.putString(TAG_AUTOMATION, automation.getSerializedName());
    }

    /** Reads the safe's own data before the covers, whose loading asks the client to redraw the safe. */
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        upgrade = ItemStack.parseOptional(registries, tag.getCompound(TAG_UPGRADE));
        automation = SafeAutomation.byName(tag.getString(TAG_AUTOMATION));
        if (tag.contains(TAG_ITEMS)) {
            items.clear();
            ContainerHelper.loadAllItems(tag, items, registries);
        }
        capacity = tag.contains(TAG_CAPACITY) ? tag.getInt(TAG_CAPACITY) : computeCapacity();
        super.loadAdditional(tag, registries);
    }

    /** Clients get how full the safe is, not what is in it. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.remove(TAG_ITEMS);
        tag.putInt(TAG_CAPACITY, capacity);
        return tag;
    }
}
