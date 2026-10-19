package com.carpentersblocks.neoforge;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.block.bed.CarpentersBedBlock;

/**
 * The Carpenter's Bed with NeoForge's respawn hook, which vanilla only answers for its own beds: players respawn
 * beside it as beside a vanilla bed.
 */
public class NeoForgeBedBlock extends CarpentersBedBlock {

    public NeoForgeBedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState state, EntityType<?> type,
            LevelReader level, BlockPos pos, float orientation) {
        if (level instanceof Level world && BedBlock.canSetSpawn(world)) {
            return BedBlock.findStandUpPosition(type, level, pos, state.getValue(FACING), orientation)
                    .map(position -> ServerPlayer.RespawnPosAngle.of(position, pos));
        }
        return Optional.empty();
    }
}
