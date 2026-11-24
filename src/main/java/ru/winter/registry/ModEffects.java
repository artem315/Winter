package ru.winter.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import ru.winter.WinterMod;
import ru.winter.effect.WarmthEffect;

public final class ModEffects {
	/** "Тепло" — protects from cold and thaws the entity. */
	public static final Holder<MobEffect> WARMTH =
		Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, WinterMod.id("warmth"), new WarmthEffect());

	private ModEffects() {
	}

	public static void init() {
	}
}
