package mods.battlegear2.mixins;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.battlegear2.api.core.IBattlegearInventoryPlayer;
import mods.battlegear2.api.core.InventorySlotType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static mods.battlegear2.api.core.Constants.*;
import static org.spongepowered.asm.lib.Opcodes.IFNULL;

@Mixin(InventoryPlayer.class)
public abstract class InventoryPlayerMixin implements IBattlegearInventoryPlayer {
    @Shadow
    public int currentItem;
    @Shadow
    public ItemStack[] mainInventory;
    @Shadow
    public ItemStack[] armorInventory;
    @Shadow
    public EntityPlayer player;

    @Shadow
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @Shadow
    public ItemStack getCurrentItem() {
        return null;
    }

    // Mark the inventory content as dirty to be sent to the client
    public boolean hasChanged = true;
    // The "battle" extra slots
    public ItemStack[] extraItems;

    @Inject(method = "<init>", at = @At("RETURN"))
    protected void initialiseExtraItemsArray(CallbackInfo ci) {
        extraItems = new ItemStack[EXTRA_INV_SIZE];
    }

    @Inject(method = "getCurrentItem", at = @At("HEAD"), cancellable = true)
    protected void getItemInExtraSlots(CallbackInfoReturnable<ItemStack> cir) {
        if (isInBattleMode()) {
            cir.setReturnValue(extraItems[currentItem - OFFSET]);
        }
    }

    @SideOnly(Side.CLIENT)
    @Inject(method = "func_146030_a", at = @At("HEAD"), cancellable = true)
    protected void cancelIfInBattleMode(CallbackInfo ci) {
        if (isInBattleMode()) {
            ci.cancel();
        }
    }

    @SideOnly(Side.CLIENT)
    @Inject(method = "changeCurrentItem", at = @At("HEAD"), cancellable = true)
    protected void changeCurrentItemInBattleMode(int direction, CallbackInfo ci) {
        if (isInBattleMode()) {
            if (direction > 0) {
                direction = 1;
            } else if (direction != 0) {
                direction = -1;
            }

            currentItem -= direction;
            while (currentItem < OFFSET) {
                currentItem += WEAPON_SETS;
            }

            while (currentItem >= OFFSET + WEAPON_SETS) {
                currentItem -= WEAPON_SETS;
            }
            ci.cancel();
        }
    }

    @Inject(method = "clearInventory", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    protected void clearOffhandSlot(Item item, int filter, CallbackInfoReturnable<Integer> cir, int j) {
        int deleted = 0;
        for (int i = 0; i < extraItems.length; i++) {
            if (extraItems[i] != null && (item == null || extraItems[i].getItem() == item)
                    && (filter <= -1 || extraItems[i].getItemDamage() == filter)) {

                deleted += extraItems[i].stackSize;
                extraItems[i] = null;
            }
        }
        hasChanged = deleted > 0;
        cir.setReturnValue(j + deleted);
    }

    @Inject(method = "decrementAnimations", at = @At("RETURN"))
    protected void decrementNewStacksAnimations(CallbackInfo ci) {
        for (int i = 0; i < this.extraItems.length; ++i) {
            if (this.extraItems[i] != null) {
                this.extraItems[i]
                        .updateAnimation(this.player.worldObj, this.player, i + OFFSET, this.currentItem == i + OFFSET);
            }
        }
    }

    @Inject(method = "consumeInventoryItem", at = @At("HEAD"), cancellable = true)
    protected void consumeFromNewStacks(Item item, CallbackInfoReturnable<Boolean> cir) {
        int j = getInventorySlotContainItem(item);
        if (j >= 0) {
            hasChanged = true;
            if (--this.extraItems[j].stackSize <= 0) {
                this.extraItems[j] = null;
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasItem", at = @At("HEAD"), cancellable = true)
    protected void hasItemInNewStacks(Item item, CallbackInfoReturnable<Boolean> cir) {
        int j = getInventorySlotContainItem(item);
        cir.setReturnValue(j >= 0);
    }

    @Inject(method = "decrStackSize", at = @At("HEAD"), cancellable = true)
    protected void decreaseNewStacksSize(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        if (slot >= OFFSET) {
            ItemStack targetStack = extraItems[slot - OFFSET];
            if (targetStack != null) {
                hasChanged = true;
                if (targetStack.stackSize <= amount) {
                    extraItems[slot - OFFSET] = null;
                    cir.setReturnValue(targetStack);
                } else {
                    targetStack = extraItems[slot - OFFSET].splitStack(amount);
                    if (extraItems[slot - OFFSET].stackSize == 0) {
                        extraItems[slot - OFFSET] = null;
                    }
                    cir.setReturnValue(targetStack);
                }
            } else {
                cir.setReturnValue(null);
            }
        }
    }

    @Inject(method = "getStackInSlotOnClosing", at = @At("HEAD"), cancellable = true)
    protected void getStackInNewSlotsOnClosing(int slot, CallbackInfoReturnable<ItemStack> cir) {
        if (slot >= OFFSET) {
            cir.setReturnValue(extraItems[slot - OFFSET]);
        }
    }

    @Inject(method = "setInventorySlotContents", at = @At("HEAD"), cancellable = true)
    protected void setNewSlotsContents(int slot, ItemStack itemStack, CallbackInfo ci) {
        if (slot >= OFFSET) {
            hasChanged = true;
            extraItems[slot - OFFSET] = itemStack;
            ci.cancel();
        }
    }

    @Inject(method = "func_146023_a", at = @At("HEAD"), cancellable = true)
    protected void checkForBattleMode(Block block, CallbackInfoReturnable<Float> cir) {
        if (isInBattleMode()) {
            ItemStack currentItemStack = getCurrentItem();
            cir.setReturnValue(currentItemStack != null ? currentItemStack.func_150997_a(block) : 1.0F);
        }
    }

    @Inject(method = "writeToNBT", at = @At("HEAD"))
    protected void writeNewSlotsToNBT(NBTTagList tagList, CallbackInfoReturnable<NBTTagList> cir) {
        for (int i = 0; i < extraItems.length; ++i) {
            if (extraItems[i] != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                // This will be -ve, but meh still works
                nbttagcompound.setByte("Slot", (byte) (i + OFFSET));
                this.extraItems[i].writeToNBT(nbttagcompound);
                tagList.appendTag(nbttagcompound);
            }
        }
    }

    @Inject(method = "readFromNBT", at = @At("HEAD"))
    protected void clearNewSlots(CallbackInfo ci) {
        extraItems = new ItemStack[EXTRA_INV_SIZE];
    }

    @Inject(method = "readFromNBT", at = @At(value = "JUMP", opcode = IFNULL, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    protected void readNewSlotsFromNBT(NBTTagList tagList, CallbackInfo ci, int i, NBTTagCompound nbttagcompound, int j, ItemStack itemstack) {
        if (j >= OFFSET && j < OFFSET + this.extraItems.length) {
            this.extraItems[j - OFFSET] = itemstack;
        }
    }

    @Inject(method = "getStackInSlot", at = @At("HEAD"), cancellable = true)
    protected void getStackInNewSlots(int slot, CallbackInfoReturnable<ItemStack> cir) {
        if (slot >= OFFSET) {
            cir.setReturnValue(extraItems[slot - OFFSET]);
        }
    }

    @Inject(method = "copyInventory", at = @At("HEAD"))
    protected void copyNewSlots(InventoryPlayer inventoryPlayer, CallbackInfo ci) {
        extraItems = new ItemStack[EXTRA_INV_SIZE];
        for (int i = 0; i < extraItems.length; i++) {
            extraItems[i] = ItemStack.copyItemStack(inventoryPlayer.getStackInSlot(i + OFFSET));
        }
    }

    @Inject(method = "dropAllItems", at = @At("HEAD"))
    protected void dropNewStacks(CallbackInfo ci) {
        hasChanged = true;
        for (int i = 0; i < this.extraItems.length; ++i) {
            if (this.extraItems[i] != null) {
                this.player.func_146097_a(this.extraItems[i], true, false);
                this.extraItems[i] = null;
            }
        }
    }

    /**
     * Get the offset item (for the left hand)
     *
     * @return the item held in left hand, if any
     */
    @Override
    public ItemStack getCurrentOffhandWeapon() {
        if (isInBattleMode()) {
            return getStackInSlot(currentItem + WEAPON_SETS);
        } else {
            return null;
        }
    }

    /**
     * @return true if the current item value is offset in the battle slot range
     */
    @Override
    public boolean isInBattleMode() {
        return currentItem >= OFFSET && currentItem < OFFSET + EXTRA_ITEMS;
    }

    /**
     * Returns a new slot index according to the type
     *
     * @param type determines which inventory array to expand
     * @return the new slot index, or Integer.MIN_VALUE if it is not possible to expand further
     */
    @Override
    public int requestNewSlot(InventorySlotType type) {
        ItemStack[] temp;
        switch (type) {
            case MAIN:
                if (mainInventory.length + 1 < ARMOR_OFFSET) {
                    temp = new ItemStack[mainInventory.length + 1];
                    System.arraycopy(mainInventory, 0, temp, 0, mainInventory.length);
                    mainInventory = temp;
                    return mainInventory.length - 1; // Between 36 and 99
                }
                break;
            case ARMOR:
                if (ARMOR_OFFSET + armorInventory.length + 1 < OFFSET) {
                    temp = new ItemStack[armorInventory.length + 1];
                    System.arraycopy(armorInventory, 0, temp, 0, armorInventory.length);
                    armorInventory = temp;
                    return ARMOR_OFFSET + armorInventory.length - 1; // Between 104 and 149
                }
                break;
            case BATTLE:
                temp = new ItemStack[extraItems.length + 1];
                System.arraycopy(extraItems, 0, temp, 0, extraItems.length);
                extraItems = temp;
                return OFFSET + extraItems.length - 1;
        }
        return Integer.MIN_VALUE; // Impossible because of byte cast in inventory NBT
    }

    @Override
    public boolean getHasChanged() {
        return hasChanged;
    }

    @Override
    public void setHasChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }

    /**
     * Returns a slot index in main inventory containing a specific itemID
     */
    private int getInventorySlotContainItem(Item item) {
        for (int j = 0; j < this.extraItems.length; ++j) {
            if (this.extraItems[j] != null && this.extraItems[j].getItem() == item) {
                return j;
            }
        }
        return -1;
    }

    @SideOnly(Side.CLIENT)
    private int getInventorySlotContainItemAndDamage(Item item, int filter) {
        for (int k = 0; k < this.extraItems.length; ++k) {
            if (this.extraItems[k] != null && this.extraItems[k].getItem() == item
                    && this.extraItems[k].getItemDamage() == filter) {
                return k;
            }
        }
        return -1;
    }
}
