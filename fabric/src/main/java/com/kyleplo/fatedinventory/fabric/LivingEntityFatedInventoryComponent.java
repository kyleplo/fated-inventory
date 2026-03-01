package com.kyleplo.fatedinventory.fabric;

import com.kyleplo.fatedinventory.FatedInventoryContainer;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LivingEntityFatedInventoryComponent extends FatedInventoryContainer implements FatedInventoryComponent {
    @Override
    public void readData(ValueInput readView) {
        deserialize(readView);
    }

    @Override
    public void writeData(ValueOutput writeView) {
        serialize(writeView);
    }
}
