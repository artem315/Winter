package ru.winter.mechanic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;
import ru.winter.registry.ModEffects;
import ru.winter.registry.ModGameRules;

/**
 * Players slowly freeze in cold (snowy) biomes, using the vanilla "frozen ticks" mechanic
 * (frost overlay, slowness, freeze damage). Warm clothing, the Warmth effect and heat sources protect.
 */
public final class FreezingHandler {
	/** Frozen ticks gained per game tick at severity 1 without any clothing (~45 s to freeze fully). */
	private static final float BASE_RATE = 0.15F;
	/** Thawing near a heat source, in addition to vanilla thawing. */
	private static final int HEAT_THAW_PER_TICK = 3;

	private static final Map<UUID, State> STATES = new HashMap<>();

	private FreezingHandler() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(FreezingHandler::tick);
		ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> STATES.remove(listener.getPlayer().getUUID()));
	}

	private static void tick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tickPlayer(player);
		}
	}

	private static void tickPlayer(ServerPlayer player) {
		ServerLevel level = player.level();
		GameRules rules = level.getGameRules();
		if (!rules.get(ModGameRules.FREEZING) || player.isCreative() || player.isSpectator() || !player.isAlive()) {
			return;
		}
		State state = STATES.computeIfAbsent(player.getUUID(), uuid -> new State());
		if ((player.tickCount + player.getId()) % 20 == 0 || state.needsUpdate) {
			state.update(player);
		}

		int frozen = player.getTicksFrozen();
		if (state.warm) {
			if (frozen > 0) {
				player.setTicksFrozen(Math.max(0, frozen - HEAT_THAW_PER_TICK));
			}
			return;
		}
		if (!state.exposed) {
			return;
		}

		boolean damageEnabled = rules.get(ModGameRules.FREEZING_DAMAGE);
		// Sitting exactly at the freeze threshold after vanilla's -2 per tick means "fully frozen".
		int cap = damageEnabled ? player.getTicksRequiredToFreeze() + 2 : player.getTicksRequiredToFreeze() - 1;
		// Cancel vanilla thawing (-2 per tick), except in powder snow where vanilla is already freezing us.
		int keep = player.isInPowderSnow ? 0 : 2;
		state.accumulator += state.rate;
		int add = (int) state.accumulator;
		state.accumulator -= add;
		int next = Math.max(frozen, Math.min(frozen + keep + add, cap));
		if (next != frozen) {
			player.setTicksFrozen(next);
		}

		// Vanilla only hurts entities that "can freeze"; a single leather piece disables that,
		// so partially dressed players are hurt here instead.
		if (damageEnabled && player.tickCount % 40 == 0 && player.isFullyFrozen() && !player.canFreeze()) {
			player.hurtServer(level, player.damageSources().freeze(), 1.0F);
		}
	}

	private static final class State {
		boolean exposed;
		boolean warm;
		boolean needsUpdate = true;
		float rate;
		float accumulator;

		void update(ServerPlayer player) {
			this.needsUpdate = false;
			this.warm = player.hasEffect(ModEffects.WARMTH) || ColdExposure.isNearHeat(player.level(), player.blockPosition());
			float severity = ColdExposure.severity(player);
			float protection = ColdExposure.insulation(player) / (float) ColdExposure.MAX_INSULATION;
			this.rate = severity * (1.0F - protection) * BASE_RATE;
			this.exposed = !this.warm && this.rate > 0.001F;
		}
	}
}
