package com.kyleplo.fatedinventory.neoforge;

import javax.annotation.Nonnull;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import com.kyleplo.fatedinventory.FatedInventoryContainer;

public class FatedInventoryAttachment extends FatedInventoryContainer implements INBTSerializable<CompoundTag> {
    @Override
    public CompoundTag serializeNBT(@Nonnull Provider provider) {
        return saveNbt(new CompoundTag(), provider);
    }

    @Override
    public void deserializeNBT(@Nonnull Provider provider, @Nonnull CompoundTag nbt) {
        readNbt(nbt, provider);
    }
}
