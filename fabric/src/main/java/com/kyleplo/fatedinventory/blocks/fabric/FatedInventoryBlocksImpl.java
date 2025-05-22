package com.kyleplo.fatedinventory.blocks.fabric;

import java.util.function.Function;

import com.kyleplo.fatedinventory.FatedInventory;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class FatedInventoryBlocksImpl {
    public static <B extends Block> void register (String name, Function<BlockBehaviour.Properties, ? extends B> func) {
        B block = func.apply(BlockBehaviour.Properties.of());
        BlockItem blockItem = new BlockItem(block, new Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, name), blockItem);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, name), block);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
		.register((itemGroup) -> itemGroup.accept(blockItem));
    }

    public static <B extends Block> void registerBlockType (Function<BlockBehaviour.Properties, ? extends B> func) {
        Registry.register(BuiltInRegistries.BLOCK_TYPE,
            ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, "simple"),
            BlockBehaviour.simpleCodec(func));
    }

     public static Holder<SoundEvent> registerSoundEvent (String name) {
        return Holder.direct(
            Registry.register(BuiltInRegistries.SOUND_EVENT,
                ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, name),
                SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, name))));
     }
}
