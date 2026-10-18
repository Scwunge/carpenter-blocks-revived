package com.carpentersblocks.neoforge;

import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.neoforge.client.CarpentersBlocksNeoForgeClient;
import com.carpentersblocks.registry.ModContent;

@Mod(CarpentersBlocks.MOD_ID)
public class CarpentersBlocksNeoForge {

    public CarpentersBlocksNeoForge(IEventBus modBus, Dist dist) {
        modBus.addListener(CarpentersBlocksNeoForge::register);
        if (dist.isClient()) {
            CarpentersBlocksNeoForgeClient.init(modBus);
        }
    }

    private static void register(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> ModContent.registerBlocks(helper::register));
        event.register(Registries.ITEM, helper -> ModContent.registerItems(helper::register));
        event.register(Registries.BLOCK_ENTITY_TYPE,
                helper -> ModContent.registerBlockEntities(helper::register, new ModContent.BlockEntityTypeFactory() {
                    @Override
                    public <T extends BlockEntity> BlockEntityType<T> create(
                            BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
                        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
                    }
                }));
        event.register(Registries.CREATIVE_MODE_TAB,
                helper -> helper.register(ModContent.TAB_ID, ModContent.buildTab(CreativeModeTab.builder())));
    }
}
