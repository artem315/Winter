package ru.winter.registry;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public enum ModToolMaterials implements ToolMaterial {
	/** Frost crystal: between iron and diamond, freezes whatever it hits. */
	FROST(BlockTags.INCORRECT_FOR_IRON_TOOL, 1100, 7.0F, 2.5F, 16, () -> Ingredient.ofItems(ModItems.FROST_CRYSTAL));

	private final TagKey<Block> inverseTag;
	private final int durability;
	private final float miningSpeed;
	private final float attackDamage;
	private final int enchantability;
	private final Supplier<Ingredient> repairIngredient;

	ModToolMaterials(TagKey<Block> inverseTag, int durability, float miningSpeed, float attackDamage, int enchantability,
		Supplier<Ingredient> repairIngredient) {
		this.inverseTag = inverseTag;
		this.durability = durability;
		this.miningSpeed = miningSpeed;
		this.attackDamage = attackDamage;
		this.enchantability = enchantability;
		this.repairIngredient = Suppliers.memoize(repairIngredient::get);
	}

	@Override
	public int getDurability() {
		return this.durability;
	}

	@Override
	public float getMiningSpeedMultiplier() {
		return this.miningSpeed;
	}

	@Override
	public float getAttackDamage() {
		return this.attackDamage;
	}

	@Override
	public TagKey<Block> getInverseTag() {
		return this.inverseTag;
	}

	@Override
	public int getEnchantability() {
		return this.enchantability;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return this.repairIngredient.get();
	}
}
