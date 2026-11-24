package ru.winter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import ru.winter.WinterMod;
import ru.winter.client.render.DedMorozRenderer;
import ru.winter.client.render.YetiRenderer;
import ru.winter.client.render.model.DedMorozModelData;
import ru.winter.client.render.model.YetiModelData;
import ru.winter.registry.ModEntities;

public class WinterClient implements ClientModInitializer {
	public static final ModelLayerLocation YETI_LAYER = new ModelLayerLocation(WinterMod.id("yeti"), "main");
	public static final ModelLayerLocation DED_MOROZ_LAYER = new ModelLayerLocation(WinterMod.id("ded_moroz"), "main");

	@Override
	public void onInitializeClient() {
		// Block render layers are picked automatically from texture transparency since 26.1.

		// Entity models are made in Blockbench (blockbench/*.bbmodel) and exported by tools/blockbench_export.js.
		ModelLayerRegistry.registerModelLayer(YETI_LAYER, YetiModelData::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(DED_MOROZ_LAYER, DedMorozModelData::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.YETI, YetiRenderer::new);
		EntityRendererRegistry.register(ModEntities.DED_MOROZ, DedMorozRenderer::new);
		EntityRendererRegistry.register(ModEntities.ICE_BALL, ThrownItemRenderer::new);

		ClientTickEvents.END_LEVEL_TICK.register(FrostyBreath::tick);
		WinterTooltips.init();
	}
}
