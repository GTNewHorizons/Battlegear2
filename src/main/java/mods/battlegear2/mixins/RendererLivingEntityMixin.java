package mods.battlegear2.mixins;

import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.IOffhandModel;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.lib.Opcodes.GETFIELD;


@Mixin(RendererLivingEntity.class)
public abstract class RendererLivingEntityMixin {
    @Shadow
    ModelBase mainModel;

    @Shadow
    ModelBase renderPassModel;

    @Inject(method = "doRender", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/entity/RendererLivingEntity;renderSwingProgress(Lnet/minecraft/entity/EntityLivingBase;F)F"))
    protected void setMainModelOffhandSwing(EntityLivingBase p1, double p2, double p3, double p4, float p6, float p7, CallbackInfo ci) {
        if (p1 instanceof IBattlePlayer) {
            ((IOffhandModel) mainModel).setOffhandSwing(((IBattlePlayer) p1).getOffhandSwingProgress(p7));
        }
    }

    @Inject(method = "doRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBase;onGround:F", opcode = GETFIELD))
    protected void setRenderPassModelOffhandSwing(EntityLivingBase p1, double p2, double p3, double p4, float p6, float p7, CallbackInfo ci) {
        ((IOffhandModel) renderPassModel).setOffhandSwing(((IOffhandModel) mainModel).getOffhandSwing());
    }
}
