package com.kyleplo.fatedinventory.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import com.kyleplo.fatedinventory.FatedInventory;
import com.kyleplo.fatedinventory.FatedInventoryCommand;

public final class FatedInventoryFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        ServerPlayerEvents.AFTER_RESPAWN.register(ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, "handle_player_respawn"), (ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) -> {
            if (!alive) {
                FatedInventory.handlePlayerRespawn(newPlayer);
            }
        });

        ServerLivingEntityEvents.ALLOW_DEATH.register(ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, "handle_player_death"), (LivingEntity entity, DamageSource source, float amount) -> {
            if (entity instanceof Player) {
                FatedInventory.handlePlayerDeath((Player) entity, source);
            }
            return true;
        });

        ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
            FatedInventory.handleRegisterStructure(server);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> FatedInventoryCommand.register(dispatcher));

        // Run our common setup.
        FatedInventory.init();
    }
}
