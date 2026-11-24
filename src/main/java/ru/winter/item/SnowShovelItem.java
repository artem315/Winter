package ru.winter.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.winter.registry.ModTags;

/**
 * A wide snow shovel: digging snow clears a 3x3 area around the block at once. Sneak to dig a single block.
 */
public class SnowShovelItem extends ShovelItem {
	public SnowShovelItem(ToolMaterial material, Settings settings) {
		super(material, settings);
	}

	@Override
	public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
		boolean result = super.postMine(stack, world, state, pos, miner);
		if (world.isClient || !state.isIn(ModTags.Blocks.SNOW_SHOVEL_CLEARABLE) || miner.isSneaking()) {
			return result;
		}
		for (BlockPos other : BlockPos.iterate(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
			if (other.equals(pos) || stack.isEmpty()) {
				continue;
			}
			BlockState otherState = world.getBlockState(other);
			if (!otherState.isIn(ModTags.Blocks.SNOW_SHOVEL_CLEARABLE)) {
				continue;
			}
			if (miner instanceof PlayerEntity player && !world.canPlayerModifyAt(player, other)) {
				continue;
			}
			BlockPos immutable = other.toImmutable();
			if (!(miner instanceof PlayerEntity player && player.isCreative())) {
				Block.dropStacks(otherState, world, immutable, null, miner, stack);
			}
			world.breakBlock(immutable, false, miner);
			stack.damage(1, miner, EquipmentSlot.MAINHAND);
		}
		return result;
	}
}
