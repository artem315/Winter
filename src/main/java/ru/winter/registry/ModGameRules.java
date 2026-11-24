package ru.winter.registry;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public final class ModGameRules {
	/** /gamerule winterFreezing — players freeze in cold biomes without warm clothes. */
	public static final GameRules.Key<GameRules.BooleanRule> FREEZING =
		GameRuleRegistry.register("winterFreezing", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

	/** /gamerule winterFreezingDamage — whether full freezing actually hurts. */
	public static final GameRules.Key<GameRules.BooleanRule> FREEZING_DAMAGE =
		GameRuleRegistry.register("winterFreezingDamage", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

	private ModGameRules() {
	}

	public static void init() {
	}
}
