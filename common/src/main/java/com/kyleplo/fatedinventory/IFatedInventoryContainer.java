package com.kyleplo.fatedinventory;

import java.util.List;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface IFatedInventoryContainer {
    public boolean hasStored();
    public boolean hasItemsStored();
    public void putInventory(Player player);
    public void compareInventory(Player player, DamageSource damageSource);
    public void dropInventoryFor(Player player);
    public void serialize(ValueOutput output);
    public void deserialize(ValueInput input);
    public void clearFatedInventory();
    public void clearStored();
    public int getExperience();
    public int getStoredExperience();
    public List<FatedInventoryItem> getItems();
    public List<FatedInventoryItem> getStoredItems();
}
