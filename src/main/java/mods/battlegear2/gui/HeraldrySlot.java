package mods.battlegear2.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import mods.battlegear2.api.heraldry.IHeraldryItem;

public final class HeraldrySlot extends Slot {

    public HeraldrySlot(IInventory par1iInventory, int par2, int par3, int par4) {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if (stack == null || stack.getItem() instanceof IHeraldryItem) {
            return super.isItemValid(stack);
        }
        return false;
    }
}
