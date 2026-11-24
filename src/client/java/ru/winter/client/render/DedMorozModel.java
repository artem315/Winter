package ru.winter.client.render;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;
import ru.winter.entity.DedMorozEntity;

/**
 * Father Frost in his long coat, leaning on his staff. Geometry comes from blockbench/ded_moroz.bbmodel
 * (see {@link ru.winter.client.render.model.DedMorozModelData}); this class only animates it.
 */
public class DedMorozModel<T extends DedMorozEntity> extends SinglePartEntityModel<T> {
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public DedMorozModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");
	}

	@Override
	public ModelPart getPart() {
		return this.root;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		this.head.yaw = headYaw * (float) (Math.PI / 180.0);
		this.head.pitch = headPitch * (float) (Math.PI / 180.0);
		this.head.roll = 0.0F;
		if (entity.getHeadRollingTimeLeft() > 0) {
			// shakes his head like a villager when there is nothing to trade
			this.head.roll = 0.3F * MathHelper.sin(0.45F * animationProgress);
			this.head.pitch = 0.4F;
		}
		float swing = MathHelper.cos(limbAngle * 0.6662F) * limbDistance;
		this.rightLeg.pitch = swing * 0.7F;
		this.leftLeg.pitch = -swing * 0.7F;
		// the staff arm barely moves, the free arm swings
		this.rightArm.pitch = -swing * 0.25F - 0.1F;
		this.leftArm.pitch = swing * 0.6F;
		this.leftArm.roll = -0.05F - MathHelper.cos(animationProgress * 0.09F) * 0.03F;
	}
}
