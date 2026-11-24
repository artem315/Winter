package ru.winter.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public final class FrostUtil {
	private FrostUtil() {
	}

	/**
	 * Chills a living entity: it becomes fully frozen for {@code extraFrozenTicks} ticks
	 * (vanilla freezing visuals + damage) and gets Slowness.
	 */
	public static void chill(LivingEntity target, int extraFrozenTicks, int slownessAmplifier, int slownessTicks, @Nullable Entity source) {
		if (target.getWorld().isClient) {
			return;
		}
		if (target.canFreeze()) {
			int frozen = Math.max(target.getFrozenTicks(), target.getMinFreezeDamageTicks());
			target.setFrozenTicks(Math.min(frozen + extraFrozenTicks, target.getMinFreezeDamageTicks() + 300));
		}
		target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, slownessTicks, slownessAmplifier), source);
		if (target.getWorld() instanceof ServerWorld world) {
			world.spawnParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getBodyY(0.5), target.getZ(),
				14, target.getWidth() * 0.4, target.getHeight() * 0.3, target.getWidth() * 0.4, 0.02);
		}
	}
}
