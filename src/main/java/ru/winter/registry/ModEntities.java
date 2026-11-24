package ru.winter.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import ru.winter.WinterMod;
import ru.winter.entity.DedMorozEntity;
import ru.winter.entity.IceBallEntity;
import ru.winter.entity.YetiEntity;

public final class ModEntities {
	public static final EntityType<IceBallEntity> ICE_BALL = register("ice_ball",
		EntityType.Builder.<IceBallEntity>of(IceBallEntity::new, MobCategory.MISC).noLootTable()
			.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));

	public static final EntityType<YetiEntity> YETI = register("yeti",
		EntityType.Builder.of(YetiEntity::new, MobCategory.MONSTER).sized(1.5F, 2.6F).eyeHeight(2.1F).clientTrackingRange(10));

	public static final EntityType<DedMorozEntity> DED_MOROZ = register("ded_moroz",
		EntityType.Builder.of(DedMorozEntity::new, MobCategory.CREATURE).sized(0.7F, 2.3F).eyeHeight(1.85F).clientTrackingRange(10));

	private ModEntities() {
	}

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, WinterMod.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(YETI, YetiEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(DED_MOROZ, Mob.createMobAttributes());

		SpawnPlacements.register(YETI, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		SpawnPlacements.register(DED_MOROZ, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
	}
}
