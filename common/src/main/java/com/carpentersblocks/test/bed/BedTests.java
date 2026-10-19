package com.carpentersblocks.test.bed;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.carpentersblocks.block.bed.CarpentersBedBlock;
import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.design.DesignList;
import com.carpentersblocks.registry.BedContent;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 8: Carpenter's Bed placement, halves, designs, the blanket and sleeping. */
public class BedTests {

    private static final BlockPos FOOT = new BlockPos(1, 1, 0);
    private static final BlockPos HEAD = new BlockPos(1, 1, 1);

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction face) {
        Vec3 onFace = Vec3.atCenterOf(helper.absolutePos(pos)).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5));
        return new BlockHitResult(onFace, face, helper.absolutePos(pos), false);
    }

    private static Player bedPlayer(GameTestHelper helper, GameType mode) {
        Player player = helper.makeMockPlayer(mode);
        player.setYRot(Direction.SOUTH.toYRot());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BedContent.CARPENTERS_BED));
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                hit(helper, FOOT.below(), Direction.UP)));
        return player;
    }

    /** Stone under both halves and a bed placed looking south, foot at {@link #FOOT}. */
    private static Player placeBed(GameTestHelper helper, GameType mode) {
        helper.setBlock(FOOT.below(), Blocks.STONE);
        helper.setBlock(HEAD.below(), Blocks.STONE);
        return bedPlayer(helper, mode);
    }

    private static CoverableBlockEntity be(GameTestHelper helper, BlockPos pos) {
        return CoverableBlock.blockEntity(helper.getLevel(), helper.absolutePos(pos));
    }

    private static void use(GameTestHelper helper, Player player, BlockPos pos) {
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(pos);
        BedContent.CARPENTERS_BED.use(level.getBlockState(abs), level, abs, player, InteractionHand.MAIN_HAND,
                hit(helper, pos, Direction.UP));
    }

    private static void attack(GameTestHelper helper, Player player, BlockPos pos) {
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(pos);
        BedContent.CARPENTERS_BED.attack(level.getBlockState(abs), level, abs, player, Direction.UP);
    }

    @CBGameTest
    public void placesBothHalvesAndBreaksTogether(GameTestHelper helper) {
        placeBed(helper, GameType.CREATIVE);
        check(helper, helper.getBlockState(FOOT).is(BedContent.CARPENTERS_BED)
                && !CarpentersBedBlock.isHead(helper.getBlockState(FOOT)), "foot where clicked");
        check(helper, helper.getBlockState(HEAD).is(BedContent.CARPENTERS_BED)
                && CarpentersBedBlock.isHead(helper.getBlockState(HEAD)), "head beyond it");
        check(helper, helper.getBlockState(HEAD).getValue(CarpentersBedBlock.FACING) == Direction.SOUTH,
                "faces the way the player looked");
        helper.setBlock(HEAD, Blocks.AIR);
        check(helper, helper.getBlockState(FOOT).isAir(), "the foot goes with the head");
        helper.succeed();
    }

    @CBGameTest
    public void needsSolidGroundUnderBothHalves(GameTestHelper helper) {
        helper.setBlock(FOOT.below(), Blocks.STONE);
        bedPlayer(helper, GameType.CREATIVE);
        check(helper, helper.getBlockState(FOOT).isAir(), "nothing under the head: not placed");
        helper.succeed();
    }

    @CBGameTest
    public void hammerCyclesTheDesignOnBothHalves(GameTestHelper helper) {
        Player player = placeBed(helper, GameType.CREATIVE);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        use(helper, player, FOOT);
        String first = DesignList.BED.names().get(0);
        check(helper, be(helper, FOOT).getDesign().equals(first), "right-click: first design");
        check(helper, be(helper, HEAD).getDesign().equals(first), "the head follows");
        attack(helper, player, HEAD);
        String last = DesignList.BED.names().get(DesignList.BED.names().size() - 1);
        check(helper, be(helper, FOOT).getDesign().equals(last), "left-click: back, wrapping round");
        player.setShiftKeyDown(true);
        use(helper, player, HEAD);
        check(helper, be(helper, FOOT).getDesign().isEmpty() && be(helper, HEAD).getDesign().isEmpty(),
                "sneak right-click: no design");
        helper.succeed();
    }

    @CBGameTest
    public void dyeColorsTheBlanketAndTheHammerTakesItBack(GameTestHelper helper) {
        Player player = placeBed(helper, GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RED_DYE, 2));
        use(helper, player, FOOT);
        check(helper, CarpentersBedBlock.blanket(be(helper, FOOT)) == DyeColor.RED, "the foot's blanket is red");
        check(helper, CarpentersBedBlock.blanket(be(helper, HEAD)) == DyeColor.RED, "and the head's");
        check(helper, player.getMainHandItem().getCount() == 1, "one dye used");
        check(helper, be(helper, FOOT).getCoverData().dye(CoverData.BASE) == null, "the frame isn't dyed");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        attack(helper, player, FOOT);
        check(helper, CarpentersBedBlock.blanket(be(helper, HEAD)) == null, "the dye comes off both halves");
        helper.assertItemEntityPresent(Items.RED_DYE, FOOT, 2);
        helper.succeed();
    }

    @CBGameTest
    public void sleepingOccupiesBothHalves(GameTestHelper helper) {
        Player player = placeBed(helper, GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, player, FOOT);
        check(helper, player.isSleeping(), "the player sleeps");
        check(helper, player.getSleepingPos().orElseThrow().equals(helper.absolutePos(HEAD)), "in the head");
        check(helper, helper.getBlockState(HEAD).getValue(CarpentersBedBlock.OCCUPIED), "head occupied");
        check(helper, helper.getBlockState(FOOT).getValue(CarpentersBedBlock.OCCUPIED), "foot occupied");
        player.stopSleeping();
        check(helper, !helper.getBlockState(HEAD).getValue(CarpentersBedBlock.OCCUPIED), "free again");
        helper.succeed();
    }

    @CBGameTest
    public void onlyTheHeadDropsTheBed(GameTestHelper helper) {
        placeBed(helper, GameType.CREATIVE);
        helper.getLevel().destroyBlock(helper.absolutePos(HEAD), true);
        check(helper, helper.getBlockState(FOOT).isAir(), "both halves go");
        helper.assertItemEntityCountIs(BedContent.CARPENTERS_BED.asItem(), FOOT, 3, 1);
        helper.succeed();
    }
}
