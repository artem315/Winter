package ru.winter.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import ru.winter.item.FrostUtil;
import ru.winter.registry.ModEntities;
import ru.winter.registry.ModItems;

/**
 * An ice ball ("ледышка"). Thrown by hand it deals 3 damage and chills the target.
 * When fired from the frost staff ("powered") it flies straight, hits much harder
 * and freezes water it touches.
 */
public class IceBallEntity extends ThrowableItemProjectile {
	private static final EntityDataAccessor<Boolean> POWERED = SynchedEntityData.defineId(IceBallEntity.class, EntityDataSerializers.BOOLEAN);

	public IceBallEntity(EntityType<? extends IceBallEntity> type, Level level) {
		super(type, level);
	}

	public IceBallEntity(Level level, LivingEntity owner, ItemStack stack) {
		super(ModEntities.ICE_BALL, owner, level, stack);
	}

	public IceBallEntity(Level level, double x, double y, double z, ItemStack stack) {
		super(ModEntities.ICE_BALL, x, y, z, level, stack);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(POWERED, false);
	}

	public boolean isPowered() {
		return this.entityData.get(POWERED);
	}

	public void setPowered(boolean powered) {
		this.entityData.set(POWERED, powered);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.ICE_BALL;
	}

	@Override
	protected double getDefaultGravity() {
		return this.isPowered() ? 0.0 : super.getDefaultGravity();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isRemoved()) {
			return;
		}
		Level level = this.level();
		if (level.isClientSide()) {
			if (this.isPowered()) {
				level.addParticle(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
			}
			return;
		}
		if (this.isPowered()) {
			if (this.tickCount > 100) {
				this.discard();
				return;
			}
			if (level.getFluidState(this.blockPosition()).is(FluidTags.WATER)) {
				this.freezeWater(this.blockPosition());
				this.discard();
			}
		}
	}

	private void freezeWater(BlockPos center) {
		Level level = this.level();
		boolean froze = false;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
			if (pos.distSqr(center) > 6.5) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			if (state.is(Blocks.WATER) && state.getValue(LiquidBlock.LEVEL) == 0 && level.getBlockState(pos.above()).isAir()) {
				level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
				froze = true;
			}
		}
		if (froze && level instanceof ServerLevel serverLevel) {
			level.playSound(null, center, SoundEvents.GLASS_PLACE, SoundSource.NEUTRAL, 1.0F, 0.6F);
			serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				30, 1.5, 0.2, 1.5, 0.02);
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			ItemStack item = this.getItem();
			for (int i = 0; i < 8; i++) {
				if (!item.isEmpty()) {
					this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(item)),
						this.getX(), this.getY(), this.getZ(),
						(this.random.nextDouble() - 0.5) * 0.15, this.random.nextDouble() * 0.15, (this.random.nextDouble() - 0.5) * 0.15);
				}
				this.level().addParticle(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(),
					(this.random.nextDouble() - 0.5) * 0.1, this.random.nextDouble() * 0.1, (this.random.nextDouble() - 0.5) * 0.1);
			}
		} else {
			super.handleEntityEvent(id);
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		Entity target = hitResult.getEntity();
		boolean powered = this.isPowered();
		if (this.level() instanceof ServerLevel serverLevel) {
			target.hurtServer(serverLevel, this.damageSources().thrown(this, this.getOwner()), powered ? 7.0F : 3.0F);
		}
		if (target instanceof LivingEntity living) {
			FrostUtil.chill(living, powered ? 120 : 30, powered ? 2 : 0, powered ? 100 : 40, this.getOwner());
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!this.level().isClientSide()) {
			this.level().broadcastEntityEvent(this, (byte) 3);
			this.playSound(SoundEvents.GLASS_BREAK, 0.4F, 1.6F + this.random.nextFloat() * 0.3F);
			this.discard();
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("Powered", this.isPowered());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setPowered(input.getBooleanOr("Powered", false));
	}
}
