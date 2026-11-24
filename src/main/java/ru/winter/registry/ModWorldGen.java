package ru.winter.registry;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import ru.winter.WinterMod;

/**
 * Hooks the mod's data-driven features (data/winter/worldgen) and mobs into cold biomes.
 * Which biomes get what is controlled by biome tags in data/winter/tags/worldgen/biome.
 */
public final class ModWorldGen {
	public static final ResourceKey<PlacedFeature> FROST_CRYSTAL_ORE = key("frost_crystal_ore");
	public static final ResourceKey<PlacedFeature> GIFT = key("gift");
	public static final ResourceKey<PlacedFeature> FESTIVE_SPRUCE = key("festive_spruce");
	public static final ResourceKey<PlacedFeature> ICICLES = key("icicles");

	private ModWorldGen() {
	}

	private static ResourceKey<PlacedFeature> key(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, WinterMod.id(name));
	}

	public static void init() {
		BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.Biomes.HAS_FROST_CRYSTAL_ORE),
			GenerationStep.Decoration.UNDERGROUND_ORES, FROST_CRYSTAL_ORE);
		BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.Biomes.HAS_FROST_CRYSTAL_ORE),
			GenerationStep.Decoration.UNDERGROUND_DECORATION, ICICLES);
		BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.Biomes.HAS_GIFTS),
			GenerationStep.Decoration.VEGETAL_DECORATION, GIFT);
		BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.Biomes.HAS_FESTIVE_SPRUCE),
			GenerationStep.Decoration.VEGETAL_DECORATION, FESTIVE_SPRUCE);

		BiomeModifications.addSpawn(BiomeSelectors.tag(ModTags.Biomes.SPAWNS_YETI), MobCategory.MONSTER, ModEntities.YETI, 12, 1, 1);
		BiomeModifications.addSpawn(BiomeSelectors.tag(ModTags.Biomes.SPAWNS_DED_MOROZ), MobCategory.CREATURE, ModEntities.DED_MOROZ, 1, 1, 1);
	}
}
