package ru.winter.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import ru.winter.WinterMod;
import ru.winter.client.WinterClient;
import ru.winter.entity.YetiEntity;

public class YetiRenderer extends MobEntityRenderer<YetiEntity, YetiModel<YetiEntity>> {
	private static final Identifier TEXTURE = WinterMod.id("textures/entity/yeti.png");

	public YetiRenderer(EntityRendererFactory.Context context) {
		super(context, new YetiModel<>(context.getPart(WinterClient.YETI_LAYER)), 0.9F);
	}

	@Override
	public Identifier getTexture(YetiEntity entity) {
		return TEXTURE;
	}

}
