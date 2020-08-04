package net.nether.additions.tools;

import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;

public class ShovelClass extends ShovelItem
{
    public ShovelClass(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }
}
