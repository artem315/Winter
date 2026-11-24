package ru.winter.client.render;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;
import ru.winter.entity.YetiEntity;

/**
 * The yeti: a hunched, knuckle-walking beast. Geometry comes from blockbench/yeti.bbmodel
 * (see {@link ru.winter.client.render.model.YetiModelData}); this class only animates it.
 */
public class YetiModel<T extends YetiEntity> extends SinglePartEntityModel<T> {
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;
	private final float bodyPitch;

	public YetiModel(ModelPart root) {
		this.root = root;
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");
		this.bodyPitch = this.body.pitch;
	}

	@Override
	public ModelPart getPart() {
		return this.root;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		this.head.yaw = headYaw * (float) (Math.PI / 180.0);
		this.head.pitch = headPitch * (float) (Math.PI / 180.0);
		// heavy breathing
		this.body.pitch = this.bodyPitch + MathHelper.sin(animationProgress * 0.08F) * 0.03F;
		float step = wave(limbAngle, 13.0F) * limbDistance;
		this.rightLeg.pitch = -1.1F * step;
		this.leftLeg.pitch = 1.1F * step;
	}

	@Override
	public void animateModel(T entity, float limbAngle, float limbDistance, float tickDelta) {
		int attackTicks = entity.getAttackTicksLeft();
		if (attackTicks > 0) {
			// both fists go up and come crashing down
			float swing = -2.0F + 1.5F * wave(attackTicks - tickDelta, 10.0F);
			this.rightArm.pitch = swing;
			this.leftArm.pitch = swing;
		} else {
			float step = wave(limbAngle, 13.0F) * limbDistance;
			this.rightArm.pitch = 0.9F * step;
			this.leftArm.pitch = -0.9F * step;
		}
		this.rightArm.roll = 0.0F;
		this.leftArm.roll = 0.0F;
	}

	/** Triangle wave in [-1, 1] (same shape as vanilla's MathHelper.wrap). */
	private static float wave(float value, float period) {
		return (Math.abs(value % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
	}
}
