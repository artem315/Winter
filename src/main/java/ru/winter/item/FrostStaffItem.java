package ru.winter.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import ru.winter.entity.IceBallEntity;
import ru.winter.registry.ModItems;

/**
 * Ded Moroz's magic staff. Fires frost bolts that fly straight, freeze enemies solid
 * and turn water into ice on impact.
 */
public class FrostStaffItem extends Item {
	public FrostStaffItem(Settings settings) {
		super(settings.rarity(Rarity.EPIC));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.PLAYERS, 0.8F, 1.6F);
		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0F, 0.6F);
		if (!world.isClient) {
			IceBallEntity bolt = new IceBallEntity(world, user);
			bolt.setItem(new ItemStack(ModItems.FROST_CRYSTAL));
			bolt.setPowered(true);
			bolt.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 2.0F, 0.3F);
			world.spawnEntity(bolt);
			stack.damage(1, user, LivingEntity.getSlotForHand(hand));
		}
		user.getItemCooldownManager().set(this, 15);
		user.incrementStat(Stats.USED.getOrCreateStat(this));
		return TypedActionResult.success(stack, world.isClient());
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}
}
