package com.kyleplo.fatedinventory.forge;

import com.kyleplo.fatedinventory.FatedInventoryContainer;
import com.kyleplo.fatedinventory.IFatedInventoryContainer;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class FatedInventoryCapability {
    public static ICapabilityProvider createProvider(final LivingEntity LivingEntity) {
        return new Provider(LivingEntity);
    }

    public static class FatedInventoryWrapper extends FatedInventoryContainer {
        LivingEntity owner;

        public FatedInventoryWrapper(final LivingEntity livingEntity) {
            this.owner = livingEntity;
        }

        public CompoundTag writeTag() {
            return saveNbt(new CompoundTag());
        }

        public void readTag(CompoundTag nbt) {
            readNbt(nbt);
        }
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        final LazyOptional<IFatedInventoryContainer> optional;
        final IFatedInventoryContainer handler;
        final LivingEntity owner;

        Provider(final LivingEntity livingEntity) {
            this.owner = livingEntity;
            this.handler = new FatedInventoryWrapper(this.owner);
            this.optional = LazyOptional.of(() -> this.handler);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
            return FatedInventoryForge.FATED_INVENTORY.orEmpty(capability, this.optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return ((FatedInventoryWrapper) this.handler).writeTag();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            ((FatedInventoryWrapper) this.handler).readTag(nbt);
        }
    }
}
