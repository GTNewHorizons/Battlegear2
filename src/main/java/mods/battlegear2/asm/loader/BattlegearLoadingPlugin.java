package mods.battlegear2.asm.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import mods.battlegear2.asm.transformers.InventoryArrayAccessTransformer;

@TransformerExclusions({ "mods.battlegear2.asm.loader", "mods.battlegear2.asm.transformers" })
@Name("Mine and Blade: Battlegear2")
@SortingIndex(1500)
@MCVersion("1.7.10")
public final class BattlegearLoadingPlugin implements IEarlyMixinLoader, IFMLLoadingPlugin {

    public static final Logger logger = LogManager.getLogger("ASM Battlegear2");
    private static boolean isObf;

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { InventoryArrayAccessTransformer.class.getName() };
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        isObf = (boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getMixinConfig() {
        return "mixins.battlegear2.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        final List<String> mixins = new ArrayList<>();
        mixins.add("MixinEntityPlayer");
        mixins.add("MixinItemStack");
        mixins.add("MixinInventoryPlayer");
        mixins.add("MixinNetHandlerPlayServer");
        if (FMLLaunchHandler.side().isClient()) {
            mixins.add("MixinEntityOtherPlayerMP");
            mixins.add("MixinInventoryPlayerClient");
            mixins.add("MixinItemRenderer");
            mixins.add("MixinModelBiped");
            mixins.add("MixinNetHandlerPlayClient");
        }
        return mixins;
    }

    public static boolean isObf() {
        return isObf;
    }

}
