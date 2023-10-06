package mods.battlegear2.coremod.transformers;

import org.objectweb.asm.tree.MethodNode;

import mods.battlegear2.api.core.BattlegearTranslator;

public final class MinecraftTransformer extends TransformerMethodProcess {

    public MinecraftTransformer() {
        super(
                "net.minecraft.client.Minecraft",
                "func_147121_ag",
                new String[] { "func_147121_ag", SIMPLEST_METHOD_DESC });
        setDebug(true);
    }

    private String entityClientPlayerClass;
    private String playerInventoryFieldName;

    @Override
    void processMethod(MethodNode method) {
        sendPatchLog("Click Mouse");

        replaceInventoryArrayAccess(method, entityClientPlayerClass, playerInventoryFieldName, 5, 9, 10);
    }

    @Override
    void setupMappings() {
        super.setupMappings();
        entityClientPlayerClass = BattlegearTranslator.getMapedClassName("client.entity.EntityClientPlayerMP");
        playerInventoryFieldName = BattlegearTranslator.getMapedFieldName("field_71071_by", "inventory");
    }
}
