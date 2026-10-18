package com.carpentersblocks.neoforge.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.client.frame.SlopeItemModel;

/** Draws the slope item as the kind in its stack's component, through item overrides. */
public class NeoForgeSlopeItemModel extends BakedModelWrapper<SlopeItemModel> {

    private final ItemOverrides overrides;

    public NeoForgeSlopeItemModel(SlopeItemModel slopes) {
        super(slopes);
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                    @Nullable LivingEntity entity, int seed) {
                return slopes.resolve(stack);
            }
        };
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }
}
