package ogresean.bats;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class BBRenderBat extends RenderLiving {
	//static private final ItemStack slime = new ItemStack(Item.slimeBall);
	private BBModelBat batModel;

	public BBRenderBat(BBModelBat modelbat, float f) {
		super(modelbat, f);
		batModel = modelbat;
	}

	//	//from RenderBiped
	//    protected void renderEquippedItems(EntityLiving entityliving, float f)
	//    {
	//    	BBEntityBat bat = (BBEntityBat) entityliving;
	//    	if(bat.batAction > 2)
	//    		renderSlimeBalls(bat, f);
	//    }
	//    
	//    //slime balls that appear above bat that indicate bat's health
	//    protected void renderSlimeBalls(BBEntityBat bat, float f)
	//    {
	//    	for(int i = 0; i < bat.getMaxHealth(); i++){
	//    		GL11.glPushMatrix();
	//        	float f3 = 0.125F;
	//            GL11.glTranslatef(0.25F + ((float)i) / 5F, 0.1875F + bat.getEyeHeight() * 2, -0.1875F);
	//            GL11.glScalef(f3, f3, f3);
	//            //GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
	//            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
	//            GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
	//            int l = 0x40a955;
	//            float f7 = (float)(l >> 16 & 0xff) / 255F;
	//            float f11 = (float)(l >> 8 & 0xff) / 255F;
	//            float f15 = (float)(l & 0xff) / 255F;
	//            if(i <= bat.health) GL11.glColor4f(f3 * f7, f3 * f11, f3 * f15, 0.2F);
	//            else GL11.glColor4f(f3 * f11, f3 * f15, f3 * f7, 0.4F);
	//            renderManager.itemRenderer.renderItem(bat, slime);
	//            GL11.glPopMatrix();
	//        }
	//    }
	//d: posX - playerPosX
	//d1: posY - playerPosY
	//d2: posZ - playerPosZ
	//f: rotationYaw
	//f1: scale
	@Override
	public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
		super.doRender(entity, d, d1 + getBatYOffset((BBEntityBat) entity), d2, f, f1);
		//while bat sleeping, set wing rotation to:
		//Left Wing: 90, -130, -50
		//Right Wing: 90, 130, 50
	}

	@Override
	protected void func_110827_b(EntityLiving entityliving, double d, double d1, double d2, float f, float f1) {
		super.func_110827_b(entityliving, d, d1, d2, f, f1);
		if (((BBEntityBat) entityliving).getBatAction() > 2 && entityliving.getDistanceToEntity(renderManager.livingPlayer) < 32F) {
			float f3 = 0.01666667F * 1.6F;
			GL11.glPushMatrix();
			GL11.glTranslatef((float) d + 0.0F, (float) d1 + 1.1F, (float) d2);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
			GL11.glScalef(-f3, -f3, f3);
			GL11.glDisable(2896 /* GL_LIGHTING */);
			GL11.glDepthMask(false);
			GL11.glDisable(2929 /* GL_DEPTH_TEST */);
			GL11.glEnable(3042 /* GL_BLEND */);
			GL11.glBlendFunc(770, 771);
			Tessellator tessellator = Tessellator.instance;
			byte byte0 = 10;
			GL11.glDisable(3553 /* GL_TEXTURE_2D */);
			tessellator.startDrawingQuads();
			float f5 = entityliving.getHealth();
			float f6 = entityliving.getMaxHealth();
			float f7 = f5 / f6;
			float f8 = 50F * f7;
			tessellator.setColorRGBA_F(1.0F, 0.0F, 0.0F, 1.0F);
			tessellator.addVertex(-25F + f8, -10 + byte0, 0.0D);
			tessellator.addVertex(-25F + f8, -6 + byte0, 0.0D);
			tessellator.addVertex(25D, -6 + byte0, 0.0D);
			tessellator.addVertex(25D, -10 + byte0, 0.0D);
			tessellator.setColorRGBA_F(0.0F, 1.0F, 0.0F, 1.0F);
			tessellator.addVertex(-25D, -10 + byte0, 0.0D);
			tessellator.addVertex(-25D, -6 + byte0, 0.0D);
			tessellator.addVertex(f8 - 25F, -6 + byte0, 0.0D);
			tessellator.addVertex(f8 - 25F, -10 + byte0, 0.0D);
			tessellator.draw();
			GL11.glEnable(3553 /* GL_TEXTURE_2D */);
			GL11.glEnable(2929 /* GL_DEPTH_TEST */);
			GL11.glDepthMask(true);
			GL11.glEnable(2896 /* GL_LIGHTING */);
			GL11.glDisable(3042 /* GL_BLEND */);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
	}

	protected float caveBatWings(BBEntityBat bat, float f) {
		float f1 = bat.winge + (bat.wingb - bat.winge) * f;
		float f2 = bat.wingd + (bat.wingc - bat.wingd) * f;
		return (MathHelper.sin(f1) + 1.0F) * f2;
	}

	protected double getBatYOffset(BBEntityBat bat) {
		if (bat.getBatAction() == 0 || bat.getBatAction() == 3)
			return bat.getScale();
		else
			return 0.0D;
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ((BBEntityBat) entity).getTexture();
	}

	@Override
	protected float handleRotationFloat(EntityLivingBase entityliving, float f) {
		return caveBatWings((BBEntityBat) entityliving, f);
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entityliving, float f) {
		BBEntityBat bat = (BBEntityBat) entityliving;
		batModel.isSleeping = bat.getBatAction() == 0 || bat.getBatAction() == 3;
	}

	//f = func_170_d
	//f1 = RenderYawOffset
	//f2 = scale?
	protected void rotateBat(BBEntityBat bat, float f, float f1, float f2) {
		GL11.glRotatef(180F - f1, 0.0F, 1.0F, 0.0F);
		GL11.glScalef(bat.getScale(), bat.getScale(), bat.getScale());
		if (bat.deathTime > 0) {
			float f3 = (((bat.deathTime + f2) - 1.0F) / 20F) * 1.6F;
			f3 = MathHelper.sqrt_float(f3);
			if (f3 > 1.0F) {
				f3 = 1.0F;
			}
			GL11.glRotatef(f3 * getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
		} else if (bat.getBatAction() == 0 || bat.getBatAction() == 3) {
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		}
	}

	@Override
	protected void rotateCorpse(EntityLivingBase entityliving, float f, float f1, float f2) {
		rotateBat((BBEntityBat) entityliving, f, f1, f2);
	}
}
