package ru.winter.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A brick fireplace ("камин"). Light it with flint and steel or a fire charge, put it out with a shovel.
 * A lit fireplace is a strong heat source for the freezing mechanic.
 */
public class FireplaceBlock extends HorizontalDirectionalBlock {
	public static final MapCodec<FireplaceBlock> CODEC = simpleCodec(FireplaceBlock::new);
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public FireplaceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hitResult) {
		boolean lit = state.getValue(LIT);
		if (!lit && (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE))) {
			boolean flint = stack.is(Items.FLINT_AND_STEEL);
			level.playSound(player, pos, flint ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRECHARGE_USE,
				SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
			level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL_IMMEDIATE);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			if (flint) {
				stack.hurtAndBreak(1, player, hand);
			} else {
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (lit && stack.is(ItemTags.SHOVELS)) {
			level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.0F);
			level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL_IMMEDIATE);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			stack.hurtAndBreak(1, player, hand);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) {
			return;
		}
		Direction facing = state.getValue(FACING);
		double x = pos.getX() + 0.5 + facing.getStepX() * 0.3;
		double y = pos.getY() + 0.2;
		double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.3;
		double spread = (random.nextDouble() - 0.5) * 0.4;
		double sx = facing.getAxis() == Direction.Axis.Z ? spread : 0.0;
		double sz = facing.getAxis() == Direction.Axis.X ? spread : 0.0;

		level.addParticle(ParticleTypes.FLAME, x + sx, y + random.nextDouble() * 0.2, z + sz, 0.0, 0.01, 0.0);
		if (random.nextInt(3) == 0) {
			level.addParticle(ParticleTypes.SMOKE, x + sx, y + 0.3, z + sz, 0.0, 0.02, 0.0);
		}
		if (random.nextInt(8) == 0) {
			level.playLocalSound(x, y, z, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
				0.6F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
		}
		// chimney smoke if nothing is on top
		if (random.nextInt(4) == 0 && level.getBlockState(pos.above()).isAir()) {
			level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true,
				pos.getX() + 0.5 + random.nextDouble() / 4.0 * (random.nextBoolean() ? 1 : -1),
				pos.getY() + 1.1,
				pos.getZ() + 0.5 + random.nextDouble() / 4.0 * (random.nextBoolean() ? 1 : -1),
				0.0, 0.05, 0.0);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}
}
