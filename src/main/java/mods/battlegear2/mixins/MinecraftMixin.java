package mods.battlegear2.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.spongepowered.asm.lib.Opcodes.GETFIELD;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    EntityClientPlayerMP thePlayer;

    @Redirect(method = "func_147121_ag", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/InventoryPlayer;mainInventory:[Lnet/minecraft/item/ItemStack;", opcode = GETFIELD, args = "array=set"))
    protected void handleSetCurrentItem(ItemStack[] mainInventory, int index, ItemStack itemStack) {
        thePlayer.inventory.setInventorySlotContents(index, itemStack);
    }
}
