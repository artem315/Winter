package ru.winter.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.core.particles.ParticleTypes;
import ru.winter.WinterMod;
import ru.winter.block.FireplaceBlock;
import ru.winter.block.GarlandBlock;
import ru.winter.block.GiftBlock;
import ru.winter.block.HangingDecorationBlock;
import ru.winter.block.IcicleBlock;
import ru.winter.block.StarBlock;

public final class ModBlocks {
	public static final List<Block> ALL = new ArrayList<>();

	public static final TreeGrower FESTIVE_SPRUCE_GROWER = new TreeGrower(
		"winter:festive_spruce",
		Optional.empty(),
		Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, WinterMod.id("festive_spruce"))),
		Optional.empty()
	);

	// --- Ice & snow building blocks -------------------------------------------------------------
	public static final Block ICE_BRICKS = register("ice_bricks", Block::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.ICE).instrument(NoteBlockInstrument.CHIME).strength(0.8F).friction(0.98F).sound(SoundType.GLASS));
	public static final Block ICE_BRICK_STAIRS = register("ice_brick_stairs",
		p -> new StairBlock(ICE_BRICKS.defaultBlockState(), p), BlockBehaviour.Properties.ofFullCopy(ICE_BRICKS));
	public static final Block ICE_BRICK_SLAB = register("ice_brick_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(ICE_BRICKS));
	public static final Block ICE_BRICK_WALL = register("ice_brick_wall", WallBlock::new, BlockBehaviour.Properties.ofFullCopy(ICE_BRICKS).forceSolidOn());
	public static final Block CHISELED_ICE_BRICKS = register("chiseled_ice_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(ICE_BRICKS));

	public static final Block SNOW_BRICKS = register("snow_bricks", Block::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.SNOW).strength(0.8F).sound(SoundType.SNOW));
	public static final Block SNOW_BRICK_STAIRS = register("snow_brick_stairs",
		p -> new StairBlock(SNOW_BRICKS.defaultBlockState(), p), BlockBehaviour.Properties.ofFullCopy(SNOW_BRICKS));
	public static final Block SNOW_BRICK_SLAB = register("snow_brick_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SNOW_BRICKS));
	public static final Block SNOW_BRICK_WALL = register("snow_brick_wall", WallBlock::new, BlockBehaviour.Properties.ofFullCopy(SNOW_BRICKS).forceSolidOn());

	public static final Block FROSTED_GLASS = register("frosted_glass", TransparentBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.COLOR_LIGHT_BLUE));

	// --- Frost crystal ----------------------------------------------------------------------------
	public static final Block FROST_CRYSTAL_ORE = register("frost_crystal_ore", p -> new DropExperienceBlock(UniformInt.of(3, 7), p),
		BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE).lightLevel(state -> 3));
	public static final Block DEEPSLATE_FROST_CRYSTAL_ORE = register("deepslate_frost_crystal_ore", p -> new DropExperienceBlock(UniformInt.of(3, 7), p),
		BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE).lightLevel(state -> 3));
	public static final Block FROST_CRYSTAL_BLOCK = register("frost_crystal_block", Block::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.DIAMOND).requiresCorrectToolForDrops().strength(4.0F, 6.0F).sound(SoundType.AMETHYST).lightLevel(state -> 6));

	// --- Icicles, snowy spruce, festive tree --------------------------------------------------------
	public static final Block ICICLE = register("icicle", IcicleBlock::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.ICE).strength(0.3F).sound(SoundType.GLASS).noOcclusion().noCollision().randomTicks()
		.pushReaction(PushReaction.DESTROY));
	public static final Block SNOWY_SPRUCE_LEAVES = register("snowy_spruce_leaves",
		p -> new UntintedParticleLeavesBlock(0.02F, ParticleTypes.SNOWFLAKE, p),
		BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LEAVES).mapColor(MapColor.SNOW));
	public static final Block FESTIVE_SPRUCE_SAPLING = register("festive_spruce_sapling",
		p -> new SaplingBlock(FESTIVE_SPRUCE_GROWER, p), BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING));

	// --- New Year decorations ------------------------------------------------------------------------
	public static final Block GARLAND = register("garland", GarlandBlock::new, BlockBehaviour.Properties.of()
		.noCollision().strength(0.2F).sound(SoundType.WOOL).lightLevel(state -> 10).noOcclusion().pushReaction(PushReaction.DESTROY));
	public static final Block RED_BAUBLE = register("red_bauble", HangingDecorationBlock::new, bauble(MapColor.COLOR_RED));
	public static final Block GOLD_BAUBLE = register("gold_bauble", HangingDecorationBlock::new, bauble(MapColor.GOLD));
	public static final Block BLUE_BAUBLE = register("blue_bauble", HangingDecorationBlock::new, bauble(MapColor.COLOR_BLUE));
	public static final Block STAR = register("star", StarBlock::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.GOLD).noCollision().strength(0.3F).sound(SoundType.METAL).lightLevel(state -> 15).noOcclusion()
		.pushReaction(PushReaction.DESTROY));
	public static final Block RED_GIFT = register("red_gift", GiftBlock::new, gift(MapColor.COLOR_RED));
	public static final Block GREEN_GIFT = register("green_gift", GiftBlock::new, gift(MapColor.COLOR_GREEN));
	public static final Block BLUE_GIFT = register("blue_gift", GiftBlock::new, gift(MapColor.COLOR_BLUE));
	public static final Block FIREPLACE = register("fireplace", FireplaceBlock::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		.noOcclusion().lightLevel(state -> state.getValue(FireplaceBlock.LIT) ? 14 : 0));

	// --- Yeti fur ------------------------------------------------------------------------------------
	public static final Block YETI_FUR_BLOCK = register("yeti_fur_block", Block::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.WHITE)).mapColor(MapColor.SNOW));
	public static final Block YETI_FUR_CARPET = register("yeti_fur_carpet", CarpetBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.CARPET.pick(DyeColor.WHITE)).mapColor(MapColor.SNOW));

	private ModBlocks() {
	}

	private static BlockBehaviour.Properties bauble(MapColor color) {
		return BlockBehaviour.Properties.of().mapColor(color).strength(0.2F).sound(SoundType.GLASS).noOcclusion().noCollision()
			.lightLevel(state -> 4).pushReaction(PushReaction.DESTROY);
	}

	private static BlockBehaviour.Properties gift(MapColor color) {
		return BlockBehaviour.Properties.of().mapColor(color).strength(0.6F).sound(SoundType.WOOL).noOcclusion()
			.pushReaction(PushReaction.DESTROY);
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, WinterMod.id(name));
		Block block = Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, WinterMod.id(name));
		BlockItem item = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
		item.registerBlocks(Item.BY_BLOCK, item);
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);
		ALL.add(block);
		return block;
	}

	public static void init() {
		FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
		flammable.add(SNOWY_SPRUCE_LEAVES, 30, 60);
		flammable.add(YETI_FUR_BLOCK, 30, 60);
		flammable.add(YETI_FUR_CARPET, 60, 20);
		flammable.add(RED_GIFT, 30, 60);
		flammable.add(GREEN_GIFT, 30, 60);
		flammable.add(BLUE_GIFT, 30, 60);
		flammable.add(GARLAND, 30, 60);
	}
}
