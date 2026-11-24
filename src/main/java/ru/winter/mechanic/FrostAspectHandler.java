package ru.winter.mechanic;

import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import ru.winter.item.FrostUtil;
import ru.winter.registry.ModEnchantments;

/** "Ледяное касание" (Frost Aspect): direct melee hits freeze and slow the victim. */
public final class FrostAspectHandler {
	private FrostAspectHandler() {
	}

	public static void init() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (blocked || !source.isDirect() || !(source.getEntity() instanceof LivingEntity attacker)) {
				return;
			}
			Optional<Holder.Reference<Enchantment>> frostAspect =
				entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(ModEnchantments.FROST_ASPECT);
			if (frostAspect.isEmpty()) {
				return;
			}
			int level = EnchantmentHelper.getItemEnchantmentLevel(frostAspect.get(), attacker.getMainHandItem());
			if (level > 0) {
				FrostUtil.chill(entity, 40 * level, level - 1, 40 + 20 * level, attacker);
			}
		});
	}
}
