package mods.battlegear2.mixins;

import com.mojang.authlib.GameProfile;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlegearInventoryPlayer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.lib.Opcodes.PUTFIELD;

@Mixin(EntityOtherPlayerMP.class)
public abstract class EntityOtherPlayerMPMixin extends AbstractClientPlayer {

    @Shadow
    boolean isItemInUse;

    public EntityOtherPlayerMPMixin(World p_i45074_1_, GameProfile p_i45074_2_) {
        super(p_i45074_1_, p_i45074_2_);
    }

    @Inject(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityOtherPlayerMP;limbSwing:F", opcode = PUTFIELD, shift = At.Shift.AFTER), cancellable = true)
    protected void useItemOnOffhand(CallbackInfo ci) {
        EntityOtherPlayerMP player = (EntityOtherPlayerMP) (Object) this;
        ItemStack itemStack = null;
        if (BattlegearUtils.isPlayerInBattlemode(player)) {
            ItemStack offhand = ((IBattlegearInventoryPlayer) player.inventory).getCurrentOffhandWeapon();
            if (offhand != null && BattlegearUtils.usagePriorAttack(offhand, player, true)) {
                itemStack = offhand;
            }
            if (!isItemInUse && this.isEating() && itemStack != null) {
                this.setItemInUse(itemStack, itemStack.getMaxItemUseDuration());
                isItemInUse = true;
            } else if (isItemInUse && !this.isEating()) {
                this.clearItemInUse();
                isItemInUse = false;
            }
            ci.cancel();
        }
    }

    @Inject(method = "setCurrentItemOrArmor", at = @At("HEAD"), cancellable = true)
    protected void cancelIfInBattleMode(int slotIndex, ItemStack itemStack, CallbackInfo ci) {
        EntityOtherPlayerMP player = (EntityOtherPlayerMP) (Object) this;
        if (BattlegearUtils.isPlayerInBattlemode(player)) {
            player.inventory.setInventorySlotContents(slotIndex, itemStack);
            ci.cancel();
        }
    }
}
