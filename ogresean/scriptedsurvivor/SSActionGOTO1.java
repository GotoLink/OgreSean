package ogresean.scriptedsurvivor;


import net.minecraft.item.ItemStack;

/**
 * 
 * @name SSActionGOTO1
 * @description This action changes a Survivor's current action depending on if certain items are in the survivor's inventory
 */
public class SSActionGOTO1 extends SSAction{

	/**
	 * 
	 * @param sAction - Action IDs to go to if required items in inventory; one of these IDs is chosen randomly upon success
	 * @param fAction - Action IDs to go to if required items not in inventory; one of these IDs is chosen randomly upon failure
	 * @param reqItems - ItemStacks that must be in inventory for a succeeding GOTO statement.  Number of each item may exceed requirement.
	 * 
	 */
	public SSActionGOTO1(int[] sActions, int[] fActions, ItemStack[] reqItems){
		succeedActions = sActions;
		failActions = fActions;
		items = reqItems;
	}
	
	public SSActionGOTO1(){
		succeedActions = null;
		failActions = null;
		items = null;
	}
	
	public void doAction(SSEntityScriptedSurvivor ess){
	}
	
	
	public boolean isActionComplete(SSEntityScriptedSurvivor ess){
		return true;
	}
	
	public void exitAction(SSEntityScriptedSurvivor ess, boolean actionCanceled){
		if(actionCanceled) return;
		
		if(itemsInInventory(ess)) ess.currentAction = succeedActions[ess.rand.nextInt(succeedActions.length)] - 2;
		else ess.currentAction = failActions[ess.rand.nextInt(failActions.length)] - 2;
	}
	
	private boolean itemsInInventory(SSEntityScriptedSurvivor ess){
		for(int i = 0; i < items.length; i++){
			if(!ess.pack.isItemStackInInventory(items[i])) return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param StringSample 5,6:3:1#6#0,3#3#0
	 * @param Explanation Succeed-Action 5 or 6, Fail-Action 3, 2 itemstacks: 6 0-damage stone, 3 0-damage dirt
	 * @return GOTO action
	 */
	public SSAction createAction(String s){
		try{
			String codes[] = s.split(":");
			String codes1[] = codes[0].split(","); //success ints
			String codes2[] = codes[1].split(","); //failure ints
			String codes3[] = codes[2].split(","); //itemstacks
			int a[] = new int[codes1.length];
			int b[] = new int[codes2.length];
			ItemStack items[] = new ItemStack[codes3.length];
			//success actions
			for(int i = 0; i < codes1.length; i++){
				a[i] = Integer.valueOf(codes1[i]).intValue();
			}
			//failure actions
			for(int i = 0; i < codes2.length; i++){
				b[i] = Integer.valueOf(codes2[i]).intValue();
			}
			//itemStacks
			for(int i = 0; i < codes3.length; i++){
				String vals[] = codes3[i].split("#");
				items[i] = new ItemStack(Integer.valueOf(vals[0]).intValue(), Integer.valueOf(vals[1]).intValue(), Integer.valueOf(vals[2]).intValue());
			}
			return new SSActionGOTO1(a, b, items);
		}catch(Exception e){
			return null;
		}
	}
	
	private int[] succeedActions;
	private int[] failActions;
	private ItemStack items[];
}
