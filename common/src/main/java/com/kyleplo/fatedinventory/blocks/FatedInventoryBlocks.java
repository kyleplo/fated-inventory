package com.kyleplo.fatedinventory.blocks;

import java.util.function.Function;
import java.util.function.Supplier;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class FatedInventoryBlocks {
    public static Supplier<SoundEvent> FATED_ALTAR_CHARGE;
    public static Supplier<SoundEvent> FATED_ALTAR_DEPLETE;
    public static Supplier<SoundEvent> FATED_ALTAR_FATE_SEALED;
    public static Supplier<SoundEvent> FATED_ALTAR_FATE_CUT;

    @ExpectPlatform
    public static <B extends Block> void register (String name, Function<BlockBehaviour.Properties, ? extends B> func) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Supplier<SoundEvent> registerSoundEvent (String name) {
        throw new AssertionError();
    }

    public static void initialize() {
        register("fated_altar", FatedAltarBlock::new);

        FATED_ALTAR_CHARGE = registerSoundEvent("block.fated_altar.fated_altar.charge");
        FATED_ALTAR_DEPLETE = registerSoundEvent("block.fated_altar.fated_altar.deplete");
        FATED_ALTAR_FATE_SEALED = registerSoundEvent("block.fated_altar.fated_altar.fate_sealed");
        FATED_ALTAR_FATE_CUT = registerSoundEvent("block.fated_altar.fated_altar.fate_cut");
    }
}
