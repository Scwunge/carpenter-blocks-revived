package com.carpentersblocks.integration.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;

import com.carpentersblocks.registry.SlopeContent;

/**
 * REI: tells slope kinds apart, since they differ only by a data component. Reached through REI's {@code rei_client}
 * entrypoint on Fabric and the annotated subclass in the NeoForge module, so it only loads with REI.
 */
public class ReiPlugin implements REIClientPlugin {

    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        if (SlopeContent.CARPENTERS_SLOPE_ITEM != null) {
            EntryComparator<net.minecraft.core.component.DataComponentMap> kind =
                    EntryComparator.component(SlopeContent.SLOPE_KIND);
            registry.register((context, stack) -> kind.hash(context, stack.getComponents()),
                    SlopeContent.CARPENTERS_SLOPE_ITEM);
        }
    }
}
