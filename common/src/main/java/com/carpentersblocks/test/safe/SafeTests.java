package com.carpentersblocks.test.safe;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.carpentersblocks.block.safe.CarpentersSafeBlock;
import com.carpentersblocks.block.safe.SafeAutomation;
import com.carpentersblocks.block.safe.SafeBlockEntity;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.registry.SafeContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 9: the Carpenter's Safe's placement, menu, lock, ownership, upgrade, automation, lights and drops. */
public class SafeTests {

    private static final BlockPos SAFE = new BlockPos(1, 1, 1);

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction face) {
        Vec3 onFace = Vec3.atCenterOf(helper.absolutePos(pos)).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5));
        return new BlockHitResult(onFace, face, helper.absolutePos(pos), false);
    }

    /** A safe set at {@link #SAFE} facing north, unlocked or locked, with no owner. */
    private static SafeBlockEntity setSafe(GameTestHelper helper, boolean locked) {
        helper.setBlock(SAFE, SafeContent.CARPENTERS_SAFE.defaultBlockState()
                .setValue(CarpentersSafeBlock.FACING, Direction.NORTH)
                .setValue(CarpentersSafeBlock.LOCKED, locked));
        return safe(helper);
    }

    private static SafeBlockEntity safe(GameTestHelper helper) {
        return (SafeBlockEntity) helper.getBlockEntity(SAFE);
    }

    /** A server player (a mock one can't open menus) standing in front of the safe. */
    private static ServerPlayer visitor(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Vec3 front = Vec3.atCenterOf(helper.absolutePos(SAFE.north()));
        player.moveTo(front.x, front.y - 0.5, front.z, Direction.SOUTH.toYRot(), 0);
        return player;
    }

    private static void use(GameTestHelper helper, Player player) {
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(SAFE);
        BlockState state = level.getBlockState(abs);
        ((CarpentersSafeBlock) state.getBlock()).use(state, level, abs, player, InteractionHand.MAIN_HAND,
                hit(helper, SAFE, Direction.NORTH));
    }

    private static boolean hasSafeOpen(Player player) {
        return player.containerMenu instanceof ChestMenu menu && menu.getContainer() instanceof SafeBlockEntity;
    }

    @CBGameTest
    public void placedFacingThePlayerLockedAndSealed(GameTestHelper helper) {
        helper.setBlock(SAFE.below(), Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setYRot(Direction.SOUTH.toYRot());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SafeContent.CARPENTERS_SAFE));
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                hit(helper, SAFE.below(), Direction.UP)));
        BlockState state = helper.getBlockState(SAFE);
        check(helper, state.is(SafeContent.CARPENTERS_SAFE), "placed");
        check(helper, state.getValue(CarpentersSafeBlock.FACING) == Direction.NORTH, "door toward the player");
        check(helper, state.getValue(CarpentersSafeBlock.LOCKED), "new safes are locked");
        check(helper, safe(helper).getAutomation() == SafeAutomation.DISABLED, "and closed to hoppers");
        check(helper, player.getUUID().equals(safe(helper).getOwner()), "owned by the placer");
        check(helper, safe(helper).getContainerSize() == 27, "27 slots");
        helper.succeed();
    }

    @CBGameTest
    public void opensForOnePlayerAtATime(GameTestHelper helper) {
        setSafe(helper, false);
        ServerPlayer first = visitor(helper);
        use(helper, first);
        check(helper, hasSafeOpen(first), "the menu opens");
        check(helper, helper.getBlockState(SAFE).getValue(CarpentersSafeBlock.OPEN), "the door slides open");
        ServerPlayer second = visitor(helper);
        use(helper, second);
        check(helper, !hasSafeOpen(second), "a second player is kept out");
        first.closeContainer();
        check(helper, !helper.getBlockState(SAFE).getValue(CarpentersSafeBlock.OPEN), "closes with the menu");
        use(helper, second);
        check(helper, hasSafeOpen(second), "then the next player can open it");
        second.closeContainer();
        helper.succeed();
    }

    @CBGameTest
    public void lockedSafeOpensOnlyForItsOwner(GameTestHelper helper) {
        SafeBlockEntity safe = setSafe(helper, true);
        safe.setOwner(UUID.randomUUID());
        ServerPlayer stranger = visitor(helper);
        use(helper, stranger);
        check(helper, !hasSafeOpen(stranger), "locked: a stranger can't open it");
        BlockPos abs = helper.absolutePos(SAFE);
        check(helper, helper.getBlockState(SAFE).getDestroyProgress(stranger, helper.getLevel(), abs) == 0,
                "nor break it");
        safe.setOwner(stranger.getUUID());
        use(helper, stranger);
        check(helper, hasSafeOpen(stranger), "the owner can");
        check(helper, helper.getBlockState(SAFE).getDestroyProgress(stranger, helper.getLevel(), abs) == 0,
                "but not break it while it is open");
        stranger.closeContainer();
        helper.succeed();
    }

    @CBGameTest
    public void unlockedSafeOpensForAnyone(GameTestHelper helper) {
        setSafe(helper, false).setOwner(UUID.randomUUID());
        ServerPlayer stranger = visitor(helper);
        use(helper, stranger);
        check(helper, hasSafeOpen(stranger), "unlocked: anyone can open it");
        stranger.closeContainer();
        helper.succeed();
    }

    @CBGameTest
    public void upgradeDoublesTheSlots(GameTestHelper helper) {
        setSafe(helper, false);
        ServerPlayer player = visitor(helper);
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND, 2));
        player.setShiftKeyDown(true);
        use(helper, player);
        player.setShiftKeyDown(false);
        check(helper, safe(helper).hasUpgrade(), "sneak with a diamond: upgraded");
        check(helper, player.getMainHandItem().getCount() == 1, "one diamond used");
        check(helper, safe(helper).getContainerSize() == 54, "54 slots");
        player.setShiftKeyDown(true);
        use(helper, player);
        player.setShiftKeyDown(false);
        check(helper, player.getMainHandItem().getCount() == 1, "only one upgrade");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, player);
        check(helper, player.containerMenu instanceof ChestMenu menu && menu.getRowCount() == 6, "a six-row menu");
        player.closeContainer();
        helper.succeed();
    }

    @CBGameTest
    public void hammerCyclesAutomationAndLocks(GameTestHelper helper) {
        setSafe(helper, true);
        ServerPlayer player = visitor(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        SafeAutomation[] order = { SafeAutomation.SEND, SafeAutomation.RECEIVE, SafeAutomation.ALL,
                SafeAutomation.DISABLED };
        for (SafeAutomation expected : order) {
            use(helper, player);
            check(helper, safe(helper).getAutomation() == expected, "right-click: " + expected);
        }
        player.setShiftKeyDown(true);
        use(helper, player);
        check(helper, !helper.getBlockState(SAFE).getValue(CarpentersSafeBlock.LOCKED), "sneak: unlocked");
        check(helper, safe(helper).getAutomation() == SafeAutomation.ALL, "unlocking opens it to hoppers");
        use(helper, player);
        check(helper, helper.getBlockState(SAFE).getValue(CarpentersSafeBlock.LOCKED), "sneak again: locked");
        check(helper, safe(helper).getAutomation() == SafeAutomation.DISABLED, "locking closes it to hoppers");
        helper.succeed();
    }

    @CBGameTest
    public void strangersCantChangeALockedSafe(GameTestHelper helper) {
        setSafe(helper, true).setOwner(UUID.randomUUID());
        ServerPlayer stranger = visitor(helper);
        stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        BlockPos abs = helper.absolutePos(SAFE);
        BlockState state = helper.getBlockState(SAFE);
        use(helper, stranger);
        ((CarpentersSafeBlock) state.getBlock()).attack(state, helper.getLevel(), abs, stranger, Direction.NORTH);
        check(helper, helper.getBlockState(SAFE).getValue(CarpentersSafeBlock.FACING) == Direction.NORTH,
                "not turned");
        check(helper, safe(helper).getAutomation() == SafeAutomation.DISABLED, "automation unchanged");
        helper.succeed();
    }

    @CBGameTest
    public void hammerTurnsItToThePlayer(GameTestHelper helper) {
        setSafe(helper, false);
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setYRot(Direction.WEST.toYRot());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        BlockPos abs = helper.absolutePos(SAFE);
        BlockState state = helper.getBlockState(SAFE);
        ((CarpentersSafeBlock) state.getBlock()).attack(state, helper.getLevel(), abs, player, Direction.EAST);
        check(helper, helper.getBlockState(SAFE).getValue(CarpentersSafeBlock.FACING) == Direction.EAST,
                "faces the player");
        helper.succeed();
    }

    @CBGameTest
    public void hoppersFollowTheAutomationSetting(GameTestHelper helper) {
        SafeBlockEntity safe = setSafe(helper, false);
        ItemStack stone = new ItemStack(Items.STONE);
        check(helper, !safe.canPlaceItemThroughFace(0, stone, Direction.UP), "disabled: nothing goes in");
        safe.setAutomation(SafeAutomation.RECEIVE);
        check(helper, safe.canPlaceItemThroughFace(0, stone, Direction.UP), "receive: items go in");
        check(helper, !safe.canPlaceItemThroughFace(0, stone, Direction.NORTH), "never through the door");
        check(helper, !safe.canTakeItemThroughFace(0, stone, Direction.DOWN), "receive: nothing comes out");
        safe.setAutomation(SafeAutomation.SEND);
        check(helper, safe.canTakeItemThroughFace(0, stone, Direction.DOWN), "send: items come out");

        safe.setAutomation(SafeAutomation.RECEIVE);
        BlockPos hopper = SAFE.above();
        helper.setBlock(hopper, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        ((HopperBlockEntity) helper.getBlockEntity(hopper)).setItem(0, new ItemStack(Items.COBBLESTONE, 4));
        helper.succeedWhen(() -> {
            if (safe(helper).getItem(0).getCount() < 4) {
                helper.fail("the hopper above fills the safe");
            }
        });
    }

    @CBGameTest
    public void lightsAndComparatorShowHowFullItIs(GameTestHelper helper) {
        SafeBlockEntity safe = setSafe(helper, false);
        for (int slot = 0; slot < 3; ++slot) {
            safe.setItem(slot, new ItemStack(Items.STONE));
        }
        check(helper, safe.getCapacity() == 1, "three slots light one of nine");
        for (int slot = 3; slot < 27; ++slot) {
            safe.setItem(slot, new ItemStack(Items.STONE));
        }
        check(helper, safe.getCapacity() == 9, "a full safe lights all nine");
        BlockPos abs = helper.absolutePos(SAFE);
        check(helper, helper.getBlockState(SAFE).getAnalogOutputSignal(helper.getLevel(), abs) > 0,
                "a comparator reads it");
        helper.succeed();
    }

    @CBGameTest
    public void dropsItsContentsAndUpgrade(GameTestHelper helper) {
        SafeBlockEntity safe = setSafe(helper, false);
        safe.setUpgrade(new ItemStack(Items.GOLD_INGOT));
        safe.setItem(40, new ItemStack(Items.APPLE, 5));
        helper.getLevel().destroyBlock(helper.absolutePos(SAFE), true);
        helper.assertItemEntityCountIs(SafeContent.CARPENTERS_SAFE.asItem(), SAFE, 2, 1);
        helper.assertItemEntityCountIs(Items.APPLE, SAFE, 2, 5);
        helper.assertItemEntityCountIs(Items.GOLD_INGOT, SAFE, 2, 1);
        helper.succeed();
    }

    @CBGameTest
    public void resistsExplosionsAsBedrock(GameTestHelper helper) {
        setSafe(helper, false);
        BlockState state = helper.getBlockState(SAFE);
        float resistance = ((CarpentersSafeBlock) state.getBlock()).getExplosionResistance(state, helper.getLevel(),
                helper.absolutePos(SAFE), null);
        check(helper, resistance == Blocks.BEDROCK.getExplosionResistance(), "bedrock's resistance");
        helper.succeed();
    }
}
