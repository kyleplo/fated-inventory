package com.kyleplo.fatedinventory;

import java.util.ArrayList;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public interface IFatedInventoryContainer {
    public int getExperience();
    public void setExperience(int experience);
    public boolean getHasDied();
    public boolean hasStored();
    public boolean hasItemsStored();
    public void setHasDied(boolean hasDied);
    public void clear();
    public void putInventory(Inventory inventory);
    public void compareInventory(Inventory inventory, DamageSource damageSource);
    public void dropInventoryFor(Player player);
    public CompoundTag saveNbt(CompoundTag nbt);
    public void readNbt(CompoundTag nbt);
    public ArrayList<FatedInventoryItem> getInventoryList();
    public void setInventoryList(ArrayList<FatedInventoryItem> inventoryList);
}
