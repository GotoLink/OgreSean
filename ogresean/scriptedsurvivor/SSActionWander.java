package ogresean.scriptedsurvivor;

import net.minecraft.util.MathHelper;

/**
 * 
 * @name SSActionWander
 * @description This action makes a Survivor wander around idly, until X time (in ticks) have passed
 *
 */
public class SSActionWander extends SSAction{
	/**
	 * 
	 * @param time - amount of ticks that Survivor will wander
	 */
	public SSActionWander(int time){
		ticksToWander = time;
	}
	
	public SSActionWander(){
		ticksToWander = 0;
	}
	
	public void doAction(SSEntityScriptedSurvivor ess){
		if(ess.actionTimer == -1 || ess.path == null || ess.rand.nextFloat() < 0.001F) setNewWayPoint(ess);
		ess.actionTimer++;
	}
	
	
	public boolean isActionComplete(SSEntityScriptedSurvivor ess){
		if(ess.actionTimer >= ticksToWander)
			return true;
		else
			return false;
	}
	
	public void exitAction(SSEntityScriptedSurvivor ess, boolean actionCanceled){
		ess.actionTimer = -1;
		if(actionCanceled){
			ess.setPathToEntity(null);
		}
	}
	
	private void setNewWayPoint(SSEntityScriptedSurvivor ess){
		boolean flag = false;
        int j = -1;
        int k = -1;
        int l = -1;
        float f2 = -99999F;
        for(int i1 = 0; i1 < 10; i1++)
        {
            int j1 = MathHelper.floor_double((ess.posX + (double) ess.rand.nextInt(21)) - 10D);
            int k1 = MathHelper.floor_double((ess.posY + (double)ess.rand.nextInt(7)) - 3D);
            int l1 = MathHelper.floor_double((ess.posZ + (double)ess.rand.nextInt(21)) - 10D);
            float f3 = (float) ess.getDistance(j1, k1, l1);
            if(f3 > f2)
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
	 * 
	 * @param StringSample 50
	 * @param Explanation 50 ticks
	 * @return Wander action
	 */
	public SSAction createAction(String s){
		try{
			int a = Integer.valueOf(s).intValue();
			return new SSActionWander(a);
		}catch(Exception e){
			return null;
		}
	}

	
	private int ticksToWander;
}
