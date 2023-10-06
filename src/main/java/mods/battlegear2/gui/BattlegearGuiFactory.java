package mods.battlegear2.gui;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.client.IModGuiFactory;
import mods.battlegear2.client.gui.BattlegearConfigGUI;

public final class BattlegearGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraft) {}

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return BattlegearConfigGUI.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}
