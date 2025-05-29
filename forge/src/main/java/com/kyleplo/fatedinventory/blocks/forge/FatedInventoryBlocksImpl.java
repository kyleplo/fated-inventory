package com.kyleplo.fatedinventory.blocks.forge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.kyleplo.fatedinventory.FatedInventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FatedInventoryBlocksImpl {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FatedInventory.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FatedInventory.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FatedInventory.MOD_ID);
    public static final List<RegistryObject<Item>> itemsForCreativeTab = new ArrayList<>();

    public static <B extends Block> void register (String name, Function<BlockBehaviour.Properties, ? extends B> func) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> func.apply(BlockBehaviour.Properties.of()));
        itemsForCreativeTab.add(ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties())));
    }

    @SuppressWarnings("removal")
    public static Supplier<SoundEvent> registerSoundEvent (String name) {
        RegistryObject<SoundEvent> soundEvent = SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FatedInventory.MOD_ID, name)));
        return () -> soundEvent.get();
    }
}
