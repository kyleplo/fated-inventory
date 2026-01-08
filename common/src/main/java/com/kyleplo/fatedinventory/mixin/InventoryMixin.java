package com.kyleplo.fatedinventory.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

@Mixin(Inventory.class)
public interface InventoryMixin {
    @Accessor("items")
    NonNullList<ItemStack> getItems();

    @Accessor("equipment")
    EntityEquipment getEquipment();
}
