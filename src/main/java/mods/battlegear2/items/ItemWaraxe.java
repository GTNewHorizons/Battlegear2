package mods.battlegear2.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;

import com.google.common.collect.Multimap;

import cpw.mods.fml.common.registry.GameRegistry;
import mods.battlegear2.api.weapons.IPenetrateWeapon;

// Should we make this also use the heraldry? It actually doesn't look as good as the sword (and makes the sword a
// little more special)
public class ItemWaraxe extends OneHandedWeapon implements IPenetrateWeapon { // implements IHeraldyItem{ Don't know if
    // we want to do this or not

    /*
     * private Icon baseIcon; private Icon trimIcon; private Icon postRenderIcon;
     */
    public ItemWaraxe(ToolMaterial material, String name, int ignoreDamageAmount) {
        super(material, name);
        // set the base damage to that of lower than usual (balance)
        this.baseDamage -= 1 + ignoreDamageAmount;
        this.setMaxDamage(material.getMaxUses() * 2);
        GameRegistry.registerItem(this, this.name);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(ItemStack stack) {
        Multimap<String, AttributeModifier> map = super.getAttributeModifiers(stack);
        map.put(
                armourPenetrate.getAttributeUnlocalizedName(),
                new AttributeModifier(penetrateArmourUUID, "Attack Modifier", this.ignoreDamageAmount, 0));
        return map;
    }

    @Override
    public boolean func_150897_b(Block par1Block) // Waraxe can harvest logs
    {
        return par1Block instanceof BlockLog;
    }

    @Override
    public int getPenetratingPower(ItemStack stack) {
        return (int) ignoreDamageAmount;
    }
}
