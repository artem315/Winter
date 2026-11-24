package ru.winter.registry;

import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import ru.winter.WinterMod;

/** Tool and armor materials. Armor textures are equipment assets: assets/winter/equipment/*.json. */
public final class ModMaterials {
	/** Frost crystal: between iron and diamond, freezes whatever it hits. */
	public static final ToolMaterial FROST = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 1100, 7.0F, 2.5F, 16,
		ModTags.Items.REPAIRS_FROST_TOOLS);

	/** Ushanka, sweater, quilted pants and valenki. Weak armor, but excellent insulation. */
	public static final ArmorMaterial WINTER_CLOTHING = new ArmorMaterial(12,
		Map.of(ArmorType.BOOTS, 1, ArmorType.LEGGINGS, 2, ArmorType.CHESTPLATE, 3, ArmorType.HELMET, 1, ArmorType.BODY, 3),
		15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, ItemTags.WOOL, asset("winter_clothing"));

	/** Ice skates: boots that make you fast on ice. */
	public static final ArmorMaterial ICE_SKATES = new ArmorMaterial(15,
		Map.of(ArmorType.BOOTS, 1, ArmorType.LEGGINGS, 1, ArmorType.CHESTPLATE, 1, ArmorType.HELMET, 1, ArmorType.BODY, 1),
		12, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, ItemTags.REPAIRS_IRON_ARMOR, asset("ice_skates"));

	private ModMaterials() {
	}

	private static ResourceKey<EquipmentAsset> asset(String name) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, WinterMod.id(name));
	}
}
