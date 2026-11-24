package ru.winter.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import ru.winter.WinterMod;
import ru.winter.block.FireplaceBlock;
import ru.winter.block.GiftBlock;
import ru.winter.item.FrostUtil;
import ru.winter.mechanic.ColdExposure;
import ru.winter.registry.ModBlocks;
import ru.winter.registry.ModEntities;
import ru.winter.registry.ModItems;

/** Smoke tests run by {@code ./gradlew runGametest}. */
public class WinterGameTests implements FabricGameTest {
	@GameTest(templateName = EMPTY_STRUCTURE)
	public void giftUnwrapsIntoLoot(TestContext context) {
		BlockPos pos = new BlockPos(1, 1, 1);
		context.setBlockState(pos, ModBlocks.RED_GIFT);
		PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
		GiftBlock.unwrap(context.getWorld(), context.getAbsolutePos(pos), player);
		context.expectBlock(Blocks.AIR, pos);
		Box box = new Box(context.getAbsolutePos(pos)).expand(2.0);
		int drops = context.getWorld().getEntitiesByClass(ItemEntity.class, box, e -> true).size();
		context.assertTrue(drops > 0, "Gift should drop loot, got " + drops + " item entities");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void litFireplaceWarms(TestContext context) {
		BlockPos pos = new BlockPos(1, 1, 1);
		BlockPos nearby = context.getAbsolutePos(new BlockPos(2, 1, 2));
		context.setBlockState(pos, ModBlocks.FIREPLACE.getDefaultState());
		context.assertFalse(ColdExposure.isNearHeat(context.getWorld(), nearby), "Unlit fireplace must not warm");
		context.setBlockState(pos, ModBlocks.FIREPLACE.getDefaultState().with(FireplaceBlock.LIT, true));
		context.assertTrue(ColdExposure.isNearHeat(context.getWorld(), nearby), "Lit fireplace must warm");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void winterClothingInsulates(TestContext context) {
		ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		context.assertTrue(ColdExposure.insulation(zombie) == 0, "Naked zombie has no insulation");
		zombie.equipStack(EquipmentSlot.HEAD, new ItemStack(ModItems.USHANKA));
		zombie.equipStack(EquipmentSlot.CHEST, new ItemStack(ModItems.SWEATER));
		zombie.equipStack(EquipmentSlot.LEGS, new ItemStack(ModItems.QUILTED_PANTS));
		zombie.equipStack(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
		int insulation = ColdExposure.insulation(zombie);
		context.assertTrue(insulation == 7, "3 warm pieces + leather boots should give 7 insulation, got " + insulation);
		zombie.equipStack(EquipmentSlot.FEET, new ItemStack(ModItems.VALENKI));
		context.assertTrue(ColdExposure.insulation(zombie) == ColdExposure.MAX_INSULATION, "Full winter set gives full insulation");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void frostFreezesTarget(TestContext context) {
		PigEntity pig = context.spawnEntity(EntityType.PIG, new BlockPos(1, 1, 1));
		FrostUtil.chill(pig, 60, 1, 60, null);
		context.assertTrue(pig.isFrozen(), "Pig should be frozen after a frost hit");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void mobsSpawn(TestContext context) {
		context.spawnEntity(ModEntities.YETI, new BlockPos(1, 1, 1));
		context.spawnEntity(ModEntities.DED_MOROZ, new BlockPos(2, 1, 2));
		context.expectEntity(ModEntities.YETI);
		context.expectEntity(ModEntities.DED_MOROZ);
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void allBlocksHaveLootTables(TestContext context) {
		for (Block block : ModBlocks.ALL) {
			LootTable table = context.getWorld().getServer().getReloadableRegistries().getLootTable(block.getLootTableKey());
			context.assertTrue(table != LootTable.EMPTY, "Missing loot table for " + block);
		}
		context.complete();
	}

	// skyAccess: no barrier ceiling over the 8-block-high test area, the tree is taller than that.
	@GameTest(templateName = EMPTY_STRUCTURE, skyAccess = true)
	public void festiveSpruceGrows(TestContext context) {
		ServerWorld world = context.getWorld();
		BlockPos ground = new BlockPos(3, 0, 3);
		BlockPos saplingPos = ground.up();
		context.setBlockState(ground, Blocks.GRASS_BLOCK);
		BlockState sapling = ModBlocks.FESTIVE_SPRUCE_SAPLING.getDefaultState().with(Properties.STAGE, 1);
		context.setBlockState(saplingPos, sapling);
		BlockPos absolute = context.getAbsolutePos(saplingPos);
		((SaplingBlock) ModBlocks.FESTIVE_SPRUCE_SAPLING).generate(world, absolute, sapling, Random.create(7));
		context.assertTrue(world.getBlockState(absolute).isOf(Blocks.SPRUCE_LOG), "Sapling should grow into a spruce trunk");
		int leaves = 0;
		for (BlockPos pos : BlockPos.iterate(absolute.add(-4, 0, -4), absolute.add(4, 12, 4))) {
			if (world.getBlockState(pos).isOf(ModBlocks.SNOWY_SPRUCE_LEAVES)) {
				leaves++;
			}
		}
		context.assertTrue(leaves > 10, "Festive tree should have snowy leaves, got " + leaves);
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void worldgenFeaturesPlaceBlocks(TestContext context) {
		ServerWorld world = context.getWorld();
		var features = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE);
		var generator = world.getChunkManager().getChunkGenerator();

		// Ore: a solid stone cube with the ore feature in the middle.
		BlockPos center = new BlockPos(3, 4, 3);
		for (BlockPos pos : BlockPos.iterate(center.add(-3, -2, -3), center.add(3, 2, 3))) {
			context.setBlockState(pos, Blocks.STONE);
		}
		ConfiguredFeature<?, ?> ore = features.get(WinterMod.id("frost_crystal_ore"));
		context.assertTrue(ore != null, "Ore feature is registered");
		ore.generate(world, generator, Random.create(42), context.getAbsolutePos(center));
		int ores = 0;
		for (BlockPos pos : BlockPos.iterate(center.add(-3, -2, -3), center.add(3, 2, 3))) {
			if (world.getBlockState(context.getAbsolutePos(pos)).isOf(ModBlocks.FROST_CRYSTAL_ORE)) {
				ores++;
			}
		}
		context.assertTrue(ores > 0, "Ore feature should place frost crystal ore");

		// Icicles hang below the stone cube.
		BlockPos below = center.add(0, -3, 0);
		features.get(WinterMod.id("icicles")).generate(world, generator, Random.create(1), context.getAbsolutePos(below));
		context.assertTrue(world.getBlockState(context.getAbsolutePos(below)).isOf(ModBlocks.ICICLE), "Icicle should be placed");

		// A gift on top of the cube.
		BlockPos top = center.add(0, 3, 0);
		features.get(WinterMod.id("gift")).generate(world, generator, Random.create(3), context.getAbsolutePos(top));
		BlockState gift = world.getBlockState(context.getAbsolutePos(top));
		context.assertTrue(gift.getBlock() instanceof GiftBlock, "Gift should be placed, got " + gift);
		context.complete();
	}
}
