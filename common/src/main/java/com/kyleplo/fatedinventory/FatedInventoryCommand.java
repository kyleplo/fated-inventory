package com.kyleplo.fatedinventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public class FatedInventoryCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fatedinventory")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("target", EntityArgument.player())
                .then(Commands.literal("seal")
                    .executes((CommandContext<CommandSourceStack> context) -> {
                        Player player = EntityArgument.getPlayer(context, "target");
                        IFatedInventoryContainer fatedInventory = FatedInventory.getFatedInventoryContainer(player);
                        fatedInventory.putInventory(player);
                        context.getSource().sendSuccess(() -> Component.translatable("commands.fated_inventory.fatedinventory.seal.success"), false);
                        return 1;
                    }))
                .then(Commands.literal("unseal")
                    .executes((CommandContext<CommandSourceStack> context) -> {
                        Player player = EntityArgument.getPlayer(context, "target");
                        IFatedInventoryContainer fatedInventory = FatedInventory.getFatedInventoryContainer(player);
                        fatedInventory.clearFatedInventory();
                        context.getSource().sendSuccess(() -> Component.translatable("commands.fated_inventory.fatedinventory.unseal.success"), false);
                        return 1;
                    }))
                .then(Commands.literal("sealed")
                    .executes((CommandContext<CommandSourceStack> context) -> {
                        Player player = EntityArgument.getPlayer(context, "target");
                        IFatedInventoryContainer fatedInventory = FatedInventory.getFatedInventoryContainer(player);
                        context.getSource().sendSuccess(() -> Component.translatable("commands.fated_inventory.fatedinventory.sealed.success", new Object[]{
                            player.getDisplayName(),
                            fatedInventory.getExperience(),
                            fatedInventory.getItems().size(),
                            ComponentUtils.formatList(fatedInventory.getItems(), item -> Component.translatable("commands.fated_inventory.fatedinventory.sealed.item", item.item.getDisplayName(), item.count))
                        }), false);
                        return fatedInventory.getItems().size();
                    }))
                .then(Commands.literal("retrieve")
                    .executes((CommandContext<CommandSourceStack> context) -> {
                        Player player = EntityArgument.getPlayer(context, "target");
                        IFatedInventoryContainer fatedInventory = FatedInventory.getFatedInventoryContainer(player);
                        fatedInventory.dropInventoryFor(player);
                        context.getSource().sendSuccess(() -> Component.translatable("commands.fated_inventory.fatedinventory.retrieve.success"), false);
                        return 1;
                    }))
                .then(Commands.literal("clear")
                    .executes((CommandContext<CommandSourceStack> context) -> {
                        Player player = EntityArgument.getPlayer(context, "target");
                        IFatedInventoryContainer fatedInventory = FatedInventory.getFatedInventoryContainer(player);
                        fatedInventory.clearStored();
                        context.getSource().sendSuccess(() -> Component.translatable("commands.fated_inventory.fatedinventory.clear.success"), false);
                        return 1;
                    }))
                .then(Commands.literal("list")
                    .executes((CommandContext<CommandSourceStack> context) -> {
                        Player player = EntityArgument.getPlayer(context, "target");
                        IFatedInventoryContainer fatedInventory = FatedInventory.getFatedInventoryContainer(player);
                        context.getSource().sendSuccess(() -> Component.translatable("commands.fated_inventory.fatedinventory.list.success", new Object[]{
                            player.getDisplayName(),
                            fatedInventory.getStoredExperience(),
                            fatedInventory.getStoredItems().size(),
                            ComponentUtils.formatList(fatedInventory.getStoredItems(), item -> Component.translatable("commands.fated_inventory.fatedinventory.list.item", item.item.getDisplayName(), item.count))
                        }), false);
                        return fatedInventory.getStoredItems().size();
                    })
                )
            )
        );
    }
}
