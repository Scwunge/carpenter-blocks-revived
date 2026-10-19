package com.carpentersblocks.integration.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;

import com.carpentersblocks.block.slope.SlopeItem;
import com.carpentersblocks.registry.SlopeContent;

/**
 * REI: tells slope kinds apart, since they differ only by a data component; REI then lists each kind from the
 * creative tab instead of merging them. Reached through REI's {@code rei_client} entrypoint on Fabric and the
 * annotated subclass in the NeoForge module, so it only loads with REI.
 */
public class ReiPlugin implements REIClientPlugin {

    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        if (SlopeContent.CARPENTERS_SLOPE_ITEM != null) {
            // By the kind itself: REI's EntryComparator.component hashed every kind the same. A bare slope is a wedge.
            registry.register((context, stack) -> SlopeItem.kind(stack).ordinal() + 1L,
                    SlopeContent.CARPENTERS_SLOPE_ITEM);
        }
    }
}
