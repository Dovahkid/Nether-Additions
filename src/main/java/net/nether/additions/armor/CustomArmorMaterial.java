package net.nether.additions.armor;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.nether.additions.NetherAdditions;

public enum CustomArmorMaterial implements ArmorMaterial
{
    QUARTZ("quartz", new int[] {2, 5, 6, 2}, 16, 30, SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            Ingredient.ofItems(Items.QUARTZ), 0, 0),
    PURPLE_DIAMOND("purple_diamond", new int[] {3, 6, 8, 3}, 35, 22,
            SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, Ingredient.ofItems(NetherAdditions.PURPLE_DIAMOND), 2, 0);

    private final int[] BASE_DURABILITY = {13, 15, 16, 11};
    private final int[] PROTECTION_AMOUNTS;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final Ingredient repairIngredient;
    private final float toughness;
    private final float knockBack;
    private final String name;
    private int durabilityModifier;

    CustomArmorMaterial(String name, int[] PROTECTION_AMOUNTS, int durabilityModifier,
                        int enchantability, SoundEvent equipSound, Ingredient repairIngredient, float toughness,
                        float knockBack)
    {
        this.PROTECTION_AMOUNTS = PROTECTION_AMOUNTS;
        this.name = name;
        this.durabilityModifier = durabilityModifier;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.repairIngredient = repairIngredient;
        this.toughness = toughness;
        this.knockBack = knockBack;
    }

    @Override
    public int getDurability(EquipmentSlot slot) {
        return BASE_DURABILITY[slot.getEntitySlotId()]*durabilityModifier;
    }

    @Override
    public int getProtectionAmount(EquipmentSlot slot) {
        return PROTECTION_AMOUNTS[slot.getEntitySlotId()];
    }

    @Override
    public int getEnchantability() {
        return enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockBack;
    }
}
