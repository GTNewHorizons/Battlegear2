package mods.battlegear2.mixins;

import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlegearInventoryPlayer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Redirect(
            method = "damageItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;destroyCurrentEquippedItem()V"))
    protected void redirectDestroyCurrentEquippedItem(EntityPlayer entityPlayer) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack == entityPlayer.getCurrentEquippedItem()) {
            entityPlayer.destroyCurrentEquippedItem();
        } else {
            ItemStack offhandItemStack = ((IBattlegearInventoryPlayer) entityPlayer.inventory)
                    .getCurrentOffhandWeapon();
            if (offhandItemStack == itemStack) {
                BattlegearUtils.setPlayerOffhandItem(entityPlayer, null);
                ForgeEventFactory.onPlayerDestroyItem(entityPlayer, offhandItemStack);
            }
        }
    }
}
