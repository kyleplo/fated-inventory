package com.kyleplo.fatedinventory.neoforge;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import com.kyleplo.fatedinventory.FatedInventory;
import com.kyleplo.fatedinventory.blocks.neoforge.FatedInventoryBlocksImpl;

@Mod(FatedInventory.MOD_ID)
public final class FatedInventoryNeoForge {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FatedInventory.MOD_ID);

    public static final Supplier<AttachmentType<FatedInventoryAttachment>> FATED_INVENTORY = ATTACHMENT_TYPES.register(
    "fated_inventory", () -> AttachmentType.serializable(() -> new FatedInventoryAttachment()).copyOnDeath().build()
);

    public FatedInventoryNeoForge(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(FatedInventoryNeoForge::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(FatedInventoryNeoForge::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(FatedInventoryNeoForge::onServerAboutToStart);
        modBus.addListener(FatedInventoryNeoForge::onBuildCreativeModeTabContents);

        ATTACHMENT_TYPES.register(modBus);

        // Run our common setup.
        FatedInventory.init();
        
        FatedInventoryBlocksImpl.BLOCKS.register(modBus);
        FatedInventoryBlocksImpl.ITEMS.register(modBus);
        FatedInventoryBlocksImpl.BLOCK_TYPES.register(modBus);
        FatedInventoryBlocksImpl.SOUNDS.register(modBus);
    }

    private static void onPlayerRespawn (PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!event.isEndConquered() && !player.level().isClientSide()) {
            FatedInventory.handlePlayerRespawn(player);
        }
    }

    private static void onLivingDeath (LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            FatedInventory.handlePlayerDeath((Player) event.getEntity(), event.getSource());
        }
    }

    private static void onBuildCreativeModeTabContents (BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            FatedInventoryBlocksImpl.itemsForCreativeTab.forEach(item -> {
                event.accept(item);
            });
        }
    }

    private static void onServerAboutToStart (ServerAboutToStartEvent event) {
        FatedInventory.handleRegisterStructure(event.getServer());
    }
}
