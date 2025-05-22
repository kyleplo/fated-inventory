package com.kyleplo.fatedinventory.fabric;

import com.kyleplo.fatedinventory.FatedInventoryContainer;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;

public class LivingEntityFatedInventoryComponent extends FatedInventoryContainer implements FatedInventoryComponent {
    @Override
    public void readFromNbt(CompoundTag nbt, Provider provider) {
        readNbt(nbt, provider);
    }

    @Override
    public void writeToNbt(CompoundTag nbt, Provider provider) {
        saveNbt(nbt, provider);
    }
}
