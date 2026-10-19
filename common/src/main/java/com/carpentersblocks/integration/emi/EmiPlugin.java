package com.carpentersblocks.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;

import com.carpentersblocks.registry.SlopeContent;

/**
 * EMI: tells slope kinds apart, since they differ only by a data component. Found by EMI's {@code @EmiEntrypoint}
 * scan on NeoForge and its {@code emi} entrypoint on Fabric, so it only loads with EMI.
 */
@EmiEntrypoint
public class EmiPlugin implements dev.emi.emi.api.EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        if (SlopeContent.CARPENTERS_SLOPE_ITEM != null) {
            registry.setDefaultComparison(SlopeContent.CARPENTERS_SLOPE_ITEM,
                    Comparison.compareData(stack -> stack.getItemStack().get(SlopeContent.SLOPE_KIND)));
        }
    }
}
