package com.kyleplo.fatedinventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class FatedInventoryItem {
    public static final TagKey<Item> NOT_SAVED_IN_ALTAR = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, "not_saved_in_altar"));
    public static final TagKey<Enchantment> NOT_SAVED_IN_ALTAR_ENCHANTMENT = TagKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, "not_saved_in_altar"));

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

    public CompoundTag save (Provider provider) {
        CompoundTag itemTag = (CompoundTag) this.item.save(provider);
        itemTag.putInt("count", count);
        return itemTag;
    }

    public static Optional<FatedInventoryItem> parse (Provider provider, Tag tag) {
        CompoundTag tagCopy = (CompoundTag) tag.copy();
        int count = tagCopy.getInt("count");
        tagCopy.putInt("count", 1);
        Optional<ItemStack> parsedItem = ItemStack.parse(provider, tagCopy);

        if (count == 0 || parsedItem.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new FatedInventoryItem(parsedItem.get(), count));
        }
    }

    public static ArrayList<FatedInventoryItem> listFromItemStack(ArrayList<FatedInventoryItem> items, ItemStack item, boolean flatten) {
        ArrayList<ItemStack> list = new ArrayList<>(1);
        list.add(item);
        return listFromItemStackList(items, list, flatten);
    }

    public static ArrayList<FatedInventoryItem> listFromItemStackList(List<ItemStack> list, boolean flatten) {
        return listFromItemStackList(new ArrayList<FatedInventoryItem>(), list, flatten);
    }

    public static ArrayList<FatedInventoryItem> listFromItemStackList(ArrayList<FatedInventoryItem> items, List<ItemStack> list, boolean flatten) {
        list.forEach((ItemStack item) -> {
            if (item.isEmpty() || 
                EnchantmentHelper.has(item, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) || 
                item.is(FatedInventoryItem.NOT_SAVED_IN_ALTAR) ||
                EnchantmentHelper.hasTag(item, NOT_SAVED_IN_ALTAR_ENCHANTMENT)
            ) {
                return;
            }

            for (FatedInventoryItem matchItem : items) {
                if (ItemStack.isSameItemSameComponents(matchItem.item, item) && item.isStackable()) {
                    matchItem.count += item.getCount();
                    return;
                }
            }

            if (flatten && FatedInventory.config.experimentalFlattenContainerItems) {
                if (item.has(DataComponents.CONTAINER)) {
                    ItemContainerContents itemContainer = item.get(DataComponents.CONTAINER);
                    ArrayList<ItemStack> containerItemList = new ArrayList<ItemStack>();
                    for (ItemStack containerItem : itemContainer.nonEmptyItems()) {
                        containerItemList.add(containerItem);
                    };
                    listFromItemStackList(items, containerItemList, true);
                }
                if (item.has(DataComponents.BUNDLE_CONTENTS)) {
                    BundleContents bundleContents = item.get(DataComponents.BUNDLE_CONTENTS);
                    ArrayList<ItemStack> containerItemList = new ArrayList<ItemStack>();
                    for (ItemStack containerItem : bundleContents.items()) {
                        containerItemList.add(containerItem);
                    };
                    listFromItemStackList(items, containerItemList, true);
                }
            }

            items.add(new FatedInventoryItem(item));
        });
        return items;
    }

    public static boolean isSameWithModifiedComponents(FatedInventoryItem a, FatedInventoryItem b) {
        return isSameWithModifiedComponents(a.item, b.item);
    }

    public static boolean isSameWithModifiedComponents(ItemStack a, ItemStack b) {
        return !a.isStackable() && ItemStack.isSameItem(a, b) && (
            a.is(FatedInventoryContainer.ALLOW_MODIFIED_COMPONENTS) || 
            FatedInventory.config.anyNonstackableAllowsModifiedComponents ||
            (FatedInventory.config.anyDurabilityItemAllowsModifiedComponents && a.has(DataComponents.DAMAGE)));
    }

    public static boolean isCloseEnough(FatedInventoryItem a, FatedInventoryItem b) {
        return isCloseEnough(a.item, b.item);
    }

    public static boolean isCloseEnough(ItemStack a, ItemStack b) {
        return isSameWithModifiedComponents(a, b) || ItemStack.isSameItemSameComponents(a, b);
    }
}
