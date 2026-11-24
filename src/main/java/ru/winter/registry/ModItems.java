package ru.winter.registry;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.DispenserBlock;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;
import ru.winter.WinterMod;
import ru.winter.item.ContainerFoodItem;
import ru.winter.item.FrostStaffItem;
import ru.winter.item.FrostSwordItem;
import ru.winter.item.IceBallItem;
import ru.winter.item.SnowShovelItem;
import ru.winter.item.ThermometerItem;

public final class ModItems {
	public static final List<Item> ALL = new ArrayList<>();

	// --- Materials ----------------------------------------------------------------------------------
	public static final Item FROST_CRYSTAL = register("frost_crystal", new Item(new Item.Settings()));
	public static final Item YETI_FUR = register("yeti_fur", new Item(new Item.Settings()));

	// --- Food & drinks --------------------------------------------------------------------------------
	public static final Item HOT_CHOCOLATE = register("hot_chocolate", new ContainerFoodItem(true, new Item.Settings().maxCount(16)
		.food(new FoodComponent.Builder().nutrition(4).saturationModifier(0.6F).alwaysEdible()
			.statusEffect(new StatusEffectInstance(ModEffects.WARMTH, 3 * 60 * 20, 0), 1.0F)
			.usingConvertsTo(Items.GLASS_BOTTLE).build())));
	public static final Item SBITEN = register("sbiten", new ContainerFoodItem(true, new Item.Settings().maxCount(16)
		.food(new FoodComponent.Builder().nutrition(3).saturationModifier(0.5F).alwaysEdible()
			.statusEffect(new StatusEffectInstance(ModEffects.WARMTH, 2 * 60 * 20, 0), 1.0F)
			.statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 10 * 20, 0), 1.0F)
			.usingConvertsTo(Items.GLASS_BOTTLE).build())));
	public static final Item GINGERBREAD_COOKIE = register("gingerbread_cookie", new Item(new Item.Settings()
		.food(new FoodComponent.Builder().nutrition(3).saturationModifier(0.4F).snack().build())));
	public static final Item CANDY_CANE = register("candy_cane", new Item(new Item.Settings()
		.food(new FoodComponent.Builder().nutrition(2).saturationModifier(0.2F).snack().alwaysEdible()
			.statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 15 * 20, 0), 1.0F).build())));
	public static final Item MANDARIN = register("mandarin", new Item(new Item.Settings()
		.food(new FoodComponent.Builder().nutrition(3).saturationModifier(0.5F).snack().build())));
	public static final Item OLIVIER_SALAD = register("olivier_salad", new ContainerFoodItem(false, new Item.Settings().maxCount(16)
		.food(new FoodComponent.Builder().nutrition(10).saturationModifier(0.9F)
			.statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 60 * 20, 0), 1.0F)
			.usingConvertsTo(Items.BOWL).build())));
	public static final Item RAW_PELMENI = register("raw_pelmeni", new Item(new Item.Settings()
		.food(new FoodComponent.Builder().nutrition(2).saturationModifier(0.2F)
			.statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 20 * 20, 0), 0.3F).build())));
	public static final Item PELMENI = register("pelmeni", new Item(new Item.Settings()
		.food(new FoodComponent.Builder().nutrition(4).saturationModifier(0.7F)
			.statusEffect(new StatusEffectInstance(ModEffects.WARMTH, 30 * 20, 0), 1.0F).build())));

	// --- Tools & weapons -----------------------------------------------------------------------------
	public static final Item FROST_SWORD = register("frost_sword", new FrostSwordItem(ModToolMaterials.FROST,
		new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.FROST, 3, -2.4F))));
	public static final Item FROST_PICKAXE = register("frost_pickaxe", new PickaxeItem(ModToolMaterials.FROST,
		new Item.Settings().attributeModifiers(PickaxeItem.createAttributeModifiers(ModToolMaterials.FROST, 1.0F, -2.8F))));
	public static final Item FROST_AXE = register("frost_axe", new AxeItem(ModToolMaterials.FROST,
		new Item.Settings().attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterials.FROST, 5.5F, -3.1F))));
	public static final Item FROST_SHOVEL = register("frost_shovel", new ShovelItem(ModToolMaterials.FROST,
		new Item.Settings().attributeModifiers(ShovelItem.createAttributeModifiers(ModToolMaterials.FROST, 1.5F, -3.0F))));
	public static final Item FROST_HOE = register("frost_hoe", new HoeItem(ModToolMaterials.FROST,
		new Item.Settings().attributeModifiers(HoeItem.createAttributeModifiers(ModToolMaterials.FROST, -2.5F, -0.5F))));
	public static final Item SNOW_SHOVEL = register("snow_shovel", new SnowShovelItem(ToolMaterials.IRON,
		new Item.Settings().attributeModifiers(ShovelItem.createAttributeModifiers(ToolMaterials.IRON, 1.5F, -3.0F))));
	public static final Item FROST_STAFF = register("frost_staff", new FrostStaffItem(new Item.Settings().maxDamage(250)));
	public static final Item ICE_BALL = register("ice_ball", new IceBallItem(new Item.Settings().maxCount(16)));
	public static final Item THERMOMETER = register("thermometer", new ThermometerItem(new Item.Settings().maxCount(1)));

	// --- Clothing -------------------------------------------------------------------------------------
	public static final Item USHANKA = register("ushanka", new ArmorItem(ModArmorMaterials.WINTER_CLOTHING, ArmorItem.Type.HELMET,
		new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(12))));
	public static final Item SWEATER = register("sweater", new ArmorItem(ModArmorMaterials.WINTER_CLOTHING, ArmorItem.Type.CHESTPLATE,
		new Item.Settings().maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(12))));
	public static final Item QUILTED_PANTS = register("quilted_pants", new ArmorItem(ModArmorMaterials.WINTER_CLOTHING, ArmorItem.Type.LEGGINGS,
		new Item.Settings().maxDamage(ArmorItem.Type.LEGGINGS.getMaxDamage(12))));
	public static final Item VALENKI = register("valenki", new ArmorItem(ModArmorMaterials.WINTER_CLOTHING, ArmorItem.Type.BOOTS,
		new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(12))));
	public static final Item ICE_SKATES = register("ice_skates", new ArmorItem(ModArmorMaterials.ICE_SKATES, ArmorItem.Type.BOOTS,
		new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(15)).rarity(Rarity.UNCOMMON)));

	// --- Spawn eggs -----------------------------------------------------------------------------------
	public static final Item YETI_SPAWN_EGG = register("yeti_spawn_egg",
		new SpawnEggItem(ModEntities.YETI, 0xEEF6FA, 0x6C93B5, new Item.Settings()));
	public static final Item DED_MOROZ_SPAWN_EGG = register("ded_moroz_spawn_egg",
		new SpawnEggItem(ModEntities.DED_MOROZ, 0xC8102E, 0xF4F4F4, new Item.Settings()));

	private ModItems() {
	}

	private static Item register(String name, Item item) {
		Item registered = Registry.register(Registries.ITEM, WinterMod.id(name), item);
		ALL.add(registered);
		return registered;
	}

	public static void init() {
		DispenserBlock.registerProjectileBehavior(ICE_BALL);
	}
}
