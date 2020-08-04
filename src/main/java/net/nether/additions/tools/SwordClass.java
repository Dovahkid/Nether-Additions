package net.nether.additions.tools;

import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;

public class SwordClass extends SwordItem
{
    public SwordClass(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }
}
