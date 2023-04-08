package mods.battlegear2.mixins;

import static org.spongepowered.asm.lib.Opcodes.GETFIELD;

import mods.battlegear2.api.core.IOffhandModel;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin extends RendererLivingEntity {

    @Shadow
    ModelBiped modelBipedMain;

    public RenderPlayerMixin(ModelBase p_i1261_1_, float p_i1261_2_) {
        super(p_i1261_1_, p_i1261_2_);
    }

    @Inject(
            method = "shouldRenderPass",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBase;onGround:F", opcode = GETFIELD),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    protected void setRenderPassModelOffhandSwing(AbstractClientPlayer p1, int p2, float p3,
            CallbackInfoReturnable<Integer> cir, ItemStack itemstack, RenderPlayerEvent.SetArmorModel event, Item item,
            ItemArmor itemarmor, ModelBiped modelbiped) {
        ((IOffhandModel) modelbiped).setOffhandSwing(((IOffhandModel) mainModel).getOffhandSwing());
    }

    @Inject(method = "renderFirstPersonArm", at = @At("HEAD"))
    protected void resetModelOffhandSwing(CallbackInfo ci) {
        ((IOffhandModel) modelBipedMain).setOffhandSwing(0F);
    }
}
