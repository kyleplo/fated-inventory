package com.kyleplo.fatedinventory.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.kyleplo.fatedinventory.FatedInventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

@Mixin(ServerPlayer.class)
public abstract class PlayerMixin {
    @Inject(method = "die", at = @At(value = "HEAD"))
    public void die(DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            FatedInventory.handlePlayerDeath(player, source);
        }
    }
}
