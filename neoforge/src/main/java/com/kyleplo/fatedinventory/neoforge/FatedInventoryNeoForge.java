package com.kyleplo.fatedinventory.neoforge;

import net.neoforged.fml.common.Mod;

import com.kyleplo.fatedinventory.FatedInventory;

@Mod(FatedInventory.MOD_ID)
public final class FatedInventoryNeoForge {
    public FatedInventoryNeoForge() {
        // Run our common setup.
        FatedInventory.init();
    }
}
