package ru.winter.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShovelItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

/**
 * A brick fireplace ("камин"). Light it with flint and steel or a fire charge, put it out with a shovel.
 * A lit fireplace is a strong heat source for the freezing mechanic.
 */
public class FireplaceBlock extends HorizontalFacingBlock {
	public static final MapCodec<FireplaceBlock> CODEC = createCodec(FireplaceBlock::new);
	public static final BooleanProperty LIT = Properties.LIT;

	public FireplaceBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
	}

	@Override
	protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
		return CODEC;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected ItemActionResult onUseWithItem(
		ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit
	) {
		boolean lit = state.get(LIT);
		if (!lit && (stack.isOf(Items.FLINT_AND_STEEL) || stack.isOf(Items.FIRE_CHARGE))) {
			boolean flint = stack.isOf(Items.FLINT_AND_STEEL);
			world.playSound(player, pos, flint ? SoundEvents.ITEM_FLINTANDSTEEL_USE : SoundEvents.ITEM_FIRECHARGE_USE,
				SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
			world.setBlockState(pos, state.with(LIT, true), Block.NOTIFY_ALL_AND_REDRAW);
			world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			if (flint) {
				stack.damage(1, player, LivingEntity.getSlotForHand(hand));
			} else {
				stack.decrementUnlessCreative(1, player);
			}
			return ItemActionResult.success(world.isClient);
		}
		if (lit && stack.getItem() instanceof ShovelItem) {
			world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.8F, 1.0F);
			world.setBlockState(pos, state.with(LIT, false), Block.NOTIFY_ALL_AND_REDRAW);
			world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			stack.damage(1, player, LivingEntity.getSlotForHand(hand));
			return ItemActionResult.success(world.isClient);
		}
		return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (!state.get(LIT)) {
			return;
		}
		Direction facing = state.get(FACING);
		double x = pos.getX() + 0.5 + facing.getOffsetX() * 0.45;
		double y = pos.getY() + 0.25;
		double z = pos.getZ() + 0.5 + facing.getOffsetZ() * 0.45;
		double spread = (random.nextDouble() - 0.5) * 0.5;
		double sx = facing.getAxis() == Direction.Axis.Z ? spread : 0.0;
		double sz = facing.getAxis() == Direction.Axis.X ? spread : 0.0;

		world.addParticle(ParticleTypes.FLAME, x + sx, y + random.nextDouble() * 0.2, z + sz, 0.0, 0.01, 0.0);
		if (random.nextInt(3) == 0) {
			world.addParticle(ParticleTypes.SMOKE, x + sx, y + 0.3, z + sz, 0.0, 0.02, 0.0);
		}
		if (random.nextInt(8) == 0) {
			world.playSound(x, y, z, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS,
				0.6F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
		}
		// Chimney smoke if nothing is on top.
		if (random.nextInt(4) == 0 && world.getBlockState(pos.up()).isAir()) {
			world.addImportantParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true,
				pos.getX() + 0.5 + random.nextDouble() / 4.0 * (random.nextBoolean() ? 1 : -1),
				pos.getY() + 1.1,
				pos.getZ() + 0.5 + random.nextDouble() / 4.0 * (random.nextBoolean() ? 1 : -1),
				0.0, 0.05, 0.0);
		}
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}
}
