package ru.winter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import ru.winter.WinterMod;
import ru.winter.client.render.DedMorozRenderer;
import ru.winter.client.render.YetiRenderer;
import ru.winter.client.render.model.DedMorozModelData;
import ru.winter.client.render.model.YetiModelData;
import ru.winter.registry.ModBlocks;
import ru.winter.registry.ModEntities;

public class WinterClient implements ClientModInitializer {
	public static final EntityModelLayer YETI_LAYER = new EntityModelLayer(WinterMod.id("yeti"), "main");
	public static final EntityModelLayer DED_MOROZ_LAYER = new EntityModelLayer(WinterMod.id("ded_moroz"), "main");

	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.FROSTED_GLASS, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
			ModBlocks.ICICLE, ModBlocks.FESTIVE_SPRUCE_SAPLING, ModBlocks.GARLAND,
			ModBlocks.RED_BAUBLE, ModBlocks.GOLD_BAUBLE, ModBlocks.BLUE_BAUBLE, ModBlocks.STAR,
			ModBlocks.RED_GIFT, ModBlocks.GREEN_GIFT, ModBlocks.BLUE_GIFT, ModBlocks.FIREPLACE);
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SNOWY_SPRUCE_LEAVES, RenderLayer.getCutoutMipped());

		// Entity models are made in Blockbench (blockbench/*.bbmodel) and exported by tools/blockbench_export.js.
		EntityModelLayerRegistry.registerModelLayer(YETI_LAYER, YetiModelData::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(DED_MOROZ_LAYER, DedMorozModelData::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.YETI, YetiRenderer::new);
		EntityRendererRegistry.register(ModEntities.DED_MOROZ, DedMorozRenderer::new);
		EntityRendererRegistry.register(ModEntities.ICE_BALL, FlyingItemEntityRenderer::new);

		ClientTickEvents.END_WORLD_TICK.register(FrostyBreath::tick);
		WinterTooltips.init();
	}
}
