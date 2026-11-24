package ru.winter.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import ru.winter.WinterMod;
import ru.winter.client.WinterClient;
import ru.winter.entity.DedMorozEntity;

public class DedMorozRenderer extends MobRenderer<DedMorozEntity, DedMorozRenderState, DedMorozModel> {
	private static final Identifier TEXTURE = WinterMod.id("textures/entity/ded_moroz.png");

	public DedMorozRenderer(EntityRendererProvider.Context context) {
		super(context, new DedMorozModel(context.bakeLayer(WinterClient.DED_MOROZ_LAYER)), 0.5F);
	}

	@Override
	public Identifier getTextureLocation(DedMorozRenderState state) {
		return TEXTURE;
	}

	@Override
	public DedMorozRenderState createRenderState() {
		return new DedMorozRenderState();
	}

	@Override
	public void extractRenderState(DedMorozEntity entity, DedMorozRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.unhappy = entity.getUnhappyCounter() > 0;
	}
}
