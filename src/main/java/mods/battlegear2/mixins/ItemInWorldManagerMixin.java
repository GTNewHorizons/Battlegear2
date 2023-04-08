package mods.battlegear2.mixins;

import static org.spongepowered.asm.lib.Opcodes.GETFIELD;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInWorldManager.class)
public abstract class ItemInWorldManagerMixin {

    @Redirect(
            method = "tryUseItem",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/player/InventoryPlayer;mainInventory:[Lnet/minecraft/item/ItemStack;",
                    opcode = GETFIELD,
                    args = "array=set"),
            require = 2)
    protected void handleSetCurrentItem(ItemStack[] mainInventory, int index, ItemStack itemStack, EntityPlayer player,
            World world, ItemStack stack) {
        player.inventory.setInventorySlotContents(index, itemStack);
    }
}
