package mods.battlegear2.mixins;

import static org.spongepowered.asm.lib.Opcodes.GETFIELD;

import mods.battlegear2.api.core.BattlegearUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.authlib.GameProfile;

@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerPlayClientMixin {

    @Shadow
    Minecraft gameController;

    EntityOtherPlayerMP spawnedPlayer = null;

    @Inject(
            method = "handleSpawnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/play/server/S0CPacketSpawnPlayer;func_148947_k()I"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    protected void getSpawnedPlayer(S0CPacketSpawnPlayer packetIn, CallbackInfo ci, double d0, double d1, double d2,
            float f, float f1, GameProfile gameprofile, EntityOtherPlayerMP entityotherplayermp) {
        spawnedPlayer = entityotherplayermp;
    }

    @Redirect(
            method = "handleSpawnPlayer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/player/InventoryPlayer;mainInventory:[Lnet/minecraft/item/ItemStack;",
                    opcode = GETFIELD,
                    args = { "array=set", "fuzz=11" }),
            require = 2)
    protected void handleSetCurrentItem(ItemStack[] mainInventory, int index, ItemStack itemStack) {
        spawnedPlayer.inventory.setInventorySlotContents(index, itemStack);
    }

    @Inject(method = "handleHeldItemChange", at = @At("HEAD"), cancellable = true)
    protected void handleHeldItemChange(S09PacketHeldItemChange packetIn, CallbackInfo ci) {
        if (BattlegearUtils.isValidSwitch(packetIn.func_149385_c())) {
            gameController.thePlayer.inventory.currentItem = packetIn.func_149385_c();
        }
        ci.cancel();
    }
}
