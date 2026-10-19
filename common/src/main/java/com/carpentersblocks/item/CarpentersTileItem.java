package com.carpentersblocks.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import com.carpentersblocks.entity.CarpentersTileEntity;
import com.carpentersblocks.entity.TileGeometry;

/**
 * The Carpenter's Tile item: places a tile on the clicked face. Unless sneaking, the new tile takes the look of the
 * tile beyond the nearest edge when clicked near one, or of the tiles along each edge when clicked in the middle. Port
 * of {@code ItemCarpentersTile}.
 */
public class CarpentersTileItem extends Item {

    public CarpentersTileItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction face = context.getClickedFace();
        BlockPos pos = context.getClickedPos().relative(face);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        if (player != null && !player.mayUseItemAt(pos, face, stack)) {
            return InteractionResult.FAIL;
        }
        CarpentersTileEntity tile = new CarpentersTileEntity(level, pos, face);
        if (!tile.survives()) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            if (player != null) {
                tile.setOwner(player.getUUID());
            }
            if (player == null || !player.isSecondaryUseActive()) {
                tile.matchNeighbors(TileGeometry.edge(face, context.getClickLocation().subtract(Vec3.atLowerCornerOf(
                        context.getClickedPos()))));
            }
            tile.playPlacementSound();
            level.gameEvent(player, GameEvent.ENTITY_PLACE, tile.position());
            level.addFreshEntity(tile);
            stack.consume(1, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
