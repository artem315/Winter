package ru.winter.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import ru.winter.registry.ModTags;

/**
 * A wide snow shovel: digging snow clears a 3x3 area around the block at once. Sneak to dig a single block.
 */
public class SnowShovelItem extends ShovelItem {
	public SnowShovelItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties properties) {
		super(material, attackDamage, attackSpeed, properties);
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
		boolean result = super.mineBlock(stack, level, state, pos, owner);
		if (!(level instanceof ServerLevel serverLevel) || !state.is(ModTags.Blocks.SNOW_SHOVEL_CLEARABLE) || owner.isShiftKeyDown()) {
			return result;
		}
		for (BlockPos other : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))) {
			if (other.equals(pos) || stack.isEmpty()) {
				continue;
			}
			BlockState otherState = level.getBlockState(other);
			if (!otherState.is(ModTags.Blocks.SNOW_SHOVEL_CLEARABLE)) {
				continue;
			}
			if (owner instanceof Player player && !level.mayInteract(player, other)) {
				continue;
			}
			BlockPos immutable = other.immutable();
			if (!(owner instanceof Player player && player.isCreative())) {
				Block.dropResources(otherState, serverLevel, immutable, null, owner, stack);
			}
			level.destroyBlock(immutable, false, owner);
			stack.hurtAndBreak(1, owner, EquipmentSlot.MAINHAND);
		}
		return result;
	}
}
