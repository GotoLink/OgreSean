package ogresean.bats;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class BBModelBat extends ModelBase {
	ModelRenderer Body;
	ModelRenderer Left_Wing;
	ModelRenderer Right_Wing;
	ModelRenderer Head;
	ModelRenderer Right_Ear;
	ModelRenderer Left_Ear;
	ModelRenderer Left_Foot;
	ModelRenderer Right_Foot;
	public boolean isSleeping;

	public BBModelBat() {
		float scale = 0F;
		Left_Foot = new ModelRenderer(this, 19, 4);
		Left_Foot.addBox(0F, 0F, 0F, 1, 2, 1, scale);
		Left_Foot.setRotationPoint(-2F, 22F, -3F);
		Right_Foot = new ModelRenderer(this, 19, 4);
		Right_Foot.addBox(0F, 0F, 0F, 1, 2, 1, scale);
		Right_Foot.setRotationPoint(1F, 22F, -3F);
		Body = new ModelRenderer(this, 0, 6);
		Body.addBox(0F, 0F, 0F, 4, 7, 3, scale);
		Body.setRotationPoint(-2F, 16F, -2F);
		Head = new ModelRenderer(this, 0, 0);
		Head.addBox(0F, 0F, 0F, 4, 3, 3, scale);
		Head.setRotationPoint(-2F, 13F, -3F);
		Left_Ear = new ModelRenderer(this, 19, 0);
		Left_Ear.addBox(0F, 0F, 0F, 1, 1, 1, scale);
		Left_Ear.setRotationPoint(-2F, 12F, -2F);
		Right_Ear = new ModelRenderer(this, 19, 0);
		Right_Ear.addBox(0F, 0F, 0F, 1, 1, 1, scale);
		Right_Ear.setRotationPoint(1F, 12F, -2F);
		Left_Wing = new ModelRenderer(this, 19, 9);
		Left_Wing.addBox(-7F, 0F, 0F, 7, 5, 1, scale);
		Left_Wing.setRotationPoint(-2F, 17F, 0F);
		Right_Wing = new ModelRenderer(this, 19, 9);
		Right_Wing.addBox(0F, 0F, 0F, 7, 5, 1, scale);
		Right_Wing.setRotationPoint(2F, 17F, 0F);
	}

	//f5 is related to scale
	//f4 is related to rotationPitch
	//f3 is related to rotationYaw - rotationYawOffset
	//f2 is related to how long the entity has existed or to other variables relating to wing flapping, etc
	//f1 is related to how far the entity has just walked, or how far the entity went when attacked
	//f is related to total distance entity has walked
	@Override
	public void render(Entity ent, float f, float f1, float f2, float f3, float f4, float f5) {
		setRotationAngles(f, f1, f2, f3, f4, f5, ent);
		Head.render(f5);
		Left_Wing.render(f5);
		Body.render(f5);
		Right_Wing.render(f5);
		Right_Ear.render(f5);
		Left_Ear.render(f5);
		Left_Foot.render(f5);
		Right_Foot.render(f5);
	}

	@Override
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity ent) {
		Head.rotateAngleX = -(f4 / 57.29578F);
		Head.rotateAngleY = f3 / 57.29578F;
		Right_Ear.rotateAngleX = Head.rotateAngleX;
		Right_Ear.rotateAngleY = Head.rotateAngleY;
		Left_Ear.rotateAngleX = Head.rotateAngleX;
		Left_Ear.rotateAngleY = Head.rotateAngleY;
		Left_Foot.rotateAngleX = Head.rotateAngleX;
		Left_Foot.rotateAngleY = Head.rotateAngleY;
		Right_Foot.rotateAngleX = Head.rotateAngleX;
		Right_Foot.rotateAngleY = Head.rotateAngleY;
		if (isSleeping) {
			Left_Wing.rotateAngleX = 1.22173F;
			Left_Wing.rotateAngleY = -2.268928F;
			Left_Wing.rotateAngleZ = 1.570796F;
			Right_Wing.rotateAngleX = 1.22173F;
			Right_Wing.rotateAngleY = 2.268928F;
			Right_Wing.rotateAngleZ = -1.570796F;
		} else {
			Right_Wing.rotateAngleX = 0F;
			Right_Wing.rotateAngleY = 0F;
			Right_Wing.rotateAngleZ = -f2;
			Left_Wing.rotateAngleX = 0F;
			Left_Wing.rotateAngleY = 0F;
			Left_Wing.rotateAngleZ = f2;
		}
	}
}
