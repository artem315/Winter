package ru.winter.client.render;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The yeti: a hunched, knuckle-walking beast. Geometry comes from blockbench/yeti.bbmodel
 * (see {@link ru.winter.client.render.model.YetiModelData}); this class only animates it.
 */
public class YetiModel extends EntityModel<YetiRenderState> {
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public YetiModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");
	}

	@Override
	public void setupAnim(YetiRenderState state) {
		super.setupAnim(state); // resets every part to its Blockbench pose
		this.head.yRot = state.yRot * (float) (Math.PI / 180.0);
		this.head.xRot = state.xRot * (float) (Math.PI / 180.0);
		this.body.xRot += Mth.sin(state.ageInTicks * 0.08F) * 0.03F; // heavy breathing

		float step = Mth.triangleWave(state.walkAnimationPos, 13.0F) * state.walkAnimationSpeed;
		this.rightLeg.xRot = -1.1F * step;
		this.leftLeg.xRot = 1.1F * step;
		if (state.attackTicks > 0.0F) {
			// both fists go up and come crashing down
			float swing = -2.0F + 1.5F * Mth.triangleWave(state.attackTicks, 10.0F);
			this.rightArm.xRot = swing;
			this.leftArm.xRot = swing;
		} else {
			this.rightArm.xRot = 0.9F * step;
			this.leftArm.xRot = -0.9F * step;
		}
	}
}
