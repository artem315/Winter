package ru.winter.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.winter.item.FrostUtil;

/**
 * The Yeti: a huge furry beast of the snowy mountains. Smashes its target into the air and chills it.
 */
public class YetiEntity extends HostileEntity {
	private int attackTicksLeft;

	public YetiEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 15;
	}

	public static DefaultAttributeContainer.Builder createYetiAttributes() {
		return HostileEntity.createHostileAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
			.add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.5)
			.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
			.add(EntityAttributes.GENERIC_ARMOR, 4.0)
			.add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(2, new MeleeAttackGoal(this, 1.15, true));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 10.0F));
		this.goalSelector.add(7, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, SnowGolemEntity.class, true));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (this.attackTicksLeft > 0) {
			this.attackTicksLeft--;
		}
	}

	@Override
	public boolean tryAttack(Entity target) {
		this.attackTicksLeft = 10;
		this.getWorld().sendEntityStatus(this, (byte) 4);
		boolean hit = super.tryAttack(target);
		if (hit) {
			target.setVelocity(target.getVelocity().add(0.0, 0.45, 0.0));
			target.velocityModified = true;
			if (target instanceof LivingEntity living) {
				FrostUtil.chill(living, 40, 1, 60, this);
			}
		}
		this.playSound(SoundEvents.ENTITY_RAVAGER_ATTACK, 1.0F, 0.8F);
		return hit;
	}

	@Override
	public void handleStatus(byte status) {
		if (status == 4) {
			this.attackTicksLeft = 10;
		} else {
			super.handleStatus(status);
		}
	}

	public int getAttackTicksLeft() {
		return this.attackTicksLeft;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_POLAR_BEAR_WARNING;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_POLAR_BEAR_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_POLAR_BEAR_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(SoundEvents.ENTITY_POLAR_BEAR_STEP, 0.4F, 0.8F);
	}

	@Override
	public float getSoundPitch() {
		return 0.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F;
	}

	@Override
	public int getMinAmbientSoundDelay() {
		return 200;
	}
}
