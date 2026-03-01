package com.kyleplo.fatedinventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.TypedInputList;
import net.minecraft.world.level.storage.ValueOutput;

import com.kyleplo.fatedinventory.mixin.EntityEquipmentMixin;
import com.kyleplo.fatedinventory.mixin.InventoryMixin;
import com.mojang.datafixers.util.Pair;;

public abstract class FatedInventoryContainer implements IFatedInventoryContainer {
    protected int experience = 0;
    protected int storedExperience = 0;
    protected ArrayList<FatedInventoryItem> inventoryList = new ArrayList<FatedInventoryItem>();
    protected ArrayList<FatedInventoryItem> savedInventoryList = new ArrayList<FatedInventoryItem>();

    public static final TagKey<Item> ALLOW_MODIFIED_COMPONENTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FatedInventory.MOD_ID, "allow_modified_components"));

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

        inventoryList = FatedInventoryItem.listFromItemStackList(((InventoryMixin) inventory).getItems());
        FatedInventoryItem.listFromItemStackList(inventoryList, ((EntityEquipmentMixin) ((InventoryMixin) inventory).getEquipment()).getItems().values());
        FatedInventoryItem.listFromItemStackList(inventoryList, FatedInventory.compatItems(player));

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

        ArrayList<FatedInventoryItem> compareList = FatedInventoryItem.listFromItemStackList(((InventoryMixin) inventory).getItems());
        FatedInventoryItem.listFromItemStackList(compareList, ((EntityEquipmentMixin) ((InventoryMixin) inventory).getEquipment()).getItems().values());
        FatedInventoryItem.listFromItemStackList(compareList, FatedInventory.compatItems(player));

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
                } else if (ItemStack.isSameItemSameComponents(item.item, compareItem.item)) {
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

    public void serialize(ValueOutput output) {
        output.putInt("experience", experience);
        output.putInt("storedExperience", storedExperience);

        ArrayList<Pair<ItemStack, Integer>> items = new ArrayList<>();
        inventoryList.forEach((FatedInventoryItem item) -> {
            if (!item.isEmpty()) {
                items.add(new Pair<ItemStack, Integer>(item.item, item.count));
            }
        });
        output.store("items", FatedInventoryItem.LIST_CODEC, items);

        ArrayList<Pair<ItemStack, Integer>> savedItems = new ArrayList<>();
        savedInventoryList.forEach((FatedInventoryItem item) -> {
            if (!item.isEmpty()) {
                savedItems.add(new Pair<ItemStack, Integer>(item.item, item.count));
            }
        });
        output.store("savedItems", FatedInventoryItem.LIST_CODEC, savedItems);
    }

    public void deserialize(ValueInput input) {
        experience = input.getInt("experience").orElse(0);
        storedExperience = input.getInt("storedExperience").orElse(0);
        
        TypedInputList<Pair<ItemStack,Integer>> items = input.listOrEmpty("items", FatedInventoryItem.CODEC);
        inventoryList.clear();
        items.forEach((Pair<ItemStack, Integer> item) -> {
            inventoryList.add(new FatedInventoryItem(item.getFirst(), item.getSecond()));
        });

        TypedInputList<Pair<ItemStack,Integer>> savedItems = input.listOrEmpty("savedItems", FatedInventoryItem.CODEC);
        savedInventoryList.clear();
        savedItems.forEach((Pair<ItemStack, Integer> item) -> {
            savedInventoryList.add(new FatedInventoryItem(item.getFirst(), item.getSecond()));
        });
    }

    public void clearFatedInventory() {
        experience = 0;
        inventoryList = new ArrayList<>();
    }

    public void clearStored() {
        storedExperience = 0;
        savedInventoryList = new ArrayList<>();
    }

    public int getExperience() {
        return experience;
    }

    public int getStoredExperience() {
        return storedExperience;
    }

    public List<FatedInventoryItem> getItems() {
        return inventoryList;
    }

    public List<FatedInventoryItem> getStoredItems() {
        return savedInventoryList;
    }
}
