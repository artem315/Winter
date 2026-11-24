package ru.winter.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import ru.winter.WinterMod;

/**
 * A wrapped present. Right-click to unwrap it and receive random loot
 * from the {@code winter:gameplay/gift} loot table; break it to carry it away (e.g. to give to a friend).
 */
public class GiftBlock extends Block {
	public static final MapCodec<GiftBlock> CODEC = createCodec(GiftBlock::new);
	public static final RegistryKey<LootTable> GIFT_LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, WinterMod.id("gameplay/gift"));
	private static final VoxelShape SHAPE = Block.createCuboidShape(1.5, 0.0, 1.5, 14.5, 14.0, 14.5);

	public GiftBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world instanceof ServerWorld serverWorld) {
			unwrap(serverWorld, pos, player);
		}
		return ActionResult.success(world.isClient);
	}

	public static void unwrap(ServerWorld world, BlockPos pos, PlayerEntity player) {
		LootTable table = world.getServer().getReloadableRegistries().getLootTable(GIFT_LOOT);
		LootContextParameterSet params = new LootContextParameterSet.Builder(world)
			.add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
			.add(LootContextParameters.THIS_ENTITY, player)
			.luck(player.getLuck())
			.build(LootContextTypes.GIFT);
		List<ItemStack> loot = table.generateLoot(params);

		world.removeBlock(pos, false);
		world.emitGameEvent(player, GameEvent.BLOCK_DESTROY, pos);
		for (ItemStack stack : loot) {
			Block.dropStack(world, pos, stack);
		}

		world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.5F, 1.6F);
		world.playSound(null, pos, SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.BLOCKS, 1.0F, 1.0F);
		world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.35, 0.35, 0.35, 0.0);
		world.spawnParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.05);

		if (player instanceof ServerPlayerEntity serverPlayer) {
			AdvancementEntry advancement = world.getServer().getAdvancementLoader().get(WinterMod.id("open_gift"));
			if (advancement != null) {
				serverPlayer.getAdvancementTracker().grantCriterion(advancement, "opened");
			}
		}
	}
}
