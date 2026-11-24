package ru.winter.registry;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import ru.winter.WinterMod;

public final class ModItemGroups {
	public static final CreativeModeTab WINTER = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, WinterMod.id("winter"),
		FabricCreativeModeTab.builder()
			.icon(() -> new ItemStack(ModBlocks.RED_GIFT))
			.title(Component.translatable("itemGroup.winter"))
			.displayItems((parameters, output) -> {
				for (Item item : ModItems.ALL) {
					output.accept(item);
				}
				for (Block block : ModBlocks.ALL) {
					output.accept(block);
				}
			})
			.build());

	private ModItemGroups() {
	}

	public static void init() {
	}
}
