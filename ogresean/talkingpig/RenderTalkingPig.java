package ogresean.talkingpig;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderPig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

import org.lwjgl.opengl.GL11;

public class RenderTalkingPig extends RenderPig {
	public RenderTalkingPig(ModelBase modelbase, ModelBase modelbase1, float f) {
		super(modelbase, modelbase1, f);
	}

	@Override
	public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
		doRenderLiving((EntityLiving) entity, d, d1, d2, f, f1);
	}

	@Override
	public void doRenderLiving(EntityLiving entityliving, double d, double d1, double d2, float f, float f1) {
		super.doRenderLiving(entityliving, d, d1, d2, f, f1);
		doRenderPigName((EntityTalkingPig) entityliving, d, d1, d2, f, f1);
	}

	public void doRenderPigName(EntityTalkingPig talkpig, double d, double d1, double d2, float f, float f1) {
		float f2 = 1.6F;
		float f3 = 0.01666667F * f2;
		float f4 = talkpig.getDistanceToEntity(renderManager.livingPlayer);
		if (f4 < 16) {
			FontRenderer fontrenderer = getFontRendererFromRenderManager();
			GL11.glPushMatrix();
			GL11.glTranslatef((float) d + 0.0F, (float) d1 + 1.3F, (float) d2);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
			GL11.glScalef(-f3, -f3, f3);
			String s = talkpig.getEntityName();
			String s1 = talkpig.getLevelMessage();
			GL11.glDisable(2896 /* GL_LIGHTING */);
			GL11.glDepthMask(false);
			GL11.glDisable(2929 /* GL_DEPTH_TEST */);
			GL11.glEnable(3042 /* GL_BLEND */);
			GL11.glBlendFunc(770, 771);
			Tessellator tessellator = Tessellator.instance;
			byte byte0 = 0;
			byte byte1 = -12;
			GL11.glDisable(3553 /* GL_TEXTURE_2D */);
			tessellator.startDrawingQuads();
			int j = fontrenderer.getStringWidth(s) / 2;
			if (s1 != null) {
				int j1 = fontrenderer.getStringWidth(s1) / 2;
				tessellator.setColorRGBA_F(0.96F, 0.25F, 0.33F, 0.25F);
				tessellator.addVertex(-j - 1, -1 + byte0, 0.0D);
				tessellator.addVertex(-j - 1, 8 + byte0, 0.0D);
				tessellator.addVertex(j + 1, 8 + byte0, 0.0D);
				tessellator.addVertex(j + 1, -1 + byte0, 0.0D);
				tessellator.addVertex(-j1 - 1, -1 + byte1, 0.0D);
				tessellator.addVertex(-j1 - 1, 8 + byte1, 0.0D);
				tessellator.addVertex(j1 + 1, 8 + byte1, 0.0D);
				tessellator.addVertex(j1 + 1, -1 + byte1, 0.0D);
				tessellator.draw();
				GL11.glEnable(3553 /* GL_TEXTURE_2D */);
				fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, byte0, 0x20ffffff);
				fontrenderer.drawString(s1, -fontrenderer.getStringWidth(s1) / 2, byte1, 0x20ffffff);
				GL11.glEnable(2929 /* GL_DEPTH_TEST */);
				GL11.glDepthMask(true);
				fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, byte0, -1);
				fontrenderer.drawString(s1, -fontrenderer.getStringWidth(s1) / 2, byte1, -1);
			} else {
				tessellator.setColorRGBA_F(0.96F, 0.25F, 0.33F, 0.25F);
				tessellator.addVertex(-j - 1, -1 + byte0, 0.0D);
				tessellator.addVertex(-j - 1, 8 + byte0, 0.0D);
				tessellator.addVertex(j + 1, 8 + byte0, 0.0D);
				tessellator.addVertex(j + 1, -1 + byte0, 0.0D);
				tessellator.draw();
				GL11.glEnable(3553 /* GL_TEXTURE_2D */);
				fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, byte0, 0x20ffffff);
				GL11.glEnable(2929 /* GL_DEPTH_TEST */);
				GL11.glDepthMask(true);
				fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, byte0, -1);
			}
			GL11.glEnable(2896 /* GL_LIGHTING */);
			GL11.glDisable(3042 /* GL_BLEND */);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
	}
}
