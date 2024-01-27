package mods.battlegear2.mixins.early;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mods.battlegear2.api.core.IBattlePlayer;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooks {

    /**
     * Exit early if in creative mode and in battle mode
     */
    @Inject(method = "onPickBlock", at = @At(value = "HEAD"), cancellable = true)
    private static void battlegear2$onPickBlock(MovingObjectPosition target, EntityPlayer player, World world,
            CallbackInfoReturnable<Boolean> cir) {
        if (((IBattlePlayer) player).battlegear2$isBattlemode()) {
            cir.setReturnValue(false);
        }
    }

}
