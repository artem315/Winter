package ru.winter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.IronGolemEntityModel;
import ru.winter.WinterMod;
import ru.winter.client.render.DedMorozRenderer;
import ru.winter.client.render.YetiRenderer;
import ru.winter.registry.ModBlocks;
import ru.winter.registry.ModEntities;

public class WinterClient implements ClientModInitializer {
	public static final EntityModelLayer YETI_LAYER = new EntityModelLayer(WinterMod.id("yeti"), "main");

	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.FROSTED_GLASS, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
			ModBlocks.ICICLE, ModBlocks.FESTIVE_SPRUCE_SAPLING, ModBlocks.GARLAND,
			ModBlocks.RED_BAUBLE, ModBlocks.GOLD_BAUBLE, ModBlocks.BLUE_BAUBLE, ModBlocks.STAR,
			ModBlocks.RED_GIFT, ModBlocks.GREEN_GIFT, ModBlocks.BLUE_GIFT);
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SNOWY_SPRUCE_LEAVES, RenderLayer.getCutoutMipped());

		// The yeti reuses the iron golem's geometry with its own texture and animations.
		EntityModelLayerRegistry.registerModelLayer(YETI_LAYER, IronGolemEntityModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.YETI, YetiRenderer::new);
		EntityRendererRegistry.register(ModEntities.DED_MOROZ, DedMorozRenderer::new);
		EntityRendererRegistry.register(ModEntities.ICE_BALL, FlyingItemEntityRenderer::new);

		ClientTickEvents.END_WORLD_TICK.register(FrostyBreath::tick);
		WinterTooltips.init();
	}
}
