package ru.winter.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import ru.winter.WinterMod;
import ru.winter.client.WinterClient;
import ru.winter.entity.YetiEntity;

public class YetiRenderer extends MobRenderer<YetiEntity, YetiRenderState, YetiModel> {
	private static final Identifier TEXTURE = WinterMod.id("textures/entity/yeti.png");

	public YetiRenderer(EntityRendererProvider.Context context) {
		super(context, new YetiModel(context.bakeLayer(WinterClient.YETI_LAYER)), 0.9F);
	}

	@Override
	public Identifier getTextureLocation(YetiRenderState state) {
		return TEXTURE;
	}

	@Override
	public YetiRenderState createRenderState() {
		return new YetiRenderState();
	}

	@Override
	public void extractRenderState(YetiEntity entity, YetiRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.attackTicks = entity.getAttackTicksLeft() > 0 ? entity.getAttackTicksLeft() - partialTicks : 0.0F;
	}
}
