package com.kyleplo.fatedinventory.forge;

import java.util.ArrayList;
import java.util.List;

import com.kyleplo.fatedinventory.IFatedInventoryContainer;
import com.kyleplo.fatedinventory.compat.AccessoriesCompat;
import com.kyleplo.fatedinventory.forge.compat.CuriosCompat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class FatedInventoryImpl {
    public static IFatedInventoryContainer getFatedInventoryContainer(Player player) {
        return player.getCapability(FatedInventoryForge.FATED_INVENTORY).resolve().get();
    }

    public static List<ItemStack> compatItems (Player player) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        if (ModList.get().isLoaded("accessories")) {
            items.addAll(AccessoriesCompat.itemsFromAccessoriesInventory(player));
        }
        if (ModList.get().isLoaded("curios")) {
            items.addAll(CuriosCompat.itemsFromCuriosInventory(player));
        }
        return items;
    }

    public static int compatRemoveMatchingItems (Player player, ItemStack matchItem, int max, DamageSource damageSource) {
        int removed = 0;
        if (ModList.get().isLoaded("accessories") && removed < max) {
            removed += AccessoriesCompat.removeMatchingItemsFromAccessoriesInventory(player, matchItem, max - removed, damageSource);
        }
        if (ModList.get().isLoaded("curios") && removed < max) {
            removed += CuriosCompat.removeMatchingItemsFromCuriosInventory(player, matchItem, max - removed, damageSource);
        }
        return removed;
    }
}
