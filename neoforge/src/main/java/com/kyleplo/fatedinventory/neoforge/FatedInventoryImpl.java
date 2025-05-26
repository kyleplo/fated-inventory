package com.kyleplo.fatedinventory.neoforge;

import java.util.ArrayList;
import java.util.List;

import com.kyleplo.fatedinventory.IFatedInventoryContainer;
import com.kyleplo.fatedinventory.neoforge.compat.CuriosCompat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public class FatedInventoryImpl {
    public static IFatedInventoryContainer getFatedInventoryContainer(Player player) {
        return player.getData(FatedInventoryNeoForge.FATED_INVENTORY);
    }

    public static List<ItemStack> compatItems (Player player) {
        if (ModList.get().isLoaded("curios")) {
            return CuriosCompat.itemsFromCuriosInventory(player);
        }
        return new ArrayList<ItemStack>();
    }

    public static int compatRemoveMatchingItems (Player player, ItemStack matchItem, int max, DamageSource damageSource) {
        if (ModList.get().isLoaded("curios")) {
            return CuriosCompat.removeMatchingItemsFromCuriosInventory(player, matchItem, max, damageSource);
        }
        return 0;
    }
}
