package ru.winter.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;
import ru.winter.WinterMod;
import ru.winter.entity.DedMorozEntity;
import ru.winter.entity.IceBallEntity;
import ru.winter.entity.YetiEntity;

public final class ModEntities {
	public static final EntityType<IceBallEntity> ICE_BALL = Registry.register(Registries.ENTITY_TYPE, WinterMod.id("ice_ball"),
		EntityType.Builder.<IceBallEntity>create(IceBallEntity::new, SpawnGroup.MISC)
			.dimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10)
			.build("ice_ball"));

	public static final EntityType<YetiEntity> YETI = Registry.register(Registries.ENTITY_TYPE, WinterMod.id("yeti"),
		EntityType.Builder.create(YetiEntity::new, SpawnGroup.MONSTER)
			.dimensions(1.5F, 2.6F).eyeHeight(2.1F).maxTrackingRange(10)
			.build("yeti"));

	public static final EntityType<DedMorozEntity> DED_MOROZ = Registry.register(Registries.ENTITY_TYPE, WinterMod.id("ded_moroz"),
		EntityType.Builder.create(DedMorozEntity::new, SpawnGroup.CREATURE)
			.dimensions(0.7F, 2.3F).eyeHeight(1.85F).maxTrackingRange(10)
			.build("ded_moroz"));

	private ModEntities() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(YETI, YetiEntity.createYetiAttributes());
		FabricDefaultAttributeRegistry.register(DED_MOROZ, MobEntity.createMobAttributes());

		SpawnRestriction.register(YETI, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
		SpawnRestriction.register(DED_MOROZ, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
	}
}
