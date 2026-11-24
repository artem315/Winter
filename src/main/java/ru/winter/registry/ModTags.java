package ru.winter.registry;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;
import ru.winter.WinterMod;

public final class ModTags {
	public static final class Blocks {
		/** Blocks that warm up players standing nearby (lit ones only, if they have a LIT property). */
		public static final TagKey<Block> HEAT_SOURCES = of("heat_sources");
		/** Blocks the snow shovel clears in a 3x3 area. */
		public static final TagKey<Block> SNOW_SHOVEL_CLEARABLE = of("snow_shovel_clearable");
		/** Surfaces on which ice skates speed the player up. */
		public static final TagKey<Block> SKATING_SURFACES = of("skating_surfaces");

		private static TagKey<Block> of(String path) {
			return TagKey.of(RegistryKeys.BLOCK, WinterMod.id(path));
		}
	}

	public static final class Items {
		/** Warm clothing: 2 insulation points per piece. */
		public static final TagKey<Item> STRONG_INSULATION = of("insulation/strong");
		/** Leather-like clothing: 1 insulation point per piece. */
		public static final TagKey<Item> LIGHT_INSULATION = of("insulation/light");

		private static TagKey<Item> of(String path) {
			return TagKey.of(RegistryKeys.ITEM, WinterMod.id(path));
		}
	}

	public static final class Biomes {
		public static final TagKey<Biome> HAS_FROST_CRYSTAL_ORE = of("has_frost_crystal_ore");
		public static final TagKey<Biome> HAS_GIFTS = of("has_gifts");
		public static final TagKey<Biome> HAS_FESTIVE_SPRUCE = of("has_festive_spruce");
		public static final TagKey<Biome> SPAWNS_YETI = of("spawns_yeti");
		public static final TagKey<Biome> SPAWNS_DED_MOROZ = of("spawns_ded_moroz");

		private static TagKey<Biome> of(String path) {
			return TagKey.of(RegistryKeys.BIOME, WinterMod.id(path));
		}
	}

	public static final class EntityTypes {
		private static TagKey<EntityType<?>> of(String path) {
			return TagKey.of(RegistryKeys.ENTITY_TYPE, WinterMod.id(path));
		}
	}

	private ModTags() {
	}
}
