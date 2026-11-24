package ru.winter.registry;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import ru.winter.WinterMod;

/** Enchantments are data-driven (see data/winter/enchantment); these are just their keys. */
public final class ModEnchantments {
	/** "Утепление" — armor enchantment, +1 insulation point per level. */
	public static final RegistryKey<Enchantment> INSULATION = RegistryKey.of(RegistryKeys.ENCHANTMENT, WinterMod.id("insulation"));
	/** "Ледяное касание" — weapon enchantment, slows and chills the victim. */
	public static final RegistryKey<Enchantment> FROST_ASPECT = RegistryKey.of(RegistryKeys.ENCHANTMENT, WinterMod.id("frost_aspect"));

	private ModEnchantments() {
	}
}
