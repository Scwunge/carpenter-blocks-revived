package com.carpentersblocks.block.slope;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.carpentersblocks.registry.SlopeContent;

/** The Carpenter's Slope item; its kind is stored in the {@code carpentersblocks:slope_kind} data component. */
public class SlopeItem extends BlockItem {

    public SlopeItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static SlopeKind kind(ItemStack stack) {
        SlopeKind kind = SlopeContent.SLOPE_KIND == null ? null : stack.get(SlopeContent.SLOPE_KIND);
        return kind == null ? SlopeKind.WEDGE : kind;
    }

    public static ItemStack stack(Block block, SlopeKind kind) {
        ItemStack stack = new ItemStack(block);
        stack.set(SlopeContent.SLOPE_KIND, kind);
        return stack;
    }

    /** One stack per kind, for the creative tab. */
    public static List<ItemStack> allKinds(Block block) {
        List<ItemStack> stacks = new ArrayList<>();
        for (SlopeKind kind : SlopeKind.values()) {
            stacks.add(stack(block, kind));
        }
        return stacks;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId() + "." + kind(stack).getSerializedName();
    }
}
