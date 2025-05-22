package com.kyleplo.fatedinventory.blocks;

import java.util.function.Function;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class FatedInventoryBlocks {
    public static Holder<SoundEvent> FATED_ALTAR_CHARGE;
    public static Holder<SoundEvent> FATED_ALTAR_DEPLETE;
    public static Holder<SoundEvent> FATED_ALTAR_FATE_SEALED;
    public static Holder<SoundEvent> FATED_ALTAR_FATE_CUT;

    @ExpectPlatform
    public static <B extends Block> void register (String name, Function<BlockBehaviour.Properties, ? extends B> func) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <B extends Block> void registerBlockType (Function<BlockBehaviour.Properties, ? extends B> func) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Holder<SoundEvent> registerSoundEvent (String name) {
        throw new AssertionError();
    }

    public static void initialize() {
        registerBlockType(FatedAltarBlock::new);

        register("fated_altar", FatedAltarBlock::new);

        FATED_ALTAR_CHARGE = registerSoundEvent("block.fated_altar.fated_altar.charge");
        FATED_ALTAR_DEPLETE = registerSoundEvent("block.fated_altar.fated_altar.deplete");
        FATED_ALTAR_FATE_SEALED = registerSoundEvent("block.fated_altar.fated_altar.fate_sealed");
        FATED_ALTAR_FATE_CUT = registerSoundEvent("block.fated_altar.fated_altar.fate_cut");
    }
}
