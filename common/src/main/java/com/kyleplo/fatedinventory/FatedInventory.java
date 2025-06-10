package com.kyleplo.fatedinventory;

import java.util.ArrayList;
import java.util.List;

import com.kyleplo.fatedinventory.blocks.FatedInventoryBlocks;
import com.kyleplo.fatedinventory.mixin.StructureTemplatePoolMixin;
import com.mojang.datafixers.util.Pair;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class FatedInventory {
    public static final String MOD_ID = "fated_inventory";
    public static Config config;

    public static void init() {
        config = Config.init();
        FatedInventoryBlocks.initialize();
    }

    public static void handlePlayerDeath(Player player, DamageSource source) {
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        IFatedInventoryContainer fatedInventory = getFatedInventoryContainer(player);

        fatedInventory.compareInventory(player, source);
    }

    public static void handlePlayerRespawn(Player player) {
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        IFatedInventoryContainer fatedInventory = getFatedInventoryContainer(player);
        if (fatedInventory.hasStored()) {
            if (config.showMessageOnRespawn) {
                if (fatedInventory.hasItemsStored()) {
                    player.displayClientMessage(Component.translatable("gui.fated_inventory.fated_inventory.items_retrievable"), false);
                } else {
                    player.displayClientMessage(Component.translatable("gui.fated_inventory.fated_inventory.experience_retrievable"), false);
                }
            }
        }
    }

    @ExpectPlatform
    public static IFatedInventoryContainer getFatedInventoryContainer (Player player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static List<ItemStack> compatItems (Player player) {
        return new ArrayList<ItemStack>();
    }

    @ExpectPlatform
    public static int compatRemoveMatchingItems (Player player, ItemStack matchItem, int max, DamageSource damageSource) {
        return 0;
    }

    public static int experienceLevelsToPoints (float levels) {
        if (levels < 16) {
            return (int) ((levels * levels) + (6 * levels));
        } else if (levels < 31) {
            return (int) ((2.5 * levels * levels) - (40.5 * levels) + 360);
        } else {
            return (int) ((4.5 * levels * levels) - (162.5 * levels) + 2220);
        }
    }

    public static void handleRegisterStructure (MinecraftServer server) {
        if (config.generateAltarBuildingsInVillages) {
            Registry<StructureTemplatePool> templatePools = server.registryAccess().registry(Registries.TEMPLATE_POOL).get();
			Registry<StructureProcessorList> processorLists = server.registryAccess().registry(Registries.PROCESSOR_LIST).get();

            addBuildingToPool(templatePools, processorLists, new ResourceLocation("village/desert/houses"), MOD_ID + ":village/houses/altar_desert", config.villageAltarBuildingWeight);
            addBuildingToPool(templatePools, processorLists, new ResourceLocation("village/plains/houses"), MOD_ID + ":village/houses/altar_plains", config.villageAltarBuildingWeight);
            addBuildingToPool(templatePools, processorLists, new ResourceLocation("village/savanna/houses"), MOD_ID + ":village/houses/altar_savanna", config.villageAltarBuildingWeight);
            addBuildingToPool(templatePools, processorLists, new ResourceLocation("village/snowy/houses"), MOD_ID + ":village/houses/altar_snowy", config.villageAltarBuildingWeight);
            addBuildingToPool(templatePools, processorLists, new ResourceLocation("village/taiga/houses"), MOD_ID + ":village/houses/altar_taiga", config.villageAltarBuildingWeight);
        }
    }

    public static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
            Registry<StructureProcessorList> processorListRegistry, ResourceLocation poolRL, String nbtPieceRL,
            int weight) {
        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null)
            return;

        ResourceLocation emptyProcessor = new ResourceLocation("minecraft", "empty");
        Holder<StructureProcessorList> processorHolder = processorListRegistry
                .getHolderOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, emptyProcessor));

        SinglePoolElement piece = SinglePoolElement.single(nbtPieceRL, processorHolder)
                .apply(StructureTemplatePool.Projection.RIGID);

        for (int i = 0; i < weight; i++) {
            ((StructureTemplatePoolMixin) pool).getTemplates().add(piece);
        }

        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(((StructureTemplatePoolMixin) pool).getTemplateCounts());
        listOfPieceEntries.add(new Pair<>(piece, weight));
        ((StructureTemplatePoolMixin) pool).setTemplateCounts(listOfPieceEntries);
    }
}
