package ru.winter.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import ru.winter.WinterMod;

/**
 * A wrapped present. Right-click to unwrap it and receive random loot
 * from the {@code winter:gameplay/gift} loot table; break it to carry it away (e.g. to give to a friend).
 */
public class GiftBlock extends Block {
	public static final MapCodec<GiftBlock> CODEC = simpleCodec(GiftBlock::new);
	public static final ResourceKey<LootTable> GIFT_LOOT = ResourceKey.create(Registries.LOOT_TABLE, WinterMod.id("gameplay/gift"));
	private static final VoxelShape SHAPE = Block.box(1.5, 0.0, 1.5, 14.5, 14.0, 14.5);

	public GiftBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level instanceof ServerLevel serverLevel) {
			unwrap(serverLevel, pos, player);
		}
		return InteractionResult.SUCCESS;
	}

	public static void unwrap(ServerLevel level, BlockPos pos, Player player) {
		LootTable table = level.getServer().reloadableRegistries().getLootTable(GIFT_LOOT);
		LootParams params = new LootParams.Builder(level)
			.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
			.withParameter(LootContextParams.THIS_ENTITY, player)
			.withLuck(player.getLuck())
			.create(LootContextParamSets.GIFT);
		List<ItemStack> loot = table.getRandomItems(params);

		level.removeBlock(pos, false);
		level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
		for (ItemStack stack : loot) {
			Block.popResource(level, pos, stack);
		}

		level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5F, 1.6F);
		level.playSound(null, pos, SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.BLOCKS, 1.0F, 1.0F);
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.35, 0.35, 0.35, 0.0);
		level.sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.05);

		if (player instanceof ServerPlayer serverPlayer) {
			AdvancementHolder advancement = level.getServer().getAdvancements().get(WinterMod.id("open_gift"));
			if (advancement != null) {
				serverPlayer.getAdvancements().award(advancement, "opened");
			}
		}
	}
}
