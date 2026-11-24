package ru.winter.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.winter.mechanic.ColdExposure;

/** Shows the current air temperature and how well the player is protected from cold. */
public class ThermometerItem extends Item {
	public ThermometerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			int celsius = ColdExposure.temperatureCelsius(level, player.blockPosition());
			int insulation = ColdExposure.insulation(player);
			ChatFormatting color = celsius <= -15 ? ChatFormatting.DARK_AQUA : celsius <= 0 ? ChatFormatting.AQUA
				: celsius < 25 ? ChatFormatting.GREEN : ChatFormatting.GOLD;
			Component temperature = Component.literal((celsius > 0 ? "+" : "") + celsius + "°C").withStyle(color, ChatFormatting.BOLD);
			Component protection = Component.literal(insulation + "/" + ColdExposure.MAX_INSULATION).withStyle(ChatFormatting.YELLOW);
			player.sendOverlayMessage(Component.translatable("item.winter.thermometer.reading", temperature, protection,
				ColdExposure.statusText(player)));
			level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.PLAYERS, 0.6F, 1.8F);
		}
		player.getCooldowns().addCooldown(stack, 10);
		return InteractionResult.SUCCESS;
	}
}
