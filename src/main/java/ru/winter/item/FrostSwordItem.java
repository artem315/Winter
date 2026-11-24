package ru.winter.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** A sword of frost crystal that freezes and slows its victims. */
public class FrostSwordItem extends Item {
	public FrostSwordItem(Properties properties) {
		super(properties);
	}

	@Override
	public void postHurtEnemy(ItemStack itemStack, LivingEntity mob, LivingEntity attacker) {
		super.postHurtEnemy(itemStack, mob, attacker);
		FrostUtil.chill(mob, 60, 1, 60, attacker);
	}
}
