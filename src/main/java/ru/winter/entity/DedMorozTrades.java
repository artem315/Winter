package ru.winter.entity;

import java.util.Optional;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import ru.winter.registry.ModBlocks;
import ru.winter.registry.ModEnchantments;
import ru.winter.registry.ModItems;

/** Trade pools of Ded Moroz. */
public final class DedMorozTrades {
	public static final TradeOffers.Factory[] COMMON = {
		sell(1, ModItems.MANDARIN, 6, 16),
		sell(1, ModItems.CANDY_CANE, 4, 16),
		sell(1, ModItems.GINGERBREAD_COOKIE, 6, 16),
		sell(2, ModItems.HOT_CHOCOLATE, 2, 12),
		sell(2, ModItems.SBITEN, 2, 12),
		sell(3, ModItems.OLIVIER_SALAD, 1, 8),
		sell(2, ModBlocks.RED_GIFT, 1, 6),
		sell(2, ModBlocks.GREEN_GIFT, 1, 6),
		sell(2, ModBlocks.BLUE_GIFT, 1, 6),
		sell(1, ModBlocks.GARLAND, 4, 12),
		sell(1, ModBlocks.RED_BAUBLE, 3, 12),
		sell(1, ModBlocks.GOLD_BAUBLE, 3, 12),
		sell(1, ModBlocks.BLUE_BAUBLE, 3, 12),
		sell(4, ModBlocks.STAR, 1, 4),
		sell(3, ModBlocks.FESTIVE_SPRUCE_SAPLING, 1, 6),
		sell(1, ModItems.ICE_BALL, 8, 16),
		(entity, random) -> new TradeOffer(new TradedItem(Items.SNOWBALL, 16), new ItemStack(Items.EMERALD), 16, 1, 0.05F),
	};

	public static final TradeOffers.Factory[] RARE = {
		(entity, random) -> new TradeOffer(new TradedItem(Items.EMERALD, 32), Optional.of(new TradedItem(ModItems.FROST_CRYSTAL, 4)),
			new ItemStack(ModItems.FROST_STAFF), 1, 10, 0.05F),
		sell(10, ModItems.ICE_SKATES, 1, 3),
		sell(6, ModItems.THERMOMETER, 1, 3),
		sell(8, ModItems.SNOW_SHOVEL, 1, 3),
		book(ModEnchantments.INSULATION, 2, 18),
		book(ModEnchantments.FROST_ASPECT, 2, 22),
	};

	private DedMorozTrades() {
	}

	private static TradeOffers.Factory sell(int emeralds, ItemConvertible item, int count, int maxUses) {
		return (entity, random) -> new TradeOffer(new TradedItem(Items.EMERALD, emeralds), new ItemStack(item, count), maxUses, 1, 0.05F);
	}

	private static TradeOffers.Factory book(RegistryKey<Enchantment> key, int level, int emeralds) {
		return (entity, random) -> {
			Optional<RegistryEntry.Reference<Enchantment>> enchantment =
				entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(key);
			if (enchantment.isEmpty()) {
				return null;
			}
			ItemStack book = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment.get(), level));
			return new TradeOffer(new TradedItem(Items.EMERALD, emeralds), Optional.of(new TradedItem(Items.BOOK)), book, 2, 5, 0.05F);
		};
	}
}
