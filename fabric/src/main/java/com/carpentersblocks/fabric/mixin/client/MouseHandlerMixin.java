package com.carpentersblocks.fabric.mixin.client;

import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.carpentersblocks.client.SlopeSelectClient;

/** Slope select: scrolling while sneaking with a slope in hand changes its kind instead of the hotbar slot. */
@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {

    @WrapOperation(method = "onScroll", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;swapPaint(D)V"))
    private void carpentersblocks$slopeSelect(Inventory inventory, double delta, Operation<Void> original) {
        if (!SlopeSelectClient.onScroll(delta)) {
            original.call(inventory, delta);
        }
    }
}
