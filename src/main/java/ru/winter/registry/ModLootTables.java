package ru.winter.registry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;

/** Adds New Year treats to vanilla chests of snowy structures. */
public final class ModLootTables {
	private ModLootTables() {
	}

	public static void init() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!source.isBuiltin()) {
				return;
			}
			if (LootTables.IGLOO_CHEST_CHEST.equals(key) || LootTables.VILLAGE_SNOWY_HOUSE_CHEST.equals(key)) {
				tableBuilder.pool(LootPool.builder()
					.rolls(UniformLootNumberProvider.create(1.0F, 3.0F))
					.with(ItemEntry.builder(ModItems.MANDARIN).weight(10)
						.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0F, 4.0F))))
					.with(ItemEntry.builder(ModItems.GINGERBREAD_COOKIE).weight(10)
						.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0F, 4.0F))))
					.with(ItemEntry.builder(ModItems.CANDY_CANE).weight(8)
						.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0F, 3.0F))))
					.with(ItemEntry.builder(ModItems.HOT_CHOCOLATE).weight(5))
					.with(ItemEntry.builder(ModItems.FROST_CRYSTAL).weight(3)
						.apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0F))))
					.with(ItemEntry.builder(ModBlocks.GARLAND).weight(4)
						.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0F, 3.0F))))
					.with(ItemEntry.builder(ModBlocks.RED_GIFT).weight(2))
					.with(ItemEntry.builder(ModItems.VALENKI).weight(2))
					.with(ItemEntry.builder(ModItems.USHANKA).weight(2)));
			}
		});
	}
}
