package mods.battlegear2.mixins;

import mods.battlegear2.api.core.IOffhandModel;
import net.minecraft.client.model.ModelBase;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelBase.class)
public abstract class ModelBaseMixin implements IOffhandModel {
    float offhandSwing = 0F;

    public void setOffhandSwing(float value) {
        offhandSwing = value;
    }

    public float getOffhandSwing() {
        return offhandSwing;
    }
}
