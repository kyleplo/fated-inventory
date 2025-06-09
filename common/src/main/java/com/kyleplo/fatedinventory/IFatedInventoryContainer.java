package com.kyleplo.fatedinventory;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public interface IFatedInventoryContainer {
    public boolean hasStored();
    public boolean hasItemsStored();
    public void putInventory(Player player);
    public void compareInventory(Player player, DamageSource damageSource);
    public void dropInventoryFor(Player player);
    public CompoundTag saveNbt(CompoundTag nbt, Provider provider);
    public void readNbt(CompoundTag nbt, Provider provider);
    public void clearFatedInventory();
}
