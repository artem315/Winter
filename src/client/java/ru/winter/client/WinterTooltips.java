package ru.winter.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			if (!WinterMod.MOD_ID.equals(Registries.ITEM.getId(stack.getItem()).getNamespace())) {
				return;
			}
			String base = stack.getItem().getTranslationKey() + ".tooltip.";
			for (int i = 0; i < MAX_LINES; i++) {
				String key = base + i;
				if (!I18n.hasTranslation(key)) {
					break;
				}
				lines.add(Math.min(lines.size(), 1 + i), Text.translatable(key).formatted(Formatting.GRAY));
			}
		});
	}
}
