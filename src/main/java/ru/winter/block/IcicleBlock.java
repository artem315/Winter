package ru.winter.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

/**
 * An icicle hanging from a ceiling (or from another icicle). Melts near bright light or in warm biomes,
 * and is sharp: entities that run into it get hurt a little.
 */
public class IcicleBlock extends Block {
	public static final MapCodec<IcicleBlock> CODEC = createCodec(IcicleBlock::new);
	private static final VoxelShape SHAPE = Block.createCuboidShape(5.0, 2.0, 5.0, 11.0, 16.0, 11.0);

	public IcicleBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockState above = world.getBlockState(pos.up());
		return above.isOf(this) || above.isSideSolidFullSquare(world, pos.up(), Direction.DOWN);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(
		BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
	) {
		if (direction == Direction.UP && !state.canPlaceAt(world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		boolean warmBiome = !world.getBiome(pos).value().isCold(pos);
		boolean brightLight = world.getLightLevel(LightType.BLOCK, pos) > 11;
		if ((brightLight || (warmBiome && world.isDay())) && random.nextInt(3) == 0) {
			world.breakBlock(pos, false);
		}
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (!world.isClient && entity instanceof LivingEntity living && entity.getVelocity().horizontalLengthSquared() > 0.04) {
			living.damage(world.getDamageSources().stalagmite(), 1.0F);
		}
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (random.nextInt(12) == 0 && !world.getBlockState(pos.down()).isOf(this)) {
			world.addParticle(ParticleTypes.DRIPPING_DRIPSTONE_WATER,
				pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
		}
	}
}
