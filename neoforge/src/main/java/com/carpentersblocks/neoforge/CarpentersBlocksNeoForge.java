package com.carpentersblocks.neoforge;

import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.carpentersblocks.network.EnrichPlantPayload;
import com.carpentersblocks.network.SlopeSelectPayload;
import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.neoforge.client.CarpentersBlocksNeoForgeClient;
import com.carpentersblocks.platform.Platform;
import com.carpentersblocks.registry.BedContent;
import com.carpentersblocks.registry.ModContent;

@Mod(CarpentersBlocks.MOD_ID)
public class CarpentersBlocksNeoForge {

    public CarpentersBlocksNeoForge(IEventBus modBus, Dist dist) {
        Platform.set(new NeoForgePlatform());
        com.carpentersblocks.neoforge.config.NeoForgeConfig.load();
        BedContent.factory = NeoForgeBedBlock::new;
        modBus.addListener(CarpentersBlocksNeoForge::register);
        modBus.addListener(CarpentersBlocksNeoForge::registerPayloads);
        NeoForge.EVENT_BUS.addListener(CarpentersBlocksNeoForge::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(CarpentersBlocksNeoForge::onLeftClickBlock);
        if (dist.isClient()) {
            CarpentersBlocksNeoForgeClient.init(modBus);
        }
    }

    /** Clicks go to the block before vanilla's sneak check, and with the clicked face (see CoverableBlock). */
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!player.isSpectator() && state.getBlock() instanceof CoverableBlock block) {
            InteractionResult result = block.use(state, event.getLevel(), event.getPos(), player, event.getHand(),
                    event.getHitVec());
            if (result != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }
        }
    }

    private static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START || event.getFace() == null) {
            return;
        }
        Player player = event.getEntity();
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!player.isSpectator() && state.getBlock() instanceof CoverableBlock block
                && block.attack(state, event.getLevel(), event.getPos(), player, event.getFace())) {
            event.setCanceled(true);
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(SlopeSelectPayload.TYPE, SlopeSelectPayload.STREAM_CODEC,
                        (payload, context) -> SlopeSelectPayload.handle(payload, context.player()))
                .playToServer(EnrichPlantPayload.TYPE, EnrichPlantPayload.STREAM_CODEC,
                        (payload, context) -> EnrichPlantPayload.handle(payload, context.player()));
    }

    private static void register(RegisterEvent event) {
        event.register(Registries.DATA_COMPONENT_TYPE, helper -> ModContent.registerDataComponents(helper::register));
        event.register(Registries.BLOCK, helper -> ModContent.registerBlocks(helper::register));
        event.register(Registries.RECIPE_SERIALIZER,
                helper -> ModContent.registerRecipeSerializers(helper::register));
        event.register(Registries.ENTITY_TYPE, helper -> ModContent.registerEntityTypes(helper::register));
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
