package com.kyleplo.fatedinventory;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public interface IFatedInventoryContainer {
    public boolean hasStored();
    public boolean hasItemsStored();
    public void putInventory(Player player);
    public void compareInventory(Player player, DamageSource damageSource);
    public void dropInventoryFor(Player player);
    public CompoundTag saveNbt(CompoundTag nbt);
    public void readNbt(CompoundTag nbt);
    public void clearFatedInventory();
    public void clearStored();
    public int getExperience();
    public int getStoredExperience();
    public List<FatedInventoryItem> getItems();
    public List<FatedInventoryItem> getStoredItems();
}
