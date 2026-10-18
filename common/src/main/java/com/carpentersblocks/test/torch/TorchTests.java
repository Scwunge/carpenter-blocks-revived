package com.carpentersblocks.test.torch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.carpentersblocks.block.sided.SidedBlock;
import com.carpentersblocks.block.torch.CarpentersTorchBlock;
import com.carpentersblocks.block.torch.TorchFlame;
import com.carpentersblocks.block.torch.TorchType;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.registry.TorchContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 6: Carpenter's Torch placement, light, relighting, weather and styles. */
public class TorchTests {

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

    private static Player place(GameTestHelper helper, BlockPos against, Direction face) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setYRot(Direction.NORTH.toYRot());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TorchContent.CARPENTERS_TORCH));
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, against, face)));
        return player;
    }

    private static Player torchOnFloor(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        return place(helper, FLOOR, Direction.UP);
    }

    @CBGameTest
    public void torchesGoOnFloorsAndWallsButNotCeilings(GameTestHelper helper) {
        torchOnFloor(helper);
        check(helper, helper.getBlockState(ON_FLOOR).getValue(SidedBlock.FACING) == Direction.UP, "floor torch");
        BlockPos wall = new BlockPos(0, 2, 1);
        helper.setBlock(wall, Blocks.STONE);
        place(helper, wall, Direction.EAST);
        check(helper, helper.getBlockState(wall.east()).getValue(SidedBlock.FACING) == Direction.EAST, "wall torch");
        BlockPos ceiling = new BlockPos(2, 3, 2);
        helper.setBlock(ceiling, Blocks.STONE);
        place(helper, ceiling, Direction.DOWN);
        check(helper, helper.getBlockState(ceiling.below()).isAir(), "no torch under a ceiling");
        helper.setBlock(wall, Blocks.AIR);
        check(helper, helper.getBlockState(wall.east()).isAir(), "wall torch falls with its wall");
        helper.succeed();
    }

    @CBGameTest
    public void torchesGiveLightByFlameAndRelightFromAnotherTorch(GameTestHelper helper) {
        Player player = torchOnFloor(helper);
        BlockState lit = helper.getBlockState(ON_FLOOR);
        check(helper, lit.getLightEmission() == 14, "lit torch shines like a vanilla one");
        helper.setBlock(ON_FLOOR, lit.setValue(CarpentersTorchBlock.FLAME, TorchFlame.SMOLDERING));
        check(helper, helper.getBlockState(ON_FLOOR).getLightEmission() == 9, "smoldering gives two thirds");
        helper.setBlock(ON_FLOOR, lit.setValue(CarpentersTorchBlock.FLAME, TorchFlame.UNLIT));
        check(helper, helper.getBlockState(ON_FLOOR).getLightEmission() == 0, "unlit is dark");

        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(ON_FLOOR);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SOUL_TORCH));
        TorchContent.CARPENTERS_TORCH.use(level.getBlockState(abs), level, abs, player, InteractionHand.MAIN_HAND,
                hit(helper, ON_FLOOR, Direction.UP));
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersTorchBlock.FLAME) == TorchFlame.LIT,
                "any torch relights it");
        helper.succeed();
    }

    @CBGameTest
    public void weatherTicksMoveTheFlame(GameTestHelper helper) {
        torchOnFloor(helper);
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(ON_FLOOR);
        helper.setBlock(ON_FLOOR, helper.getBlockState(ON_FLOOR).setValue(CarpentersTorchBlock.FLAME,
                TorchFlame.UNLIT));
        boolean wet = level.isRainingAt(abs);
        level.getBlockState(abs).randomTick(helper.getLevel(), abs, level.getRandom());
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersTorchBlock.FLAME)
                == TorchFlame.UNLIT.weather(wet), "a random tick moves the flame with the weather");

        helper.setBlock(ON_FLOOR, helper.getBlockState(ON_FLOOR).setValue(CarpentersTorchBlock.TYPE,
                TorchType.LANTERN).setValue(CarpentersTorchBlock.FLAME, TorchFlame.UNLIT));
        level.getBlockState(abs).randomTick(helper.getLevel(), abs, level.getRandom());
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersTorchBlock.FLAME) == TorchFlame.UNLIT,
                "lanterns ignore the weather");
        helper.succeed();
    }

    @CBGameTest
    public void hammerSwitchesToALanternYouCanBumpInto(GameTestHelper helper) {
        Player player = torchOnFloor(helper);
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(ON_FLOOR);
        check(helper, level.getBlockState(abs).getCollisionShape(level, abs).isEmpty(), "torches let you through");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        TorchContent.CARPENTERS_TORCH.attack(level.getBlockState(abs), level, abs, player, Direction.UP);
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersTorchBlock.TYPE) == TorchType.LANTERN,
                "left-click: lantern");
        check(helper, !level.getBlockState(abs).getCollisionShape(level, abs).isEmpty(), "lanterns are solid");
        TorchContent.CARPENTERS_TORCH.use(level.getBlockState(abs), level, abs, player, InteractionHand.MAIN_HAND,
                hit(helper, ON_FLOOR, Direction.UP));
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersTorchBlock.TYPE) == TorchType.VANILLA,
                "right-click: back to a torch");
        helper.succeed();
    }
}
