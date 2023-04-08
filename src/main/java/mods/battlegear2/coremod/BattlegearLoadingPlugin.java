package mods.battlegear2.coremod;

import java.io.File;
import java.util.Map;

import mods.battlegear2.api.core.BattlegearTranslator;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@TransformerExclusions({"mods.battlegear2.coremod"})
@Name("Mine and Blade: Battlegear2")
@SortingIndex(1500)
@MCVersion("1.7.10")
public final class BattlegearLoadingPlugin implements IFMLLoadingPlugin {

    public static final String NetServerHandlerTransformer = "mods.battlegear2.coremod.transformers.NetServerHandlerTransformer";
    public static File debugOutputLocation;

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        // return "mods.battlegear2.coremod.BattlegearCoremodContainer";
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        debugOutputLocation = new File(data.get("mcLocation").toString(), "bg edited classes");
        BattlegearTranslator.obfuscatedEnv = Boolean.class.cast(data.get("runtimeDeobfuscationEnabled"));
    }
}
