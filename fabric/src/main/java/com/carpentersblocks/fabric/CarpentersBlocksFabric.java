package com.carpentersblocks.fabric;

import java.util.function.BiFunction;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.registry.ModContent;

public class CarpentersBlocksFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModContent.registerBlocks((id, block) -> Registry.register(BuiltInRegistries.BLOCK, id, block));
        ModContent.registerItems((id, item) -> Registry.register(BuiltInRegistries.ITEM, id, item));
        ModContent.registerBlockEntities(
                (id, type) -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type),
                new ModContent.BlockEntityTypeFactory() {
                    @Override
                    public <T extends BlockEntity> BlockEntityType<T> create(
                            BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
                        return FabricBlockEntityTypeBuilder.create(factory::apply, blocks).build();
                    }
                });
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ModContent.TAB_ID,
                ModContent.buildTab(FabricItemGroup.builder()));

        // Clicks go to the block before vanilla's sneak check, and with the clicked face (see CoverableBlock)
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            BlockState state = level.getBlockState(hit.getBlockPos());
            if (!player.isSpectator() && state.getBlock() instanceof CoverableBlock block) {
                return block.use(state, level, hit.getBlockPos(), player, hand, hit);
            }
            return InteractionResult.PASS;
        });
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            BlockState state = level.getBlockState(pos);
            if (!player.isSpectator() && state.getBlock() instanceof CoverableBlock block
                    && block.attack(state, level, pos, player, direction)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }
}
