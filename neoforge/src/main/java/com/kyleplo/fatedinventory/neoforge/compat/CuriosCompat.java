package com.kyleplo.fatedinventory.neoforge.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.kyleplo.fatedinventory.FatedInventoryItem;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurio.DropRule;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CuriosCompat {
    private static int removedCount = 0;

    public static List<ItemStack> itemsFromCuriosInventory (Player player) {
        List<ItemStack> items = new ArrayList<ItemStack>();
        Optional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(player);

        if (maybeCuriosInventory.isPresent()) {
            ICuriosItemHandler curiosInventory = maybeCuriosInventory.get();
            curiosInventory.getCurios().forEach((String slot, ICurioStacksHandler curioStackHandler) -> {
                IDynamicStackHandler curioStacks = curioStackHandler.getStacks();
                for (int i = 0; i < curioStacks.getSlots(); i++) {
                    items.add(curioStacks.getStackInSlot(i));
                }
            });
        }

        return items;
    }

    public static int removeMatchingItemsFromCuriosInventory (Player player, ItemStack matchItem, int max, DamageSource damageSource) {
        Optional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(player);
        removedCount = 0;
        if (maybeCuriosInventory.isPresent()) {
            ICuriosItemHandler curiosInventory = maybeCuriosInventory.get();
            List<SlotResult> matchingCurios = curiosInventory.findCurios((ItemStack item) -> {
                return FatedInventoryItem.isCloseEnough(item, matchItem);
            });
            matchingCurios.forEach((curioSlot -> {
                Optional<ICurio> curio = CuriosApi.getCurio(curioSlot.stack());
                
                if (curio.isPresent()) {
                    DropRule dropRule = curio.get().getDropRule(curioSlot.slotContext(), damageSource, true);
                    if (dropRule == DropRule.ALWAYS_KEEP || dropRule == DropRule.DESTROY) {
                        return;
                    }
                }

                if (removedCount < max) {
                    removedCount += curioSlot.stack().getCount();
                    curiosInventory.setEquippedCurio(curioSlot.slotContext().identifier(), curioSlot.slotContext().index(), ItemStack.EMPTY);
                }
            }));
            return removedCount;
        } else {
            return 0;
        }
    }
}
