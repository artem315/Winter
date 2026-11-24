package ru.winter.mechanic;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import ru.winter.registry.ModEffects;
import ru.winter.registry.ModEnchantments;
import ru.winter.registry.ModTags;

/**
 * Shared rules of the cold: where it is cold, how cold it is and how well a player is dressed.
 */
public final class ColdExposure {
	/** Full protection: 4 pieces of warm clothing (2 points each). */
	public static final int MAX_INSULATION = 8;
	private static final int HEAT_RADIUS = 3;

	private ColdExposure() {
	}

	/** Same rule vanilla uses for snowfall: biome temperature at this position (with altitude) below 0.15. */
	public static boolean isColdAt(Level level, BlockPos pos) {
		return level.dimensionType().hasSkyLight() && level.getBiome(pos).value().coldEnoughToSnow(pos, level.getSeaLevel());
	}

	/** How strongly the cold bites right now; 0 when it is not cold here at all. */
	public static float severity(Player player) {
		Level level = player.level();
		BlockPos pos = player.blockPosition();
		if (!isColdAt(level, pos)) {
			return 0.0F;
		}
		boolean sheltered = !level.canSeeSky(pos.above());
		float severity = 1.0F;
		if (level.isDarkOutside()) {
			severity += 0.5F;
		}
		if (level.isRaining() && !sheltered) {
			severity += 1.0F; // a blizzard
		}
		if (player.isInWater()) {
			severity += 1.5F; // icy water
		}
		if (sheltered) {
			severity *= 0.5F;
		}
		return severity;
	}

	/** Insulation points of worn armor (0..{@link #MAX_INSULATION}). */
	public static int insulation(LivingEntity entity) {
		Optional<Holder.Reference<Enchantment>> insulationEnchantment =
			entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(ModEnchantments.INSULATION);
		int points = 0;
		for (EquipmentSlot slot : EquipmentSlot.VALUES) {
			if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
				continue;
			}
			ItemStack stack = entity.getItemBySlot(slot);
			if (stack.isEmpty()) {
				continue;
			}
			if (stack.is(ModTags.Items.STRONG_INSULATION)) {
				points += 2;
			} else if (stack.is(ModTags.Items.LIGHT_INSULATION)) {
				points += 1;
			}
			if (insulationEnchantment.isPresent()) {
				points += EnchantmentHelper.getItemEnchantmentLevel(insulationEnchantment.get(), stack);
			}
		}
		return Math.min(points, MAX_INSULATION);
	}

	/** Is there a (lit) heat source such as a campfire or a fireplace within a few blocks? */
	public static boolean isNearHeat(Level level, BlockPos center) {
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-HEAT_RADIUS, -2, -HEAT_RADIUS), center.offset(HEAT_RADIUS, 2, HEAT_RADIUS))) {
			BlockState state = level.getBlockState(pos);
			if (state.is(ModTags.Blocks.HEAT_SOURCES)
				&& (!state.hasProperty(BlockStateProperties.LIT) || state.getValue(BlockStateProperties.LIT))) {
				return true;
			}
		}
		return false;
	}

	/** Air temperature for the thermometer, roughly in degrees Celsius. */
	public static int temperatureCelsius(Level level, BlockPos pos) {
		Biome biome = level.getBiome(pos).value();
		float temperature = biome.getBaseTemperature();
		if (pos.getY() > level.getSeaLevel() + 17) {
			temperature -= (pos.getY() - level.getSeaLevel() - 17) * 0.05F / 40.0F;
		}
		boolean cold = isColdAt(level, pos);
		boolean sky = level.dimensionType().hasSkyLight();
		// keep the number consistent with whether it actually snows here
		if (cold) {
			temperature = Math.min(temperature, 0.14F);
		} else if (sky) {
			temperature = Math.max(temperature, 0.16F);
		}
		float celsius = (temperature - 0.15F) * 25.0F;
		if (sky) {
			if (level.isDarkOutside()) {
				celsius -= 6.0F;
			}
			if (level.isRaining() && level.canSeeSky(pos.above())) {
				celsius -= cold ? 5.0F : 2.0F;
			}
		}
		if (cold) {
			celsius = Math.min(celsius, -1.0F);
		}
		return Math.round(celsius);
	}

	public static Component statusText(Player player) {
		Level level = player.level();
		if (player.hasEffect(ModEffects.WARMTH)) {
			return Component.translatable("winter.cold.status.warmth").withStyle(ChatFormatting.GOLD);
		}
		if (isNearHeat(level, player.blockPosition())) {
			return Component.translatable("winter.cold.status.near_fire").withStyle(ChatFormatting.GOLD);
		}
		if (severity(player) <= 0.0F) {
			return Component.translatable("winter.cold.status.comfortable").withStyle(ChatFormatting.GREEN);
		}
		if (insulation(player) >= MAX_INSULATION) {
			return Component.translatable("winter.cold.status.dressed").withStyle(ChatFormatting.GREEN);
		}
		if (player.isFullyFrozen()) {
			return Component.translatable("winter.cold.status.frozen").withStyle(ChatFormatting.DARK_AQUA);
		}
		return Component.translatable("winter.cold.status.freezing").withStyle(ChatFormatting.AQUA);
	}
}
