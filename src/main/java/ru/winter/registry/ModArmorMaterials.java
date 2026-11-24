package ru.winter.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import ru.winter.WinterMod;

public final class ModArmorMaterials {
	/** Ushanka, sweater, quilted pants and valenki. Weak armor, but excellent insulation. */
	public static final RegistryEntry<ArmorMaterial> WINTER_CLOTHING = register("winter_clothing",
		Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
			map.put(ArmorItem.Type.BOOTS, 1);
			map.put(ArmorItem.Type.LEGGINGS, 2);
			map.put(ArmorItem.Type.CHESTPLATE, 3);
			map.put(ArmorItem.Type.HELMET, 1);
			map.put(ArmorItem.Type.BODY, 3);
		}), 15, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, () -> Ingredient.fromTag(ItemTags.WOOL), 0.0F, 0.0F);

	/** Ice skates: boots that make you fast on ice. */
	public static final RegistryEntry<ArmorMaterial> ICE_SKATES = register("ice_skates",
		Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
			map.put(ArmorItem.Type.BOOTS, 1);
			map.put(ArmorItem.Type.LEGGINGS, 1);
			map.put(ArmorItem.Type.CHESTPLATE, 1);
			map.put(ArmorItem.Type.HELMET, 1);
			map.put(ArmorItem.Type.BODY, 1);
		}), 12, SoundEvents.ITEM_ARMOR_EQUIP_IRON, () -> Ingredient.ofItems(Items.IRON_INGOT), 0.0F, 0.0F);

	private ModArmorMaterials() {
	}

	private static RegistryEntry<ArmorMaterial> register(
		String name, EnumMap<ArmorItem.Type, Integer> defense, int enchantability, RegistryEntry<SoundEvent> equipSound,
		Supplier<Ingredient> repairIngredient, float toughness, float knockbackResistance
	) {
		List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(WinterMod.id(name)));
		return Registry.registerReference(Registries.ARMOR_MATERIAL, WinterMod.id(name),
			new ArmorMaterial(defense, enchantability, equipSound, repairIngredient, layers, toughness, knockbackResistance));
	}
}
