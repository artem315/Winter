package ru.winter.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import ru.winter.mechanic.ColdExposure;

/** Shows the current air temperature and how well the player is protected from cold. */
public class ThermometerItem extends Item {
	public ThermometerItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient) {
			int celsius = ColdExposure.temperatureCelsius(world, user.getBlockPos());
			int insulation = ColdExposure.insulation(user);
			Formatting color = celsius <= -15 ? Formatting.DARK_AQUA : celsius <= 0 ? Formatting.AQUA : celsius < 25 ? Formatting.GREEN : Formatting.GOLD;
			Text temperature = Text.literal((celsius > 0 ? "+" : "") + celsius + "°C").formatted(color, Formatting.BOLD);
			Text protection = Text.literal(insulation + "/" + ColdExposure.MAX_INSULATION).formatted(Formatting.YELLOW);
			Text status = ColdExposure.statusText(user);
			user.sendMessage(Text.translatable("item.winter.thermometer.reading", temperature, protection, status), true);
			world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_STEP, SoundCategory.PLAYERS, 0.6F, 1.8F);
		}
		user.getItemCooldownManager().set(this, 10);
		return TypedActionResult.success(stack, world.isClient());
	}
}
