package ru.winter.mechanic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
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
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> STATES.remove(handler.getPlayer().getUuid()));
	}

	private static void tick(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			tickPlayer(player);
		}
	}

	private static void tickPlayer(ServerPlayerEntity player) {
		GameRules rules = player.getServerWorld().getGameRules();
		if (!rules.getBoolean(ModGameRules.FREEZING) || player.isCreative() || player.isSpectator() || !player.isAlive()) {
			return;
		}
		State state = STATES.computeIfAbsent(player.getUuid(), uuid -> new State());
		if ((player.age + player.getId()) % 20 == 0 || state.needsUpdate) {
			state.update(player);
		}

		int frozen = player.getFrozenTicks();
		if (state.warm) {
			if (frozen > 0) {
				player.setFrozenTicks(Math.max(0, frozen - HEAT_THAW_PER_TICK));
			}
			return;
		}
		if (!state.exposed) {
			return;
		}

		boolean damageEnabled = rules.getBoolean(ModGameRules.FREEZING_DAMAGE);
		// Sitting exactly at the freeze threshold after vanilla's -2 per tick means "fully frozen".
		int cap = damageEnabled ? player.getMinFreezeDamageTicks() + 2 : player.getMinFreezeDamageTicks() - 1;
		// Cancel vanilla thawing (-2 per tick), except in powder snow where vanilla is already freezing us.
		int keep = player.inPowderSnow ? 0 : 2;
		state.accumulator += state.rate;
		int add = (int) state.accumulator;
		state.accumulator -= add;
		int next = Math.max(frozen, Math.min(frozen + keep + add, cap));
		if (next != frozen) {
			player.setFrozenTicks(next);
		}

		// Vanilla only hurts entities that "can freeze"; a single leather piece disables that,
		// so partially dressed players are hurt here instead.
		if (damageEnabled && player.age % 40 == 0 && player.isFrozen() && !player.canFreeze()) {
			player.damage(player.getDamageSources().freeze(), 1.0F);
		}
	}

	/** Called when something important changes (e.g. armor), to refresh the cached exposure next tick. */
	public static void invalidate(ServerPlayerEntity player) {
		State state = STATES.get(player.getUuid());
		if (state != null) {
			state.needsUpdate = true;
		}
	}

	private static final class State {
		boolean exposed;
		boolean warm;
		boolean needsUpdate = true;
		float rate;
		float accumulator;

		void update(ServerPlayerEntity player) {
			this.needsUpdate = false;
			this.warm = player.hasStatusEffect(ModEffects.WARMTH) || ColdExposure.isNearHeat(player.getWorld(), player.getBlockPos());
			float severity = ColdExposure.severity(player);
			float protection = ColdExposure.insulation(player) / (float) ColdExposure.MAX_INSULATION;
			this.rate = severity * (1.0F - protection) * BASE_RATE;
			this.exposed = !this.warm && this.rate > 0.001F;
		}
	}
}
