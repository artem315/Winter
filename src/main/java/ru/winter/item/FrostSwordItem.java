package ru.winter.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;

/** A sword of frost crystal that freezes and slows its victims. */
public class FrostSwordItem extends SwordItem {
	public FrostSwordItem(ToolMaterial material, Settings settings) {
		super(material, settings);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		FrostUtil.chill(target, 60, 1, 60, attacker);
		return super.postHit(stack, target, attacker);
	}
}
