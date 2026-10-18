package com.carpentersblocks.neoforge.test;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.test.CBGameTests;

/** Hands the common GameTests to NeoForge. The event only fires when GameTests are enabled (dev runs). */
@EventBusSubscriber(modid = CarpentersBlocks.MOD_ID)
public final class NeoForgeGameTests {

    private NeoForgeGameTests() {}

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        event.register(CBGameTests.class);
    }
}
