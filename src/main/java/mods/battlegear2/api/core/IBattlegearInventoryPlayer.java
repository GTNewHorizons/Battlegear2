package mods.battlegear2.api.core;

import net.minecraft.item.ItemStack;

public interface IBattlegearInventoryPlayer {

    ItemStack getCurrentOffhandWeapon();

    boolean isInBattleMode();

    int requestNewSlot(InventorySlotType type);

    boolean getHasChanged();

    void setHasChanged(boolean hasChanged);
}
