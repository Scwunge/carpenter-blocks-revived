package com.carpentersblocks.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BedBlock;

import com.carpentersblocks.block.bed.CarpentersBedBlock;

/**
 * Players respawn at a Carpenter's Bed as at a vanilla one: vanilla's respawn check only knows its own bed class, and
 * the rest of its bed branch reads the vanilla bed properties the Carpenter's Bed shares. NeoForge asks the block
 * through its respawn hook instead.
 */
@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin {

    @WrapOperation(method = "findRespawnAndUseSpawnBlock", constant = @Constant(classValue = BedBlock.class))
    private static boolean carpentersblocks$carpentersBed(Object block, Operation<Boolean> original) {
        return block instanceof CarpentersBedBlock || original.call(block);
    }
}
