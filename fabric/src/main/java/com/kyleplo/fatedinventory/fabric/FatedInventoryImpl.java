package com.kyleplo.fatedinventory.fabric;

import java.util.ArrayList;
import java.util.List;

import com.kyleplo.fatedinventory.IFatedInventoryContainer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FatedInventoryImpl {
    public static IFatedInventoryContainer getFatedInventoryContainer(Player player) {
        return FatedInventoryComponentRegistry.FATED_INVENTORY.get(player);
    }

    public static List<ItemStack> compatItems (Player player) {
        return new ArrayList<ItemStack>();
    }

    public static int compatRemoveMatchingItems (Player player, ItemStack matchItem, int max) {
        return 0;
    }
}
