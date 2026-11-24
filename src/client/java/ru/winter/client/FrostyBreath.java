package ru.winter.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import ru.winter.mechanic.ColdExposure;

/** Purely cosmetic: players breathe out little clouds of vapour in the cold. */
public final class FrostyBreath {
	private FrostyBreath() {
	}

	public static void tick(ClientLevel level) {
		Minecraft minecraft = Minecraft.getInstance();
		for (Player player : level.players()) {
			if ((player.tickCount + player.getId() * 7) % 50 != 0 || player.isInvisible() || player.isUnderWater()) {
				continue;
			}
			boolean firstPerson = player == minecraft.getCameraEntity() && minecraft.options.getCameraType().isFirstPerson();
			if (firstPerson || !ColdExposure.isColdAt(level, player.blockPosition())) {
				continue;
			}
			Vec3 look = player.getViewVector(1.0F);
			double x = player.getX() + look.x * 0.4;
			double y = player.getEyeY() - 0.15;
			double z = player.getZ() + look.z * 0.4;
			for (int i = 0; i < 3; i++) {
				level.addParticle(ParticleTypes.WHITE_SMOKE, x, y, z, look.x * 0.02, 0.01, look.z * 0.02);
			}
		}
	}
}
