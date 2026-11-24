package ru.winter.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import ru.winter.WinterMod;
import ru.winter.block.FireplaceBlock;
import ru.winter.block.GiftBlock;
import ru.winter.item.FrostUtil;
import ru.winter.mechanic.ColdExposure;
import ru.winter.registry.ModBlocks;
import ru.winter.registry.ModEntities;
import ru.winter.registry.ModItems;

/** Server game tests, run by {@code ./gradlew runGameTest} and as part of {@code ./gradlew build}. */
public class WinterGameTests {
	@GameTest
	public void giftUnwrapsIntoLoot(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, ModBlocks.RED_GIFT);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		GiftBlock.unwrap(helper.getLevel(), helper.absolutePos(pos), player);
		helper.assertBlockPresent(Blocks.AIR, pos);
		AABB box = new AABB(helper.absolutePos(pos)).inflate(2.0);
		int drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, box, e -> true).size();
		helper.assertTrue(drops > 0, "Gift should drop loot, got " + drops + " item entities");
		helper.succeed();
	}

	@GameTest
	public void litFireplaceWarms(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		BlockPos nearby = helper.absolutePos(new BlockPos(2, 1, 2));
		helper.setBlock(pos, ModBlocks.FIREPLACE.defaultBlockState());
		helper.assertFalse(ColdExposure.isNearHeat(helper.getLevel(), nearby), "Unlit fireplace must not warm");
		helper.setBlock(pos, ModBlocks.FIREPLACE.defaultBlockState().setValue(FireplaceBlock.LIT, true));
		helper.assertTrue(ColdExposure.isNearHeat(helper.getLevel(), nearby), "Lit fireplace must warm");
		helper.succeed();
	}

	@GameTest
	public void winterClothingInsulates(GameTestHelper helper) {
		Zombie zombie = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
		helper.assertTrue(ColdExposure.insulation(zombie) == 0, "Naked zombie has no insulation");
		zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.USHANKA));
		zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.SWEATER));
		zombie.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.QUILTED_PANTS));
		zombie.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
		int insulation = ColdExposure.insulation(zombie);
		helper.assertTrue(insulation == 7, "3 warm pieces + leather boots should give 7 insulation, got " + insulation);
		zombie.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.VALENKI));
		helper.assertTrue(ColdExposure.insulation(zombie) == ColdExposure.MAX_INSULATION, "Full winter set gives full insulation");
		helper.succeed();
	}

	@GameTest
	public void frostFreezesTarget(GameTestHelper helper) {
		Pig pig = helper.spawn(EntityTypes.PIG, new BlockPos(1, 1, 1));
		FrostUtil.chill(pig, 60, 1, 60, null);
		helper.assertTrue(pig.isFullyFrozen(), "Pig should be frozen after a frost hit");
		helper.succeed();
	}

	@GameTest
	public void mobsSpawn(GameTestHelper helper) {
		helper.spawn(ModEntities.YETI, new BlockPos(1, 1, 1));
		helper.spawn(ModEntities.DED_MOROZ, new BlockPos(2, 1, 2));
		helper.assertEntityPresent(ModEntities.YETI);
		helper.assertEntityPresent(ModEntities.DED_MOROZ);
		helper.succeed();
	}

	@GameTest
	public void allBlocksHaveLootTables(GameTestHelper helper) {
		for (Block block : ModBlocks.ALL) {
			LootTable table = block.getLootTable()
				.map(key -> helper.getLevel().getServer().reloadableRegistries().getLootTable(key))
				.orElse(LootTable.EMPTY);
			helper.assertTrue(table != LootTable.EMPTY, "Missing loot table for " + block);
		}
		helper.succeed();
	}

	// skyAccess: no barrier ceiling over the 8-block-high test area, the tree is taller than that.
	@GameTest(skyAccess = true)
	public void festiveSpruceGrows(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos ground = new BlockPos(3, 0, 3);
		BlockPos saplingPos = ground.above();
		helper.setBlock(ground, Blocks.GRASS_BLOCK);
		BlockState sapling = ModBlocks.FESTIVE_SPRUCE_SAPLING.defaultBlockState().setValue(BlockStateProperties.STAGE, 1);
		helper.setBlock(saplingPos, sapling);
		BlockPos absolute = helper.absolutePos(saplingPos);
		((SaplingBlock) ModBlocks.FESTIVE_SPRUCE_SAPLING).advanceTree(level, absolute, sapling, RandomSource.create(7));
		helper.assertTrue(level.getBlockState(absolute).is(Blocks.SPRUCE_LOG), "Sapling should grow into a spruce trunk");
		int leaves = 0;
		for (BlockPos pos : BlockPos.betweenClosed(absolute.offset(-4, 0, -4), absolute.offset(4, 12, 4))) {
			if (level.getBlockState(pos).is(ModBlocks.SNOWY_SPRUCE_LEAVES)) {
				leaves++;
			}
		}
		helper.assertTrue(leaves > 10, "Festive tree should have snowy leaves, got " + leaves);
		helper.succeed();
	}

	@GameTest
	public void worldgenFeaturesPlaceBlocks(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Registry<ConfiguredFeature<?, ?>> features = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE);
		ChunkGenerator generator = level.getChunkSource().getGenerator();

		// ore: a solid stone cube with the ore feature in the middle
		BlockPos center = new BlockPos(3, 4, 3);
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
			helper.setBlock(pos, Blocks.STONE);
		}
		features.getValue(WinterMod.id("frost_crystal_ore")).place(level, generator, RandomSource.create(42), helper.absolutePos(center));
		int ores = 0;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
			if (level.getBlockState(helper.absolutePos(pos)).is(ModBlocks.FROST_CRYSTAL_ORE)) {
				ores++;
			}
		}
		helper.assertTrue(ores > 0, "Ore feature should place frost crystal ore");

		// icicles hang below the stone cube
		BlockPos below = center.offset(0, -3, 0);
		features.getValue(WinterMod.id("icicles")).place(level, generator, RandomSource.create(1), helper.absolutePos(below));
		helper.assertTrue(level.getBlockState(helper.absolutePos(below)).is(ModBlocks.ICICLE), "Icicle should be placed");

		// a gift on top of the cube
		BlockPos top = center.offset(0, 3, 0);
		features.getValue(WinterMod.id("gift")).place(level, generator, RandomSource.create(3), helper.absolutePos(top));
		BlockState gift = level.getBlockState(helper.absolutePos(top));
		helper.assertTrue(gift.getBlock() instanceof GiftBlock, "Gift should be placed, got " + gift);
		helper.succeed();
	}
}
