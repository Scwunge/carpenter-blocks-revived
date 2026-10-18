package com.carpentersblocks.fabric.mixin.client;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import com.carpentersblocks.block.coverable.CoverableBlock;

/**
 * Block particles of a coverable block show its cover. Digging and breaking particles sit on the block and use
 * the base cover; running and landing particles sit just above it and use the top face, as in the original.
 */
@Mixin(TerrainParticle.class)
abstract class TerrainParticleMixin {

    private static final String CONSTRUCTOR = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V";

    private static BlockState carpentersblocks$source(BlockState state, ClientLevel level, BlockPos pos) {
        if (!(state.getBlock() instanceof CoverableBlock)) {
            return state;
        }
        BlockState source = null;
        if (level.getBlockState(pos).getBlock() instanceof CoverableBlock) {
            source = CoverableBlock.featureSensitiveState(level, pos, null);
        } else if (level.getBlockState(pos.below()).getBlock() instanceof CoverableBlock) {
            source = CoverableBlock.featureSensitiveState(level, pos.below(), Direction.UP);
        }
        return source != null && !(source.getBlock() instanceof CoverableBlock) ? source : state;
    }

    @WrapOperation(method = CONSTRUCTOR, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getParticleIcon(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private TextureAtlasSprite carpentersblocks$coverSprite(BlockModelShaper shaper, BlockState state,
            Operation<TextureAtlasSprite> original, @Local(argsOnly = true) ClientLevel level,
            @Local(argsOnly = true) BlockPos pos) {
        return original.call(shaper, carpentersblocks$source(state, level, pos));
    }

    @WrapOperation(method = CONSTRUCTOR, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/color/block/BlockColors;getColor(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;I)I"))
    private int carpentersblocks$coverColor(BlockColors colors, BlockState state, BlockAndTintGetter tintLevel,
            BlockPos tintPos, int tint, Operation<Integer> original, @Local(argsOnly = true) ClientLevel level,
            @Local(argsOnly = true) BlockPos pos) {
        return original.call(colors, carpentersblocks$source(state, level, pos), tintLevel, tintPos, tint);
    }
}
