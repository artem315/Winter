package ru.winter.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.winter.item.FrostUtil;
import ru.winter.registry.ModEntities;
import ru.winter.registry.ModItems;

/**
 * An ice ball ("ледышка"). Thrown by hand it deals 3 damage and chills the target.
 * When fired from the frost staff ("powered") it flies straight, hits much harder
 * and freezes water it touches.
 */
public class IceBallEntity extends ThrownItemEntity {
	private static final TrackedData<Boolean> POWERED = DataTracker.registerData(IceBallEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	public IceBallEntity(EntityType<? extends IceBallEntity> type, World world) {
		super(type, world);
	}

	public IceBallEntity(World world, LivingEntity owner) {
		super(ModEntities.ICE_BALL, owner, world);
	}

	public IceBallEntity(World world, double x, double y, double z) {
		super(ModEntities.ICE_BALL, x, y, z, world);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(POWERED, false);
	}

	public boolean isPowered() {
		return this.dataTracker.get(POWERED);
	}

	public void setPowered(boolean powered) {
		this.dataTracker.set(POWERED, powered);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.ICE_BALL;
	}

	@Override
	protected double getGravity() {
		return this.isPowered() ? 0.0 : super.getGravity();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isRemoved()) {
			return;
		}
		World world = this.getWorld();
		if (world.isClient) {
			if (this.isPowered()) {
				world.addParticle(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
			}
			return;
		}
		if (this.isPowered()) {
			if (this.age > 100) {
				this.discard();
				return;
			}
			if (world.getFluidState(this.getBlockPos()).isIn(FluidTags.WATER)) {
				this.freezeWater(this.getBlockPos());
				this.discard();
			}
		}
	}

	private void freezeWater(BlockPos center) {
		World world = this.getWorld();
		boolean froze = false;
		for (BlockPos pos : BlockPos.iterate(center.add(-2, -1, -2), center.add(2, 1, 2))) {
			if (pos.getSquaredDistance(center) > 6.5) {
				continue;
			}
			BlockState state = world.getBlockState(pos);
			if (state.isOf(Blocks.WATER) && state.get(FluidBlock.LEVEL) == 0 && world.getBlockState(pos.up()).isAir()) {
				world.setBlockState(pos, Blocks.ICE.getDefaultState());
				froze = true;
			}
		}
		if (froze) {
			world.playSound(null, center, SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.NEUTRAL, 1.0F, 0.6F);
			if (world instanceof ServerWorld serverWorld) {
				serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
					30, 1.5, 0.2, 1.5, 0.02);
			}
		}
	}

	@Override
	public void handleStatus(byte status) {
		if (status == 3) {
			ItemStackParticleEffect particle = new ItemStackParticleEffect(ParticleTypes.ITEM, this.getStack());
			for (int i = 0; i < 8; i++) {
				this.getWorld().addParticle(particle, this.getX(), this.getY(), this.getZ(),
					(this.random.nextDouble() - 0.5) * 0.15, this.random.nextDouble() * 0.15, (this.random.nextDouble() - 0.5) * 0.15);
				this.getWorld().addParticle(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(),
					(this.random.nextDouble() - 0.5) * 0.1, this.random.nextDouble() * 0.1, (this.random.nextDouble() - 0.5) * 0.1);
			}
		} else {
			super.handleStatus(status);
		}
	}

	@Override
	protected void onEntityHit(EntityHitResult hit) {
		super.onEntityHit(hit);
		Entity target = hit.getEntity();
		boolean powered = this.isPowered();
		target.damage(this.getDamageSources().thrown(this, this.getOwner()), powered ? 7.0F : 3.0F);
		if (target instanceof LivingEntity living) {
			FrostUtil.chill(living, powered ? 120 : 30, powered ? 2 : 0, powered ? 100 : 40, this.getOwner());
		}
	}

	@Override
	protected void onCollision(HitResult hit) {
		super.onCollision(hit);
		if (!this.getWorld().isClient) {
			this.getWorld().sendEntityStatus(this, (byte) 3);
			this.playSound(SoundEvents.BLOCK_GLASS_BREAK, 0.4F, 1.6F + this.random.nextFloat() * 0.3F);
			this.discard();
		}
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("Powered", this.isPowered());
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		this.setPowered(nbt.getBoolean("Powered"));
	}
}
