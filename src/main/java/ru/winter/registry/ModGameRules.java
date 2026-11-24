package ru.winter.registry;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import ru.winter.WinterMod;

public final class ModGameRules {
	/** /gamerule winter:freezing — players freeze in cold biomes without warm clothes. */
	public static final GameRule<Boolean> FREEZING =
		GameRuleBuilder.forBoolean(true).category(GameRuleCategory.PLAYER).buildAndRegister(WinterMod.id("freezing"));

	/** /gamerule winter:freezing_damage — whether full freezing actually hurts. */
	public static final GameRule<Boolean> FREEZING_DAMAGE =
		GameRuleBuilder.forBoolean(true).category(GameRuleCategory.PLAYER).buildAndRegister(WinterMod.id("freezing_damage"));

	private ModGameRules() {
	}

	public static void init() {
	}
}
