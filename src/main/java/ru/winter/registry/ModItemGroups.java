package ru.winter.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import ru.winter.WinterMod;

public final class ModItemGroups {
	public static final ItemGroup WINTER = Registry.register(Registries.ITEM_GROUP, WinterMod.id("winter"),
		FabricItemGroup.builder()
			.icon(() -> new ItemStack(ModBlocks.RED_GIFT))
			.displayName(Text.translatable("itemGroup.winter"))
			.entries((context, entries) -> {
				for (Item item : ModItems.ALL) {
					entries.add(item);
				}
				for (Block block : ModBlocks.ALL) {
					entries.add(block);
				}
			})
			.build());

	private ModItemGroups() {
	}

	public static void init() {
	}
}
