package mods.battlegear2.mixins;

import static org.spongepowered.asm.lib.Opcodes.GETFIELD;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIControlledByPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityAIControlledByPlayer.class)
public abstract class EntityAIControlledByPlayerMixin {

    @Shadow
    @Final
    EntityLiving thisEntity;

    @Redirect(
            method = "updateTask",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/player/InventoryPlayer;mainInventory:[Lnet/minecraft/item/ItemStack;",
                    opcode = GETFIELD,
                    args = "array=set"))
    protected void handleSetCurrentItem(ItemStack[] mainInventory, int index, ItemStack itemStack) {
        ((EntityPlayer) thisEntity.riddenByEntity).inventory.setInventorySlotContents(index, itemStack);
    }
}
