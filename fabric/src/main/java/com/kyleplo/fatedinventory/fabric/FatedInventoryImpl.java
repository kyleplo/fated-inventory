package com.kyleplo.fatedinventory.fabric;

import java.util.ArrayList;
import java.util.List;

import com.kyleplo.fatedinventory.IFatedInventoryContainer;
import com.kyleplo.fatedinventory.compat.AccessoriesCompat;
import com.kyleplo.fatedinventory.fabric.compat.TrinketsCompat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FatedInventoryImpl {
    public static IFatedInventoryContainer getFatedInventoryContainer(Player player) {
        return FatedInventoryComponentRegistry.FATED_INVENTORY.get(player);
    }

    public static List<ItemStack> compatItems (Player player) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();

        if (FabricLoader.getInstance().isModLoaded("accessories")) {
            items.addAll(AccessoriesCompat.itemsFromAccessoriesInventory(player));
        }
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            items.addAll(TrinketsCompat.itemsFromTrinketsInventory(player));
        }

        return items;
    }

    public static int compatRemoveMatchingItems (Player player, ItemStack matchItem, int max, DamageSource damageSource) {
        int removed = 0;

        if (FabricLoader.getInstance().isModLoaded("accessories") && removed < max) {
            removed += AccessoriesCompat.removeMatchingItemsFromAccessoriesInventory(player, matchItem, max, damageSource);
        }
        if (FabricLoader.getInstance().isModLoaded("trinkets") && removed < max) {
            removed += TrinketsCompat.removeMatchingItemsFromTrinketsInventory(player, matchItem, max);
        }

        return removed;
    }
}
