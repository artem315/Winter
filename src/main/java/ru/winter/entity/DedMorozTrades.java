package ru.winter.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;
import ru.winter.registry.ModBlocks;
import ru.winter.registry.ModEnchantments;
import ru.winter.registry.ModItems;

/** Trade pools of Ded Moroz. */
public final class DedMorozTrades {
	@FunctionalInterface
	public interface Trade {
		@Nullable MerchantOffer create(ServerLevel level, RandomSource random);
	}

	public static final List<Trade> COMMON = List.of(
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
		(level, random) -> new MerchantOffer(new ItemCost(Items.SNOWBALL, 16), new ItemStack(Items.EMERALD), 16, 1, 0.05F)
	);

	public static final List<Trade> RARE = List.of(
		(level, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 32), Optional.of(new ItemCost(ModItems.FROST_CRYSTAL, 4)),
			new ItemStack(ModItems.FROST_STAFF), 1, 10, 0.05F),
		sell(10, ModItems.ICE_SKATES, 1, 3),
		sell(6, ModItems.THERMOMETER, 1, 3),
		sell(8, ModItems.SNOW_SHOVEL, 1, 3),
		book(ModEnchantments.INSULATION, 2, 18),
		book(ModEnchantments.FROST_ASPECT, 2, 22)
	);

	private DedMorozTrades() {
	}

	/** Adds {@code count} different random trades of the pool. */
	public static void addRandom(ServerLevel level, MerchantOffers offers, List<Trade> pool, int count) {
		List<Trade> remaining = new ArrayList<>(pool);
		RandomSource random = level.getRandom();
		while (count > 0 && !remaining.isEmpty()) {
			MerchantOffer offer = remaining.remove(random.nextInt(remaining.size())).create(level, random);
			if (offer != null) {
				offers.add(offer);
				count--;
			}
		}
	}

	private static Trade sell(int emeralds, ItemLike item, int count, int maxUses) {
		return (level, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), new ItemStack(item, count), maxUses, 1, 0.05F);
	}

	private static Trade book(ResourceKey<Enchantment> key, int enchantmentLevel, int emeralds) {
		return (level, random) -> {
			Optional<Holder.Reference<Enchantment>> enchantment = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key);
			if (enchantment.isEmpty()) {
				return null;
			}
			ItemStack book = EnchantmentHelper.createBook(new EnchantmentInstance(enchantment.get(), enchantmentLevel));
			return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), Optional.of(new ItemCost(Items.BOOK)), book, 2, 5, 0.05F);
		};
	}
}
