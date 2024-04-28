package mods.battlegear2.mixins.early;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import mods.battlegear2.api.core.IInventoryPlayerBattle;

@Mixin(NetHandlerPlayServer.class)
public class MixinNetHandlerPlayServer {

    @ModifyExpressionValue(
            method = "processHeldItemChange",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/play/client/C09PacketHeldItemChange;func_149614_c()I",
                    ordinal = 1))
    private int battlegear2$isValidIventorySlot(int original) {
        // return a valid int e.g. between 0 and < 9
        return IInventoryPlayerBattle.isValidSwitch(original) ? 0 : -1;
    }

    @WrapOperation(
            method = "processPlayerBlockPlacement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/inventory/Container;getSlotFromInventory(Lnet/minecraft/inventory/IInventory;I)Lnet/minecraft/inventory/Slot;"))
    private Slot battlegear2$captureSlotVariable(Container instance, IInventory j, int i, Operation<Slot> original,
            @Share("slot") LocalRef<Slot> slotRef) {
        Slot slot = original.call(instance, j, i);
        slotRef.set(slot);
        return slot;
    }

    @Inject(
            method = "processPlayerBlockPlacement",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/player/EntityPlayerMP;isChangingQuantityOnly:Z",
                    shift = At.Shift.AFTER,
                    ordinal = 1),
            cancellable = true)
    private void battlegear2$fixNPE(C08PacketPlayerBlockPlacement packetIn, CallbackInfo ci,
            @Share("slot") LocalRef<Slot> slotRef) {
        if (slotRef.get() == null) {
            ci.cancel();
        }
    }
}
