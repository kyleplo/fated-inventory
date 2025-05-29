package com.kyleplo.fatedinventory.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.kyleplo.fatedinventory.FatedInventory;
import com.kyleplo.fatedinventory.IFatedInventoryContainer;
import com.kyleplo.fatedinventory.blocks.forge.FatedInventoryBlocksImpl;

@Mod(FatedInventory.MOD_ID)
public final class FatedInventoryForge {
    public static Capability<IFatedInventoryContainer> FATED_INVENTORY = CapabilityManager.get(new CapabilityToken<>(){});

    public FatedInventoryForge() {
        @SuppressWarnings("removal")
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        MinecraftForge.EVENT_BUS.addListener(FatedInventoryForge::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(FatedInventoryForge::onPlayerRespawn);
        MinecraftForge.EVENT_BUS.addListener(FatedInventoryForge::onServerAboutToStart);
        modBus.addListener(FatedInventoryForge::onRegisterCapabilities);
        modBus.addListener(FatedInventoryForge::onBuildCreativeModeTabContents);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, FatedInventoryForge::onAttachCapabilities);
        MinecraftForge.EVENT_BUS.addListener(FatedInventoryForge::onPlayerClone);

        // Run our common setup.
        FatedInventory.init();
        
        FatedInventoryBlocksImpl.BLOCKS.register(modBus);
        FatedInventoryBlocksImpl.ITEMS.register(modBus);
        FatedInventoryBlocksImpl.SOUNDS.register(modBus);
    }

    private static void onPlayerClone (PlayerEvent.Clone event) {
        Player player = event.getEntity();
        Player oldPlayer = event.getOriginal();
        oldPlayer.revive();
        LazyOptional<IFatedInventoryContainer> oldHandler = oldPlayer.getCapability(FatedInventoryForge.FATED_INVENTORY);
        LazyOptional<IFatedInventoryContainer> newHandler = player.getCapability(FatedInventoryForge.FATED_INVENTORY);
        oldHandler.ifPresent(
            oldFatedInventory -> newHandler.ifPresent(
                newFatedInventory -> newFatedInventory.readNbt(((FatedInventoryCapability.FatedInventoryWrapper) oldFatedInventory).writeTag())));
    }

    private static void onRegisterCapabilities (RegisterCapabilitiesEvent event) {
        event.register(IFatedInventoryContainer.class);
    }

    @SuppressWarnings("removal")
    private static void onAttachCapabilities (AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(FatedInventory.MOD_ID, "fated_inventory"), FatedInventoryCapability.createProvider((Player) event.getObject()));
        }
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
