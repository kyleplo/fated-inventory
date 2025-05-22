package com.kyleplo.fatedinventory.fabric;

import com.kyleplo.fatedinventory.IFatedInventoryContainer;

import net.minecraft.world.entity.player.Player;

public class FatedInventoryImpl {
    public static IFatedInventoryContainer getFatedInventoryContainer(Player player) {
        return FatedInventoryComponentRegistry.FATED_INVENTORY.get(player);
    }
}
