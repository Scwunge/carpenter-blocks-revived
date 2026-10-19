package com.carpentersblocks.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.slope.SlopeKind;
import com.carpentersblocks.registry.SlopeContent;

/**
 * JEI: lists each slope kind as its own entry, since they differ only by a data component. Found by JEI's
 * {@code @JeiPlugin} scan on NeoForge and its {@code jei_mod_plugin} entrypoint on Fabric, so it only loads with JEI.
 */
@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return CarpentersBlocks.id("jei");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        if (SlopeContent.CARPENTERS_SLOPE_ITEM != null) {
            registration.registerSubtypeInterpreter(SlopeContent.CARPENTERS_SLOPE_ITEM, SlopeKinds.INSTANCE);
        }
    }

    private enum SlopeKinds implements ISubtypeInterpreter<ItemStack> {
        INSTANCE;

        @Override
        @Nullable
        public Object getSubtypeData(ItemStack stack, UidContext context) {
            return stack.get(SlopeContent.SLOPE_KIND);
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
            SlopeKind kind = stack.get(SlopeContent.SLOPE_KIND);
            return kind == null ? "" : kind.getSerializedName();
        }
    }
}
