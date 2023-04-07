package mods.battlegear2.mixins;

import mods.battlegear2.api.core.IOffhandModel;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.spongepowered.asm.lib.Opcodes.GETFIELD;

@Mixin(RenderBiped.class)
public abstract class RenderBipedMixin extends RenderLiving {
    public RenderBipedMixin(ModelBase p_i1262_1_, float p_i1262_2_) {
        super(p_i1262_1_, p_i1262_2_);
    }

    @Inject(method = "shouldRenderPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBase;onGround:F", opcode = GETFIELD), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    protected void setModelOffhandSwing(EntityLiving p1, int p2, float p3, CallbackInfoReturnable<Integer> cir, ItemStack itemstack, Item item, ItemArmor itemarmor, ModelBiped modelbiped) {
        ((IOffhandModel) modelbiped).setOffhandSwing(((IOffhandModel) mainModel).getOffhandSwing());
    }
}
