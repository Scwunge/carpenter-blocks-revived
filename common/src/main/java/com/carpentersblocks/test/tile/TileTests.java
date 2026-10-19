package com.carpentersblocks.test.tile;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.carpentersblocks.entity.CarpentersTileEntity;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.registry.TileContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 10: Carpenter's Tile placement, support, hammer designs and turns, dye, removal and neighbor matching. */
public class TileTests {

    /** A stone block whose north face takes the tiles, in the cell north of it. */
    private static final BlockPos WALL = new BlockPos(2, 1, 2);
    private static final BlockPos CELL = WALL.north();

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static Player setUp(GameTestHelper helper, GameType mode) {
        helper.setBlock(WALL, Blocks.STONE);
        helper.setBlock(WALL.east(), Blocks.STONE);
        return helper.makeMockPlayer(mode);
    }

    /** Uses a tile item on the north face of {@code wall} at {@code u, v} across it (0 to 1, x then y). */
    private static void place(GameTestHelper helper, Player player, BlockPos wall, double u, double v) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TileContent.CARPENTERS_TILE_ITEM, 4));
        BlockPos abs = helper.absolutePos(wall);
        Vec3 at = new Vec3(abs.getX() + u, abs.getY() + v, abs.getZ());
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(at, Direction.NORTH, abs, false)));
    }

    private static List<CarpentersTileEntity> tiles(GameTestHelper helper, BlockPos cell) {
        return helper.getLevel().getEntitiesOfClass(CarpentersTileEntity.class, new AABB(helper.absolutePos(cell)));
    }

    private static CarpentersTileEntity tile(GameTestHelper helper, BlockPos cell) {
        List<CarpentersTileEntity> tiles = tiles(helper, cell);
        if (tiles.size() != 1) {
            helper.fail("expected one tile at " + cell + ", found " + tiles.size());
        }
        return tiles.get(0);
    }

    private static void hold(Player player, Item item, boolean sneaking) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
        player.setShiftKeyDown(sneaking);
    }

    private static boolean attack(GameTestHelper helper, Player player, CarpentersTileEntity tile) {
        return tile.hurt(helper.getLevel().damageSources().playerAttack(player), 1);
    }

    @CBGameTest
    public void placedAgainstTheClickedFace(GameTestHelper helper) {
        Player player = setUp(helper, GameType.SURVIVAL);
        place(helper, player, WALL, 0.5, 0.5);
        CarpentersTileEntity tile = tile(helper, CELL);
        check(helper, tile.getDirection() == Direction.NORTH, "facing out of the wall");
        double back = tile.getBoundingBox().maxZ;
        check(helper, Math.abs(back - helper.absolutePos(WALL).getZ()) < 1e-6, "against the wall: " + back);
        check(helper, player.getMainHandItem().getCount() == 3, "one tile used");
        check(helper, tile.getDesign().isEmpty() && tile.getDye() == DyeColor.WHITE, "blank and white");
        place(helper, player, WALL.above(), 0.5, 0.5);
        check(helper, tiles(helper, CELL.above()).isEmpty(), "nothing to hold a tile in the air");
        helper.succeed();
    }

    @CBGameTest(timeoutTicks = 60)
    public void dropsWhenTheWallGoes(GameTestHelper helper) {
        Player player = setUp(helper, GameType.SURVIVAL);
        place(helper, player, WALL, 0.5, 0.5);
        helper.setBlock(WALL, Blocks.AIR);
        helper.succeedWhen(() -> {
            helper.assertEntityNotPresent(TileContent.CARPENTERS_TILE, CELL);
            helper.assertItemEntityPresent(TileContent.CARPENTERS_TILE_ITEM, CELL, 2);
        });
    }

    @CBGameTest
    public void hammerCyclesDesignsAndTurns(GameTestHelper helper) {
        Player player = setUp(helper, GameType.SURVIVAL);
        place(helper, player, WALL, 0.5, 0.5);
        CarpentersTileEntity tile = tile(helper, CELL);
        hold(player, ModContent.CARPENTERS_HAMMER, false);
        tile.interact(player, InteractionHand.MAIN_HAND);
        check(helper, tile.getDesign().equals("stock_striped"), "right-click: previous design, from the last");
        check(helper, !attack(helper, player, tile) && tile.getDesign().equals("stock_chain"),
                "left-click: next design, wrapping, and the tile stays");
        hold(player, ModContent.CARPENTERS_HAMMER, true);
        tile.interact(player, InteractionHand.MAIN_HAND);
        tile.interact(player, InteractionHand.MAIN_HAND);
        check(helper, tile.getRotation() == 2 && tile.getDesign().equals("stock_chain"), "sneak: two quarter turns");
        helper.succeed();
    }

    @CBGameTest
    public void sneakDyeColors(GameTestHelper helper) {
        Player player = setUp(helper, GameType.SURVIVAL);
        place(helper, player, WALL, 0.5, 0.5);
        CarpentersTileEntity tile = tile(helper, CELL);
        hold(player, Items.BLUE_DYE, false);
        tile.interact(player, InteractionHand.MAIN_HAND);
        check(helper, tile.getDye() == DyeColor.WHITE, "not without sneaking");
        hold(player, Items.BLUE_DYE, true);
        tile.interact(player, InteractionHand.MAIN_HAND);
        check(helper, tile.getDye() == DyeColor.BLUE, "blue");
        hold(player, Items.WHITE_DYE, true);
        tile.interact(player, InteractionHand.MAIN_HAND);
        check(helper, tile.getDye() == DyeColor.WHITE, "and back to white");
        helper.succeed();
    }

    @CBGameTest
    public void sneakHammerTakesItDown(GameTestHelper helper) {
        Player player = setUp(helper, GameType.SURVIVAL);
        place(helper, player, WALL, 0.5, 0.5);
        CarpentersTileEntity tile = tile(helper, CELL);
        hold(player, Items.STICK, false);
        check(helper, !attack(helper, player, tile) && tile.isAlive(), "survival without the hammer does nothing");
        hold(player, ModContent.CARPENTERS_HAMMER, true);
        check(helper, attack(helper, player, tile) && !tile.isAlive(), "sneak + hammer removes it");
        helper.assertItemEntityPresent(TileContent.CARPENTERS_TILE_ITEM, CELL, 2);
        helper.succeed();
    }

    @CBGameTest
    public void creativeRemovesWithoutDrop(GameTestHelper helper) {
        Player player = setUp(helper, GameType.CREATIVE);
        place(helper, player, WALL, 0.5, 0.5);
        CarpentersTileEntity tile = tile(helper, CELL);
        hold(player, Items.STICK, false);
        check(helper, attack(helper, player, tile) && !tile.isAlive(), "creative left-click removes it");
        helper.assertItemEntityNotPresent(TileContent.CARPENTERS_TILE_ITEM, CELL, 2);
        helper.succeed();
    }

    @CBGameTest
    public void newTilesMatchTheirNeighbor(GameTestHelper helper) {
        Player player = setUp(helper, GameType.SURVIVAL);
        place(helper, player, WALL, 0.5, 0.5);
        CarpentersTileEntity first = tile(helper, CELL);
        first.setDesign("stock_grid");
        first.setDye(DyeColor.RED);
        first.setRotation(1);
        // Near the west edge of the next wall block: the edge toward the first tile
        place(helper, player, WALL.east(), 0.1, 0.5);
        CarpentersTileEntity second = tile(helper, CELL.east());
        check(helper, second.getDesign().equals("stock_grid") && second.getDye() == DyeColor.RED
                && second.getRotation() == 1, "copied the neighbor");
        second.discard();
        player.setShiftKeyDown(true);
        place(helper, player, WALL.east(), 0.1, 0.5);
        CarpentersTileEntity third = tile(helper, CELL.east());
        check(helper, third.getDesign().isEmpty() && third.getDye() == DyeColor.WHITE, "sneaking places it plain");
        helper.succeed();
    }
}
