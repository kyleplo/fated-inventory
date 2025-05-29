package com.kyleplo.fatedinventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class FatedInventoryItem {
    public static final TagKey<Item> NOT_SAVED_IN_ALTAR = TagKey.create(Registries.ITEM, new ResourceLocation(FatedInventory.MOD_ID, "not_saved_in_altar"));
    public static final TagKey<Enchantment> NOT_SAVED_IN_ALTAR_ENCHANTMENT = TagKey.create(Registries.ENCHANTMENT, new ResourceLocation(FatedInventory.MOD_ID, "not_saved_in_altar"));

    public ItemStack item;
    public int count;

    public FatedInventoryItem(ItemStack item, int count) {
        this.item = item.copy();
        this.count = count;
    }

    public FatedInventoryItem(ItemStack item) {
        this.item = item.copy();
        this.count = item.getCount();
    }

    public boolean isEmpty () {
        return item.isEmpty() || count <= 0;
    }

    public CompoundTag save () {
        CompoundTag itemTag = (CompoundTag) this.item.save(new CompoundTag());
        itemTag.putInt("count", count);
        return itemTag;
    }

    public static Optional<FatedInventoryItem> parse (Tag tag) {
        CompoundTag tagCopy = (CompoundTag) tag.copy();
        int count = tagCopy.getInt("count");
        tagCopy.putInt("count", 1);
        ItemStack parsedItem = ItemStack.of(tagCopy);

        if (count == 0 || parsedItem.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new FatedInventoryItem(parsedItem, count));
        }
    }

    public static ArrayList<FatedInventoryItem> listFromItemStackList(List<ItemStack> list) {
        return listFromItemStackList(new ArrayList<FatedInventoryItem>(), list);
    }

    public static ArrayList<FatedInventoryItem> listFromItemStackList(ArrayList<FatedInventoryItem> items, List<ItemStack> list) {
        list.forEach((ItemStack item) -> {
            if (item.isEmpty() || 
                EnchantmentHelper.hasVanishingCurse(item) || 
                item.is(FatedInventoryItem.NOT_SAVED_IN_ALTAR)
            ) {
                return;
            }

            for (FatedInventoryItem matchItem : items) {
                if (ItemStack.isSameItemSameTags(matchItem.item, item) && item.isStackable()) {
                    matchItem.count += item.getCount();
                    return;
                }
            }

            items.add(new FatedInventoryItem(item));
        });
        return items;
    }
}
