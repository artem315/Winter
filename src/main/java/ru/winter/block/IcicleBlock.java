package ru.winter.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Icicles hanging from a ceiling (or from other icicles). They melt near bright light or in warm biomes,
 * and are sharp: entities that run into them get hurt a little.
 */
public class IcicleBlock extends Block {
	public static final MapCodec<IcicleBlock> CODEC = simpleCodec(IcicleBlock::new);
	private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 13.0);

	public IcicleBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockState above = level.getBlockState(pos.above());
		return above.is(this) || above.isFaceSturdy(level, pos.above(), Direction.DOWN);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
		BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (direction == Direction.UP && !state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		boolean warmBiome = !level.getBiome(pos).value().coldEnoughToSnow(pos, level.getSeaLevel());
		boolean brightLight = level.getBrightness(LightLayer.BLOCK, pos) > 11;
		if ((brightLight || (warmBiome && level.isBrightOutside())) && random.nextInt(3) == 0) {
			level.destroyBlock(pos, false);
		}
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier,
		boolean isPrecise) {
		if (level instanceof ServerLevel serverLevel && entity instanceof LivingEntity living
			&& entity.getDeltaMovement().horizontalDistanceSqr() > 0.04) {
			living.hurtServer(serverLevel, level.damageSources().stalagmite(), 1.0F);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(12) == 0 && !level.getBlockState(pos.below()).is(this)) {
			level.addParticle(ParticleTypes.DRIPPING_DRIPSTONE_WATER, pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
		}
	}
}
