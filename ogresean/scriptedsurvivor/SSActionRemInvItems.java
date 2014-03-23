package ogresean.scriptedsurvivor;

import net.minecraft.item.ItemStack;

/**
 * 
 * @name SSActionRemInvItems
 * @description This action removes items from a Survivor's inventory.
 */
public class SSActionRemInvItems extends SSAction{

	/**
	 * 
	 * @param removedItems - ItemStacks that are removed from the survivor's inventory.
	 * 
	 */
	public SSActionRemInvItems(ItemStack[] removedItems){
		items = removedItems;
	}
	
	public SSActionRemInvItems(){
		items = null;
	}
	
	public void doAction(SSEntityScriptedSurvivor ess){
	}
	
	
	public boolean isActionComplete(SSEntityScriptedSurvivor ess){
		return true;
	}
	
	public void exitAction(SSEntityScriptedSurvivor ess, boolean actionCanceled){
		if(actionCanceled) return;
		
		//add items to inventory
		for(int i = 0; i < items.length; i++){
			if(!ess.pack.removeItemStackFromInventory(items[i].copy())) break;
		}
	}
	
	/**
	 * 
	 * @param StringSample 1#6#0,3#3#0
	 * @param Explanation 2 itemstacks: 6 0-damage stone, 3 0-damage dirt
	 * @return RemInvItems action
	 */
	public SSAction createAction(String s){
		try{
			String codes3[] = s.split(","); //itemstacks
			ItemStack items[] = new ItemStack[codes3.length];
			//itemStacks
			for(int i = 0; i < codes3.length; i++){
				String vals[] = codes3[i].split("#");
				items[i] = new ItemStack(Integer.valueOf(vals[0]).intValue(), Integer.valueOf(vals[1]).intValue(), Integer.valueOf(vals[2]).intValue());
			}
			return new SSActionRemInvItems(items);
		}catch(Exception e){
			return null;
		}
	}
	
	private ItemStack items[];

}
