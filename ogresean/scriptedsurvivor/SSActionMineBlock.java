package ogresean.scriptedsurvivor;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * PROCESS:
 ** 1. When action entered, select preferred tool from inventory if possible, else use fists
 * 2. if no ess.coords, look nearby where standing and set coords to valid block; if none found, end action
 * 3. Begin swinging tool multiple times (based on tool strength versus block)
 * 4. Destroy block, leaving entityitem(if applicable), and damaging/destroying tool(if applicable)
 * 5a. If max num reached, end action
 * 5b. else, return to step 1
 *
 *
 */
public class SSActionMineBlock extends SSAction{

	/**
	 * 
	 * @param blockID - blocks to mine and collect
	 * @param maxNum - maximum number of the block that can be collected before action ends
	 * @param miningTools - item ids of mining tools to be used on the block; ids arranged from best tool[0] to worst tool[size]
	 */
	public SSActionMineBlock(int[] blockIDs, int maxNum, int[] miningTools){
		BIDs = blockIDs;
		maxCollect = maxNum;
		toolIDs = miningTools;
	}
	
	public SSActionMineBlock(){
		BIDs = null;
		maxCollect = 0;
		toolIDs = null;
	}
	
	public void doAction(SSEntityScriptedSurvivor ess){
		if(ess.actionTimer == -1) setDefaultValues(ess); //Step 0 : Done one time ever
		else if(ess.actionTimer == 11)selectPreferredTool(ess); //Step 1 : Done once
		else if(ess.actionTimer == 21) checkForBlock(ess); //Step 2 : Done once
		else if(ess.actionTimer >= 76 && ess.actionTimer <= 80) faceCoordinates(ess); //Done few times
		else if(ess.actionTimer == 81) swingToolAndDamageBlock(ess); //Step 4 : Done multiple until blcok destroyed
		//while in idle time, survivor has chance to collect item
		else if(ess.actionTimer >= 90 && ess.actionTimer <= 100 && ess.swingProgress != 0.0F) swingArm(ess);
		else if((ess.actionTimer >= 101 && ess.actionTimer <= 120 && ess.entityToAttack == null)
				|| ess.actionTimer == 121) checkMaxItems(ess); //Step 5: Done once
		ess.actionTimer++;
	}
	
	//completed based on actionTimer
	public boolean isActionComplete(SSEntityScriptedSurvivor ess){
		if(ess.actionTimer >= 500)
			return true;
		else
			return false;
	}
	
	public void exitAction(SSEntityScriptedSurvivor ess, boolean actionCanceled){
		ess.actionTimer = -1;
		ess.nums = null;
    	ess.prevSwingProgress = 0.0F;
    	ess.swingProgress = 0.0F;
    	ess.coords = null;
		if(actionCanceled){
			ess.coords = null;
			ess.setPathToEntity(null);
		}
	}
	
	/**
	 * @purpose This method checks if the block at a specific coordinate is one of the blocks
	 * that are designated for mining
	 */
	private boolean isTargetBlock(SSEntityScriptedSurvivor ess, int i, int j, int k){
		for(int x = 0; x < BIDs.length; x++)
			if(ess.worldObj.getBlockId(i, j, k) == BIDs[x]) return true;
		return false;
	}
	
	/**
	 * @purpose This method sets all of the default values for this action
	 */
	private void setDefaultValues(SSEntityScriptedSurvivor ess){
		ess.nums = new float[5];
    	ess.nums[0] = 0.0F; //block damage
    	ess.nums[1] = 0.0F; //block damage sound effect
    	ess.nums[2] = 0.0F; //number blocks destroyed
    	ess.nums[3] = 9999F; //rotationYaw
    	ess.nums[4] = 9999F; //rotationPitch
    	ess.prevSwingProgress = 0.0F;
    	ess.swingProgress = 0.0F;
    	ess.actionTimer = 10;
	}
	
	/**
	 * @purpose This method makes the Survivor's current item the best available tool in his inventory,
	 *  or just his hands if no tool is available
	 */
	private void selectPreferredTool(SSEntityScriptedSurvivor ess){
		ess.actionTimer = 20;
		ess.setPathToEntity(null);
		for(int i = 0; i < toolIDs.length; i++){
			if(ess.pack.setCurrentItem(toolIDs[i])) return;
		}
		ess.pack.currentItem = -1;
	}
	
	/**
	 * @purpose This method checks if the correct block is at the survivor's coordinates with less than 2D distance; 
	 * if not, the 3x3x3 grid of blocks surrounding the survivor are checked for the block; If block still not found, this action ends
	 */
	private void checkForBlock(SSEntityScriptedSurvivor ess){
		//check if coords fits criteria
		if(ess.coords != null && isTargetBlock(ess, ess.coords[0], ess.coords[1], ess.coords[2]) && ess.getDistance(ess.coords[0], ess.coords[1], ess.coords[2]) < 2.2D){
			ess.actionTimer = 75;
			return;
		}
		int x = MathHelper.floor_double(ess.posX);
		int y = MathHelper.floor_double(ess.boundingBox.minY);
		int z = MathHelper.floor_double(ess.posZ);
		
		//grid search around survivor for valid block
		for(int i = x-1; i <= x + 1; i++)
			for(int j = y-1; j <= y + 1; j++)
				for(int k = z-1; k <= z + 1; k++)
					if(isTargetBlock(ess, i, j, k)){
						ess.coords = new int[3];
						ess.coords[0] = i;
						ess.coords[1] = j;
						ess.coords[2] = k;
						ess.actionTimer = 75;
						return;
					}
		//failure: end action
		ess.actionTimer = 500;
		
	}
	
	/**
	 * @purpose This method makes the survivor turn his head toward his coordinates.
	 */
	private void faceCoordinates(SSEntityScriptedSurvivor ess){
		if(ess.nums[3] == 9999F) ess.nums[3] = ess.rotationYaw;
		if(ess.nums[4] == 9999F) ess.nums[4] = ess.rotationPitch;
		double d = ess.coords[0] - ess.posX;
        double d2 = ess.coords[2] - ess.posZ;
        double d1;
        float f = 30F;
        d1 = ess.coords[1] - (ess.posY + (double)ess.getEyeHeight());
        double d3 = MathHelper.sqrt_double(d * d + d2 * d2);
        float f1 = (float)((Math.atan2(d2, d) * 180D) / 3.1415927410125732D) - 90F;
        float f2 = (float)(-(Math.atan2(d1, d3) * 180D) / 3.1415927410125732D);
        ess.prevRotationPitch = ess.nums[4];
        ess.prevRotationYaw = ess.nums[3];
        ess.nums[4] = updateSurvivorRotation(ess.nums[4], f2, f); //minus removed
        ess.nums[3] = updateSurvivorRotation(ess.nums[3], f1, f);
        ess.rotationYaw = ess.nums[3];
        ess.rotationPitch = ess.nums[4];
	}
	
	private float updateSurvivorRotation(float f, float f1, float f2)
    {
        float f3;
        for(f3 = f1 - f; f3 < -180F; f3 += 360F) { }
        for(; f3 >= 180F; f3 -= 360F) { }
        if(f3 > f2)
        {
            f3 = f2;
        }
        if(f3 < -f2)
        {
            f3 = -f2;
        }
        return f + f3;
    }
	
	/**
	 * @purpose This method makes the survivor emulate the arm swinging and block destroying behavior of a player.
	 * @num[0] Used as currentDamage
	 * @num[1] Used for block damaging sound
	 * @num[2] Used for total number of blocks collected
	 * @num[3] Used for handling rotationYaw
	 * @num[4] Used for handling rotationPitch
	 */
	private void swingToolAndDamageBlock(SSEntityScriptedSurvivor ess){
		//get block at coordinates
		faceCoordinates(ess);
		Block block = ess.worldObj.getBlock(ess.coords[0], ess.coords[1], ess.coords[2]);
		if(block != null){
			//block cracking effect
			//drawBlockBreaking(ess, 1.0F); //probably won't work right
			//block destroying effect
			ModLoader.getMinecraftInstance().effectRenderer.addBlockHitEffects(ess.coords[0], ess.coords[1], ess.coords[2], ess.rand.nextInt(6));
			
			//block sound effect
			ess.nums[1] = ess.nums[1] > 400F ? 0.0F : ess.nums[1] + 1.0F;
			if(ess.nums[1] % 4F == 0.0F)
        	{
            	ess.worldObj.playSound(block.stepSound.stepSoundDir(), (float) ess.coords[0] + 0.5F, (float) ess.coords[1] + 0.5F, (float) ess.coords[2] + 0.5F, (block.stepSound.getVolume() + 1.0F) / 8F, block.stepSound.getPitch() * 0.5F);
        	}
			
			//arm swinging effect
			swingArm(ess);
			
			//block destroying progress
			ess.nums[0] += ess.blockStrength(block);
			if(ess.nums[0] > 1.0F){ //block destruction complete
		    	
				//block destruction effect
				ModLoader.getMinecraftInstance().effectRenderer.addBlockDestroyEffects(ess.coords[0], ess.coords[1], ess.coords[2], bid, 0);
				
				//destroy block
				ItemStack itemstack = ess.pack.getCurrentItem();
		        boolean flag1 = ess.canHarvestBlock(block);
		        if(itemstack != null && itemstack.getItem() > 255) //damage item
		        {
		        	itemstack.getItem().onBlockDestroyed(itemstack, ess.coords[0], ess.coords[1], ess.coords[2], 0, ess);
		            if(itemstack.stackSize == 0)
		            {
		                ess.pack.setInventorySlotContents(ess.pack.currentItem, null);
		            }
		        }
		        if(flag1)
		        {
		        	int i1 = block == Blocks.snow ? 1 : block.quantityDropped(ess.worldObj.rand);
		                for(int j1 = 0; j1 < i1; j1++)
		                {
		                     int k1 = block == Blocks.snow ? Items.snowball : block.idDropped(0, ess.worldObj.rand);
		                    if(k1 > 0)
		                    {
		                        float f1 = 0.7F;
		                        double d = (double)(ess.worldObj.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		                        double d1 = (double)(ess.worldObj.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		                        double d2 = (double)(ess.worldObj.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		                        EntityItem entityitem = new EntityItem(ess.worldObj, (double)ess.coords[0] + d, (double)ess.coords[1] + d1, (double)ess.coords[2] + d2, new ItemStack(k1, 1, Block.blocksList[bid].damageDropped(0)));
		                      //make item fly toward survivor
				                double md = ess.posX - entityitem.posX;
				                double md1 = ess.posY - entityitem.posY;
				                double md2 = ess.posZ - entityitem.posZ;
				                double md3 = MathHelper.sqrt_double(md * md + md1 * md1 + md2 * md2);

				                entityitem.setVelocity(md / md3 * 0.30000000000000003D, md1 / md3 * 0.30000000000000003D, md2 / md3 * 0.30000000000000003D);
				                entityitem.delayBeforeCanPickup = 0;
				                ess.worldObj.entityJoinedWorld(entityitem);
								ess.entityToAttack = entityitem;
		                    }
		                }
		        }
				ess.worldObj.setBlock(ess.coords[0], ess.coords[1], ess.coords[2], Blocks.air);
				ess.actionTimer = 90;
				ess.nums[2] += 1.0F;
				ess.nums[3] = 9999F;
		        ess.nums[4] = 9999F;
			}
			else
				ess.actionTimer = 80;
		}
		else ess.actionTimer = 90;
	}
	
	private void swingArm(SSEntityScriptedSurvivor ess){
		//arm swinging effect
		ess.prevSwingProgress = ess.swingProgress;
		ess.swingProgress = ess.swingProgress == 0.875F ? 0.0F : ess.swingProgress + 0.125F;
	}
	
	private void checkMaxItems(SSEntityScriptedSurvivor ess){
		ess.entityToAttack = null;
		if(ess.nums[2] >= maxCollect) ess.actionTimer = 500;
		else{
	    	ess.nums[0] = 0.0F; //block damage
	    	ess.nums[1] = 0.0F; //block damage sound effect
	    	ess.prevSwingProgress = 0.0F;
	    	ess.swingProgress = 0.0F;
	    	ess.actionTimer = 10;
		}
	}
	
//	private void drawBlockBreaking(SSEntityScriptedSurvivor ess, float f)
//    {
//		EntityPlayer ep = ModLoader.getMinecraftInstance().thePlayer;
//        GL11.glEnable(2884 /*GL_CULL_FACE*/);
//        GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
//        Tessellator tessellator = Tessellator.instance;
//        GL11.glEnable(3042 /*GL_BLEND*/);
//        GL11.glEnable(3008 /*GL_ALPHA_TEST*/);
//        GL11.glBlendFunc(770, 1);
//        GL11.glColor4f(1.0F, 1.0F, 1.0F, (MathHelper.sin((float)System.currentTimeMillis() / 100F) * 0.2F + 0.4F) * 0.5F);
//        if(ess.nums[0] > 0.0F)
//            {
//                GL11.glBlendFunc(774, 768);
//                int j = ModLoader.getMinecraftInstance().renderEngine.getTexture("/terrain.png");
//                GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, j);
//                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
//                GL11.glPushMatrix();
//                int k = ess.worldObj.getBlockId(ess.coords[0], ess.coords[1], ess.coords[2]);
//                Block block = k <= 0 ? null : Block.blocksList[k];
//                GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
//                GL11.glPolygonOffset(-3F, -3F);
//                GL11.glEnable(32823 /*GL_POLYGON_OFFSET_FILL*/);
//                double d = ep.lastTickPosX + (ep.posX - ep.lastTickPosX) * (double)f;
//                double d1 = ep.lastTickPosY + (ep.posY - ep.lastTickPosY) * (double)f;
//                double d2 = ep.lastTickPosZ + (ep.posZ - ep.lastTickPosZ) * (double)f;
//                if(block == null)
//                {
//                    block = Block.stone;
//                }
//                GL11.glEnable(3008 /*GL_ALPHA_TEST*/);
//                tessellator.startDrawingQuads();
//                tessellator.setTranslationD(-d, -d1, -d2);
//                tessellator.disableColor();
//                mod_ScriptedSurvivors.globalRenderBlocks.renderBlockUsingTexture(block, ess.coords[0], ess.coords[1], ess.coords[2], 240 + (int)(ess.nums[0] * 10F));
//                tessellator.draw();
//                tessellator.setTranslationD(0.0D, 0.0D, 0.0D);
//                GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
//                GL11.glPolygonOffset(0.0F, 0.0F);
//                GL11.glDisable(32823 /*GL_POLYGON_OFFSET_FILL*/);
//                GL11.glEnable(3008 /*GL_ALPHA_TEST*/);
//                GL11.glDepthMask(true);
//                GL11.glPopMatrix();
//            }
//        GL11.glDisable(3042 /*GL_BLEND*/);
//        GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
//    }
	
	/**
	 * 
	 * @param StringSample 1,16:12:1,3
	 * @param Explanation mine Stone/Coal; get up to 12; use Stone or Dirt as tools
	 * @return MineBlock action
	 */
	public SSAction createAction(String s){
		try{
			String codes[] = s.split(":");
			String codes1[] = codes[0].split(","); //block ids
			String codes2[] = codes[2].split(","); //tool ids
			int a[] = new int[codes1.length];
			int num = Integer.valueOf(codes[1]).intValue();
			int b[] = new int[codes2.length];
			//block ids
			for(int i = 0; i < codes1.length; i++){
				a[i] = Integer.valueOf(codes1[i]).intValue();
			}
			//tool ids
			for(int i = 0; i < codes2.length; i++){
				b[i] = Integer.valueOf(codes2[i]).intValue();
			}
			return new SSActionMineBlock(a, num, b);
		}catch(Exception e){
			return null;
		}
	}
	
	private Block[] BIDs; //block ids to mine and collect
	private int maxCollect; //maximum number to collect before action ends
	private int[] toolIDs; //item ids of tools to use for mining the block
}
