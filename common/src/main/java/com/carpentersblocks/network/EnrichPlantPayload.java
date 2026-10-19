package com.carpentersblocks.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.flowerpot.CarpentersFlowerPotBlock;

/**
 * Sent when a player uses fertilizer on a flower pot whose plant takes a tint, which only the client can tell. Port of
 * the original {@code PacketEnrichPlant}, which sent the plant's color for the server to check instead.
 *
 * @param pos the flower pot
 */
public record EnrichPlantPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<EnrichPlantPayload> TYPE = new Type<>(CarpentersBlocks.id("enrich_plant"));

    public static final StreamCodec<FriendlyByteBuf, EnrichPlantPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, EnrichPlantPayload::pos,
            EnrichPlantPayload::new);

    @Override
    public Type<EnrichPlantPayload> type() {
        return TYPE;
    }

    /** Server side, on the server thread. */
    public static void handle(EnrichPlantPayload payload, Player player) {
        if (player.canInteractWithBlock(payload.pos, 1.0)
                && player.level().getBlockState(payload.pos).getBlock() instanceof CarpentersFlowerPotBlock) {
            CarpentersFlowerPotBlock.enrich(player.level(), payload.pos, player);
        }
    }
}
