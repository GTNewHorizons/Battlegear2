package mods.battlegear2.mixins;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.spongepowered.asm.lib.Opcodes.GETFIELD;

@Mixin(PlayerControllerMP.class)
public abstract class PlayerControllerMPMixin {
    @Redirect(method = "sendUseItem", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/InventoryPlayer;mainInventory:[Lnet/minecraft/item/ItemStack;", opcode = GETFIELD, args = "array=set"), require = 2)
    protected void handleSetCurrentItem(ItemStack[] mainInventory, int index, ItemStack itemStack, EntityPlayer player) {
        player.inventory.setInventorySlotContents(index, itemStack);
    }
}
