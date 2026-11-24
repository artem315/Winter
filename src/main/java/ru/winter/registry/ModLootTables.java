package ru.winter.registry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/** Adds New Year treats to vanilla chests of snowy structures. */
public final class ModLootTables {
	private ModLootTables() {
	}

	public static void init() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!source.isBuiltin()) {
				return;
			}
			if (BuiltInLootTables.IGLOO_CHEST.equals(key) || BuiltInLootTables.VILLAGE_SNOWY_HOUSE.equals(key)) {
				tableBuilder.withPool(LootPool.lootPool()
					.setRolls(UniformGenerator.between(1.0F, 3.0F))
					.add(LootItem.lootTableItem(ModItems.MANDARIN).setWeight(10)
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
					.add(LootItem.lootTableItem(ModItems.GINGERBREAD_COOKIE).setWeight(10)
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
					.add(LootItem.lootTableItem(ModItems.CANDY_CANE).setWeight(8)
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(ModItems.HOT_CHOCOLATE).setWeight(5))
					.add(LootItem.lootTableItem(ModItems.FROST_CRYSTAL).setWeight(3))
					.add(LootItem.lootTableItem(ModBlocks.GARLAND).setWeight(4)
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(ModBlocks.RED_GIFT).setWeight(2))
					.add(LootItem.lootTableItem(ModItems.VALENKI).setWeight(2))
					.add(LootItem.lootTableItem(ModItems.USHANKA).setWeight(2)));
			}
		});
	}
}
