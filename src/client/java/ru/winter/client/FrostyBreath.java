package ru.winter.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import ru.winter.mechanic.ColdExposure;

/** Purely cosmetic: players breathe out little clouds of vapour in the cold. */
public final class FrostyBreath {
	private FrostyBreath() {
	}

	public static void tick(ClientWorld world) {
		MinecraftClient client = MinecraftClient.getInstance();
		for (AbstractClientPlayerEntity player : world.getPlayers()) {
			if ((player.age + player.getId() * 7) % 50 != 0 || player.isInvisible() || player.isSubmergedInWater()) {
				continue;
			}
			boolean firstPerson = player == client.getCameraEntity() && client.options.getPerspective().isFirstPerson();
			if (firstPerson || !ColdExposure.isColdAt(world, player.getBlockPos())) {
				continue;
			}
			Vec3d look = player.getRotationVec(1.0F);
			double x = player.getX() + look.x * 0.4;
			double y = player.getEyeY() - 0.15;
			double z = player.getZ() + look.z * 0.4;
			for (int i = 0; i < 3; i++) {
				world.addParticle(ParticleTypes.WHITE_SMOKE, x, y, z, look.x * 0.02, 0.01, look.z * 0.02);
			}
		}
	}
}
