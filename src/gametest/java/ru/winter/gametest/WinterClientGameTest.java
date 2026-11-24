package ru.winter.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import ru.winter.registry.ModBlocks;
import ru.winter.registry.ModItems;

/**
 * Client game test ({@code ./gradlew runClientGameTest}): builds a winter scene with the mod's blocks, mobs and
 * clothing in a flat test world and takes screenshots (build/run/clientGameTest/screenshots). It checks that
 * models, textures and entity renderers load and render without errors.
 */
@SuppressWarnings("UnstableApiUsage")
public class WinterClientGameTest implements FabricClientGameTest {
	private BlockPos origin = BlockPos.ZERO;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getConnection().waitForChunksRender();
			BlockPos player = singleplayer.getServer().computeOnServer(server -> server.getPlayerList().getPlayers().getFirst().blockPosition());
			this.origin = new BlockPos(player.getX(), player.getY() + 40, player.getZ());

			for (String command : this.sceneCommands()) {
				singleplayer.getServer().runCommand(command);
			}
			this.tp(singleplayer, 0.5, 0, 9.5, 180, 12);
			context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			context.waitTicks(20);
			context.takeScreenshot("winter_day");

			context.getInput().pressKey(options -> options.keyToggleGui); // hide the HUD (F1)
			singleplayer.getServer().runCommand("time set 18000");
			context.waitTicks(40);
			context.takeScreenshot("winter_night");

			singleplayer.getServer().runCommand("time set 6000");
			this.tp(singleplayer, 2.5, 0, 9.5, 180, 6);
			context.waitTicks(40);
			context.takeScreenshot("winter_mobs");

			context.getInput().pressKey(options -> options.keyToggleGui); // show the HUD again
			for (String command : this.inventoryCommands()) {
				singleplayer.getServer().runCommand(command);
			}
			context.waitTicks(20);
			context.setScreen(() -> new InventoryScreen(context.computeOnClient(minecraft -> minecraft.player)));
			context.waitTicks(10);
			context.takeScreenshot("winter_inventory");
			context.setScreen(() -> null);
		}
	}

	private List<String> sceneCommands() {
		List<String> c = new ArrayList<>();
		c.add("gamemode creative @p");
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
		c.add(String.format(Locale.ROOT, "place feature winter:festive_spruce %d %d %d", x(-4), y(0), z(-2)));
		c.add(set(-5, 0, 0, "winter:red_gift"));
		c.add(set(-3, 0, 0, "winter:green_gift"));
		c.add(set(-4, 0, 1, "winter:blue_gift"));
		// ice arch with icicles and baubles
		c.add(fill(3, 0, 0, 3, 2, 0, "winter:snow_brick_wall"));
		c.add(fill(6, 0, 0, 6, 2, 0, "winter:snow_brick_wall"));
		c.add(fill(3, 3, -1, 6, 3, 1, "winter:ice_bricks"));
		c.add(set(4, 2, 0, "winter:icicle"));
		c.add(set(5, 2, -1, "winter:icicle"));
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
		c.add(summon("minecraft:armor_stand", 2.5, 0, 3.5, "ShowArms:1b,Rotation:[15f,0f],equipment:{"
			+ "feet:{id:\"winter:valenki\"},legs:{id:\"winter:quilted_pants\"},chest:{id:\"winter:sweater\"},"
			+ "head:{id:\"winter:ushanka\"},mainhand:{id:\"winter:frost_staff\"}}"));
		c.add(summon("minecraft:armor_stand", -5.5, 0, 4.5, "ShowArms:1b,Rotation:[-15f,0f],equipment:{"
			+ "feet:{id:\"winter:ice_skates\"},mainhand:{id:\"winter:frost_sword\"},offhand:{id:\"winter:candy_cane\"}}"));
		String[] hotbar = {"frost_sword", "frost_staff", "snow_shovel", "ice_ball", "hot_chocolate", "candy_cane", "mandarin",
			"olivier_salad", "thermometer"};
		for (int i = 0; i < hotbar.length; i++) {
			c.add("item replace entity @p hotbar." + i + " with winter:" + hotbar[i]);
		}
		c.add("kill @e[type=item]");
		return c;
	}

	private List<String> inventoryCommands() {
		List<String> c = new ArrayList<>();
		c.add("clear @p");
		c.add("gamemode survival @p");
		c.add("item replace entity @p armor.head with winter:ushanka");
		c.add("item replace entity @p armor.chest with winter:sweater");
		c.add("item replace entity @p armor.legs with winter:quilted_pants");
		c.add("item replace entity @p armor.feet with winter:valenki");
		List<Item> items = new ArrayList<>(ModItems.ALL);
		for (Block block : List.of(ModBlocks.RED_GIFT, ModBlocks.GREEN_GIFT, ModBlocks.BLUE_GIFT, ModBlocks.GARLAND,
			ModBlocks.RED_BAUBLE, ModBlocks.GOLD_BAUBLE, ModBlocks.BLUE_BAUBLE, ModBlocks.STAR, ModBlocks.FIREPLACE,
			ModBlocks.FESTIVE_SPRUCE_SAPLING)) {
			items.add(block.asItem());
		}
		for (Item item : items) {
			if (item != ModItems.USHANKA && item != ModItems.SWEATER && item != ModItems.QUILTED_PANTS && item != ModItems.VALENKI) {
				c.add("give @p " + BuiltInRegistries.ITEM.getKey(item));
			}
		}
		return c;
	}

	private String fill(int x1, int y1, int z1, int x2, int y2, int z2, String block) {
		return String.format(Locale.ROOT, "fill %d %d %d %d %d %d %s", x(x1), y(y1), z(z1), x(x2), y(y2), z(z2), block);
	}

	private String set(int dx, int dy, int dz, String block) {
		return String.format(Locale.ROOT, "setblock %d %d %d %s", x(dx), y(dy), z(dz), block);
	}

	private String summon(String type, double dx, double dy, double dz, String nbt) {
		return String.format(Locale.ROOT, "summon %s %.2f %.2f %.2f {%s}", type,
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

	private void tp(TestSingleplayerContext singleplayer, double dx, double dy, double dz, float yaw, float pitch) {
		singleplayer.getServer().runCommand(String.format(Locale.ROOT, "tp @p %.2f %.2f %.2f %.1f %.1f",
			this.origin.getX() + dx, this.origin.getY() + dy, this.origin.getZ() + dz, yaw, pitch));
	}
}
