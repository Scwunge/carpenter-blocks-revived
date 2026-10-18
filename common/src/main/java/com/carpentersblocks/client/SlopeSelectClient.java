package com.carpentersblocks.client;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.carpentersblocks.block.slope.SlopeItem;
import com.carpentersblocks.network.SlopeSelectPayload;

/**
 * Slope select: scrolling while sneaking with a Carpenter's Slope in hand changes its kind instead of the hotbar slot,
 * as the original's {@code EventHandler.onMouseEvent}. Each loader calls {@link #onScroll} from its scroll hook.
 */
public final class SlopeSelectClient {

    /** Sends a payload to the server; set by each loader's client entry point. */
    public static Consumer<CustomPacketPayload> sender = payload -> {};

    private SlopeSelectClient() {}

    /** Returns true when the scroll was used, so the loader skips vanilla's hotbar scrolling. */
    public static boolean onScroll(double delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.screen != null || player == null || !player.isShiftKeyDown() || player.isSpectator()
                || !(player.getMainHandItem().getItem() instanceof SlopeItem)) {
            return false;
        }
        if (delta != 0) {
            sender.accept(new SlopeSelectPayload(player.getInventory().selected, delta > 0));
        }
        return true;
    }
}
