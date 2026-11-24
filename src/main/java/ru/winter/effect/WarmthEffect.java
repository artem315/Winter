package ru.winter.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Warmth: the entity does not get colder and slowly thaws.
 * The freezing handler additionally treats it as full insulation.
 */
public class WarmthEffect extends MobEffect {
	public WarmthEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF9A3C);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
		return true;
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
		if (mob.getTicksFrozen() > 0) {
			mob.setTicksFrozen(Math.max(0, mob.getTicksFrozen() - (2 + amplification)));
		}
		return true;
	}
}
