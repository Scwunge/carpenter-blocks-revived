package com.carpentersblocks.test.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.carpentersblocks.block.redstone.CarpentersButtonBlock;
import com.carpentersblocks.block.redstone.CarpentersDaylightSensorBlock;
import com.carpentersblocks.block.redstone.CarpentersLeverBlock;
import com.carpentersblocks.block.redstone.CarpentersPressurePlateBlock;
import com.carpentersblocks.block.redstone.PlateTrigger;
import com.carpentersblocks.block.redstone.SensorSensitivity;
import com.carpentersblocks.block.redstone.SidedRedstoneBlock;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.registry.RedstoneContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 5: signal levels, timing, polarity and modes of the Button, Lever, Pressure Plate and Daylight Sensor. */
public class RedstoneTests {

    /** A stone block with things hung on it; whatever is strongly powered shows on its far side. */
    private static final BlockPos SUPPORT = new BlockPos(1, 1, 1);
    /** Beyond the support from anything hung on its south face. */
    private static final BlockPos BEHIND = SUPPORT.north();
    private static final BlockPos FLOOR = new BlockPos(1, 0, 1);
    private static final BlockPos ON_FLOOR = FLOOR.above();

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction face) {
        Vec3 onFace = Vec3.atCenterOf(helper.absolutePos(pos)).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5));
        return new BlockHitResult(onFace, face, helper.absolutePos(pos), false);
    }

    /** Places {@code block} on a face of {@code against} as a player looking {@code look}; returns that player. */
    private static Player place(GameTestHelper helper, Block block, BlockPos against, Direction face, Direction look) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setYRot(look.toYRot());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(block));
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, against, face)));
        return player;
    }

    private static void use(GameTestHelper helper, Player player, BlockPos pos) {
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(pos);
        ((SidedRedstoneBlock) level.getBlockState(abs).getBlock()).use(level.getBlockState(abs), level, abs, player,
                InteractionHand.MAIN_HAND, hit(helper, pos, Direction.UP));
    }

    private static void hammerRight(GameTestHelper helper, Player player, BlockPos pos) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        use(helper, player, pos);
    }

    private static void hammerLeft(GameTestHelper helper, Player player, BlockPos pos) {
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(pos);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        ((SidedRedstoneBlock) level.getBlockState(abs).getBlock()).attack(level.getBlockState(abs), level, abs, player, Direction.UP);
    }

    /** Weak power reaching a spot from its neighbors. */
    private static int signalAt(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getBestNeighborSignal(helper.absolutePos(pos));
    }

    /** Whether the support is strongly powered: a spot beyond it then sees power. */
    private static boolean throughSupport(GameTestHelper helper) {
        return helper.getLevel().hasNeighborSignal(helper.absolutePos(BEHIND));
    }

    private static Player buttonOnSupport(GameTestHelper helper) {
        helper.setBlock(SUPPORT, Blocks.STONE);
        return place(helper, RedstoneContent.CARPENTERS_BUTTON, SUPPORT, Direction.SOUTH, Direction.NORTH);
    }

    @CBGameTest
    public void buttonsPowerTheirSupportForOneSecond(GameTestHelper helper) {
        Player player = buttonOnSupport(helper);
        BlockPos button = SUPPORT.south();
        check(helper, helper.getBlockState(button).getValue(SidedRedstoneBlock.FACING) == Direction.SOUTH,
                "button faces out of the support");
        check(helper, !throughSupport(helper), "idle button gives no power");
        use(helper, player, button);
        check(helper, helper.getBlockState(button).getValue(CarpentersButtonBlock.POWERED), "pressed");
        check(helper, signalAt(helper, button.east()) == 15, "full weak power beside it");
        check(helper, throughSupport(helper), "strong power through the support");
        helper.runAtTickTime(CarpentersButtonBlock.PRESS_TICKS - 2, () -> check(helper,
                helper.getBlockState(button).getValue(CarpentersButtonBlock.POWERED), "still pressed before a second"));
        helper.runAtTickTime(CarpentersButtonBlock.PRESS_TICKS + 2, () -> {
            check(helper, !helper.getBlockState(button).getValue(CarpentersButtonBlock.POWERED), "pops back out");
            check(helper, !throughSupport(helper), "and the power goes");
            helper.succeed();
        });
    }

    @CBGameTest
    public void invertedButtonsPowerUntilPressed(GameTestHelper helper) {
        Player player = buttonOnSupport(helper);
        BlockPos button = SUPPORT.south();
        hammerLeft(helper, player, button);
        check(helper, helper.getBlockState(button).getValue(SidedRedstoneBlock.INVERTED), "hammer inverts it");
        check(helper, throughSupport(helper), "inverted idle button powers");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, player, button);
        check(helper, !throughSupport(helper), "pressing cuts the power");
        helper.succeed();
    }

    @CBGameTest
    public void buttonsFallWithTheirSupport(GameTestHelper helper) {
        buttonOnSupport(helper);
        helper.setBlock(SUPPORT, Blocks.AIR);
        check(helper, helper.getBlockState(SUPPORT.south()).isAir(), "button drops off");
        helper.succeed();
    }

    @CBGameTest
    public void leversToggleAndFollowTheirPlacement(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        Player player = place(helper, RedstoneContent.CARPENTERS_LEVER, FLOOR, Direction.UP, Direction.EAST);
        BlockState lever = helper.getBlockState(ON_FLOOR);
        check(helper, lever.is(RedstoneContent.CARPENTERS_LEVER), "lever placed");
        check(helper, lever.getValue(SidedRedstoneBlock.FACING) == Direction.UP, "on the floor");
        check(helper, !CarpentersLeverBlock.alongZ(lever), "swings the way the player looks");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, player, ON_FLOOR);
        check(helper, signalAt(helper, ON_FLOOR.east()) == 15, "on");
        check(helper, throughSupportBelow(helper), "powers the floor");
        use(helper, player, ON_FLOOR);
        check(helper, signalAt(helper, ON_FLOOR.east()) == 0, "off again");

        BlockPos wall = new BlockPos(0, 1, 0);
        helper.setBlock(wall, Blocks.STONE);
        place(helper, RedstoneContent.CARPENTERS_LEVER, wall, Direction.EAST, Direction.NORTH);
        check(helper, !CarpentersLeverBlock.alongZ(helper.getBlockState(wall.east())),
                "a wall lever swings out from the wall, whichever way the player looks");
        helper.succeed();
    }

    @CBGameTest
    public void platesIgnoreAnimalsUntilSetForThem(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        Player player = place(helper, RedstoneContent.CARPENTERS_PRESSURE_PLATE, FLOOR, Direction.UP,
                Direction.NORTH);
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersPressurePlateBlock.TRIGGER)
                == PlateTrigger.PLAYER, "players by default");
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, ON_FLOOR);
        helper.startSequence()
                .thenIdle(10)
                .thenExecute(() -> {
                    check(helper, !helper.getBlockState(ON_FLOOR).getValue(CarpentersPressurePlateBlock.POWERED),
                            "a pig doesn't press a player plate");
                    hammerRight(helper, player, ON_FLOOR);
                    hammerRight(helper, player, ON_FLOOR);
                    check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersPressurePlateBlock.TRIGGER)
                            == PlateTrigger.ANIMAL, "hammer cycles to animals");
                })
                .thenWaitUntil(() -> helper.assertBlockProperty(ON_FLOOR, CarpentersPressurePlateBlock.POWERED, true))
                .thenExecute(() -> {
                    check(helper, throughSupportBelow(helper), "strong power into the floor");
                    pig.discard();
                })
                .thenWaitUntil(() -> helper.assertBlockProperty(ON_FLOOR, CarpentersPressurePlateBlock.POWERED, false))
                .thenExecute(() -> check(helper, signalAt(helper, ON_FLOOR.east()) == 0, "released"))
                .thenSucceed();
    }

    /** Whether the floor is strongly powered: a spot beside it then sees power. */
    private static boolean throughSupportBelow(GameTestHelper helper) {
        return helper.getLevel().hasNeighborSignal(helper.absolutePos(FLOOR.west()));
    }

    @CBGameTest
    public void daylightSensorsReadBySensitivityAndPolarity(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        Player player = place(helper, RedstoneContent.CARPENTERS_DAYLIGHT_SENSOR, FLOOR, Direction.UP,
                Direction.NORTH);
        check(helper, helper.getBlockState(ON_FLOOR).is(RedstoneContent.CARPENTERS_DAYLIGHT_SENSOR), "placed");
        hammerRight(helper, player, ON_FLOOR);
        hammerRight(helper, player, ON_FLOOR);
        BlockState sensor = helper.getBlockState(ON_FLOOR);
        check(helper, sensor.getValue(CarpentersDaylightSensorBlock.SENSITIVITY) == SensorSensitivity.DYNAMIC,
                "hammer cycles to dynamic");

        BlockPos beside = ON_FLOOR.east();
        helper.setBlock(ON_FLOOR, sensor.setValue(CarpentersDaylightSensorBlock.READING, 10));
        check(helper, signalAt(helper, beside) == 10, "dynamic gives the reading");
        hammerLeft(helper, player, ON_FLOOR);
        check(helper, signalAt(helper, beside) == 5, "inverted gives the rest");
        hammerRight(helper, player, ON_FLOOR);
        check(helper, signalAt(helper, beside) == 15, "inverted sleep: dark enough to sleep at 10");
        hammerLeft(helper, player, ON_FLOOR);
        check(helper, signalAt(helper, beside) == 0, "sleep: off at 10");
        helper.setBlock(ON_FLOOR, helper.getBlockState(ON_FLOOR).setValue(CarpentersDaylightSensorBlock.READING, 12));
        check(helper, signalAt(helper, beside) == 15, "sleep: on at 12");
        helper.succeed();
    }

    @CBGameTest
    public void daylightSensorsStayOffCeilings(GameTestHelper helper) {
        BlockPos ceiling = new BlockPos(1, 3, 1);
        helper.setBlock(ceiling, Blocks.STONE);
        place(helper, RedstoneContent.CARPENTERS_DAYLIGHT_SENSOR, ceiling, Direction.DOWN, Direction.NORTH);
        check(helper, helper.getBlockState(ceiling.below()).isAir(), "no sensor under a ceiling");
        helper.succeed();
    }
}
