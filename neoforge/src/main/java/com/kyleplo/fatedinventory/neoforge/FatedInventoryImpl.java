package com.kyleplo.fatedinventory.neoforge;

import com.kyleplo.fatedinventory.IFatedInventoryContainer;

import net.minecraft.world.entity.player.Player;

public class FatedInventoryImpl {
    public static IFatedInventoryContainer getFatedInventoryContainer(Player player) {
        return player.getData(FatedInventoryNeoForge.FATED_INVENTORY);
    }
}
