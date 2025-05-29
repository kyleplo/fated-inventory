package com.kyleplo.fatedinventory.fabric.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketEnums;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.TrinketEnums.DropRule;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TrinketsCompat {
    private static int removedCount = 0;

    public static List<ItemStack> itemsFromTrinketsInventory (Player player) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        Optional<TrinketComponent> maybeTrinketComponent = TrinketsApi.getTrinketComponent(player);

        if (maybeTrinketComponent.isPresent()) {
            TrinketComponent trinketComponent = maybeTrinketComponent.get();
            trinketComponent.getAllEquipped().forEach((Tuple<SlotReference, ItemStack> trinket) -> {
                items.add(trinket.getB());
            });
        }

        return items;
    }

    public static int removeMatchingItemsFromTrinketsInventory (Player player, ItemStack matchItem, int max) {
        Optional<TrinketComponent> maybeTrinketComponent = TrinketsApi.getTrinketComponent(player);
        removedCount = 0;

        if (maybeTrinketComponent.isPresent()) {
            TrinketComponent trinketComponent = maybeTrinketComponent.get();
            trinketComponent.getInventory().forEach((String groupName, Map<String, TrinketInventory> groupSlots) -> {
                groupSlots.forEach((String slotName, TrinketInventory slotInventory) -> {
                    for (int i = 0; i < slotInventory.getContainerSize(); i++) {
                        ItemStack slotItem = slotInventory.getItem(i);
                        DropRule dropRule = TrinketsApi.getTrinket(slotItem.getItem()).getDropRule(slotItem, new SlotReference(slotInventory, i), player);
                        
                        if (removedCount < max && ItemStack.isSameItemSameTags(matchItem, slotItem) && dropRule != TrinketEnums.DropRule.DESTROY && dropRule != TrinketEnums.DropRule.KEEP) {
                            slotInventory.setItem(i, ItemStack.EMPTY);
                            removedCount += slotItem.getCount();
                        }
                    }
                });
            });
            return removedCount;
        }

        return 0;
    }
}
