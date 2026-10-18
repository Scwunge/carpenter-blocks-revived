package com.carpentersblocks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.slope.SlopeItem;
import com.carpentersblocks.registry.SlopeContent;

/**
 * Sent when a sneaking player scrolls while holding a Carpenter's Slope: steps the slope in that hotbar slot to the
 * next or previous item kind. Port of the original {@code PacketSlopeSelect}.
 *
 * @param slot    the hotbar slot holding the slope
 * @param forward scrolled up, to the next kind
 */
public record SlopeSelectPayload(int slot, boolean forward) implements CustomPacketPayload {

    public static final Type<SlopeSelectPayload> TYPE = new Type<>(CarpentersBlocks.id("slope_select"));

    public static final StreamCodec<FriendlyByteBuf, SlopeSelectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SlopeSelectPayload::slot,
            ByteBufCodecs.BOOL, SlopeSelectPayload::forward,
            SlopeSelectPayload::new);

    @Override
    public Type<SlopeSelectPayload> type() {
        return TYPE;
    }

    /** Server side, on the server thread. */
    public static void handle(SlopeSelectPayload payload, Player player) {
        if (payload.slot < 0 || payload.slot >= Inventory.getSelectionSize()) {
            return;
        }
        ItemStack stack = player.getInventory().getItem(payload.slot);
        if (stack.getItem() instanceof SlopeItem) {
            stack.set(SlopeContent.SLOPE_KIND, SlopeItem.kind(stack).cycle(payload.forward));
        }
    }
}
