package com.carpentersblocks.test.block;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.carpentersblocks.block.CarpentersBlockBlock;
import com.carpentersblocks.blockentity.CarpentersBlockEntity;
import com.carpentersblocks.blockentity.CoverData;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.test.CBGameTest;

/** GameTests for the base Carpenter's Block. */
public class CarpentersBlockTests {

    private static final BlockPos POS = new BlockPos(1, 1, 1);

    @CBGameTest
    public void stoneCoverMakesBlockSolid(GameTestHelper helper) {
        helper.setBlock(POS, ModContent.CARPENTERS_BLOCK);
        helper.assertBlockProperty(POS, CarpentersBlockBlock.SOLID, false);

        CarpentersBlockEntity entity = helper.getBlockEntity(POS);
        entity.setCover(CoverData.BASE, Blocks.STONE.defaultBlockState());

        helper.assertBlockProperty(POS, CarpentersBlockBlock.SOLID, true);
        helper.succeed();
    }}
