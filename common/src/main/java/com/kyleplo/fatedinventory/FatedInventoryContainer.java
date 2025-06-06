package com.kyleplo.fatedinventory;

import java.util.ArrayList;
import java.util.Optional;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FatedInventoryContainer implements IFatedInventoryContainer {
    protected int experience = 0;
    protected ArrayList<FatedInventoryItem> inventoryList = new ArrayList<FatedInventoryItem>();
    protected ArrayList<FatedInventoryItem> savedInventoryList = new ArrayList<FatedInventoryItem>();

    public static final TagKey<Item> ALLOW_MODIFIED_COMPONENTS = TagKey.create(Registries.ITEM, new ResourceLocation(FatedInventory.MOD_ID, "allow_modified_components"));

    public int getExperience() {
        return this.experience;
    }

    public void setExperience(int experience) {
        if (FatedInventory.config.fateStoresXp) {
            this.experience = experience;
        }
    }

    public boolean hasStored() {
        return experience > 0 || hasItemsStored();
    }

    public boolean hasItemsStored() {
        return savedInventoryList.size() > 0;
    }

    public int removeFromInventory(Inventory inventory, ItemStack matchItem, int max, DamageSource damageSource) {
        int removed = inventory.clearOrCountMatchingItems((ItemStack otherItem) -> FatedInventoryItem.isCloseEnough(matchItem, otherItem), max, inventory);
        if (removed < max) {
            removed += FatedInventory.compatRemoveMatchingItems(inventory.player, matchItem, max, damageSource);
        }
//        System.out.println("removed " + removed + "/" + max + " " + matchItem.getItem().getDescriptionId());
        return removed;
    }

    public void putInventory(Inventory inventory) {
        if (!FatedInventory.config.fateStoresItems) {
            inventoryList = new ArrayList<>();
            return;
        }

        inventoryList = FatedInventoryItem.listFromItemStackList(inventory.items);
        FatedInventoryItem.listFromItemStackList(inventoryList, inventory.armor);
        FatedInventoryItem.listFromItemStackList(inventoryList, inventory.offhand);
        FatedInventoryItem.listFromItemStackList(inventoryList, FatedInventory.compatItems(inventory.player));

//        inventoryList.forEach((FatedInventoryItem item) -> {
//            System.out.println(item.item.getDescriptionId() + " x" + item.count);
//        });
    }

    public void compareInventory(Inventory inventory, DamageSource damageSource) {
        if (!FatedInventory.config.fateStoresItems) {
            inventoryList = new ArrayList<>();
            return;
        }

        ArrayList<FatedInventoryItem> compareList = FatedInventoryItem.listFromItemStackList(inventory.items);
        FatedInventoryItem.listFromItemStackList(compareList, inventory.armor);
        FatedInventoryItem.listFromItemStackList(compareList, inventory.offhand);
        FatedInventoryItem.listFromItemStackList(compareList, FatedInventory.compatItems(inventory.player));

//        compareList.forEach((FatedInventoryItem item) -> {
//            System.out.println(item.item.getDescriptionId() + " x" + item.count);
//        });

        inventoryList.forEach((FatedInventoryItem item) -> {
            if (item.isEmpty()) {
                return;
            }

            for (FatedInventoryItem compareItem : compareList) {
                if (compareItem.isEmpty()) {
                    continue;
                }

                if (FatedInventoryItem.isSameWithModifiedComponents(item, compareItem)) {
//                    System.out.println(item.item.getDescriptionId() + " is present in both and allows modified components, copying item to saved inventory and removing from real inventory");
                    item.item = compareItem.item.copy();
                    ItemStack itemCopy = compareItem.item.copy();
                    int moved = removeFromInventory(inventory, compareItem.item, 1, damageSource);
                    compareItem.count -= moved;
                    itemCopy.setCount(moved);
                    FatedInventoryItem.listFromItemStack(savedInventoryList, itemCopy);
                    compareItem.item.setCount(0);
                    return;
                } else if (ItemStack.isSameItemSameTags(item.item, compareItem.item)) {
//                    System.out.println(item.item.getDescriptionId() + " is present in both, moving the amount in the fated inventory from real inventory to saved inventory");
                    ItemStack itemCopy = compareItem.item.copy();
                    int moved = removeFromInventory(inventory, item.item, item.count, damageSource);
                    compareItem.count -= moved;
                    itemCopy.setCount(moved);
                    FatedInventoryItem.listFromItemStack(savedInventoryList, itemCopy);
                    return;
                }
            }
        });

//        savedInventoryList.forEach((FatedInventoryItem item) -> {
//            System.out.println(item.item.getDescriptionId() + " x" + item.count);
//        });
    }

    public void dropInventoryFor (Player player) {
        savedInventoryList.forEach((FatedInventoryItem item) -> {
            int count = item.count;
            while (count > 0) {
                int toDrop = Math.min(count, item.item.getMaxStackSize());
                ItemStack itemCopy = item.item.copyWithCount(toDrop);
                player.drop(itemCopy, true);
                count -= toDrop;
            }
        });
        savedInventoryList = new ArrayList<>();
    }

    public CompoundTag saveNbt(CompoundTag nbt) {
        nbt.putInt("experience", experience);

        ListTag items = new ListTag();
        inventoryList.forEach((FatedInventoryItem item) -> {
            if (!item.isEmpty()) {
                items.add(item.save());
            }
        });
        nbt.put("items", items);

        ListTag savedItems = new ListTag();
        savedInventoryList.forEach((FatedInventoryItem item) -> {
            if (!item.isEmpty()) {
                savedItems.add(item.save());
            }
        });
        nbt.put("savedItems", savedItems);
        return nbt;
    }

    public void readNbt(CompoundTag nbt) {
        experience = nbt.getInt("experience");
        
        ListTag items = nbt.getList("items", ListTag.TAG_COMPOUND);
        inventoryList.clear();
        items.forEach((Tag tag) -> {
            Optional<FatedInventoryItem> parsedItem = FatedInventoryItem.parse(tag);
            if (parsedItem.isPresent()) {
                inventoryList.add(parsedItem.get());
            }
        });

        ListTag savedItems = nbt.getList("savedItems", ListTag.TAG_COMPOUND);
        savedInventoryList.clear();
        savedItems.forEach((Tag tag) -> {
            Optional<FatedInventoryItem> parsedItem = FatedInventoryItem.parse(tag);
            if (parsedItem.isPresent()) {
                savedInventoryList.add(parsedItem.get());
            }
        });
    }

    public void clearFatedInventory() {
        experience = 0;
        inventoryList = new ArrayList<>();
    }
}
