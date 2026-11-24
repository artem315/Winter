package ru.winter.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.UseAction;

/**
 * Food or drink that comes in a container (bottle/bowl). The container itself is returned through
 * the food component's {@code usingConvertsTo}; this class only switches the animation and sounds for drinks.
 */
public class ContainerFoodItem extends Item {
	private final boolean drink;

	public ContainerFoodItem(boolean drink, Settings settings) {
		super(settings);
		this.drink = drink;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return this.drink ? UseAction.DRINK : UseAction.EAT;
	}

	@Override
	public SoundEvent getDrinkSound() {
		return SoundEvents.ENTITY_GENERIC_DRINK;
	}

	@Override
	public SoundEvent getEatSound() {
		return this.drink ? SoundEvents.ENTITY_GENERIC_DRINK : super.getEatSound();
	}
}
