package mods.battlegear2.mixins;

import mods.battlegear2.api.core.IOffhandRender;
import mods.battlegear2.client.utils.BattlegearRenderHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin implements IOffhandRender {

    @Shadow
    Minecraft mc;

    ItemStack offhandItemToRender = null;
    int equippedItemOffhandSlot = -1;
    float equippedOffhandProgress = 0F;
    float prevEquippedOffhandProgress = 0F;

    @Inject(method = "updateEquippedItem", at = @At("RETURN"))
    protected void updateEquippedOffhandItem(CallbackInfo ci) {
        ItemRenderer itemRenderer = (ItemRenderer) (Object) this;
        BattlegearRenderHelper.updateEquippedItem(itemRenderer, mc);
    }

    @Inject(method = "renderItemInFirstPerson", at = @At("RETURN"))
    protected void renderOffhandItemInFirstPerson(float partialTicks, CallbackInfo ci) {
        ItemRenderer itemRenderer = (ItemRenderer) (Object) this;
        BattlegearRenderHelper.renderItemInFirstPerson(partialTicks, mc, itemRenderer);
    }

    @Override
    public ItemStack getOffHandItemToRender() {
        return offhandItemToRender;
    }

    @Override
    public void setOffHandItemToRender(ItemStack item) {
        offhandItemToRender = item;
    }

    @Override
    public int getEquippedItemOffhandSlot() {
        return equippedItemOffhandSlot;
    }

    @Override
    public void serEquippedItemOffhandSlot(int slot) {
        equippedItemOffhandSlot = slot;
    }

    @Override
    public float getEquippedOffHandProgress() {
        return equippedOffhandProgress;
    }

    @Override
    public void setEquippedOffHandProgress(float progress) {
        equippedOffhandProgress = progress;
    }

    @Override
    public float getPrevEquippedOffHandProgress() {
        return prevEquippedOffhandProgress;
    }

    @Override
    public void setPrevEquippedOffHandProgress(float progress) {
        prevEquippedOffhandProgress = progress;
    }
}
