package com.carpentersblocks.fabric;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.block.bed.CarpentersBedBlock;

/**
 * Lets Fabric's sleeping code treat the Carpenter's Bed as a bed: sleepers stay in it, lie the right way and get up
 * beside it. Respawning goes through {@code ServerPlayerMixin}. NeoForge asks the block itself.
 */
final class FabricBeds {

    private FabricBeds() {}

    static void register() {
        EntitySleepEvents.ALLOW_BED.register((entity, sleepingPos, state, vanillaResult) ->
                state.getBlock() instanceof CarpentersBedBlock ? InteractionResult.SUCCESS : InteractionResult.PASS);

        EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register((entity, sleepingPos, sleepingDirection) -> {
            BlockState state = entity.level().getBlockState(sleepingPos);
            return sleepingDirection == null && state.getBlock() instanceof CarpentersBedBlock
                    ? state.getValue(CarpentersBedBlock.FACING) : sleepingDirection;
        });

        EntitySleepEvents.MODIFY_WAKE_UP_POSITION.register((entity, sleepingPos, bedState, wakeUpPos) -> {
            if (wakeUpPos != null || !(bedState.getBlock() instanceof CarpentersBedBlock)) {
                return wakeUpPos;
            }
            return BedBlock.findStandUpPosition(entity.getType(), entity.level(), sleepingPos,
                    bedState.getValue(CarpentersBedBlock.FACING), entity.getYRot()).orElse(null);
        });
    }
}
