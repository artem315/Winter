package ru.winter.client.render;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Father Frost in his long coat, leaning on his staff. Geometry comes from blockbench/ded_moroz.bbmodel
 * (see {@link ru.winter.client.render.model.DedMorozModelData}); this class only animates it.
 */
public class DedMorozModel extends EntityModel<DedMorozRenderState> {
	private final ModelPart head;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public DedMorozModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");
	}

	@Override
	public void setupAnim(DedMorozRenderState state) {
		super.setupAnim(state); // resets every part to its Blockbench pose
		this.head.yRot = state.yRot * (float) (Math.PI / 180.0);
		this.head.xRot = state.xRot * (float) (Math.PI / 180.0);
		if (state.unhappy) {
			// shakes his head like a villager when there is nothing to trade
			this.head.zRot = 0.3F * Mth.sin(0.45F * state.ageInTicks);
			this.head.xRot = 0.4F;
		}
		float swing = Mth.cos(state.walkAnimationPos * 0.6662F) * state.walkAnimationSpeed;
		this.rightLeg.xRot = swing * 0.7F;
		this.leftLeg.xRot = -swing * 0.7F;
		// the staff arm barely moves, the free arm swings
		this.rightArm.xRot = -swing * 0.25F - 0.1F;
		this.leftArm.xRot = swing * 0.6F;
		this.leftArm.zRot = -0.05F - Mth.cos(state.ageInTicks * 0.09F) * 0.03F;
	}
}
