package com.carpentersblocks.test.wind;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

import com.carpentersblocks.block.barrier.CarpentersGateBlock;
import com.carpentersblocks.block.door.CarpentersDoorBlock;
import com.carpentersblocks.block.door.CarpentersGarageDoorBlock;
import com.carpentersblocks.block.door.CarpentersHatchBlock;
import com.carpentersblocks.block.redstone.CarpentersButtonBlock;
import com.carpentersblocks.block.redstone.CarpentersLeverBlock;
import com.carpentersblocks.registry.BarrierContent;
import com.carpentersblocks.registry.DoorContent;
import com.carpentersblocks.registry.RedstoneContent;
import com.carpentersblocks.test.CBGameTest;

/** Modern addition: wind charges toggle Carpenter's doors, hatches, garage doors, gates, buttons and levers. */
public class WindChargeTests {

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    /** A wind charge's burst (it triggers blocks rather than breaking them) at the middle of {@code pos}. */
    private static void burst(GameTestHelper helper, BlockPos pos) {
        Vec3 at = Vec3.atCenterOf(helper.absolutePos(pos));
        helper.getLevel().explode(null, null, null, at.x, at.y, at.z, 1.2F, false, Level.ExplosionInteraction.TRIGGER,
                ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.WIND_CHARGE_BURST);
    }

    private static boolean opens(BlockState state) {
        return state.getValue(CarpentersDoorBlock.OPEN);
    }

    private static void door(GameTestHelper helper, BlockPos lower, DoorHingeSide hinge, boolean rigid) {
        BlockState state = DoorContent.CARPENTERS_DOOR.defaultBlockState()
                .setValue(CarpentersDoorBlock.FACING, Direction.SOUTH)
                .setValue(CarpentersDoorBlock.HINGE, hinge)
                .setValue(CarpentersDoorBlock.RIGID, rigid);
        helper.setBlock(lower.below(), Blocks.STONE);
        helper.setBlock(lower, state.setValue(CarpentersDoorBlock.HALF, DoubleBlockHalf.LOWER));
        helper.setBlock(lower.above(), state.setValue(CarpentersDoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    @CBGameTest
    public void windChargeSwingsADoorOnce(GameTestHelper helper) {
        BlockPos lower = new BlockPos(1, 1, 1);
        door(helper, lower, DoorHingeSide.LEFT, false);
        burst(helper, lower.above());
        check(helper, opens(helper.getBlockState(lower)) && opens(helper.getBlockState(lower.above())),
                "both halves open, though the blast reaches both");
        helper.succeed();
    }

    @CBGameTest
    public void windChargeSwingsDoubleDoorsTogether(GameTestHelper helper) {
        BlockPos left = new BlockPos(2, 1, 1);
        BlockPos right = new BlockPos(1, 1, 1);
        door(helper, left, DoorHingeSide.LEFT, false);
        door(helper, right, DoorHingeSide.RIGHT, false);
        burst(helper, right);
        check(helper, opens(helper.getBlockState(left)) && opens(helper.getBlockState(right)), "both doors open");
        helper.succeed();
    }

    @CBGameTest
    public void rigidDoorIgnoresWindCharges(GameTestHelper helper) {
        BlockPos lower = new BlockPos(1, 1, 1);
        door(helper, lower, DoorHingeSide.LEFT, true);
        burst(helper, lower);
        check(helper, !opens(helper.getBlockState(lower)), "a rigid door stays shut, as an iron door");
        helper.succeed();
    }

    @CBGameTest
    public void otherExplosionsDontSwingDoors(GameTestHelper helper) {
        BlockPos lower = new BlockPos(1, 1, 1);
        door(helper, lower, DoorHingeSide.LEFT, false);
        // An explosion that neither breaks nor triggers blocks
        Vec3 at = Vec3.atCenterOf(helper.absolutePos(lower));
        helper.getLevel().explode(null, at.x, at.y, at.z, 1.2F, Level.ExplosionInteraction.NONE);
        check(helper, !opens(helper.getBlockState(lower)), "only wind charges swing doors");
        helper.succeed();
    }

    @CBGameTest
    public void windChargeSwingsAHatch(GameTestHelper helper) {
        BlockPos hatch = new BlockPos(1, 1, 1);
        helper.setBlock(hatch.south(), Blocks.STONE);
        helper.setBlock(hatch, DoorContent.CARPENTERS_HATCH.defaultBlockState()
                .setValue(CarpentersHatchBlock.FACING, Direction.SOUTH));
        burst(helper, hatch);
        check(helper, helper.getBlockState(hatch).getValue(CarpentersHatchBlock.OPEN), "opens");
        helper.succeed();
    }

    @CBGameTest
    public void windChargeRollsUpAGarageDoorOnce(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 3, 1), Blocks.STONE);
        BlockState piece = DoorContent.CARPENTERS_GARAGE_DOOR.defaultBlockState()
                .setValue(CarpentersGarageDoorBlock.FACING, Direction.SOUTH);
        helper.setBlock(new BlockPos(1, 2, 1), piece.setValue(CarpentersGarageDoorBlock.HOST, true));
        helper.setBlock(new BlockPos(1, 1, 1), piece.setValue(CarpentersGarageDoorBlock.HOST, false));
        helper.setBlock(new BlockPos(1, 0, 1), piece.setValue(CarpentersGarageDoorBlock.HOST, false));
        burst(helper, new BlockPos(1, 1, 1));
        for (int y = 0; y <= 2; ++y) {
            check(helper, helper.getBlockState(new BlockPos(1, y, 1)).getValue(CarpentersGarageDoorBlock.OPEN),
                    "every piece opens, once, at y " + y);
        }
        helper.succeed();
    }

    @CBGameTest
    public void windChargeSwingsAGate(GameTestHelper helper) {
        BlockPos gate = new BlockPos(1, 1, 1);
        helper.setBlock(gate, BarrierContent.CARPENTERS_GATE.defaultBlockState());
        burst(helper, gate);
        check(helper, helper.getBlockState(gate).getValue(CarpentersGateBlock.OPEN), "opens");
        helper.succeed();
    }

    @CBGameTest
    public void windChargePressesAButtonAndFlipsALever(GameTestHelper helper) {
        BlockPos button = new BlockPos(1, 1, 1);
        BlockPos lever = new BlockPos(1, 1, 2);
        helper.setBlock(button.below(), Blocks.STONE);
        helper.setBlock(lever.below(), Blocks.STONE);
        helper.setBlock(button, RedstoneContent.CARPENTERS_BUTTON.defaultBlockState()
                .setValue(CarpentersButtonBlock.FACING, Direction.UP));
        helper.setBlock(lever, RedstoneContent.CARPENTERS_LEVER.defaultBlockState()
                .setValue(CarpentersLeverBlock.FACING, Direction.UP));
        burst(helper, button);
        check(helper, helper.getBlockState(button).getValue(CarpentersButtonBlock.POWERED), "the button is pressed");
        check(helper, helper.getBlockState(lever).getValue(CarpentersLeverBlock.POWERED), "the lever is on");
        helper.succeed();
    }
}
