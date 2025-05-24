package com.kyleplo.fatedinventory;

import java.util.ArrayList;
import java.util.Optional;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class FatedInventoryContainer implements IFatedInventoryContainer {
    protected int experience = 0;
    protected boolean hasDied = false;
    protected ArrayList<FatedInventoryItem> inventoryList = new ArrayList<FatedInventoryItem>();

    public static final TagKey<Item> ALLOW_MODIFIED_COMPONENTS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, "allow_modified_components"));

    public int getExperience() {
        return this.experience;
    }

    public void setExperience(int experience) {
        if (FatedInventory.config.fateStoresXp) {
            this.experience = experience;
        }
    }

    public boolean getHasDied() {
        return this.hasDied;
    }

    public boolean hasStored() {
        return experience > 0 || hasItemsStored();
    }

    public boolean hasItemsStored() {
        return inventoryList.size() > 0;
    }

    public void setHasDied(boolean hasDied) {
        this.hasDied = hasDied;
    }

    public void clear() {
        this.hasDied = false;
        this.experience = 0;
        this.inventoryList = new ArrayList<FatedInventoryItem>();
    }

    public void removeFromInventory(Inventory inventory, ItemStack matchItem, int max) {
        max -= inventory.clearOrCountMatchingItems((ItemStack otherItem) -> ItemStack.isSameItemSameComponents(matchItem, otherItem), max, inventory);
        if (max > 0) {
            FatedInventory.compatRemoveMatchingItems(null, matchItem, max);
        }
    }

    public void putInventory(Inventory inventory) {
        inventoryList = FatedInventoryItem.listFromItemStackList(inventory.items, true);
        FatedInventoryItem.listFromItemStackList(inventoryList, inventory.armor, true);
        FatedInventoryItem.listFromItemStackList(inventoryList, inventory.offhand, true);
        FatedInventoryItem.listFromItemStackList(inventoryList, FatedInventory.compatItems(inventory.player), true);
    }

    public void compareInventory(Inventory inventory) {
        ArrayList<FatedInventoryItem> compareList = FatedInventoryItem.listFromItemStackList(inventory.items, false);
        FatedInventoryItem.listFromItemStackList(compareList, inventory.armor, false);
        FatedInventoryItem.listFromItemStackList(compareList, inventory.offhand, false);
        FatedInventoryItem.listFromItemStackList(compareList, FatedInventory.compatItems(inventory.player), false);

        inventoryList.forEach((FatedInventoryItem item) -> {
            if (item.isEmpty()) {
                return;
            }

            for (FatedInventoryItem compareItem : compareList) {
                if (compareItem.isEmpty()) {
                    continue;
                }

                if (!item.item.isStackable() && ItemStack.isSameItem(item.item, compareItem.item) && item.item.is(FatedInventoryContainer.ALLOW_MODIFIED_COMPONENTS)) {
                    item.item = compareItem.item.copy();
                    removeFromInventory(inventory, compareItem.item, 1);
                    compareItem.count -= 1;
                    return;
                } else if (ItemStack.isSameItemSameComponents(item.item, compareItem.item)) {
                    if (compareItem.count > item.count) {
                        removeFromInventory(inventory, item.item, item.count);
                        compareItem.count -= item.count;
                    } else {
                        item.count = compareItem.count;
                        removeFromInventory(inventory, item.item, item.count);
                        compareItem.count = 0;
                    }
                    return;
                }
            }

            item.count = 0;
        });
    }

    public void dropInventoryFor (Player player) {
        inventoryList.forEach((FatedInventoryItem item) -> {
            int count = item.count;
            while (count > 0) {
                int toDrop = Math.min(count, item.item.getMaxStackSize());
                ItemStack itemCopy = item.item.copyWithCount(toDrop);
                player.drop(itemCopy, true);
                count -= toDrop;
            }
        });
    }

    public CompoundTag saveNbt(CompoundTag nbt, Provider provider) {
        nbt.putInt("experience", experience);
        nbt.putBoolean("has_died", hasDied);

        ListTag items = new ListTag();
        inventoryList.forEach((FatedInventoryItem item) -> {
            if (!item.isEmpty()) {
                items.add(item.save(provider));
            }
        });
        nbt.put("items", items);
        return nbt;
    }

    public void readNbt(CompoundTag nbt, Provider provider) {
        experience = nbt.getInt("experience");
        hasDied = nbt.getBoolean("has_died");
        
        ListTag items = nbt.getList("items", ListTag.TAG_COMPOUND);
        inventoryList.clear();
        items.forEach((Tag tag) -> {
            Optional<FatedInventoryItem> parsedItem = FatedInventoryItem.parse(provider, tag);
            if (parsedItem.isPresent()) {
                inventoryList.add(parsedItem.get());
            }
        });
    }
}
