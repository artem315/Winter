package ru.winter.mechanic;

import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import ru.winter.item.FrostUtil;
import ru.winter.registry.ModEnchantments;

/** "Ледяное касание" (Frost Aspect): direct melee hits freeze and slow the victim. */
public final class FrostAspectHandler {
	private FrostAspectHandler() {
	}

	public static void init() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (blocked || !source.isDirect() || !(source.getAttacker() instanceof LivingEntity attacker)) {
				return;
			}
			Optional<RegistryEntry.Reference<Enchantment>> frostAspect =
				entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(ModEnchantments.FROST_ASPECT);
			if (frostAspect.isEmpty()) {
				return;
			}
			int level = EnchantmentHelper.getLevel(frostAspect.get(), attacker.getMainHandStack());
			if (level > 0) {
				FrostUtil.chill(entity, 40 * level, level - 1, 40 + 20 * level, attacker);
			}
		});
	}
}
