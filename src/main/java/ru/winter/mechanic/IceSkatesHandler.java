package ru.winter.mechanic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import ru.winter.WinterMod;
import ru.winter.registry.ModItems;
import ru.winter.registry.ModTags;

/** Ice skates make the player much faster on ice. */
public final class IceSkatesHandler {
	private static final Identifier SKATING_SPEED = WinterMod.id("ice_skating");
	private static final EntityAttributeModifier SKATING_MODIFIER =
		new EntityAttributeModifier(SKATING_SPEED, 0.6, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);

	private IceSkatesHandler() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				tickPlayer(player);
			}
		});
	}

	private static void tickPlayer(ServerPlayerEntity player) {
		EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}
		boolean wearing = player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.ICE_SKATES);
		boolean active = speed.hasModifier(SKATING_SPEED);
		boolean skating;
		if (!wearing || player.isSpectator()) {
			skating = false;
		} else if (player.isOnGround()) {
			skating = player.getWorld().getBlockState(player.getVelocityAffectingPos()).isIn(ModTags.Blocks.SKATING_SURFACES);
		} else {
			skating = active; // keep the speed while jumping
		}

		if (skating && !active) {
			speed.addTemporaryModifier(SKATING_MODIFIER);
		} else if (!skating && active) {
			speed.removeModifier(SKATING_SPEED);
		}

		if (skating && player.isOnGround() && player.age % 3 == 0 && moved(player)) {
			player.getServerWorld().spawnParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.05, player.getZ(),
				1, 0.15, 0.0, 0.15, 0.01);
		}
	}

	/** Server-side player velocity is not reliable (movement is client-driven), so use the position delta. */
	private static boolean moved(ServerPlayerEntity player) {
		double dx = player.getX() - player.prevX;
		double dz = player.getZ() - player.prevZ;
		return dx * dx + dz * dz > 0.01;
	}
}
