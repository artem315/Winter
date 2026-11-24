package ru.winter.registry;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import ru.winter.WinterMod;
import ru.winter.effect.WarmthEffect;

public final class ModEffects {
	/** "Тепло" — protects from cold and thaws the entity. */
	public static final RegistryEntry<StatusEffect> WARMTH =
		Registry.registerReference(Registries.STATUS_EFFECT, WinterMod.id("warmth"), new WarmthEffect());

	private ModEffects() {
	}

	public static void init() {
	}
}
