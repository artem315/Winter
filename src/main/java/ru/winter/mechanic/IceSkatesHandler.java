package ru.winter.mechanic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import ru.winter.WinterMod;
import ru.winter.registry.ModItems;
import ru.winter.registry.ModTags;

/** Ice skates make the player much faster on ice. */
public final class IceSkatesHandler {
	private static final Identifier SKATING_SPEED = WinterMod.id("ice_skating");
	private static final AttributeModifier SKATING_MODIFIER =
		new AttributeModifier(SKATING_SPEED, 0.6, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

	private IceSkatesHandler() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickPlayer(player);
			}
		});
	}

	private static void tickPlayer(ServerPlayer player) {
		AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}
		boolean wearing = player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ICE_SKATES);
		boolean active = speed.hasModifier(SKATING_SPEED);
		boolean skating;
		if (!wearing || player.isSpectator()) {
			skating = false;
		} else if (player.onGround()) {
			skating = player.level().getBlockState(player.getBlockPosBelowThatAffectsMyMovement()).is(ModTags.Blocks.SKATING_SURFACES);
		} else {
			skating = active; // keep the speed while jumping
		}

		if (skating && !active) {
			speed.addTransientModifier(SKATING_MODIFIER);
		} else if (!skating && active) {
			speed.removeModifier(SKATING_SPEED);
		}

		if (skating && player.onGround() && player.tickCount % 3 == 0 && moved(player)) {
			player.level().sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.05, player.getZ(), 1, 0.15, 0.0, 0.15, 0.01);
		}
	}

	/** Server-side player velocity is not reliable (movement is client-driven), so use the position delta. */
	private static boolean moved(ServerPlayer player) {
		double dx = player.getX() - player.xo;
		double dz = player.getZ() - player.zo;
		return dx * dx + dz * dz > 0.01;
	}
}
