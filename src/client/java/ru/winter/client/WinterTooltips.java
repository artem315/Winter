package ru.winter.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import ru.winter.WinterMod;

/**
 * Adds short descriptions to the mod's items. Any item of the mod gets the lines
 * {@code <translation key>.tooltip.0}, {@code .tooltip.1}, ... if they exist in the language file.
 */
public final class WinterTooltips {
	private static final int MAX_LINES = 4;

	private WinterTooltips() {
	}

	public static void init() {
		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
			if (!WinterMod.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace())) {
				return;
			}
			String base = stack.getItem().getDescriptionId() + ".tooltip.";
			for (int i = 0; i < MAX_LINES; i++) {
				String key = base + i;
				if (!Language.getInstance().has(key)) {
					break;
				}
				lines.add(Math.min(lines.size(), 1 + i), Component.translatable(key).withStyle(ChatFormatting.GRAY));
			}
		});
	}
}
