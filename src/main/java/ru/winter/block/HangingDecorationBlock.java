package ru.winter.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

/**
 * A Christmas tree bauble: hangs below leaves or any block with a small solid bottom.
 */
public class HangingDecorationBlock extends Block {
	public static final MapCodec<HangingDecorationBlock> CODEC = createCodec(HangingDecorationBlock::new);
	private static final VoxelShape SHAPE = VoxelShapes.union(
		Block.createCuboidShape(4.5, 2.5, 4.5, 11.5, 9.5, 11.5),
		Block.createCuboidShape(6.5, 9.0, 6.5, 9.5, 16.0, 9.5)
	);

	public HangingDecorationBlock(Settings settings) {
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
		return Block.sideCoversSmallSquare(world, pos.up(), Direction.DOWN);
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
}
