package ru.winter.gametest;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.winter.registry.ModBlocks;
import ru.winter.registry.ModItems;

/**
 * Development-only helper (never shipped in the mod jar): with {@code -Dwinter.showcase=true}
 * it creates a world, builds a scene with the mod's blocks and mobs, takes screenshots and quits.
 * Run with {@code ./gradlew runShowcase}; screenshots land in build/showcase/screenshots.
 */
public class ShowcaseClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("winter-showcase");
	private int stage;
	private int wait;
	private BlockPos origin;

	@Override
	public void onInitializeClient() {
		if (Boolean.getBoolean("winter.showcase")) {
			ClientTickEvents.END_CLIENT_TICK.register(this::tick);
		}
	}

	private void tick(MinecraftClient client) {
		if (this.wait > 0) {
			this.wait--;
			return;
		}
		switch (this.stage) {
			case 0 -> {
				if (client.currentScreen instanceof TitleScreen) {
					createWorld(client);
					next(1);
				}
			}
			case 1 -> {
				if (client.world != null && client.player != null && client.getServer() != null && client.currentScreen == null) {
					next(60);
				}
			}
			case 2 -> {
				buildScene(client);
				next(400);
			}
			case 3 -> {
				screenshot(client, "winter_day");
				client.options.hudHidden = true;
				command(client, "time set 18000");
				next(80);
			}
			case 4 -> {
				screenshot(client, "winter_night");
				command(client, "time set 6000");
				tp(client, 2.5, 0, 9.5, 180, 6);
				next(120);
			}
			case 5 -> {
				screenshot(client, "winter_mobs");
				client.options.hudHidden = false;
				fillInventory(client);
				next(40);
			}
			case 6 -> {
				client.setScreen(new InventoryScreen(client.player));
				next(30);
			}
			case 7 -> {
				screenshot(client, "winter_inventory");
				next(40);
			}
			case 8 -> {
				LOGGER.info("Showcase finished");
				client.scheduleStop();
				next(1000);
			}
			default -> {
			}
		}
	}

	private void next(int ticks) {
		this.stage++;
		this.wait = ticks;
	}

	private static void createWorld(MinecraftClient client) {
		GameRules rules = new GameRules();
		rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
		rules.get(GameRules.DO_WEATHER_CYCLE).set(false, null);
		rules.get(GameRules.DO_MOB_SPAWNING).set(false, null);
		LevelInfo info = new LevelInfo("WinterShowcase", GameMode.CREATIVE, false, Difficulty.EASY, true, rules,
			ModResourcePackUtil.createDefaultDataConfiguration());
		client.createIntegratedServerLoader().createAndStart("WinterShowcase", info, new GeneratorOptions(20251224L, false, false),
			WorldPresets::createDemoOptions, client.currentScreen);
	}

	private void buildScene(MinecraftClient client) {
		BlockPos p = client.player.getBlockPos();
		this.origin = new BlockPos(p.getX(), 200, p.getZ());
		List<String> c = new ArrayList<>();
		c.add("weather clear");
		c.add("time set 6000");
		c.add(fill(-14, -1, -14, 14, -1, 14, "minecraft:snow_block"));
		c.add(fill(-14, 0, -14, 14, 12, 14, "minecraft:air"));
		// back wall
		c.add(fill(-7, 0, -6, -1, 4, -6, "winter:ice_bricks"));
		c.add(fill(1, 0, -6, 7, 4, -6, "winter:snow_bricks"));
		c.add(fill(0, 0, -6, 0, 4, -6, "winter:chiseled_ice_bricks"));
		c.add(fill(-7, 5, -6, -1, 5, -6, "winter:ice_brick_wall"));
		c.add(fill(1, 5, -6, 7, 5, -6, "winter:snow_brick_wall"));
		c.add(fill(0, 5, -6, 0, 5, -6, "winter:ice_brick_slab"));
		c.add(fill(-5, 2, -6, -3, 3, -6, "winter:frosted_glass"));
		c.add(fill(3, 2, -6, 5, 3, -6, "winter:frosted_glass"));
		c.add(fill(-7, 4, -5, -1, 4, -5, "winter:garland[facing=south]"));
		c.add(fill(1, 4, -5, 7, 4, -5, "winter:garland[facing=south]"));
		c.add(set(0, 0, -5, "winter:fireplace[facing=south,lit=true]"));
		c.add(set(0, 1, -5, "winter:star"));
		c.add(fill(-1, 0, -4, 1, 0, -3, "winter:yeti_fur_carpet"));
		c.add(fill(-7, 0, -5, -6, 0, -5, "winter:ice_brick_stairs[facing=north]"));
		c.add(fill(6, 0, -5, 7, 0, -5, "winter:snow_brick_stairs[facing=north]"));
		// festive tree with presents
		c.add(String.format("place feature winter:festive_spruce %d %d %d", x(-4), y(0), z(-2)));
		c.add(set(-5, 0, 0, "winter:red_gift"));
		c.add(set(-3, 0, 0, "winter:green_gift"));
		c.add(set(-4, 0, 1, "winter:blue_gift"));
		// ice arch with icicles and baubles
		c.add(fill(3, 0, 0, 3, 2, 0, "winter:snow_brick_wall"));
		c.add(fill(6, 0, 0, 6, 2, 0, "winter:snow_brick_wall"));
		c.add(fill(3, 3, -1, 6, 3, 1, "winter:ice_bricks"));
		c.add(set(4, 2, 0, "winter:icicle"));
		c.add(set(5, 2, -1, "winter:icicle"));
		c.add(set(5, 1, -1, "winter:icicle"));
		c.add(set(4, 2, 1, "winter:red_bauble"));
		c.add(set(5, 2, 1, "winter:blue_bauble"));
		c.add(set(5, 2, 0, "winter:gold_bauble"));
		// materials
		c.add(set(-8, 0, 3, "winter:frost_crystal_block"));
		c.add(set(-8, 0, 4, "winter:frost_crystal_ore"));
		c.add(set(-7, 0, 4, "winter:deepslate_frost_crystal_ore"));
		c.add(set(-8, 1, 3, "winter:yeti_fur_block"));
		c.add(fill(8, 0, -4, 8, 1, -3, "winter:snowy_spruce_leaves[persistent=true]"));
		c.add(set(8, 0, -1, "winter:festive_spruce_sapling"));
		// mobs and a dressed-up armor stand
		c.add(summon("winter:yeti", 6.5, 0, 4.5, "NoAI:1b,PersistenceRequired:1b,Rotation:[20f,0f]"));
		c.add(summon("winter:ded_moroz", -1.5, 0, 2.5, "NoAI:1b,PersistenceRequired:1b,Rotation:[-10f,0f]"));
		c.add(summon("minecraft:armor_stand", 2.5, 0, 3.5,
			"ShowArms:1b,Rotation:[15f,0f],ArmorItems:[{id:\"winter:valenki\",count:1},{id:\"winter:quilted_pants\",count:1},"
				+ "{id:\"winter:sweater\",count:1},{id:\"winter:ushanka\",count:1}],HandItems:[{id:\"winter:frost_staff\",count:1},{}]"));
		c.add(summon("minecraft:armor_stand", -5.5, 0, 4.5,
			"ShowArms:1b,Rotation:[-15f,0f],ArmorItems:[{id:\"winter:ice_skates\",count:1},{},{},{}],"
				+ "HandItems:[{id:\"winter:frost_sword\",count:1},{id:\"winter:candy_cane\",count:1}]"));
		String[] hotbar = {"frost_sword", "frost_staff", "snow_shovel", "ice_ball", "hot_chocolate", "candy_cane", "mandarin",
			"olivier_salad", "thermometer"};
		for (int i = 0; i < hotbar.length; i++) {
			c.add("item replace entity @p hotbar." + i + " with winter:" + hotbar[i]);
		}
		c.add("kill @e[type=item]");
		for (String s : c) {
			command(client, s);
		}
		tp(client, 0.5, 0, 9.5, 180, 12);
		client.player.getInventory().selectedSlot = 0;
	}

	private void fillInventory(MinecraftClient client) {
		command(client, "clear @p");
		command(client, "gamemode survival @p");
		command(client, "item replace entity @p armor.head with winter:ushanka");
		command(client, "item replace entity @p armor.chest with winter:sweater");
		command(client, "item replace entity @p armor.legs with winter:quilted_pants");
		command(client, "item replace entity @p armor.feet with winter:valenki");
		List<Item> items = new ArrayList<>(ModItems.ALL);
		for (Block block : List.of(ModBlocks.RED_GIFT, ModBlocks.GREEN_GIFT, ModBlocks.BLUE_GIFT, ModBlocks.GARLAND,
			ModBlocks.RED_BAUBLE, ModBlocks.GOLD_BAUBLE, ModBlocks.BLUE_BAUBLE, ModBlocks.STAR, ModBlocks.FIREPLACE,
			ModBlocks.FESTIVE_SPRUCE_SAPLING)) {
			items.add(block.asItem());
		}
		for (Item item : items) {
			if (item == ModItems.USHANKA || item == ModItems.SWEATER || item == ModItems.QUILTED_PANTS || item == ModItems.VALENKI) {
				continue;
			}
			command(client, "give @p " + Registries.ITEM.getId(item));
		}
	}

	private String fill(int x1, int y1, int z1, int x2, int y2, int z2, String block) {
		return String.format("fill %d %d %d %d %d %d %s", x(x1), y(y1), z(z1), x(x2), y(y2), z(z2), block);
	}

	private String set(int dx, int dy, int dz, String block) {
		return String.format("setblock %d %d %d %s", x(dx), y(dy), z(dz), block);
	}

	private String summon(String type, double dx, double dy, double dz, String nbt) {
		return String.format(java.util.Locale.ROOT, "summon %s %.2f %.2f %.2f {%s}", type,
			this.origin.getX() + dx, this.origin.getY() + dy, this.origin.getZ() + dz, nbt);
	}

	private int x(int d) {
		return this.origin.getX() + d;
	}

	private int y(int d) {
		return this.origin.getY() + d;
	}

	private int z(int d) {
		return this.origin.getZ() + d;
	}

	private void tp(MinecraftClient client, double dx, double dy, double dz, float yaw, float pitch) {
		command(client, String.format(java.util.Locale.ROOT, "tp @p %.2f %.2f %.2f %.1f %.1f",
			this.origin.getX() + dx, this.origin.getY() + dy, this.origin.getZ() + dz, yaw, pitch));
	}

	private static void command(MinecraftClient client, String command) {
		MinecraftServer server = client.getServer();
		server.execute(() -> server.getCommandManager().executeWithPrefix(server.getCommandSource(), command));
	}

	private static void screenshot(MinecraftClient client, String name) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, name + ".png", client.getFramebuffer(),
			text -> LOGGER.info(text.getString()));
	}
}
