package ru.winter.mechanic;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
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
	public static boolean isColdAt(World world, BlockPos pos) {
		return world.getDimension().hasSkyLight() && world.getBiome(pos).value().isCold(pos);
	}

	/** How strongly the cold bites right now; 0 when it is not cold here at all. */
	public static float severity(PlayerEntity player) {
		World world = player.getWorld();
		BlockPos pos = player.getBlockPos();
		if (!isColdAt(world, pos)) {
			return 0.0F;
		}
		boolean sheltered = !world.isSkyVisible(pos.up());
		float severity = 1.0F;
		if (world.isNight()) {
			severity += 0.5F;
		}
		if (world.isRaining() && !sheltered) {
			severity += 1.0F; // a blizzard
		}
		if (player.isTouchingWater()) {
			severity += 1.5F; // icy water
		}
		if (sheltered) {
			severity *= 0.5F;
		}
		return severity;
	}

	/** Insulation points of worn armor (0..{@link #MAX_INSULATION}). */
	public static int insulation(LivingEntity entity) {
		Optional<RegistryEntry.Reference<Enchantment>> insulationEnchantment =
			entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(ModEnchantments.INSULATION);
		int points = 0;
		for (ItemStack stack : entity.getArmorItems()) {
			if (stack.isEmpty()) {
				continue;
			}
			if (stack.isIn(ModTags.Items.STRONG_INSULATION)) {
				points += 2;
			} else if (stack.isIn(ModTags.Items.LIGHT_INSULATION)) {
				points += 1;
			}
			if (insulationEnchantment.isPresent()) {
				points += EnchantmentHelper.getLevel(insulationEnchantment.get(), stack);
			}
		}
		return Math.min(points, MAX_INSULATION);
	}

	/** Is there a (lit) heat source such as a campfire or a fireplace within a few blocks? */
	public static boolean isNearHeat(World world, BlockPos center) {
		for (BlockPos pos : BlockPos.iterate(center.add(-HEAT_RADIUS, -2, -HEAT_RADIUS), center.add(HEAT_RADIUS, 2, HEAT_RADIUS))) {
			BlockState state = world.getBlockState(pos);
			if (state.isIn(ModTags.Blocks.HEAT_SOURCES) && (!state.contains(Properties.LIT) || state.get(Properties.LIT))) {
				return true;
			}
		}
		return false;
	}

	/** Air temperature for the thermometer, roughly in degrees Celsius. */
	public static int temperatureCelsius(World world, BlockPos pos) {
		Biome biome = world.getBiome(pos).value();
		float temperature = biome.getTemperature();
		if (pos.getY() > 80) {
			temperature -= (pos.getY() - 80) * 0.05F / 40.0F;
		}
		boolean cold = isColdAt(world, pos);
		// Keep the number consistent with whether it actually snows here.
		if (cold) {
			temperature = Math.min(temperature, 0.14F);
		} else if (world.getDimension().hasSkyLight()) {
			temperature = Math.max(temperature, 0.16F);
		}
		float celsius = (temperature - 0.15F) * 25.0F;
		if (world.getDimension().hasSkyLight()) {
			if (world.isNight()) {
				celsius -= 6.0F;
			}
			if (world.isRaining() && world.isSkyVisible(pos.up())) {
				celsius -= cold ? 5.0F : 2.0F;
			}
		}
		if (cold) {
			celsius = Math.min(celsius, -1.0F);
		}
		return Math.round(celsius);
	}

	public static Text statusText(PlayerEntity player) {
		World world = player.getWorld();
		if (player.hasStatusEffect(ModEffects.WARMTH)) {
			return Text.translatable("winter.cold.status.warmth").formatted(Formatting.GOLD);
		}
		if (isNearHeat(world, player.getBlockPos())) {
			return Text.translatable("winter.cold.status.near_fire").formatted(Formatting.GOLD);
		}
		float severity = severity(player);
		if (severity <= 0.0F) {
			return Text.translatable("winter.cold.status.comfortable").formatted(Formatting.GREEN);
		}
		int insulation = insulation(player);
		if (insulation >= MAX_INSULATION) {
			return Text.translatable("winter.cold.status.dressed").formatted(Formatting.GREEN);
		}
		if (player.isFrozen()) {
			return Text.translatable("winter.cold.status.frozen").formatted(Formatting.DARK_AQUA);
		}
		return Text.translatable("winter.cold.status.freezing").formatted(Formatting.AQUA);
	}
}
