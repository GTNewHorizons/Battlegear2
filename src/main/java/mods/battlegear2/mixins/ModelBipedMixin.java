package mods.battlegear2.mixins;

import static org.spongepowered.asm.lib.Opcodes.GETFIELD;

import mods.battlegear2.client.utils.BattlegearRenderHelper;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public abstract class ModelBipedMixin {

    @Inject(
            method = "setRotationAngles",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;isSneak:Z", opcode = GETFIELD))
    protected void moveOffhandArm(float p1, float p2, float p3, float p4, float p5, float p6, Entity p7,
            CallbackInfo ci) {
        ModelBiped modelBiped = (ModelBiped) (Object) this;
        BattlegearRenderHelper.moveOffHandArm(p7, modelBiped);
    }
}
