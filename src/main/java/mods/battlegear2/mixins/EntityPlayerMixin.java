package mods.battlegear2.mixins;

import mods.battlegear2.api.PlayerEventChild;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.IBattlegearInventoryPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase implements IBattlePlayer {
    public EntityPlayerMixin(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    @Shadow
    ItemStack itemInUse;

    public float offhandSwingProgress = 0F;
    public float prevOffhandSwingProgress = 0F;
    public int offhandSwingProgressInt = 0;
    public boolean isOffhandSwingInProgress = false;
    public int specialActionTimer = 0;
    public boolean isShielding = false;

    @Override
    public void onEntityUpdate() {
        prevOffhandSwingProgress = offhandSwingProgress;
        super.onEntityUpdate();
    }

    @Inject(method = "updateEntityActionState", at = @At("RETURN"))
    protected void updateOffhandArmSwingProgress(CallbackInfo ci) {
        int animationEnd = getArmSwingAnimationEnd();
        if (isOffhandSwingInProgress) {
            ++offhandSwingProgressInt;
            if (offhandSwingProgressInt >= animationEnd) {
                offhandSwingProgressInt = 0;
                isOffhandSwingInProgress = false;
            }
        } else {
            offhandSwingProgressInt = 0;
        }
        offhandSwingProgress = (float) offhandSwingProgressInt / (float) animationEnd;
        if (specialActionTimer > 0) {
            isOffhandSwingInProgress = false;
            isSwingInProgress = false;
            offhandSwingProgress = 0F;
            offhandSwingProgressInt = 0;
            swingProgress = 0F;
            swingProgressInt = 0;
        }
    }

    @Inject(method = "onItemUseFinish",
            at = @At(value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraftforge/event/ForgeEventFactory;onItemUseFinish(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;ILnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            cancellable = true)
    protected void modifyOnItemUseFinish(CallbackInfo ci, int i, ItemStack itemstack) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (isInBattleMode()) {
            if (itemstack != itemInUse || (itemstack != null && itemstack.stackSize != i)) {
                if (itemInUse == player.getCurrentEquippedItem()) {
                    if (itemstack != null && itemstack.stackSize == 0) {
                        BattlegearUtils.setPlayerCurrentItem(player, null);
                    } else {
                        BattlegearUtils.setPlayerCurrentItem(player, itemstack);
                    }
                } else if (itemInUse == ((IBattlegearInventoryPlayer) player.inventory).getCurrentOffhandWeapon()) {
                    if (itemstack != null && itemstack.stackSize == 0) {
                        BattlegearUtils.setPlayerOffhandItem(player, null);
                    } else {
                        BattlegearUtils.setPlayerOffhandItem(player, itemstack);
                    }
                }
            }
            player.clearItemInUse();
            ci.cancel();
        }
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"))
    protected ItemStack redirectGetCurrentItemStack(InventoryPlayer inventoryPlayer) {
        if (isInBattleMode()) {
            ItemStack itemStack = ((IBattlegearInventoryPlayer) inventoryPlayer).getCurrentOffhandWeapon();
            if (itemInUse == itemStack) {
                return itemStack;
            }
        }
        return inventoryPlayer.getCurrentItem();
    }

    @Inject(method = "setCurrentItemOrArmor", at = @At("HEAD"), cancellable = true)
    protected void cancelIfInBattleMode(int slotIndex, ItemStack itemStack, CallbackInfo ci) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (isInBattleMode()) {
            player.inventory.setInventorySlotContents(slotIndex, itemStack);
            ci.cancel();
        }
    }

    @Inject(method = "interactWith", at = @At("RETURN"), cancellable = true)
    protected void interactWithOffhandStack(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (!cir.getReturnValue() && isInBattleMode()) {
            boolean interacted = false;
            ItemStack itemstack = BattlegearUtils.refreshAttributes(player, false);
            ItemStack itemstack1 = itemstack != null ? itemstack.copy() : null;
            if (entity.interactFirst(player)) {
                if (itemstack != null && itemstack == player.getCurrentEquippedItem()) {
                    if (itemstack.stackSize <= 0 && !player.capabilities.isCreativeMode) {
                        player.destroyCurrentEquippedItem();
                    } else if (itemstack.stackSize < itemstack1.stackSize && player.capabilities.isCreativeMode) {
                        itemstack.stackSize = itemstack1.stackSize;
                    }
                }
                interacted = true;
                BattlegearUtils.refreshAttributes(player, true);
                cir.setReturnValue(true);
            } else if (itemstack != null && entity instanceof EntityLivingBase) {
                if (player.capabilities.isCreativeMode) {
                    itemstack = itemstack1;
                }
                if (itemstack.interactWithEntity(player, (EntityLivingBase) entity)) {
                    if (itemstack.stackSize <= 0 && !player.capabilities.isCreativeMode) {
                        player.destroyCurrentEquippedItem();
                    }
                    interacted = true;
                    BattlegearUtils.refreshAttributes(player, true);
                    cir.setReturnValue(true);
                }
            }
            if (!interacted) {
                BattlegearUtils.refreshAttributes(player, true);
                EntityInteractEvent event = new EntityInteractEvent(player, entity);
                ItemStack offhandItem = ((IBattlegearInventoryPlayer) player.inventory).getCurrentOffhandWeapon();
                PlayerEventChild.OffhandAttackEvent offAttackEvent = new PlayerEventChild.OffhandAttackEvent(event, offhandItem);
                if (!MinecraftForge.EVENT_BUS.post(offAttackEvent)) {
                    if (offAttackEvent.swingOffhand) {
                        BattlegearUtils.sendOffSwingEvent(event, offAttackEvent.offHand);
                    }
                    if (offAttackEvent.shouldAttack) {
                        attackTargetEntityWithCurrentOffhandItem(offAttackEvent.getTarget());
                    }
                    if (offAttackEvent.cancelParent) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Override
    public void swingOffhandItem() {
        if (!isOffhandSwingInProgress || offhandSwingProgressInt >= getArmSwingAnimationEnd() / 2 || offhandSwingProgressInt < 0) {
            offhandSwingProgressInt = -1;
            isOffhandSwingInProgress = true;
        }
    }

    @Override
    public float getOffhandSwingProgress(float frame) {
        float difference = offhandSwingProgress - prevOffhandSwingProgress;
        if (difference < 0) {
            difference++;
        }
        return prevOffhandSwingProgress + difference * frame;
    }

    @Override
    public void attackTargetEntityWithCurrentOffhandItem(Entity target) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        BattlegearUtils.attackTargetEntityWithCurrentOffItem(player, target);
    }

    @Override
    public boolean isInBattleMode() {
        EntityPlayer player = (EntityPlayer) (Object) this;
        return BattlegearUtils.isPlayerInBattlemode(player);
    }

    @Override
    public boolean isBlockingWithShield() {
        EntityPlayer player = (EntityPlayer) (Object) this;
        return BattlegearUtils.canBlockWithShield(player) && isShielding;
    }

    @Override
    public void setBlockingWithShield(boolean block) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        isShielding = BattlegearUtils.canBlockWithShield(player) && block;
    }

    @Override
    public int getSpecialActionTimer() {
        return specialActionTimer;
    }

    @Override
    public void setSpecialActionTimer(int time) {
        specialActionTimer = time;
    }
}
