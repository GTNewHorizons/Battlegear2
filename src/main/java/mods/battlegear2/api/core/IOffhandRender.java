package mods.battlegear2.api.core;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;

/**
 * Interface added to {@link ItemRenderer} to support offhand rendering Note that, they only provide access to added
 * fields for the offhand, NOT the fields for the mainhand
 * 
 * @author GotoLink
 */
public interface IOffhandRender {

    public ItemStack getOffHandItemToRender();

    public void setOffHandItemToRender(ItemStack item);

    public int getEquippedItemOffhandSlot();

    public void serEquippedItemOffhandSlot(int slot);

    public float getEquippedOffHandProgress();

    public void setEquippedOffHandProgress(float progress);

    public float getPrevEquippedOffHandProgress();

    public void setPrevEquippedOffHandProgress(float progress);
}
