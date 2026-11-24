package ru.winter.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

/**
 * Ded Moroz (Father Frost): a rare festive trader wandering through snowy biomes.
 * Sells New Year treats, decorations, gifts and his magic frost staff.
 * Naturally spawned ones leave after about two in-game days; ones from spawn eggs stay.
 */
public class DedMorozEntity extends WanderingTrader {
	public DedMorozEntity(EntityType<? extends WanderingTrader> type, Level level) {
		super(type, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
		this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 0.5, 0.5));
		this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, AbstractIllager.class, 12.0F, 0.5, 0.5));
		this.goalSelector.addGoal(1, new PanicGoal(this, 0.5));
		this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
		this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35));
		this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35));
		this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
	}

	@Override
	protected void updateTrades(ServerLevel level) {
		DedMorozTrades.addRandom(level, this.getOffers(), DedMorozTrades.COMMON, 6);
		DedMorozTrades.addRandom(level, this.getOffers(), DedMorozTrades.RARE, 2);
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason,
		@Nullable SpawnGroupData groupData) {
		if (spawnReason == EntitySpawnReason.NATURAL || spawnReason == EntitySpawnReason.CHUNK_GENERATION) {
			this.setDespawnDelay(48000);
		}
		this.setPersistenceRequired();
		return super.finalizeSpawn(level, difficulty, spawnReason, groupData);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide() && this.random.nextInt(8) == 0) {
			this.level().addParticle(ParticleTypes.SNOWFLAKE, this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), 0.0, -0.02, 0.0);
		}
	}
}
