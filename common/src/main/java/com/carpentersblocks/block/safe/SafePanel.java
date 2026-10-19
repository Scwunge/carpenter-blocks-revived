package com.carpentersblocks.block.safe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

/**
 * The metal of a safe's front panel and handle: iron, or the material of its upgrade. The original matched the upgrade
 * by the ore dictionary names ingotGold, gemDiamond and gemEmerald; these are the common tags that replaced them.
 */
public enum SafePanel {
    IRON(null, Blocks.IRON_BLOCK, "iron_block"),
    GOLD("ingots/gold", Blocks.GOLD_BLOCK, "gold_block"),
    DIAMOND("gems/diamond", Blocks.DIAMOND_BLOCK, "diamond_block"),
    EMERALD("gems/emerald", Blocks.EMERALD_BLOCK, "emerald_block");

    @Nullable
    private final TagKey<Item> upgrade;
    private final Block block;
    private final ResourceLocation texture;

    SafePanel(@Nullable String upgradeTag, Block block, String texture) {
        this.upgrade = upgradeTag == null ? null
                : TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", upgradeTag));
        this.block = block;
        this.texture = ResourceLocation.withDefaultNamespace("block/" + texture);
    }

    /** The block the panel looks like; its sound plays when the upgrade goes in. */
    public Block block() {
        return block;
    }

    public ResourceLocation texture() {
        return texture;
    }

    /** The panel an upgrade item gives, or {@code null} if the item isn't an upgrade. */
    @Nullable
    public static SafePanel forUpgrade(ItemStack stack) {
        for (SafePanel panel : values()) {
            if (panel.upgrade != null && stack.is(panel.upgrade)) {
                return panel;
            }
        }
        return null;
    }

    /** The panel of a safe with this upgrade (iron with none). */
    public static SafePanel of(ItemStack upgrade) {
        SafePanel panel = upgrade.isEmpty() ? null : forUpgrade(upgrade);
        return panel == null ? IRON : panel;
    }
}
