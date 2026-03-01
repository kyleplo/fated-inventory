package com.kyleplo.fatedinventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class FatedInventoryItem {
    public static final TagKey<Item> NOT_SAVED_IN_ALTAR = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FatedInventory.MOD_ID, "not_saved_in_altar"));
    public static final TagKey<Enchantment> NOT_SAVED_IN_ALTAR_ENCHANTMENT = TagKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(FatedInventory.MOD_ID, "not_saved_in_altar"));

    public static final Codec<Pair<ItemStack,Integer>> CODEC = Codec.pair(
        ItemStack.SINGLE_ITEM_CODEC.fieldOf("item").codec(),
        Codec.INT.fieldOf("count").codec()
    );
    public static final Codec<List<Pair<ItemStack, Integer>>> LIST_CODEC = CODEC.listOf();

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

    public static ArrayList<FatedInventoryItem> listFromItemStack(ArrayList<FatedInventoryItem> items, ItemStack item) {
        ArrayList<ItemStack> list = new ArrayList<>(1);
        list.add(item);
        return listFromItemStackList(items, list);
    }

    public static ArrayList<FatedInventoryItem> listFromItemStackList(Collection<ItemStack> list) {
        return listFromItemStackList(new ArrayList<FatedInventoryItem>(), list);
    }

    public static ArrayList<FatedInventoryItem> listFromItemStackList(ArrayList<FatedInventoryItem> items, Collection<ItemStack> list) {
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
