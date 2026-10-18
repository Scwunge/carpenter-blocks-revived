package com.carpentersblocks.block.coverable;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;

/**
 * Surface overlays that can be applied over a cover, such as grass or snow. Port of {@code OverlayHandler}.
 * <p>
 * The original mapped items to overlays by display name in the config; here each overlay has an item tag,
 * {@code carpentersblocks:overlays/<name>}, so packs can add items.
 */
public enum Overlay implements StringRepresentable {

    GRASS(Blocks.GRASS_BLOCK, true),
    SNOW(Blocks.SNOW_BLOCK, true),
    WEB(Blocks.COBWEB, false),
    VINE(Blocks.VINE, false),
    HAY(Blocks.HAY_BLOCK, true),
    MYCELIUM(Blocks.MYCELIUM, true);

    private final Block block;
    private final boolean topOnly;
    private final TagKey<Item> items;

    Overlay(Block block, boolean topOnly) {
        this.block = block;
        this.topOnly = topOnly;
        this.items = TagKey.create(Registries.ITEM, CarpentersBlocks.id("overlays/" + getSerializedName()));
    }

    /** The block the overlay imitates; its color handler tints the overlay. */
    public Block block() {
        return block;
    }

    public TagKey<Item> items() {
        return items;
    }

    /**
     * Whether the overlay hides the whole face, which decides the particles shown when the face is hit. Grass, snow,
     * hay and mycelium only fully cover the top face.
     */
    public boolean coversFullSide(Direction face) {
        return !topOnly || face == Direction.UP;
    }

    /**
     * Texture drawn over the given face, or {@code null} if that face shows nothing. Top-only overlays draw their
     * block's top on the top face, a fringe on the sides, and nothing underneath.
     */
    @Nullable
    public ResourceLocation texture(Direction face) {
        return switch (this) {
            case GRASS -> face == Direction.UP ? mc("grass_block_top")
                    : face == Direction.DOWN ? null : mc("grass_block_side_overlay");
            case SNOW -> face == Direction.UP ? mc("snow") : face == Direction.DOWN ? null : own("overlay_snow_side");
            case HAY -> face == Direction.UP ? mc("hay_block_top")
                    : face == Direction.DOWN ? null : own("overlay_hay_side");
            case MYCELIUM -> face == Direction.UP ? mc("mycelium_top")
                    : face == Direction.DOWN ? null : own("overlay_mycelium_side");
            case WEB -> mc("cobweb");
            case VINE -> mc("vine");
        };
    }

    /** Whether the overlay texture takes its block's biome color. */
    public boolean tinted() {
        return this == GRASS || this == VINE;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    @Nullable
    public static Overlay byName(String name) {
        for (Overlay overlay : values()) {
            if (overlay.getSerializedName().equals(name)) {
                return overlay;
            }
        }
        return null;
    }

    @Nullable
    public static Overlay forItem(ItemStack stack) {
        for (Overlay overlay : values()) {
            if (stack.is(overlay.items)) {
                return overlay;
            }
        }
        return null;
    }

    private static ResourceLocation mc(String name) {
        return ResourceLocation.withDefaultNamespace("block/" + name);
    }

    private static ResourceLocation own(String name) {
        return CarpentersBlocks.id("block/overlay/" + name);
    }
}
