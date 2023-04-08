package mods.battlegear2.mixins;

import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {
    @Shadow
    EntityPlayerMP playerEntity;

    @Final
    @Shadow
    static Logger logger;

    @Shadow
    public void sendPacket(final Packet packetIn) {
    }

    @Inject(method = "processPlayerBlockPlacement", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;", ordinal = 1), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    protected void handleIfInBattleMode(C08PacketPlayerBlockPlacement packetIn, CallbackInfo ci, WorldServer worldserver, ItemStack itemstack, boolean flag, boolean placeResult) {
        if (((IBattlePlayer) playerEntity).isInBattleMode()) {
            if (itemstack != null && itemstack.stackSize == 0) {
                playerEntity.inventory.setInventorySlotContents(playerEntity.inventory.currentItem, null);
                itemstack = null;
            }

            if (itemstack == null || itemstack.getMaxItemUseDuration() == 0) {
                playerEntity.isChangingQuantityOnly = true;
                playerEntity.inventory.setInventorySlotContents(playerEntity.inventory.currentItem, ItemStack.copyItemStack(playerEntity.inventory.getCurrentItem()));
                Slot slot = playerEntity.openContainer.getSlotFromInventory(playerEntity.inventory, playerEntity.inventory.currentItem);
                playerEntity.openContainer.detectAndSendChanges();
                playerEntity.isChangingQuantityOnly = false;

                if (slot != null && (!ItemStack.areItemStacksEqual(playerEntity.inventory.getCurrentItem(), packetIn.func_149574_g()) || !placeResult)) // force client itemstack update if place event was cancelled
                {
                    sendPacket(new S2FPacketSetSlot(playerEntity.openContainer.windowId, slot.slotNumber, playerEntity.inventory.getCurrentItem()));
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "processHeldItemChange", at = @At("HEAD"), cancellable = true)
    protected void handleHeldItemChange(C09PacketHeldItemChange packetIn, CallbackInfo ci) {
        if (BattlegearUtils.isValidSwitch(packetIn.func_149614_c())) {
            playerEntity.inventory.currentItem = packetIn.func_149614_c();
            playerEntity.func_143004_u();
        } else {
            logger.warn(playerEntity.getCommandSenderName() + " tried to set an invalid carried item");
        }
        ci.cancel();
    }
}
