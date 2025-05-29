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
    protected boolean hasDied = false;
    protected ArrayList<FatedInventoryItem> inventoryList = new ArrayList<FatedInventoryItem>();

    public static final TagKey<Item> ALLOW_MODIFIED_COMPONENTS = TagKey.create(Registries.ITEM, new ResourceLocation(FatedInventory.MOD_ID, "allow_modified_components"));

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

    public int removeFromInventory(Inventory inventory, ItemStack matchItem, int max, DamageSource damageSource) {
        int removed = inventory.clearOrCountMatchingItems((ItemStack otherItem) -> ItemStack.isSameItemSameTags(matchItem, otherItem), max, inventory);
        if (removed < max) {
            removed += FatedInventory.compatRemoveMatchingItems(inventory.player, matchItem, max, damageSource);
        }
//        System.out.println("removed " + removed + "/" + max + " " + matchItem.getItem().getDescriptionId());
        return Math.max(max - removed, 0);
    }

    public void putInventory(Inventory inventory) {
        inventoryList = FatedInventoryItem.listFromItemStackList(inventory.items);
        FatedInventoryItem.listFromItemStackList(inventoryList, inventory.armor);
        FatedInventoryItem.listFromItemStackList(inventoryList, inventory.offhand);
        FatedInventoryItem.listFromItemStackList(inventoryList, FatedInventory.compatItems(inventory.player));

//        inventoryList.forEach((FatedInventoryItem item) -> {
//            System.out.println(item.item.getDescriptionId() + " x" + item.count);
//        });
    }

    public void compareInventory(Inventory inventory, DamageSource damageSource) {
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

                if (!item.item.isStackable() && ItemStack.isSameItem(item.item, compareItem.item) && (
                    item.item.is(FatedInventoryContainer.ALLOW_MODIFIED_COMPONENTS) || 
                    FatedInventory.config.anyNonstackableAllowsModifiedComponents ||
                    (FatedInventory.config.anyDurabilityItemAllowsModifiedComponents && item.item.getTagElement(ItemStack.TAG_DAMAGE) != null))  
                ) {
//                    System.out.println(item.item.getDescriptionId() + " is present in both and allows modified components, copying components to fated inventory and removing from real inventory");
                    item.item = compareItem.item.copy();
                    compareItem.count -= removeFromInventory(inventory, compareItem.item, 1, damageSource);
                    return;
                } else if (ItemStack.isSameItemSameTags(item.item, compareItem.item)) {
                    if (compareItem.count > item.count) {
//                        System.out.println(item.item.getDescriptionId() + " has increased, removing the amount in the fated inventory from the real inventory");
                        compareItem.count -= removeFromInventory(inventory, item.item, item.count, damageSource);
                    } else {
//                        System.out.println(item.item.getDescriptionId() + " has decreased/stayed the same, removing excess from fated inventory and removing all from the real inventory");
                        item.count = compareItem.count;
                        compareItem.count -= removeFromInventory(inventory, item.item, item.count, damageSource);
                    }
                    return;
                }
            }

//            System.out.println(item.item.getDescriptionId() + " is no longer in inventory, removing all from fated inventory");
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

    public CompoundTag saveNbt(CompoundTag nbt) {
        nbt.putInt("experience", experience);
        nbt.putBoolean("has_died", hasDied);

        ListTag items = new ListTag();
        inventoryList.forEach((FatedInventoryItem item) -> {
            if (!item.isEmpty()) {
                items.add(item.save());
            }
        });
        nbt.put("items", items);
        return nbt;
    }

    public void readNbt(CompoundTag nbt) {
        experience = nbt.getInt("experience");
        hasDied = nbt.getBoolean("has_died");
        
        ListTag items = nbt.getList("items", ListTag.TAG_COMPOUND);
        inventoryList.clear();
        items.forEach((Tag tag) -> {
            Optional<FatedInventoryItem> parsedItem = FatedInventoryItem.parse(tag);
            if (parsedItem.isPresent()) {
                inventoryList.add(parsedItem.get());
            }
        });
    }

    public ArrayList<FatedInventoryItem> getInventoryList() {
        return this.inventoryList;
    };

    public void setInventoryList(ArrayList<FatedInventoryItem> inventoryList) {
        this.inventoryList = inventoryList;
    };
}
