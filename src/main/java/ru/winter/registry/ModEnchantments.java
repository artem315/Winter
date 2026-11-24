package ru.winter.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import ru.winter.WinterMod;

/** Enchantments are data-driven (see data/winter/enchantment); these are just their keys. */
public final class ModEnchantments {
	/** "Утепление" — armor enchantment, +1 insulation point per level. */
	public static final ResourceKey<Enchantment> INSULATION = ResourceKey.create(Registries.ENCHANTMENT, WinterMod.id("insulation"));
	/** "Ледяное касание" — weapon enchantment, slows and chills the victim. */
	public static final ResourceKey<Enchantment> FROST_ASPECT = ResourceKey.create(Registries.ENCHANTMENT, WinterMod.id("frost_aspect"));

	private ModEnchantments() {
	}
}
