package com.kyleplo.fatedinventory;

import java.util.ArrayList;
import java.util.List;

import com.kyleplo.fatedinventory.blocks.FatedInventoryBlocks;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

public final class FatedInventory {
    public static final String MOD_ID = "fated_inventory";
    public static Config config;

    public static void init() {
        config = Config.init();
        FatedInventoryBlocks.initialize();
    }

    public static void handlePlayerDeath(Player player, DamageSource source) {
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || player.getInventory().contains(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "death_protection")))) {
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
}
