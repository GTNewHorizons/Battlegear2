package mods.battlegear2.asm.hooks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import mods.battlegear2.api.core.IInventoryPlayerBattle;

public class InventoryAccessHook {

    public static boolean setPlayerCurrentItem(EntityPlayer player, ItemStack stack) {
        if (player.inventory.currentItem >= IInventoryPlayerBattle.OFFSET) {
            ((IInventoryPlayerBattle) (player.inventory))
                    .battlegear2$setInventorySlotContents(player.inventory.currentItem, stack, false);
            return true;
        }
        return false;
    }
}
