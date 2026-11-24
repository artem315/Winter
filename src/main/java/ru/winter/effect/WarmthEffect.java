package ru.winter.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * Warmth: the entity does not get colder and slowly thaws.
 * The freezing handler additionally treats it as full insulation.
 */
public class WarmthEffect extends StatusEffect {
	public WarmthEffect() {
		super(StatusEffectCategory.BENEFICIAL, 0xFF9A3C);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
		if (!entity.getWorld().isClient && entity.getFrozenTicks() > 0) {
			entity.setFrozenTicks(Math.max(0, entity.getFrozenTicks() - (2 + amplifier)));
		}
		return true;
	}
}
