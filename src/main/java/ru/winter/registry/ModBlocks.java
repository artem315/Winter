package ru.winter.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import ru.winter.WinterMod;
import ru.winter.block.FireplaceBlock;
import ru.winter.block.GarlandBlock;
import ru.winter.block.GiftBlock;
import ru.winter.block.HangingDecorationBlock;
import ru.winter.block.IcicleBlock;
import ru.winter.block.StarBlock;

public final class ModBlocks {
	public static final List<Block> ALL = new ArrayList<>();

	public static final SaplingGenerator FESTIVE_SPRUCE_GENERATOR = new SaplingGenerator(
		"winter:festive_spruce",
		Optional.empty(),
		Optional.of(RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, WinterMod.id("festive_spruce"))),
		Optional.empty()
	);

	// --- Ice & snow building blocks -------------------------------------------------------------
	public static final Block ICE_BRICKS = register("ice_bricks", new Block(AbstractBlock.Settings.create()
		.mapColor(MapColor.PALE_PURPLE).instrument(NoteBlockInstrument.CHIME)
		.strength(0.8F).slipperiness(0.98F).sounds(BlockSoundGroup.GLASS)));
	public static final Block ICE_BRICK_STAIRS = register("ice_brick_stairs",
		new StairsBlock(ICE_BRICKS.getDefaultState(), AbstractBlock.Settings.copy(ICE_BRICKS)));
	public static final Block ICE_BRICK_SLAB = register("ice_brick_slab", new SlabBlock(AbstractBlock.Settings.copy(ICE_BRICKS)));
	public static final Block ICE_BRICK_WALL = register("ice_brick_wall", new WallBlock(AbstractBlock.Settings.copy(ICE_BRICKS).solid()));
	public static final Block CHISELED_ICE_BRICKS = register("chiseled_ice_bricks", new Block(AbstractBlock.Settings.copy(ICE_BRICKS)));

	public static final Block SNOW_BRICKS = register("snow_bricks", new Block(AbstractBlock.Settings.create()
		.mapColor(MapColor.WHITE).strength(0.8F).sounds(BlockSoundGroup.SNOW)));
	public static final Block SNOW_BRICK_STAIRS = register("snow_brick_stairs",
		new StairsBlock(SNOW_BRICKS.getDefaultState(), AbstractBlock.Settings.copy(SNOW_BRICKS)));
	public static final Block SNOW_BRICK_SLAB = register("snow_brick_slab", new SlabBlock(AbstractBlock.Settings.copy(SNOW_BRICKS)));
	public static final Block SNOW_BRICK_WALL = register("snow_brick_wall", new WallBlock(AbstractBlock.Settings.copy(SNOW_BRICKS).solid()));

	public static final Block FROSTED_GLASS = register("frosted_glass",
		new TransparentBlock(AbstractBlock.Settings.copy(Blocks.GLASS).mapColor(MapColor.LIGHT_BLUE)));

	// --- Frost crystal ----------------------------------------------------------------------------
	public static final Block FROST_CRYSTAL_ORE = register("frost_crystal_ore", new ExperienceDroppingBlock(UniformIntProvider.create(3, 7),
		AbstractBlock.Settings.copy(Blocks.DIAMOND_ORE).luminance(state -> 3)));
	public static final Block DEEPSLATE_FROST_CRYSTAL_ORE = register("deepslate_frost_crystal_ore", new ExperienceDroppingBlock(UniformIntProvider.create(3, 7),
		AbstractBlock.Settings.copy(Blocks.DEEPSLATE_DIAMOND_ORE).luminance(state -> 3)));
	public static final Block FROST_CRYSTAL_BLOCK = register("frost_crystal_block", new Block(AbstractBlock.Settings.create()
		.mapColor(MapColor.DIAMOND_BLUE).requiresTool().strength(4.0F, 6.0F).sounds(BlockSoundGroup.AMETHYST_BLOCK).luminance(state -> 6)));

	// --- Icicles, snowy spruce, festive tree --------------------------------------------------------
	public static final Block ICICLE = register("icicle", new IcicleBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.PALE_PURPLE).strength(0.3F).sounds(BlockSoundGroup.GLASS).nonOpaque().noCollision()
		.ticksRandomly().pistonBehavior(PistonBehavior.DESTROY)));
	public static final Block SNOWY_SPRUCE_LEAVES = register("snowy_spruce_leaves",
		new LeavesBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_LEAVES).mapColor(MapColor.WHITE)));
	public static final Block FESTIVE_SPRUCE_SAPLING = register("festive_spruce_sapling",
		new SaplingBlock(FESTIVE_SPRUCE_GENERATOR, AbstractBlock.Settings.copy(Blocks.SPRUCE_SAPLING)));

	// --- New Year decorations ------------------------------------------------------------------------
	public static final Block GARLAND = register("garland", new GarlandBlock(AbstractBlock.Settings.create()
		.noCollision().strength(0.2F).sounds(BlockSoundGroup.WOOL).luminance(state -> 10).nonOpaque()
		.pistonBehavior(PistonBehavior.DESTROY)));
	public static final Block RED_BAUBLE = register("red_bauble", bauble(MapColor.RED));
	public static final Block GOLD_BAUBLE = register("gold_bauble", bauble(MapColor.GOLD));
	public static final Block BLUE_BAUBLE = register("blue_bauble", bauble(MapColor.BLUE));
	public static final Block STAR = register("star", new StarBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.GOLD).noCollision().strength(0.3F).sounds(BlockSoundGroup.METAL).luminance(state -> 15).nonOpaque()
		.pistonBehavior(PistonBehavior.DESTROY)));
	public static final Block RED_GIFT = register("red_gift", gift(MapColor.RED));
	public static final Block GREEN_GIFT = register("green_gift", gift(MapColor.GREEN));
	public static final Block BLUE_GIFT = register("blue_gift", gift(MapColor.BLUE));
	public static final Block FIREPLACE = register("fireplace", new FireplaceBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.RED).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(2.0F, 6.0F).nonOpaque()
		.luminance(state -> state.get(FireplaceBlock.LIT) ? 14 : 0)));

	// --- Yeti fur ------------------------------------------------------------------------------------
	public static final Block YETI_FUR_BLOCK = register("yeti_fur_block",
		new Block(AbstractBlock.Settings.copy(Blocks.WHITE_WOOL).mapColor(MapColor.WHITE)));
	public static final Block YETI_FUR_CARPET = register("yeti_fur_carpet",
		new CarpetBlock(AbstractBlock.Settings.copy(Blocks.WHITE_CARPET).mapColor(MapColor.WHITE)));

	private ModBlocks() {
	}

	private static Block bauble(MapColor color) {
		return new HangingDecorationBlock(AbstractBlock.Settings.create()
			.mapColor(color).strength(0.2F).sounds(BlockSoundGroup.GLASS).nonOpaque().noCollision()
			.luminance(state -> 4).pistonBehavior(PistonBehavior.DESTROY));
	}

	private static Block gift(MapColor color) {
		return new GiftBlock(AbstractBlock.Settings.create()
			.mapColor(color).strength(0.6F).sounds(BlockSoundGroup.WOOL).nonOpaque().pistonBehavior(PistonBehavior.DESTROY));
	}

	private static Block register(String name, Block block) {
		return register(name, block, b -> new BlockItem(b, new Item.Settings()));
	}

	private static Block register(String name, Block block, Function<Block, Item> itemFactory) {
		Block registered = Registry.register(Registries.BLOCK, WinterMod.id(name), block);
		Registry.register(Registries.ITEM, WinterMod.id(name), itemFactory.apply(registered));
		ALL.add(registered);
		return registered;
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
