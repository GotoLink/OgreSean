package ogresean.scriptedsurvivor;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class SSRenderSurvivor extends RenderLiving {
	public SSRenderSurvivor(ModelBiped modelbiped){
		super(modelbiped, 0.5F);
		mainBiped = modelbiped;
        modelArmorChestplate = new ModelBiped(1.0F);
        modelArmor = new ModelBiped(0.5F);
	}
	
	protected boolean shouldRenderPass(EntityLiving entityliving, int i, float f)
    {
        return setArmorModel((SSEntityScriptedSurvivor)entityliving, i, f);
    }
	
	//from RenderBiped
    protected void renderEquippedItems(EntityLiving entityliving, float f)
    {
        ItemStack itemstack = entityliving.getHeldItem();
        if(itemstack != null)
        {
            GL11.glPushMatrix();
            mainBiped.bipedRightArm.postRender(0.0625F);
            GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);
            if(itemstack.itemID < 256 && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack.getItem()).getRenderType()))
            {
                float f1 = 0.5F;
                GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                f1 *= 0.75F;
                GL11.glRotatef(20F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(f1, -f1, f1);
            } else
            if(itemstack.getItem().isFull3D())
            {
                float f2 = 0.625F;
                GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
                GL11.glScalef(f2, -f2, f2);
                GL11.glRotatef(-100F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
            } else
            {
                float f3 = 0.375F;
                GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
                GL11.glScalef(f3, f3, f3);
                GL11.glRotatef(60F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-90F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(20F, 0.0F, 0.0F, 1.0F);
            }
            renderManager.itemRenderer.renderItem(entityliving, itemstack);
            GL11.glPopMatrix();
        }
    }
	
	protected boolean setArmorModel(SSEntityScriptedSurvivor entitysurvivor, int i, float f)
    {
        ItemStack itemstack = entitysurvivor.pack.armorItemInSlot(3 - i);
        if(itemstack != null)
        {
            Item item = itemstack.getItem();
            if(item instanceof ItemArmor)
            {
                ItemArmor itemarmor = (ItemArmor)item;
                loadTexture((new StringBuilder()).append("/armor/").append(armorFilenamePrefix[itemarmor.renderIndex]).append("_").append(i != 2 ? 1 : 2).append(".png").toString());
                float f1 = entitysurvivor.getEntityBrightness(f);
                GL11.glColor3f(f1 * 0.81F, f1 * 0.73F, f1 * 0.66F); //color armor slightly differently       
                ModelBiped modelbiped = i != 2 ? modelArmorChestplate : modelArmor;
                modelbiped.bipedHead.showModel = i == 0;
                modelbiped.bipedHeadwear.showModel = i == 0;
                modelbiped.bipedBody.showModel = i == 1 || i == 2;
                modelbiped.bipedRightArm.showModel = i == 1;
                modelbiped.bipedLeftArm.showModel = i == 1;
                modelbiped.bipedRightLeg.showModel = i == 2 || i == 3;
                modelbiped.bipedLeftLeg.showModel = i == 2 || i == 3;
                setRenderPassModel(modelbiped);
                return true;
            }
        }
        return false;
    }
	
	protected void passSpecialRender(EntityLiving entityliving, double d, double d1, double d2)
    {
        renderName((SSEntityScriptedSurvivor)entityliving, d, d1, d2);
    }
	
	protected void renderName(SSEntityScriptedSurvivor ess, double d, double d1, double d2)
    {
        float f = 1.6F;
            float f1 = 0.01666667F * f;
            float f2 = ess.getDistanceToEntity(renderManager.livingPlayer);
            if(f2 < 100F)
            {
                StringBuilder s = new StringBuilder(mod_ScriptedSurvivors.scripts.get(ess.type).name);
                if(mod_ScriptedSurvivors.survivorDebug)
                    s.append(" ").append(Integer.valueOf(ess.currentAction).toString());
               renderLivingLabel(ess, s.toString(), d, d1, d2, 100);
                    
            }
    }
	
	private ModelBiped modelArmorChestplate;
    private ModelBiped modelArmor;
    protected ModelBiped mainBiped; //from RenderBiped
    private static final String armorFilenamePrefix[] = {
        "cloth", "chain", "iron", "diamond", "gold"
    };
}
