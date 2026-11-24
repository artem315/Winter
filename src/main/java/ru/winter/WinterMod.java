package ru.winter;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.winter.mechanic.FreezingHandler;
import ru.winter.mechanic.FrostAspectHandler;
import ru.winter.mechanic.IceSkatesHandler;
import ru.winter.registry.ModBlocks;
import ru.winter.registry.ModEffects;
import ru.winter.registry.ModEntities;
import ru.winter.registry.ModGameRules;
import ru.winter.registry.ModItemGroups;
import ru.winter.registry.ModItems;
import ru.winter.registry.ModLootTables;
import ru.winter.registry.ModWorldGen;

/**
 * Winter — a winter / New Year themed content mod.
 */
public class WinterMod implements ModInitializer {
	public static final String MOD_ID = "winter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		// Order matters: effects are used by food components, blocks/entities by items.
		ModEffects.init();
		ModBlocks.init();
		ModEntities.init();
		ModItems.init();
		ModItemGroups.init();
		ModGameRules.init();
		ModWorldGen.init();
		ModLootTables.init();
		FreezingHandler.init();
		IceSkatesHandler.init();
		FrostAspectHandler.init();
		LOGGER.info("Winter has come: {} blocks and {} items registered", ModBlocks.ALL.size(), ModItems.ALL.size());
	}
}
