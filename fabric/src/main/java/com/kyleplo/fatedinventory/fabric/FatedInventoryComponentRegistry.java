package com.kyleplo.fatedinventory.fabric;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;

import com.kyleplo.fatedinventory.FatedInventory;

import net.minecraft.resources.ResourceLocation;

public final class FatedInventoryComponentRegistry implements EntityComponentInitializer {
    public static final ComponentKey<FatedInventoryComponent> FATED_INVENTORY = ComponentRegistry.getOrCreate(new ResourceLocation(FatedInventory.MOD_ID, "fated_inventory"), FatedInventoryComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(FATED_INVENTORY, player -> new LivingEntityFatedInventoryComponent(), RespawnCopyStrategy.ALWAYS_COPY);
    }
}