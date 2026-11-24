package ru.winter.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import ru.winter.item.FrostUtil;

/**
 * The Yeti: a huge furry beast of the snowy mountains. Smashes its target into the air and chills it.
 */
public class YetiEntity extends Monster {
	private int attackTicksLeft;

	public YetiEntity(EntityType<? extends Monster> type, Level level) {
		super(type, level);
		this.xpReward = 15;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 60.0)
			.add(Attributes.MOVEMENT_SPEED, 0.27)
			.add(Attributes.ATTACK_DAMAGE, 8.0)
			.add(Attributes.ATTACK_KNOCKBACK, 0.5)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
			.add(Attributes.FOLLOW_RANGE, 32.0)
			.add(Attributes.ARMOR, 4.0)
			.add(Attributes.STEP_HEIGHT, 1.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, true));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SnowGolem.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.attackTicksLeft > 0) {
			this.attackTicksLeft--;
		}
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		this.attackTicksLeft = 10;
		level.broadcastEntityEvent(this, (byte) 4);
		boolean hit = super.doHurtTarget(level, target);
		if (hit) {
			target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.45, 0.0));
			target.hurtMarked = true;
			if (target instanceof LivingEntity living) {
				FrostUtil.chill(living, 40, 1, 60, this);
			}
		}
		this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 0.8F);
		return hit;
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 4) {
			this.attackTicksLeft = 10;
		} else {
			super.handleEntityEvent(id);
		}
	}

	/** Remaining ticks of the two-armed smash animation (synced to the client by entity event 4). */
	public int getAttackTicksLeft() {
		return this.attackTicksLeft;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.POLAR_BEAR_WARNING;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.POLAR_BEAR_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.POLAR_BEAR_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(SoundEvents.POLAR_BEAR_STEP, 0.4F, 0.8F);
	}

	@Override
	public float getVoicePitch() {
		return 0.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 200;
	}
}
