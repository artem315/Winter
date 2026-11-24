package ru.winter.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import ru.winter.WinterMod;
import ru.winter.client.WinterClient;
import ru.winter.entity.DedMorozEntity;

public class DedMorozRenderer extends MobEntityRenderer<DedMorozEntity, DedMorozModel<DedMorozEntity>> {
	private static final Identifier TEXTURE = WinterMod.id("textures/entity/ded_moroz.png");

	public DedMorozRenderer(EntityRendererFactory.Context context) {
		super(context, new DedMorozModel<>(context.getPart(WinterClient.DED_MOROZ_LAYER)), 0.5F);
	}

	@Override
	public Identifier getTexture(DedMorozEntity entity) {
		return TEXTURE;
	}
}
