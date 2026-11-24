package ru.winter.client.render;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import ru.winter.entity.YetiEntity;

/** Iron-golem-shaped model, animated like one: swinging arms, heavy steps, two-armed smash attack. */
public class YetiModel<T extends YetiEntity> extends SinglePartEntityModel<T> {
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public YetiModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild(EntityModelPartNames.HEAD);
		this.rightArm = root.getChild(EntityModelPartNames.RIGHT_ARM);
		this.leftArm = root.getChild(EntityModelPartNames.LEFT_ARM);
		this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
		this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
	}

	@Override
	public ModelPart getPart() {
		return this.root;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		this.head.yaw = headYaw * (float) (Math.PI / 180.0);
		this.head.pitch = headPitch * (float) (Math.PI / 180.0);
		this.rightLeg.pitch = -1.5F * wave(limbAngle, 13.0F) * limbDistance;
		this.leftLeg.pitch = 1.5F * wave(limbAngle, 13.0F) * limbDistance;
		this.rightLeg.yaw = 0.0F;
		this.leftLeg.yaw = 0.0F;
	}

	@Override
	public void animateModel(T entity, float limbAngle, float limbDistance, float tickDelta) {
		int attackTicks = entity.getAttackTicksLeft();
		if (attackTicks > 0) {
			float swing = -2.0F + 1.5F * wave(attackTicks - tickDelta, 10.0F);
			this.rightArm.pitch = swing;
			this.leftArm.pitch = swing;
		} else {
			this.rightArm.pitch = (-0.2F + 1.5F * wave(limbAngle, 13.0F)) * limbDistance;
			this.leftArm.pitch = (-0.2F - 1.5F * wave(limbAngle, 13.0F)) * limbDistance;
		}
	}

	/** Triangle wave in [-1, 1] (same shape as vanilla's MathHelper.wrap). */
	private static float wave(float value, float period) {
		return (Math.abs(value % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
	}
}
