package net.nether.additions;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.nether.additions.armor.CustomArmorMaterial;
import net.nether.additions.blocks.CustomFurnaceBlock;
import net.nether.additions.blocks.CustomFurnaceBlockEntity;
import net.nether.additions.foods.CalamariFood;
import net.nether.additions.fuels.FuelClass;
import net.nether.additions.tools.*;

public class NetherAdditions implements ModInitializer{

    public static final String MODID = "netheradditions";

    public static final FuelClass GLOWSTONE_FUEL = new FuelClass(3200);
    public static final Item CALAMARI = new CalamariFood();

    public static final Item QUARTZ_HELMET = new ArmorItem(CustomArmorMaterial.QUARTZ, EquipmentSlot.HEAD, new Item.Settings().group(ItemGroup.COMBAT));
    public static final Item QUARTZ_CHESTPLATE = new ArmorItem(CustomArmorMaterial.QUARTZ, EquipmentSlot.CHEST, new Item.Settings().group(ItemGroup.COMBAT));
    public static final Item QUARTZ_LEGGINGS = new ArmorItem(CustomArmorMaterial.QUARTZ, EquipmentSlot.LEGS, new Item.Settings().group(ItemGroup.COMBAT));
    public static final Item QUARTZ_BOOTS = new ArmorItem(CustomArmorMaterial.QUARTZ, EquipmentSlot.FEET, new Item.Settings().group(ItemGroup.COMBAT));

    public static final Item QUARTZ_SWORD = new SwordClass(CustomToolMaterial.QUARTZ, 6, -2.4f, new Item.Settings().group(ItemGroup.COMBAT));
    public static final Item QUARTZ_PICKAXE = new PickaxeClass(CustomToolMaterial.QUARTZ, 4, -2.8f, new Item.Settings().group(ItemGroup.TOOLS));
    public static final Item QUARTZ_SHOVEL = new ShovelClass(CustomToolMaterial.QUARTZ, 4.5f, -3f, new Item.Settings().group(ItemGroup.TOOLS));
    public static final Item QUARTZ_AXE = new AxeClass(CustomToolMaterial.QUARTZ, 9, -3.1f, new Item.Settings().group(ItemGroup.TOOLS));
    public static final Item QUARTZ_HOE = new HoeClass(CustomToolMaterial.QUARTZ, 1, -1f, new Item.Settings().group(ItemGroup.TOOLS));

    public static final Item PURPLE_DIAMOND = new Item(new Item.Settings().group(ItemGroup.MISC));
    public static final Block PURPLE_DIAMOND_ORE = new Block(FabricBlockSettings.of(Material.STONE).breakByHand(false)
            .breakByTool(FabricToolTags.PICKAXES, 2).hardness(4f).resistance(10f).requiresTool());

    public static final Item PURPLE_DIAMOND_HELMET = new ArmorItem(CustomArmorMaterial.PURPLE_DIAMOND, EquipmentSlot.HEAD, new Item.Settings().group(ItemGroup.COMBAT));
    public static final Item PURPLE_DIAMOND_CHESTPLATE = new ArmorItem(CustomArmorMaterial.PURPLE_DIAMOND, EquipmentSlot.CHEST, new Item.Settings().group(ItemGroup.COMBAT));
    public static final Item PURPLE_DIAMOND_LEGGINGS = new ArmorItem(CustomArmorMaterial.PURPLE_DIAMOND, EquipmentSlot.LEGS, new Item.Settings().group(ItemGroup.COMBAT));
    public static final Item PURPLE_DIAMOND_BOOTS = new ArmorItem(CustomArmorMaterial.PURPLE_DIAMOND, EquipmentSlot.FEET, new Item.Settings().group(ItemGroup.COMBAT));

    public static final Item PURPLE_DIAMOND_SWORD = new SwordClass(CustomToolMaterial.PURPLE_DIAMOND, 7, -2.4f, new Item.Settings().group(ItemGroup.COMBAT));
    public static final Item PURPLE_DIAMOND_PICKAXE = new PickaxeClass(CustomToolMaterial.PURPLE_DIAMOND, 5, -2.8f, new Item.Settings().group(ItemGroup.TOOLS));
    public static final Item PURPLE_DIAMOND_SHOVEL = new ShovelClass(CustomToolMaterial.PURPLE_DIAMOND, 5.5f, -3f, new Item.Settings().group(ItemGroup.TOOLS));
    public static final Item PURPLE_DIAMOND_AXE = new AxeClass(CustomToolMaterial.PURPLE_DIAMOND, 9, -3f, new Item.Settings().group(ItemGroup.TOOLS));
    public static final Item PURPLE_DIAMOND_HOE = new HoeClass(CustomToolMaterial.PURPLE_DIAMOND, 1, 0f, new Item.Settings().group(ItemGroup.TOOLS));


    public static final Block NETHERRACK_FURNACE = new CustomFurnaceBlock(FabricBlockSettings.copy(Blocks.FURNACE));
    public static BlockEntityType CUSTOM_FURNACE_ENTITY;


    private void handleBiome(Biome biome)
    {
        if(biome.getCategory() == Biome.Category.NETHER)
        {
            biome.addFeature(
                    GenerationStep.Feature.UNDERGROUND_ORES,
                    Feature.ORE.configure
                            (
                                    new OreFeatureConfig
                                            (
                                                    OreFeatureConfig.Target.NETHERRACK,
                                                    PURPLE_DIAMOND_ORE.getDefaultState(),
                                                    8
                                            )
                            ).createDecoratedFeature
                            (
                                    Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(
                                            12,
                                            0,0,
                                            255
                                    )))
            );


        }
    }


    @Override
    public void onInitialize()
    {
        System.out.println("############################# NETHER ADDITIONS LOADED #############################");

        // Netherrack Furnace
        Registry.register(Registry.BLOCK, new Identifier(MODID, "netherrack_furnace"), NETHERRACK_FURNACE);
        Registry.register(Registry.ITEM, new Identifier(MODID, "netherrack_furnace"),
                new BlockItem(NETHERRACK_FURNACE, new Item.Settings().group(ItemGroup.DECORATIONS)));
        CUSTOM_FURNACE_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID, BlockEntityType.Builder.create(CustomFurnaceBlockEntity::new, NETHERRACK_FURNACE).build(null));

        // Glow Stone Fuel
        Registry.register(Registry.ITEM, new Identifier(MODID, "glowstone_fuel"), GLOWSTONE_FUEL);
        FuelRegistry.INSTANCE.add(GLOWSTONE_FUEL, GLOWSTONE_FUEL.ticks);

        // Quartz Tools
        Registry.register(Registry.ITEM, new Identifier(MODID, "quartz_shovel"), QUARTZ_SHOVEL);
        Registry.register(Registry.ITEM, new Identifier(MODID, "quartz_pickaxe"), QUARTZ_PICKAXE);
        Registry.register(Registry.ITEM, new Identifier(MODID, "quartz_axe"), QUARTZ_AXE);
        Registry.register(Registry.ITEM, new Identifier(MODID, "quartz_hoe"), QUARTZ_HOE);
        Registry.register(Registry.ITEM, new Identifier(MODID, "quartz_sword"), QUARTZ_SWORD);

        // Quartz Armor
        Registry.register(Registry.ITEM, new Identifier(MODID, "quartz_helmet"), QUARTZ_HELMET);
        Registry.register(Registry.ITEM, new Identifier(MODID, "quartz_chestplate"), QUARTZ_CHESTPLATE);
        Registry.register(Registry.ITEM, new Identifier(MODID, "quartz_leggings"), QUARTZ_LEGGINGS);
        Registry.register(Registry.ITEM, new Identifier(MODID, "quartz_boots"), QUARTZ_BOOTS);

        // Random Tools
        Registry.register(Registry.ITEM, new Identifier(MODID, "glass_cannon_sword"), new SwordClass(CustomToolMaterial.GLASS, 19, 0, new Item.Settings().group(ItemGroup.COMBAT)));
        // Make the glass cannon sword be able to shoot 1 single projective when right clicked. Also find a way to add every variant of glass. Hopefully not all by hand

        // Food
        Registry.register(Registry.ITEM, new Identifier(MODID, "calamari"), CALAMARI);



        //Purple Diamond
        Registry.register(Registry.BLOCK, new Identifier(MODID, "purple_diamond_ore"), PURPLE_DIAMOND_ORE);
        Registry.register(Registry.ITEM, new Identifier(MODID, "purple_diamond_ore"),
                new BlockItem(PURPLE_DIAMOND_ORE, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));
        Registry.register(Registry.ITEM, new Identifier(MODID, "purple_diamond"), PURPLE_DIAMOND);

        Registry.register(Registry.ITEM, new Identifier(MODID, "purple_diamond_helmet"), PURPLE_DIAMOND_HELMET);
        Registry.register(Registry.ITEM, new Identifier(MODID, "purple_diamond_chestplate"), PURPLE_DIAMOND_CHESTPLATE);
        Registry.register(Registry.ITEM, new Identifier(MODID, "purple_diamond_leggings"), PURPLE_DIAMOND_LEGGINGS);
        Registry.register(Registry.ITEM, new Identifier(MODID, "purple_diamond_boots"), PURPLE_DIAMOND_BOOTS);

        Registry.register(Registry.ITEM, new Identifier(MODID, "purple_diamond_shovel"), PURPLE_DIAMOND_SHOVEL);
        Registry.register(Registry.ITEM, new Identifier(MODID, "purple_diamond_pickaxe"), PURPLE_DIAMOND_PICKAXE);
        Registry.register(Registry.ITEM, new Identifier(MODID, "purple_diamond_axe"), PURPLE_DIAMOND_AXE);
        Registry.register(Registry.ITEM, new Identifier(MODID, "purple_diamond_hoe"), PURPLE_DIAMOND_HOE);
        Registry.register(Registry.ITEM, new Identifier(MODID, "purple_diamond_sword"), PURPLE_DIAMOND_SWORD);

        Registry.BIOME.forEach(this::handleBiome);
        RegistryEntryAddedCallback.event(Registry.BIOME).register((i, identifier, biome) -> handleBiome(biome));
    }

}
