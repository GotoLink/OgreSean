package ogresean;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderNPC extends RenderBiped{
    public RenderNPC() {
        super(new ModelBiped(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLiving living) {
        return ((OGSEEntityEasyNPC) living).getTexture();
    }
}
