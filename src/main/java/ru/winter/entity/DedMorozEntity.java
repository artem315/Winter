package ru.winter.entity;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.LookAtCustomerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.StopFollowingCustomerGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Ded Moroz (Father Frost): a rare festive trader wandering through snowy biomes.
 * Sells New Year treats, decorations, gifts and his magic frost staff.
 * Naturally spawned ones leave after about two in-game days; ones from spawn eggs stay.
 */
public class DedMorozEntity extends WanderingTraderEntity {
	public DedMorozEntity(EntityType<? extends WanderingTraderEntity> type, World world) {
		super(type, world);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new StopFollowingCustomerGoal(this));
		this.goalSelector.add(1, new FleeEntityGoal<>(this, ZombieEntity.class, 8.0F, 0.5, 0.5));
		this.goalSelector.add(1, new FleeEntityGoal<>(this, IllagerEntity.class, 12.0F, 0.5, 0.5));
		this.goalSelector.add(1, new FleeEntityGoal<>(this, VexEntity.class, 8.0F, 0.5, 0.5));
		this.goalSelector.add(1, new EscapeDangerGoal(this, 0.5));
		this.goalSelector.add(1, new LookAtCustomerGoal(this));
		this.goalSelector.add(4, new GoToWalkTargetGoal(this, 0.35));
		this.goalSelector.add(8, new WanderAroundFarGoal(this, 0.35));
		this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
		this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
	}

	@Override
	protected void fillRecipes() {
		TradeOfferList offers = this.getOffers();
		this.fillRecipesFromPool(offers, DedMorozTrades.COMMON, 6);
		this.fillRecipesFromPool(offers, DedMorozTrades.RARE, 2);
	}

	@Nullable
	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
		if (spawnReason == SpawnReason.NATURAL || spawnReason == SpawnReason.CHUNK_GENERATION) {
			this.setDespawnDelay(48000);
		}
		this.setPersistent();
		return super.initialize(world, difficulty, spawnReason, entityData);
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (this.getWorld().isClient && this.random.nextInt(8) == 0) {
			this.getWorld().addParticle(ParticleTypes.SNOWFLAKE,
				this.getParticleX(0.6), this.getRandomBodyY(), this.getParticleZ(0.6), 0.0, -0.02, 0.0);
		}
	}
}
