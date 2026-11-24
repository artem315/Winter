package ru.winter.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.DispenserBlock;
import ru.winter.WinterMod;
import ru.winter.item.FrostStaffItem;
import ru.winter.item.FrostSwordItem;
import ru.winter.item.IceBallItem;
import ru.winter.item.SnowShovelItem;
import ru.winter.item.ThermometerItem;

public final class ModItems {
	public static final List<Item> ALL = new ArrayList<>();

	// --- Materials ----------------------------------------------------------------------------------
	public static final Item FROST_CRYSTAL = register("frost_crystal", Item::new, new Item.Properties());
	public static final Item YETI_FUR = register("yeti_fur", Item::new, new Item.Properties());

	// --- Food & drinks --------------------------------------------------------------------------------
	public static final Item HOT_CHOCOLATE = register("hot_chocolate", Item::new, new Item.Properties().stacksTo(16)
		.food(food(4, 0.6F, true), drink(new MobEffectInstance(ModEffects.WARMTH, 3 * 60 * 20, 0)))
		.usingConvertsTo(Items.GLASS_BOTTLE));
	public static final Item SBITEN = register("sbiten", Item::new, new Item.Properties().stacksTo(16)
		.food(food(3, 0.5F, true), drink(new MobEffectInstance(ModEffects.WARMTH, 2 * 60 * 20, 0),
			new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 0)))
		.usingConvertsTo(Items.GLASS_BOTTLE));
	public static final Item GINGERBREAD_COOKIE = register("gingerbread_cookie", Item::new, new Item.Properties()
		.food(food(3, 0.4F, false), snack()));
	public static final Item CANDY_CANE = register("candy_cane", Item::new, new Item.Properties()
		.food(food(2, 0.2F, true), snack(new MobEffectInstance(MobEffects.SPEED, 15 * 20, 0))));
	public static final Item MANDARIN = register("mandarin", Item::new, new Item.Properties()
		.food(food(3, 0.5F, false), snack()));
	public static final Item OLIVIER_SALAD = register("olivier_salad", Item::new, new Item.Properties().stacksTo(16)
		.food(food(10, 0.9F, false), meal(new MobEffectInstance(MobEffects.ABSORPTION, 60 * 20, 0)))
		.usingConvertsTo(Items.BOWL));
	public static final Item RAW_PELMENI = register("raw_pelmeni", Item::new, new Item.Properties()
		.food(food(2, 0.2F, false), Consumables.defaultFood()
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 20, 0), 0.3F)).build()));
	public static final Item PELMENI = register("pelmeni", Item::new, new Item.Properties()
		.food(food(4, 0.7F, false), meal(new MobEffectInstance(ModEffects.WARMTH, 30 * 20, 0))));

	// --- Tools & weapons -----------------------------------------------------------------------------
	public static final Item FROST_SWORD = register("frost_sword", FrostSwordItem::new,
		new Item.Properties().sword(ModMaterials.FROST, 3.0F, -2.4F));
	public static final Item FROST_PICKAXE = register("frost_pickaxe", Item::new,
		new Item.Properties().pickaxe(ModMaterials.FROST, 1.0F, -2.8F));
	public static final Item FROST_AXE = register("frost_axe", p -> new AxeItem(ModMaterials.FROST, 5.5F, -3.1F, p), new Item.Properties());
	public static final Item FROST_SHOVEL = register("frost_shovel", p -> new ShovelItem(ModMaterials.FROST, 1.5F, -3.0F, p), new Item.Properties());
	public static final Item FROST_HOE = register("frost_hoe", p -> new HoeItem(ModMaterials.FROST, -2.5F, -0.5F, p), new Item.Properties());
	public static final Item SNOW_SHOVEL = register("snow_shovel", p -> new SnowShovelItem(ToolMaterial.IRON, 1.5F, -3.0F, p), new Item.Properties());
	public static final Item FROST_STAFF = register("frost_staff", FrostStaffItem::new,
		new Item.Properties().durability(250).rarity(Rarity.EPIC));
	public static final Item ICE_BALL = register("ice_ball", IceBallItem::new, new Item.Properties().stacksTo(16));
	public static final Item THERMOMETER = register("thermometer", ThermometerItem::new, new Item.Properties().stacksTo(1));

	// --- Clothing -------------------------------------------------------------------------------------
	public static final Item USHANKA = register("ushanka", Item::new,
		new Item.Properties().humanoidArmor(ModMaterials.WINTER_CLOTHING, ArmorType.HELMET));
	public static final Item SWEATER = register("sweater", Item::new,
		new Item.Properties().humanoidArmor(ModMaterials.WINTER_CLOTHING, ArmorType.CHESTPLATE));
	public static final Item QUILTED_PANTS = register("quilted_pants", Item::new,
		new Item.Properties().humanoidArmor(ModMaterials.WINTER_CLOTHING, ArmorType.LEGGINGS));
	public static final Item VALENKI = register("valenki", Item::new,
		new Item.Properties().humanoidArmor(ModMaterials.WINTER_CLOTHING, ArmorType.BOOTS));
	public static final Item ICE_SKATES = register("ice_skates", Item::new,
		new Item.Properties().humanoidArmor(ModMaterials.ICE_SKATES, ArmorType.BOOTS).rarity(Rarity.UNCOMMON));

	// --- Spawn eggs -----------------------------------------------------------------------------------
	public static final Item YETI_SPAWN_EGG = register("yeti_spawn_egg", SpawnEggItem::new,
		new Item.Properties().spawnEgg(ModEntities.YETI));
	public static final Item DED_MOROZ_SPAWN_EGG = register("ded_moroz_spawn_egg", SpawnEggItem::new,
		new Item.Properties().spawnEgg(ModEntities.DED_MOROZ));

	private ModItems() {
	}

	private static FoodProperties food(int nutrition, float saturation, boolean alwaysEdible) {
		FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation);
		if (alwaysEdible) {
			builder.alwaysEdible();
		}
		return builder.build();
	}

	private static Consumable drink(MobEffectInstance... effects) {
		return Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(List.of(effects))).build();
	}

	private static Consumable meal(MobEffectInstance... effects) {
		return Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(List.of(effects))).build();
	}

	private static Consumable snack(MobEffectInstance... effects) {
		Consumable.Builder builder = Consumables.defaultFood().consumeSeconds(0.8F);
		if (effects.length > 0) {
			builder.onConsume(new ApplyStatusEffectsConsumeEffect(List.of(effects)));
		}
		return builder.build();
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, WinterMod.id(name));
		Item item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
		ALL.add(item);
		return item;
	}

	public static void init() {
		DispenserBlock.registerProjectileBehavior(ICE_BALL);
	}
}
