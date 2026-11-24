package ru.winter.registry;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import ru.winter.WinterMod;

/**
 * Hooks the mod's data-driven features (data/winter/worldgen) and mobs into cold biomes.
 * Which biomes get what is controlled by biome tags in data/winter/tags/worldgen/biome.
 */
public final class ModWorldGen {
	public static final RegistryKey<PlacedFeature> FROST_CRYSTAL_ORE = key("frost_crystal_ore");
	public static final RegistryKey<PlacedFeature> GIFT = key("gift");
	public static final RegistryKey<PlacedFeature> FESTIVE_SPRUCE = key("festive_spruce");
	public static final RegistryKey<PlacedFeature> ICICLES = key("icicles");

	private ModWorldGen() {
	}

	private static RegistryKey<PlacedFeature> key(String name) {
		return RegistryKey.of(RegistryKeys.PLACED_FEATURE, WinterMod.id(name));
	}

	public static void init() {
		BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.Biomes.HAS_FROST_CRYSTAL_ORE),
			GenerationStep.Feature.UNDERGROUND_ORES, FROST_CRYSTAL_ORE);
		BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.Biomes.HAS_FROST_CRYSTAL_ORE),
			GenerationStep.Feature.UNDERGROUND_DECORATION, ICICLES);
		BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.Biomes.HAS_GIFTS),
			GenerationStep.Feature.VEGETAL_DECORATION, GIFT);
		BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.Biomes.HAS_FESTIVE_SPRUCE),
			GenerationStep.Feature.VEGETAL_DECORATION, FESTIVE_SPRUCE);

		BiomeModifications.addSpawn(BiomeSelectors.tag(ModTags.Biomes.SPAWNS_YETI), SpawnGroup.MONSTER, ModEntities.YETI, 12, 1, 1);
		BiomeModifications.addSpawn(BiomeSelectors.tag(ModTags.Biomes.SPAWNS_DED_MOROZ), SpawnGroup.CREATURE, ModEntities.DED_MOROZ, 1, 1, 1);
	}
}
