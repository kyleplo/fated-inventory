package com.kyleplo.fatedinventory.blocks.neoforge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.kyleplo.fatedinventory.FatedInventory;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FatedInventoryBlocksImpl {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FatedInventory.MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FatedInventory.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, FatedInventory.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, FatedInventory.MOD_ID);
    public static final List<DeferredItem<BlockItem>> itemsForCreativeTab = new ArrayList<>();

    public static <B extends Block> void register (String name, Function<BlockBehaviour.Properties, ? extends B> func) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, func);
        itemsForCreativeTab.add(ITEMS.registerSimpleBlockItem(name, block, new Item.Properties()));
    }

    public static <B extends Block> void registerBlockType (Function<BlockBehaviour.Properties, ? extends B> func) {
        BLOCK_TYPES.register(
            "simple",
            () -> BlockBehaviour.simpleCodec(func));
    }

    public static Holder<SoundEvent> registerSoundEvent (String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, name)));
    }
}
