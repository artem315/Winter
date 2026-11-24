package ru.winter.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.winter.entity.IceBallEntity;
import ru.winter.registry.ModItems;

/**
 * Ded Moroz's magic staff. Fires frost bolts that fly straight, freeze enemies solid
 * and turn water into ice on impact.
 */
public class FrostStaffItem extends Item {
	public FrostStaffItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.8F, 1.6F);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.6F);
		if (level instanceof ServerLevel) {
			IceBallEntity bolt = new IceBallEntity(level, player, new ItemStack(ModItems.FROST_CRYSTAL));
			bolt.setPowered(true);
			bolt.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 0.3F);
			level.addFreshEntity(bolt);
			stack.hurtAndBreak(1, player, hand);
		}
		player.getCooldowns().addCooldown(stack, 15);
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return true;
	}
}
