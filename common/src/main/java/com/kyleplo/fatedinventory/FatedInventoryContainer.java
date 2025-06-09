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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class FatedInventoryContainer implements IFatedInventoryContainer {
    protected int experience = 0;
    protected int storedExperience = 0;
    protected ArrayList<FatedInventoryItem> inventoryList = new ArrayList<FatedInventoryItem>();
    protected ArrayList<FatedInventoryItem> savedInventoryList = new ArrayList<FatedInventoryItem>();

    public static final TagKey<Item> ALLOW_MODIFIED_COMPONENTS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, "allow_modified_components"));

    public boolean hasStored() {
        return storedExperience > 0 || hasItemsStored();
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

    public void putInventory(Player player) {
        if (FatedInventory.config.fateStoresXp) {
            experience = FatedInventory.experienceLevelsToPoints((float) player.experienceLevel + player.experienceProgress);
        }

        if (!FatedInventory.config.fateStoresItems) {
            inventoryList = new ArrayList<>();
            return;
        }

        Inventory inventory = player.getInventory();

        inventoryList = FatedInventoryItem.listFromItemStackList(inventory.items, true);
        FatedInventoryItem.listFromItemStackList(inventoryList, inventory.armor, true);
        FatedInventoryItem.listFromItemStackList(inventoryList, inventory.offhand, true);
        FatedInventoryItem.listFromItemStackList(inventoryList, FatedInventory.compatItems(player), true);

//        inventoryList.forEach((FatedInventoryItem item) -> {
//            System.out.println(item.item.getDescriptionId() + " x" + item.count);
//        });
    }

    public void compareInventory(Player player, DamageSource damageSource) {
        if (FatedInventory.config.fateStoresXp) {
            int xpToTransfer = Math.min(experience, FatedInventory.experienceLevelsToPoints((float) player.experienceLevel + player.experienceProgress));
            player.giveExperiencePoints(0 - xpToTransfer);
            storedExperience += xpToTransfer;
        }

        if (!FatedInventory.config.fateStoresItems) {
            inventoryList = new ArrayList<>();
            return;
        }

        Inventory inventory = player.getInventory();

        ArrayList<FatedInventoryItem> compareList = FatedInventoryItem.listFromItemStackList(inventory.items, false);
        FatedInventoryItem.listFromItemStackList(compareList, inventory.armor, false);
        FatedInventoryItem.listFromItemStackList(compareList, inventory.offhand, false);
        FatedInventoryItem.listFromItemStackList(compareList, FatedInventory.compatItems(player), false);

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
                    FatedInventoryItem.listFromItemStack(savedInventoryList, itemCopy, false);
                    compareItem.item.setCount(0);
                    return;
                } else if (ItemStack.isSameItemSameComponents(item.item, compareItem.item)) {
//                    System.out.println(item.item.getDescriptionId() + " is present in both, moving the amount in the fated inventory from real inventory to saved inventory");
                    ItemStack itemCopy = compareItem.item.copy();
                    int moved = removeFromInventory(inventory, item.item, item.count, damageSource);
                    compareItem.count -= moved;
                    itemCopy.setCount(moved);
                    FatedInventoryItem.listFromItemStack(savedInventoryList, itemCopy, false);
                    return;
                }
            }
        });
    }

    public void dropInventoryFor (Player player) {
        player.giveExperiencePoints(storedExperience);
        storedExperience = 0;

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

    public CompoundTag saveNbt(CompoundTag nbt, Provider provider) {
        nbt.putInt("experience", experience);
        nbt.putInt("storedExperience", storedExperience);

        ListTag items = new ListTag();
        inventoryList.forEach((FatedInventoryItem item) -> {
            if (!item.isEmpty()) {
                items.add(item.save(provider));
            }
        });
        nbt.put("items", items);

        ListTag savedItems = new ListTag();
        savedInventoryList.forEach((FatedInventoryItem item) -> {
            if (!item.isEmpty()) {
                savedItems.add(item.save(provider));
            }
        });
        nbt.put("savedItems", savedItems);
        return nbt;
    }

    public void readNbt(CompoundTag nbt, Provider provider) {
        experience = nbt.getInt("experience");
        storedExperience = nbt.getInt("storedExperience");
        
        ListTag items = nbt.getList("items", ListTag.TAG_COMPOUND);
        inventoryList.clear();
        items.forEach((Tag tag) -> {
            Optional<FatedInventoryItem> parsedItem = FatedInventoryItem.parse(provider, tag);
            if (parsedItem.isPresent()) {
                inventoryList.add(parsedItem.get());
            }
        });

        ListTag savedItems = nbt.getList("savedItems", ListTag.TAG_COMPOUND);
        savedInventoryList.clear();
        savedItems.forEach((Tag tag) -> {
            Optional<FatedInventoryItem> parsedItem = FatedInventoryItem.parse(provider, tag);
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
