package ru.winter.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class FrostUtil {
	private FrostUtil() {
	}

	/**
	 * Chills a living entity: it becomes fully frozen for {@code extraFrozenTicks} ticks
	 * (vanilla freezing visuals + damage) and gets Slowness.
	 */
	public static void chill(LivingEntity target, int extraFrozenTicks, int slownessAmplifier, int slownessTicks, @Nullable Entity source) {
		if (!(target.level() instanceof ServerLevel level)) {
			return;
		}
		if (target.canFreeze()) {
			int frozen = Math.max(target.getTicksFrozen(), target.getTicksRequiredToFreeze());
			target.setTicksFrozen(Math.min(frozen + extraFrozenTicks, target.getTicksRequiredToFreeze() + 300));
		}
		target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, slownessTicks, slownessAmplifier), source);
		level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.5), target.getZ(),
			14, target.getBbWidth() * 0.4, target.getBbHeight() * 0.3, target.getBbWidth() * 0.4, 0.02);
	}
}
