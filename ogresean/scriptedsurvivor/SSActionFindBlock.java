package ogresean.scriptedsurvivor;

import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;

/**
 * 
 * @name SSActionFindBlock
 * @description This action makes a Survivor wander around idly, until X time (in ticks) have passed,
 *  or until Survivor has selected one of the designated blocks as the target to move to
 */
public class SSActionFindBlock extends SSAction{

	/**
	 * 
	 * @param blockIDs - IDs of blocks that Survivor will attempt to find
	 * @param maxTicks - maximum time, in ticks, that Survivor will look for this block
	 */
	public SSActionFindBlock(int[] blockIDs, int maxTicks){
		BIDs = blockIDs;
		maxTicksToLook = maxTicks;
	}
	
	public SSActionFindBlock(){
		BIDs = null;
		maxTicksToLook = 0;
	}
	
	public void doAction(SSEntityScriptedSurvivor ess){
		if(ess.actionTimer == -1 || ess.path == null || ess.rand.nextFloat() < 0.001F) setNewWayPoint(ess);
		if(ess.actionTimer % 10 == 0) checkNearbyBlocks(ess);
		ess.actionTimer++;
	}
	
	
	public boolean isActionComplete(SSEntityScriptedSurvivor ess){
		if(ess.coords != null && ess.getDistance(ess.coords[0], ess.coords[1], ess.coords[2]) < 2D)
			return true;
		return ess.actionTimer >= maxTicksToLook;
	}
	
	public void exitAction(SSEntityScriptedSurvivor ess, boolean actionCanceled){
		ess.actionTimer = -1;
		if(actionCanceled){
			ess.coords = null;
			ess.setPathToEntity(null);
		}
	}
	
	private void checkNearbyBlocks(SSEntityScriptedSurvivor ess){
		int i = MathHelper.floor_double(ess.posX);
		int j = MathHelper.floor_double(ess.boundingBox.minY);
		int k = MathHelper.floor_double(ess.posZ);
		boolean flag = true;
		if(isTargetBlock(ess,i,j-1,k)){
			j--;
		}
		else if(isTargetBlock(ess,i,j+2,k)){
			j += 2;
		}
		else if(isTargetBlock(ess,i-1,j,k)){
			i--;
		}
		else if(isTargetBlock(ess,i,j,k-1)){
			k--;
		}
		else if(isTargetBlock(ess,i+1,j,k)){
			i++;
		}
		else if(isTargetBlock(ess,i,j,k+1)){
			k++;
		}
		else flag = false;
		
		if(flag){
			ess.coords = new int[3];
			ess.coords[0] = i;
			ess.coords[1] = j;
			ess.coords[2] = k;
			ess.path = ess.worldObj.getEntityPathToXYZ(ess, i, j, k, 12F);
		}
	}
	
	private void setNewWayPoint(SSEntityScriptedSurvivor ess){
		boolean flag = false;
        int j = -1;
        int k = -1;
        int l = -1;
        float f2 = -99999F;
        ess.coords = null;
        for(int i1 = 0; i1 < 10; i1++)
        {
            int j1 = MathHelper.floor_double((ess.posX + (double)ess.rand.nextInt(21)) - 10D);
            int k1 = MathHelper.floor_double((ess.posY + (double)ess.rand.nextInt(7)) - 3D);
            int l1 = MathHelper.floor_double((ess.posZ + (double)ess.rand.nextInt(21)) - 10D);
            float f3 = isTargetBlock(ess, j1, k1, l1) ? 1000F : (float) ess.getDistance(j1, k1, l1);
            if(f3 == 1000F){
            	j = j1;
                k = k1;
                l = l1;
                ess.coords = new int[3];
                ess.coords[0] = j;
                ess.coords[1] = k;
                ess.coords[2] = l;
            	flag = true;
            	break;
            }
            else if(f3 > f2)
            {
                f2 = f3;
                j = j1;
                k = k1;
                l = l1;
                flag = true;
            }
        }

        if(flag)
        {
        	ess.path = ess.worldObj.getEntityPathToXYZ(ess, j, k, l, 18F);
        }
	}
	
	/**
	 * @purpose This method checks if the block at a specific coordinate is one of the blocks
	 * that are designated for mining
	 */
	private boolean isTargetBlock(SSEntityScriptedSurvivor ess, int i, int j, int k){
		for(int x = 0; x < BIDs.length; x++)
			if(ess.worldObj.getBlock(i, j, k) == BIDs[x]) return true;
		return false;
	}
	
	/**
	 * 
	 * @param StringSample 1,3:80
	 * @param Explanation Stone/Dirt; look for 80 ticks
	 * @return FindBlock action
	 */
	public SSAction createAction(String s){
		try{
			String codes[] = s.split(":");
			String codes1[] = codes[0].split(","); //block ids
			int a[] = new int[codes1.length];
			int b = Integer.valueOf(codes[1]).intValue(); //ticks to look
			//block ids
			for(int i = 0; i < codes1.length; i++){
				a[i] = Integer.valueOf(codes1[i]).intValue();
			}
			return new SSActionFindBlock(a, b);
		}catch(Exception e){
			return null;
		}
	}
	
	private Block[] BIDs;
	private int maxTicksToLook;
}
