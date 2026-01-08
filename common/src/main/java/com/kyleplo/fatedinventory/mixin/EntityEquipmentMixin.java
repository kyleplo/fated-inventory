package com.kyleplo.fatedinventory.mixin;

import java.util.EnumMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

@Mixin(EntityEquipment.class)
public interface EntityEquipmentMixin {
    @Accessor("items")
    public EnumMap<EquipmentSlot, ItemStack> getItems();
}
