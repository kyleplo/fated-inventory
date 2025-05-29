package com.kyleplo.fatedinventory.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.DropRule;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AccessoriesCompat {
    private static int removedCount = 0;

    public static List<ItemStack> itemsFromAccessoriesInventory (Player player) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        Optional<AccessoriesCapability> maybeAccessoriesCapability = AccessoriesCapability.getOptionally(player);

        if (maybeAccessoriesCapability.isPresent()) {
            AccessoriesCapability accessoriesCapability = maybeAccessoriesCapability.get();
            accessoriesCapability.getAllEquipped().forEach((SlotEntryReference slotEntry) -> {
                items.add(slotEntry.stack());
            });
        }

        return items;
    }

    public static int removeMatchingItemsFromAccessoriesInventory (Player player, ItemStack matchItem, int max, DamageSource damageSource) {
        Optional<AccessoriesCapability> maybeAccessoriesCapability = AccessoriesCapability.getOptionally(player);
        removedCount = 0;

        if (maybeAccessoriesCapability.isPresent()) {
            AccessoriesCapability accessoriesCapability = maybeAccessoriesCapability.get();
            accessoriesCapability.getEquipped((ItemStack accessoryItem) -> {
                return ItemStack.isSameItemSameTags(accessoryItem, matchItem);
            }).forEach((SlotEntryReference slotEntry) -> {
                DropRule dropRule = AccessoriesAPI.getAccessory(slotEntry.stack()).getDropRule(slotEntry.stack(), slotEntry.reference(), damageSource);
                if (removedCount < max && dropRule != DropRule.DESTROY && dropRule != DropRule.KEEP) {
                    if(slotEntry.reference().setStack(ItemStack.EMPTY)) {
                        removedCount += slotEntry.stack().getCount();
                    }
                }
            });
        }

        return removedCount;
    }
}
