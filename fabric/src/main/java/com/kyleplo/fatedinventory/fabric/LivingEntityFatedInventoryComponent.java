package com.kyleplo.fatedinventory.fabric;

import com.kyleplo.fatedinventory.FatedInventoryContainer;

import net.minecraft.nbt.CompoundTag;

public class LivingEntityFatedInventoryComponent extends FatedInventoryContainer implements FatedInventoryComponent {
    @Override
    public void readFromNbt(CompoundTag nbt) {
        readNbt(nbt);
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        saveNbt(nbt);
    }
}
