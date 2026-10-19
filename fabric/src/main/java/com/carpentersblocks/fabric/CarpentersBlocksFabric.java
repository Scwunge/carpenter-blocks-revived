package com.carpentersblocks.fabric;

import java.util.function.BiFunction;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.network.EnrichPlantPayload;
import com.carpentersblocks.network.SlopeSelectPayload;
import com.carpentersblocks.platform.Platform;
import com.carpentersblocks.registry.ModContent;

public class CarpentersBlocksFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Platform.set(new FabricPlatform());
        com.carpentersblocks.fabric.config.FabricConfig.load();
        FabricBeds.register();
        PayloadTypeRegistry.playC2S().register(SlopeSelectPayload.TYPE, SlopeSelectPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SlopeSelectPayload.TYPE,
                (payload, context) -> SlopeSelectPayload.handle(payload, context.player()));
        PayloadTypeRegistry.playC2S().register(EnrichPlantPayload.TYPE, EnrichPlantPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EnrichPlantPayload.TYPE,
                (payload, context) -> EnrichPlantPayload.handle(payload, context.player()));
        ModContent.registerDataComponents(
                (id, type) -> Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, type));
        ModContent.registerBlocks((id, block) -> Registry.register(BuiltInRegistries.BLOCK, id, block));
        ModContent.registerRecipeSerializers(
                (id, serializer) -> Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer));
        ModContent.registerEntityTypes((id, type) -> Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type));
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

        // Lets fire treat the frames as flammable; how well each one burns follows its covers (FireBlockMixin)
        for (Block block : ModContent.coverableBlocks()) {
            FlammableBlockRegistry.getDefaultInstance().add(block, CoverableBlock.FRAME_FIRE_SPREAD,
                    CoverableBlock.FRAME_FLAMMABILITY);
        }

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
