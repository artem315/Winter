package ru.winter.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import ru.winter.WinterMod;
import ru.winter.entity.DedMorozEntity;

/** Ded Moroz uses the villager-like trader model with his own red-and-white coat texture. */
public class DedMorozRenderer extends MobEntityRenderer<DedMorozEntity, VillagerResemblingModel<DedMorozEntity>> {
	private static final Identifier TEXTURE = WinterMod.id("textures/entity/ded_moroz.png");

	public DedMorozRenderer(EntityRendererFactory.Context context) {
		super(context, new VillagerResemblingModel<>(context.getPart(EntityModelLayers.WANDERING_TRADER)), 0.5F);
		this.addFeature(new HeadFeatureRenderer<>(this, context.getModelLoader(), context.getHeldItemRenderer()));
		this.addFeature(new VillagerHeldItemFeatureRenderer<>(this, context.getHeldItemRenderer()));
	}

	@Override
	public Identifier getTexture(DedMorozEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(DedMorozEntity entity, MatrixStack matrices, float amount) {
		matrices.scale(1.0F, 1.0F, 1.0F);
	}
}
