package ru.winter.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class YetiRenderState extends LivingEntityRenderState {
	/** Remaining ticks of the smash attack animation (with partial ticks). */
	public float attackTicks;
}
