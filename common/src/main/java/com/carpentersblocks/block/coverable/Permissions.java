package com.carpentersblocks.block.coverable;

import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import com.carpentersblocks.config.CBConfig;

/** Who may change a block's attributes. Port of {@code PlayerPermissions}, with owners stored by UUID. */
public final class Permissions {

    private Permissions() {}

    /**
     * Whether the player may alter the block: always in singleplayer and for operators; otherwise anyone unless
     * ownership is enforced (by the config or the caller), in which case only the owner.
     * <p>
     * On the client this always answers yes; the server makes the real decision.
     */
    public static boolean hasElevatedPermission(CoverableBlockEntity blockEntity, Player player,
            boolean enforceOwnership) {
        MinecraftServer server = player.getServer();
        if (server == null || server.isSingleplayer()) {
            return true;
        }
        if (server.getPlayerList().isOp(player.getGameProfile())) {
            return true;
        }
        if (!enforceOwnership && !CBConfig.enableOwnership) {
            return true;
        }
        return isOwner(blockEntity, player);
    }

    /** Blocks without a recorded owner (placed by commands, for instance) belong to everyone. */
    public static boolean isOwner(CoverableBlockEntity blockEntity, Player player) {
        UUID owner = blockEntity.getOwner();
        return owner == null || owner.equals(player.getUUID());
    }
}
